package com.example.campusmarket

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.LockerCellData
import com.example.campusmarket.data.LockerDataSource
import com.example.campusmarket.data.LockerGroupData
import com.example.campusmarket.data.SelectedLockerGroup
import com.example.campusmarket.model.ProfileInitRequest
import com.example.campusmarket.network.dto.LockerSaveRequest
import kotlinx.coroutines.launch

class StartGetInfo : AppCompatActivity() {

    private lateinit var etNickname: EditText
    private lateinit var btnRandom: Button
    private lateinit var btnCheck: Button
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView
    private lateinit var webView: WebView
    private lateinit var tvLockerSelector: TextView

    private var isNicknameChecked = false

    private var currentBuildingName: String = ""
    private var currentFloor: Int = 1
    private var currentImageIndex: Int = 1

    private var selectedLockerGroup: SelectedLockerGroup? = null
    private var selectedLockerCell: LockerCellData? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_get_info)

        etNickname = findViewById(R.id.etNickname)
        btnRandom = findViewById(R.id.btnRandom)
        btnCheck = findViewById(R.id.btnCheck)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)
        webView = findViewById(R.id.webViewMap)
        tvLockerSelector = findViewById(R.id.tvLockerSelector)

        etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isNicknameChecked = false
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        btnRandom.setOnClickListener {
            getRandomNickname()
        }

        btnCheck.setOnClickListener {
            val nickname = etNickname.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkNickname(nickname)
        }

        btnNext.setOnClickListener {
            val nickname = etNickname.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isNicknameChecked) {
                Toast.makeText(this, "닉네임 중복 확인을 해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedLockerCell == null) {
                Toast.makeText(this, "사물함을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveProfileToServer()
        }

        tvSkip.setOnClickListener {
            goToMarket()
        }

        initWebView()
        loadSavedLocker()
        loadSavedNickname()
    }

    private fun initWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.clearCache(true)
        webView.clearHistory()

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        webView.addJavascriptInterface(WebAppInterface(), "AndroidBridge")
        webView.loadUrl("https://map-web-sigma.vercel.app/")
    }

    private fun goToMarket() {
        val intent = Intent(this, MarketActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getRandomNickname() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApi.getRandomNickname()

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {
                        val nickname = body.result.nickname
                        etNickname.setText(nickname)
                        isNicknameChecked = false
                        Log.d("API", "닉네임 성공: $nickname")
                    } else {
                        Toast.makeText(
                            this@StartGetInfo,
                            body?.message ?: "닉네임 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@StartGetInfo, "서버 오류", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API", "닉네임 예외", e)
                Toast.makeText(this@StartGetInfo, "연결 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkNickname(nickname: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApi.checkNickname(nickname)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {
                        if (body.result.available) {
                            isNicknameChecked = true
                            Toast.makeText(
                                this@StartGetInfo,
                                "사용 가능한 닉네임입니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            isNicknameChecked = false
                            Toast.makeText(
                                this@StartGetInfo,
                                "이미 사용 중인 닉네임입니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        isNicknameChecked = false
                        Toast.makeText(
                            this@StartGetInfo,
                            body?.message ?: "닉네임 확인 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    isNicknameChecked = false
                    Toast.makeText(this@StartGetInfo, "서버 오류", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isNicknameChecked = false
                Log.e("API", "닉네임 체크 예외", e)
                Toast.makeText(this@StartGetInfo, "연결 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onBuildingSelected(buildingName: String) {
            runOnUiThread {
                showFloorDialog(buildingName)
            }
        }
    }

    private fun showFloorDialog(buildingName: String) {
        currentBuildingName = buildingName

        val dialog = FloorSelectDialogFragment(buildingName) { selectedFloor ->
            currentFloor = selectedFloor

            when (buildingName) {
                "차관" -> {
                    Toast.makeText(this, "차관 ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                    openLockerGroupPopup(buildingName, selectedFloor, 1)
                }

                "인문대" -> {
                    Toast.makeText(this, "인문대 ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                }

                "자연대" -> {
                    Toast.makeText(this, "자연대 ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                }

                "예술대학" -> {
                    Toast.makeText(this, "예술대학 ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "$buildingName ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show(supportFragmentManager, "FloorSelectDialog")
    }

    private fun openLockerGroupPopup(buildingName: String, floor: Int, imageIndex: Int) {
        currentImageIndex = imageIndex

        val popup = LockerGroupPopupDialogFragment(
            buildingName = buildingName,
            floor = floor,
            imageIndex = imageIndex,
            loungeImages = LockerDataSource.loungeImageList,
            lockerGroups = LockerDataSource.lockerList
        ) { selectedGroup ->

            selectedLockerGroup = selectedGroup

            Log.d(
                "LOCKER_GROUP",
                "그룹 선택: building=${selectedGroup.buildingName}, floor=${selectedGroup.floor}, major=${selectedGroup.major}, group=${selectedGroup.groupNumber}"
            )

            val matchedGroupData = LockerDataSource.lockerList.find {
                it.buildingName == selectedGroup.buildingName &&
                        it.floor == selectedGroup.floor &&
                        it.major == selectedGroup.major &&
                        it.groupNumber == selectedGroup.groupNumber
            }

            if (matchedGroupData == null) {
                Toast.makeText(
                    this,
                    "선택한 사물함 그룹 데이터를 찾을 수 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(
                    "LOCKER_GROUP",
                    "groupData not found: building=${selectedGroup.buildingName}, floor=${selectedGroup.floor}, major=${selectedGroup.major}, group=${selectedGroup.groupNumber}"
                )
                return@LockerGroupPopupDialogFragment
            }

            openLockerFrontPopup(matchedGroupData)
        }

        popup.show(supportFragmentManager, "LockerGroupPopup")
    }

    private fun openLockerFrontPopup(group: LockerGroupData) {
        val lockerCells = LockerDataSource.createLockerCells(group)

        val dialog = LockerFrontPopupDialogFragment(
            lockerCellImageResId = group.frontImageResId,
            rowCount = group.rowCount,
            colCount = group.colCount,
            lockerCells = lockerCells
        ) { selectedCell ->

            selectedLockerCell = selectedCell

            Log.d(
                "LOCKER_CELL",
                "셀 선택: building=${selectedCell.buildingName}, floor=${selectedCell.floor}, major=${selectedCell.major}, group=${selectedCell.lockerGroup}, row=${selectedCell.row}, col=${selectedCell.col}"
            )

            saveLockerToServer(selectedCell)
        }

        dialog.show(supportFragmentManager, "LockerFrontPopup")
    }

    private fun loadSavedNickname() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedNickname = prefs.getString("nickname", null)

        if (!savedNickname.isNullOrBlank()) {
            etNickname.setText(savedNickname)
            isNicknameChecked = true
        }
    }

    private fun loadSavedLocker() {
        lifecycleScope.launch {
            try {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val guestUuid = prefs.getString("guestUuid", null)

                if (guestUuid.isNullOrBlank()) {
                    Log.e("LOCKER_GET", "guestUuid 없음")
                    return@launch
                }

                val response = RetrofitClient.memberApi.getMyLocker(guestUuid)

                Log.d("LOCKER_GET", "응답 code = ${response.code()}")
                Log.d("LOCKER_GET", "응답 body = ${response.body()}")
                Log.d("LOCKER_GET", "응답 errorBody = ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success && body.result != null) {
                        val result = body.result
                        tvLockerSelector.text =
                            "${result.building} ${result.floor}층 ${result.major} ${result.lockerGroup}그룹 ${result.row}행 ${result.col}열"
                    } else {
                        tvLockerSelector.text = "지도에서 사물함 위치를 선택하세요!"
                    }
                } else {
                    tvLockerSelector.text = "지도에서 사물함 위치를 선택하세요!"
                }
            } catch (e: Exception) {
                Log.e("LOCKER_GET", "조회 예외", e)
            }
        }
    }

    private fun saveLockerToServer(cell: LockerCellData) {
        lifecycleScope.launch {
            try {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val guestUuid = prefs.getString("guestUuid", null)

                if (guestUuid.isNullOrBlank()) {
                    Toast.makeText(
                        this@StartGetInfo,
                        "게스트 정보가 없습니다. 첫 화면부터 다시 시작해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LOCKER_API", "guestUuid 없음")
                    return@launch
                }

                val request = LockerSaveRequest(
                    building = cell.buildingName,
                    floor = cell.floor,
                    major = cell.major,
                    lockerGroup = cell.lockerGroup,
                    row = cell.row,
                    col = cell.col
                )

                Log.d("LOCKER_API", "guestUuid = $guestUuid")
                Log.d("LOCKER_API", "보내는 request = $request")

                val response = RetrofitClient.memberApi.saveMyLocker(
                    guestUuid,
                    request
                )

                Log.d("LOCKER_API", "응답 code = ${response.code()}")
                Log.d("LOCKER_API", "응답 message = ${response.message()}")
                Log.d("LOCKER_API", "응답 body = ${response.body()}")
                Log.d("LOCKER_API", "응답 errorBody = ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success && body.result != null) {
                        val lockerText =
                            "${body.result.building} ${body.result.floor}층 ${body.result.major} ${body.result.lockerGroup}그룹 ${body.result.row}행 ${body.result.col}열"

                        tvLockerSelector.text = lockerText

                        Toast.makeText(
                            this@StartGetInfo,
                            "사물함 저장 성공",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@StartGetInfo,
                            body?.message ?: "사물함 저장 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@StartGetInfo,
                        "서버 응답 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("LOCKER_API", "예외 발생", e)
                Toast.makeText(
                    this@StartGetInfo,
                    "사물함 저장 중 오류가 발생했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveProfileToServer() {
        val nickname = etNickname.text.toString().trim()
        val lockerCell = selectedLockerCell

        if (nickname.isBlank()) {
            Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (lockerCell == null) {
            Toast.makeText(this, "사물함을 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val guestUuid = GuestManager.getGuestUuid(this)

        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "회원 식별 정보가 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val lockerName =
            "${lockerCell.buildingName} ${lockerCell.floor}층 ${lockerCell.major} ${lockerCell.lockerGroup}그룹 ${lockerCell.row}행 ${lockerCell.col}열"

        val request = ProfileInitRequest(
            nickname = nickname,
            profileImageUrl = "https://your-domain.com/default-profile.png",
            lockerName = lockerName,
            timetableData = ""
        )

        lifecycleScope.launch {
            try {
                Log.d("PROFILE_API", "guestUuid=$guestUuid")
                Log.d("PROFILE_API", "request=$request")

                val response = RetrofitClient.memberApi.saveProfile(
                    guestUuid = guestUuid,
                    request = request
                )

                Log.d("PROFILE_API", "code=${response.code()}")
                Log.d("PROFILE_API", "body=${response.body()}")
                Log.d("PROFILE_API", "errorBody=${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success && body.result != null) {
                        val result = body.result

                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("nickname", result.nickname)
                            .putString("lockerName", result.lockerName)
                            .apply()

                        Toast.makeText(this@StartGetInfo, "프로필 저장 성공", Toast.LENGTH_SHORT).show()
                        goToMarket()
                    } else {
                        Toast.makeText(this@StartGetInfo, body?.message ?: "프로필 저장 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PROFILE_API", "HTTP 오류 ${response.code()}: $errorBody")
                    Toast.makeText(this@StartGetInfo, "저장 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("PROFILE_API", "error", e)
                Toast.makeText(this@StartGetInfo, "오류: ${e.message ?: "알 수 없는 오류"}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}