package com.example.campusmarket

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.model.ParsedTimetable
import com.example.campusmarket.data.model.TimetableClass
import com.example.campusmarket.model.ProfileInitRequest
import com.example.campusmarket.network.MemberApi
import com.example.campusmarket.ui.view.WeeklyTimetableView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class MypageActivity : AppCompatActivity() {

    private lateinit var btnEditTimetable: Button
    private lateinit var btnEditLocker: Button
    private lateinit var profileNameButton: Button
    private lateinit var tvLockerName: TextView
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var timetableContainer: FrameLayout
    private lateinit var weeklyTimetableView: WeeklyTimetableView

    private var currentNickname: String = ""
    private var currentProfileImageUrl: String = ""
    private var currentLockerName: String = ""
    private var currentTimetableData: String = ""

    private val memberApi: MemberApi by lazy { RetrofitClient.memberApi }

    private val pickTimetableImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    uploadTimetableImage(uri)
                } else {
                    Toast.makeText(this, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        bindViews()
        setupTimetableContainer()
        setupListeners()
        fetchMyProfile()
    }

    private fun bindViews() {
        btnEditTimetable = findViewById(R.id.btnEditTimetable)
        btnEditLocker = findViewById(R.id.btnEditLocker)
        profileNameButton = findViewById(R.id.profilename)
        tvLockerName = findViewById(R.id.tvLockerName)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        timetableContainer = findViewById(R.id.boxTimetableImage)
    }

    private fun setupTimetableContainer() {
        timetableContainer.removeAllViews()
        weeklyTimetableView = WeeklyTimetableView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        timetableContainer.addView(weeklyTimetableView)
    }

    private fun setupListeners() {
        btnEditTimetable.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickTimetableImageLauncher.launch(intent)
        }

        btnEditLocker.setOnClickListener {
            startActivity(Intent(this, StartGetInfo::class.java))
        }

        profileNameButton.setOnClickListener {
            showNicknameEditDialog()
        }

        findViewById<LinearLayout>(R.id.gohome)?.setOnClickListener {
            startActivity(Intent(this, MarketActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.goMymarket)?.setOnClickListener {
            startActivity(Intent(this, MyMarketActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.gochat)?.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.gomypage)?.setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
        }
    }

    private fun showNicknameEditDialog() {
        val editText = EditText(this).apply {
            setText(currentNickname)
            setSingleLine()
        }
        AlertDialog.Builder(this)
            .setTitle("닉네임 수정")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                val newNickname = editText.text.toString().trim()
                if (newNickname.isNotBlank()) {
                    saveProfile(nickname = newNickname)
                } else {
                    Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun fetchMyProfile() {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            showEmptyTimetable()
            return
        }

        lifecycleScope.launch {
            try {
                val response = memberApi.getMyProfile(guestUuid)
                if (!response.isSuccessful || response.body()?.result == null) {
                    showEmptyTimetable()
                    return@launch
                }

                val data = response.body()!!.result!!
                currentNickname = data.nickname ?: ""
                currentProfileImageUrl = data.profileImageUrl ?: ""
                currentLockerName = data.lockerName ?: ""
                currentTimetableData = data.timetableData ?: ""

                profileNameButton.text = currentNickname.ifBlank { "닉네임 없음" }

                // 사물함 상세 정보 별도 조회 → "차관 1층 수학과 3그룹 3행 9열" 포맷
                try {
                    val lockerResp = memberApi.getMyLocker(guestUuid)
                    val lr = lockerResp.body()?.result
                    if (lr != null) {
                        val floorNum = lr.floor.trim().toIntOrNull() ?: lr.floor
                        val formatted = "${lr.building} ${floorNum}층 ${lr.major} ${lr.lockerGroup}그룹 ${lr.row}행 ${lr.col}열"
                        tvLockerName.text = "• $formatted"
                        currentLockerName = formatted
                    } else {
                        tvLockerName.text = if (currentLockerName.isBlank()) "• 등록된 사물함이 없습니다." else "• $currentLockerName"
                    }
                } catch (e: Exception) {
                    tvLockerName.text = if (currentLockerName.isBlank()) "• 등록된 사물함이 없습니다." else "• $currentLockerName"
                }

                if (currentTimetableData.isNotBlank()) {
                    renderTimetableFromJson(currentTimetableData)
                } else {
                    showEmptyTimetable()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                showEmptyTimetable()
            }
        }
    }

    private fun saveProfile(
        nickname: String = currentNickname,
        profileImageUrl: String = currentProfileImageUrl,
        lockerName: String = currentLockerName,
        timetableData: String = currentTimetableData
    ) {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProfileInitRequest(
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            lockerName = lockerName,
            timetableData = timetableData
        )

        lifecycleScope.launch {
            try {
                val response = memberApi.updateProfile(guestUuid, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val result = response.body()!!.result
                    if (result != null) {
                        currentNickname = result.nickname
                        currentProfileImageUrl = result.profileImageUrl
                        currentLockerName = result.lockerName
                        currentTimetableData = result.timetableData

                        profileNameButton.text = currentNickname.ifBlank { "닉네임 없음" }
                        // 저장 후에도 상세 포맷으로 재조회
                        try {
                            val lr2 = memberApi.getMyLocker(guestUuid).body()?.result
                            if (lr2 != null) {
                                val floorNum = lr2.floor.trim().toIntOrNull() ?: lr2.floor
                                tvLockerName.text = "• ${lr2.building} ${floorNum}층 ${lr2.major} ${lr2.lockerGroup}그룹 ${lr2.row}행 ${lr2.col}열"
                            } else {
                                tvLockerName.text = if (currentLockerName.isBlank()) "• 등록된 사물함이 없습니다." else "• $currentLockerName"
                            }
                        } catch (e2: Exception) {
                            tvLockerName.text = if (currentLockerName.isBlank()) "• 등록된 사물함이 없습니다." else "• $currentLockerName"
                        }
                    }
                    Toast.makeText(this@MypageActivity, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MypageActivity, "저장 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MypageActivity, "저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderTimetableFromJson(json: String) {
        try {
            val parsedTimetable = Gson().fromJson(json, ParsedTimetable::class.java)
            val classList = parsedTimetable.classes.filter { !it.name.isNullOrBlank() }
            if (classList.isEmpty()) showEmptyTimetable() else showTimetable(classList)
        } catch (e: Exception) {
            showEmptyTimetable()
        }
    }

    private fun uploadTimetableImage(uri: Uri) {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val imageFile = withContext(Dispatchers.IO) { uriToFile(uri) }
                if (imageFile == null || !imageFile.exists()) {
                    Toast.makeText(this@MypageActivity, "이미지 파일 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val requestFile = imageFile.asRequestBody(contentResolver.getType(uri)?.toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

                val response = memberApi.parseTimetableImage(guestUuid, imagePart)
                if (!response.isSuccessful) {
                    Toast.makeText(this@MypageActivity, "업로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val rawElement = response.body()?.result?.timetableData
                val newTimetableData = when {
                    rawElement == null -> null
                    rawElement.isJsonPrimitive && rawElement.asJsonPrimitive.isString -> rawElement.asString
                    else -> rawElement.toString()
                }
                if (!newTimetableData.isNullOrBlank()) {
                    renderTimetableFromJson(newTimetableData)
                    saveProfile(timetableData = newTimetableData)
                } else {
                    Toast.makeText(this@MypageActivity, "시간표 파싱 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MypageActivity, "시간표 업로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTimetable(classList: List<TimetableClass>) {
        timetableContainer.visibility = View.VISIBLE
        weeklyTimetableView.visibility = View.VISIBLE
        weeklyTimetableView.setTimetable(classList)
    }

    private fun showEmptyTimetable() {
        timetableContainer.visibility = View.VISIBLE
        weeklyTimetableView.visibility = View.VISIBLE
        weeklyTimetableView.setTimetable(emptyList())
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = getFileName(uri) ?: "timetable_image.jpg"
            val tempFile = File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && index >= 0) {
                    name = cursor.getString(index)
                }
            }
        }
        if (name == null) name = uri.lastPathSegment
        return name
    }
}
