package com.example.campusmarket

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campusmarket.RetrofitClient
import kotlinx.coroutines.launch

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
class RewriteProductActivity : AppCompatActivity() {

    private lateinit var btnDone: Button

    private lateinit var slotViews: List<FrameLayout>
    private lateinit var imageViews: List<ImageView>

    private var selectedSlotIndex: Int = -1

    // 사용자가 고른 원본 이미지 Uri
    private val selectedImageUris = MutableList<Uri?>(5) { null }

    // 업로드 후 받은 tempImageId 저장
    private val tempImageIds = MutableList<Long?>(5) { null }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null && selectedSlotIndex in 0..4) {
                selectedImageUris[selectedSlotIndex] = uri

                Glide.with(this)
                    .load(uri)
                    .into(imageViews[selectedSlotIndex])
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewrite_product_acitivity)

        btnDone = findViewById(R.id.btnDone)

        slotViews = listOf(
            findViewById(R.id.slot1),
            findViewById(R.id.slot2),
            findViewById(R.id.slot3),
            findViewById(R.id.slot4),
            findViewById(R.id.slot5)
        )

        imageViews = listOf(
            findViewById(R.id.ivPhoto1),
            findViewById(R.id.ivPhoto2),
            findViewById(R.id.ivPhoto3),
            findViewById(R.id.ivPhoto4),
            findViewById(R.id.ivPhoto5)
        )

        slotViews.forEachIndexed { index, frameLayout ->
            frameLayout.setOnClickListener {
                selectedSlotIndex = index
                pickImageLauncher.launch("image/*")
            }
        }

        btnDone.setOnClickListener {
            uploadAllSelectedImages()
        }
    }

    private fun uploadAllSelectedImages() {
        val imagesToUpload = selectedImageUris.mapIndexedNotNull { index, uri ->
            if (uri != null) index to uri else null
        }

        if (imagesToUpload.isEmpty()) {
            Toast.makeText(this, "먼저 사진을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            for ((index, uri) in imagesToUpload) {
                uploadSingleImage(index, uri)
            }

            Log.d("UPLOAD", "tempImageIds = $tempImageIds")
            Toast.makeText(this@RewriteProductActivity, "배경 제거 요청 완료", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun uploadSingleImage(index: Int, uri: Uri) {
        val imagePart = uriToMultipart(uri)

        if (imagePart == null) {
            Toast.makeText(this, "${index + 1}번 이미지 변환 실패", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val response = RetrofitClient.productImageApi.uploadTempImage(
                guestUuid = "550e8400-e29b-41d4-a716-446655440000", // 비회원 예시값
                memberId = null,
                files = imagePart
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null && body.success && body.result != null) {
                    val result = body.result
                    tempImageIds[index] = result.tempImageId

                    Log.d("UPLOAD", "index=$index")
                    Log.d("UPLOAD", "tempImageId=${result.tempImageId}")
                    Log.d("UPLOAD", "originalImageUrl=${result.originalImageUrl}")
                    Log.d("UPLOAD", "backgroundRemovedImageUrl=${result.backgroundRemovedImageUrl}")
                    Log.d("UPLOAD", "backgroundRemoved=${result.backgroundRemoved}")

                    val previewUrl = result.backgroundRemovedImageUrl ?: result.originalImageUrl

                    if (!previewUrl.isNullOrBlank()) {
                        Glide.with(this)
                            .load(previewUrl)
                            .into(imageViews[index])
                    }
                } else {
                    Log.e("UPLOAD", "응답 body 이상: $body")
                }
            } else {
                Log.e("UPLOAD", "response code = ${response.code()}")
                Log.e("UPLOAD", "errorBody = ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("UPLOAD", "exception = ${e.message}", e)
        }
    }

    private fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        return try {
            val resolver = contentResolver
            val inputStream = resolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = resolver.getType(uri) ?: "image/*"
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

            MultipartBody.Part.createFormData(
                "files",
                "upload_image.jpg",
                requestBody
            )
        } catch (e: Exception) {
            Log.e("UPLOAD", "uriToMultipart error = ${e.message}", e)
            null
        }
    }
}