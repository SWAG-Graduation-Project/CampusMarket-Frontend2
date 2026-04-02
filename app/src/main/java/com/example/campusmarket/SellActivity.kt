package com.example.campusmarket

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.model.BackgroundRemovalRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.HttpURLConnection
import java.net.URL

class SellActivity : AppCompatActivity() {

    private lateinit var checkRemoveBg: CheckBox
    private lateinit var btnNext: Button

    private lateinit var layoutPhotoUpload1: FrameLayout
    private lateinit var layoutUploadPlaceholder1: LinearLayout
    private lateinit var ivPhotoPreview1: ImageView

    private var selectedSlotIndex: Int = -1

    private val selectedImageUris = MutableList<Uri?>(5) { null }
    private val tempImageIds = MutableList<Long?>(5) { null }
    private val bgRemovedImageUrls = MutableList<String?>(5) { null }

    private val uploadingSlots = mutableSetOf<Int>()
    private var isRemovingBackground = false
    private var isNavigatingAfterBgRemoval = false
    private var suppressCheckListener = false

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null && selectedSlotIndex in 0..4) {
                onImageSelected(selectedSlotIndex, uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)

        checkRemoveBg = findViewById(R.id.checkRemoveBg)
        btnNext = findViewById(R.id.btnNext)

        layoutPhotoUpload1 = findViewById(R.id.layoutPhotoUpload1)
        layoutUploadPlaceholder1 = findViewById(R.id.layoutUploadPlaceholder1)
        ivPhotoPreview1 = findViewById(R.id.ivPhotoPreview1)

        layoutPhotoUpload1.setOnClickListener {
            selectedSlotIndex = 0
            pickImageLauncher.launch("image/*")
        }

        checkRemoveBg.setOnCheckedChangeListener { _, isChecked ->
            if (suppressCheckListener) return@setOnCheckedChangeListener

            if (isChecked) {
                val hasSelectedImage = selectedImageUris.any { it != null }
                if (!hasSelectedImage) {
                    toast("먼저 사진을 업로드해주세요.")
                    setRemoveBgChecked(false)
                    return@setOnCheckedChangeListener
                }

                if (isAnyUploadInProgress()) {
                    toast("이미지 업로드가 끝난 뒤 배경 제거를 진행해주세요.")
                    return@setOnCheckedChangeListener
                }

                if (!areAllSelectedImagesUploaded()) {
                    toast("이미지 업로드가 아직 완료되지 않았습니다.")
                    return@setOnCheckedChangeListener
                }

                removeBackgroundForUploadedImages()
            } else {
                restoreOriginalPreviewImages()
            }
        }

        btnNext.setOnClickListener {
            prepareAndMoveToNextPage()
        }

        updateNextButtonState()
    }

    private fun onImageSelected(index: Int, uri: Uri) {
        selectedImageUris[index] = uri
        tempImageIds[index] = null
        bgRemovedImageUrls[index] = null

        renderOriginalPreview(index, uri)
        uploadSingleTempImage(index, uri)
        updateNextButtonState()
    }

    private fun prepareAndMoveToNextPage() {
        val hasImage = selectedImageUris.any { it != null }

        if (!hasImage) {
            toast("이미지를 최소 1개 이상 선택해주세요.")
            return
        }

        if (isAnyUploadInProgress()) {
            toast("이미지 업로드가 아직 진행 중입니다. 잠시 후 다시 시도해주세요.")
            return
        }

        if (!areAllSelectedImagesUploaded()) {
            toast("업로드가 완료되지 않은 이미지가 있습니다.")
            return
        }

        if (checkRemoveBg.isChecked) {
            if (isRemovingBackground) {
                toast("배경 제거가 진행 중입니다. 잠시 후 다시 시도해주세요.")
                return
            }

            val requiredSlots = getSelectedSlots()
            val allBgReady = requiredSlots.all { !bgRemovedImageUrls[it].isNullOrBlank() }

            if (!allBgReady) {
                isNavigatingAfterBgRemoval = true
                removeBackgroundForUploadedImages()
                return
            }
        }

        moveToNextPageInternal()
    }

    private fun moveToNextPageInternal() {
        val imageList = if (checkRemoveBg.isChecked) {
            getSelectedSlots().mapNotNull { bgRemovedImageUrls[it] }
        } else {
            getSelectedSlots().mapNotNull { selectedImageUris[it]?.toString() }
        }

        val validTempIds = getSelectedSlots().mapNotNull { tempImageIds[it] }

        if (imageList.isEmpty()) {
            toast("표시할 이미지가 없습니다.")
            return
        }

        Log.d("NEXT_PAGE", "checkRemoveBg=${checkRemoveBg.isChecked}")
        Log.d("NEXT_PAGE", "imageList=$imageList")
        Log.d("NEXT_PAGE", "validTempIds=$validTempIds")

        val intent = Intent(this, SellDetailActivity::class.java)
        intent.putStringArrayListExtra("imageList", ArrayList(imageList))
        intent.putExtra("tempImageIds", validTempIds.toLongArray())
        startActivity(intent)
    }

    private fun uploadSingleTempImage(index: Int, uri: Uri) {
        val imagePart = uriToMultipart(uri)

        if (imagePart == null) {
            toast("이미지 변환 실패")
            return
        }

        val guestUuid = GuestManager.getGuestUuid(this)
        val memberId = GuestManager.getMemberId(this)

        if (guestUuid == null) {
            toast("게스트 정보가 없습니다. 처음 화면부터 시작해주세요.")
            return
        }

        uploadingSlots.add(index)
        updateNextButtonState()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.productImageApi.uploadTempImage(
                    guestUuid = guestUuid,
                    memberId = memberId,
                    files = imagePart
                )

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success && body.result != null) {
                        val result = body.result
                        tempImageIds[index] = result.tempImageId

                        Log.d(
                            "BG_REMOVE",
                            "temp upload success: slot=$index, tempImageId=${result.tempImageId}"
                        )

                        if (checkRemoveBg.isChecked && areAllSelectedImagesUploaded()) {
                            removeBackgroundForUploadedImages()
                        }
                    } else {
                        toast("임시 업로드 실패")
                    }
                } else {
                    Log.e("BG_REMOVE", "temp upload code=${response.code()}")
                    Log.e("BG_REMOVE", "temp upload error=${response.errorBody()?.string()}")
                    toast("임시 업로드 실패")
                }
            } catch (e: Exception) {
                Log.e("BG_REMOVE", "temp upload exception=${e.message}", e)
                toast("업로드 중 오류")
            } finally {
                uploadingSlots.remove(index)
                updateNextButtonState()
            }
        }
    }

    private fun removeBackgroundForUploadedImages() {
        if (isRemovingBackground) {
            Log.d("BG_REMOVE", "이미 배경 제거 요청 진행 중이라 중복 호출 무시")
            return
        }

        val selectedSlots = getSelectedSlots()
        val validIds = selectedSlots.mapNotNull { tempImageIds[it] }

        if (selectedSlots.isEmpty() || validIds.isEmpty()) {
            toast("먼저 사진을 업로드해주세요.")
            return
        }

        if (selectedSlots.size != validIds.size) {
            toast("이미지 업로드가 아직 완료되지 않았습니다.")
            return
        }

        val guestUuid = GuestManager.getGuestUuid(this)
        val memberId = GuestManager.getMemberId(this)

        if (guestUuid == null) {
            toast("게스트 정보가 없습니다. 처음 화면부터 시작해주세요.")
            return
        }

        isRemovingBackground = true
        updateNextButtonState()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.productImageApi.removeBackground(
                    guestUuid = guestUuid,
                    memberId = memberId,
                    request = BackgroundRemovalRequest(tempImageIds = validIds)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("BG_REMOVE", "remove body = $body")

                    if (body != null && body.success && body.result != null) {
                        val items = body.result.items

                        for (item in items) {
                            val slotIndex = tempImageIds.indexOf(item.tempImageId)
                            val rawUrl = item.backgroundRemovedImageUrl

                            if (slotIndex == -1 || rawUrl.isNullOrBlank()) {
                                continue
                            }

                            val usableUrl = toUsableImageUrl(rawUrl)

                            if (usableUrl != null) {
                                bgRemovedImageUrls[slotIndex] = usableUrl

                                when (slotIndex) {
                                    0 -> loadImageFromUrl(ivPhotoPreview1, usableUrl)
                                }
                            } else {
                                Log.e("BG_REMOVE", "사용 불가능한 배경제거 URL: $rawUrl")
                            }
                        }

                        Log.d("BG_REMOVE", "최종 bgRemovedImageUrls=$bgRemovedImageUrls")

                        val requiredSlots = getSelectedSlots()
                        val allBgReady = requiredSlots.all { !bgRemovedImageUrls[it].isNullOrBlank() }

                        if (allBgReady) {
                            toast("배경 제거 완료")

                            if (isNavigatingAfterBgRemoval) {
                                isNavigatingAfterBgRemoval = false
                                moveToNextPageInternal()
                            }
                        } else {
                            Log.e("BG_REMOVE", "배경제거 응답은 왔지만 일부 슬롯 URL이 비어 있음")
                            toast("배경 제거된 이미지가 일부 누락되었습니다.")
                            isNavigatingAfterBgRemoval = false
                        }
                    } else {
                        toast("배경 제거 실패")
                        isNavigatingAfterBgRemoval = false
                    }
                } else {
                    Log.e("BG_REMOVE", "remove code=${response.code()}")
                    Log.e("BG_REMOVE", "remove error=${response.errorBody()?.string()}")
                    toast("배경 제거 실패")
                    isNavigatingAfterBgRemoval = false
                }
            } catch (e: Exception) {
                Log.e("BG_REMOVE", "remove exception=${e.message}", e)
                toast("배경 제거 중 오류")
                isNavigatingAfterBgRemoval = false
            } finally {
                isRemovingBackground = false
                updateNextButtonState()
            }
        }
    }

    private fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        return try {
            val resolver = contentResolver
            val inputStream = resolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val requestBody = bytes.toRequestBody()

            MultipartBody.Part.createFormData(
                "files",
                "upload_image.jpg",
                requestBody
            )
        } catch (e: Exception) {
            Log.e("BG_REMOVE", "uriToMultipart error=${e.message}", e)
            null
        }
    }

    private fun loadImageFromUrl(imageView: ImageView, imageUrl: String) {
        lifecycleScope.launch {
            try {
                val finalUrl = toUsableImageUrl(imageUrl)
                if (finalUrl.isNullOrBlank()) {
                    Log.e("BG_REMOVE", "loadImageFromUrl: finalUrl is null or blank, original=$imageUrl")
                    return@launch
                }

                val bitmap = withContext(Dispatchers.IO) {
                    val url = URL(finalUrl)
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 10000
                        readTimeout = 10000
                        doInput = true
                        connect()
                    }

                    val responseCode = connection.responseCode
                    Log.d("BG_REMOVE", "image responseCode=$responseCode, url=$finalUrl")

                    if (responseCode !in 200..299) {
                        connection.disconnect()
                        throw IllegalStateException("이미지 응답 코드 비정상: $responseCode")
                    }

                    val stream = connection.inputStream
                    val decodedBitmap = BitmapFactory.decodeStream(stream)
                    stream.close()
                    connection.disconnect()

                    decodedBitmap ?: throw IllegalStateException("Bitmap decode 실패")
                }

                imageView.setImageBitmap(bitmap)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                Log.e("BG_REMOVE", "load url image error=$imageUrl", e)
                toast("배경 제거 이미지 로딩 실패")
            }
        }
    }

    private fun renderOriginalPreview(index: Int, uri: Uri) {
        when (index) {
            0 -> {
                ivPhotoPreview1.setImageURI(uri)
                ivPhotoPreview1.scaleType = ImageView.ScaleType.CENTER_CROP
                layoutUploadPlaceholder1.visibility = View.GONE
            }
        }
    }

    private fun restoreOriginalPreviewImages() {
        getSelectedSlots().forEach { slotIndex ->
            val uri = selectedImageUris[slotIndex] ?: return@forEach
            renderOriginalPreview(slotIndex, uri)
        }
    }

    private fun toUsableImageUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null

        return when {
            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
            rawUrl.startsWith("/uploads/") -> "http://3.36.120.78:8080$rawUrl"
            else -> null
        }
    }

    private fun getSelectedSlots(): List<Int> {
        return selectedImageUris.mapIndexedNotNull { index, uri ->
            if (uri != null) index else null
        }
    }

    private fun areAllSelectedImagesUploaded(): Boolean {
        val selectedSlots = getSelectedSlots()
        return selectedSlots.isNotEmpty() && selectedSlots.all { tempImageIds[it] != null }
    }

    private fun isAnyUploadInProgress(): Boolean {
        return uploadingSlots.isNotEmpty()
    }

    private fun updateNextButtonState() {
        btnNext.isEnabled = !isAnyUploadInProgress() && !isRemovingBackground
        btnNext.alpha = if (btnNext.isEnabled) 1.0f else 0.6f
    }

    private fun setRemoveBgChecked(checked: Boolean) {
        suppressCheckListener = true
        checkRemoveBg.isChecked = checked
        suppressCheckListener = false
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}