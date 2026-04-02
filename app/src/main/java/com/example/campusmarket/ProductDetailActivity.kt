package com.example.campusmarket

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.model.ChatRoomRequest
import com.example.campusmarket.data.model.ProductImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var tvStoreTitle: TextView
    private lateinit var ivProduct: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tagCategory: TextView
    private lateinit var tagState: TextView
    private lateinit var tagColor: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnChat: Button
    private lateinit var btnLike: Button

    private val apiService by lazy { RetrofitClient.apiService }
    private val apiBaseUrl = "http://3.36.120.78:8080"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomButtonContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()

        val productId = intent.getLongExtra("productId", -1L)

        if (productId == -1L) {
            Toast.makeText(this, "상품 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadProductDetail(productId)

        btnChat.setOnClickListener {
            createOrEnterChatRoom(productId)
        }
    }

    private fun bindViews() {
        tvStoreTitle = findViewById(R.id.tvStoreTitle)
        ivProduct = findViewById(R.id.ivProduct)
        tvTitle = findViewById(R.id.tvTitle)
        tvDate = findViewById(R.id.tvDate)
        tvPrice = findViewById(R.id.tvPrice)
        tagCategory = findViewById(R.id.tagCategory)
        tagState = findViewById(R.id.tagState)
        tagColor = findViewById(R.id.tagColor)
        tvDescription = findViewById(R.id.tvDescription)
        btnChat = findViewById(R.id.btnChat)
        btnLike = findViewById(R.id.btnLike)
    }

    private fun loadProductDetail(productId: Long) {
        lifecycleScope.launch {
            try {
                val response = apiService.getProductDetail(productId)
                val result = response.result

                val sellerNickname = result.seller?.nickname?.takeIf { it.isNotBlank() } ?: "이름없는"
                tvStoreTitle.text = "${sellerNickname}네 상점"

                tvTitle.text = result.name
                tvDate.text = formatDate(result.createdAt)
                tvPrice.text = if (result.isFree) "무료 나눔" else formatPrice(result.price)

                tagCategory.text = result.category?.subCategoryName ?: "-"
                tagState.text = formatCondition(result.productCondition)
                tagColor.text = formatColor(result.color)

                tvDescription.text = result.description?.takeIf { it.isNotBlank() } ?: "상품 설명이 없습니다."
                btnLike.text = "관심 ${result.wishCount}"

                val selectedImageUrl = pickProductImageUrl(
                    images = result.images,
                    fallbackDisplayAssetImageUrl = result.displayAssetImageUrl
                )

                val bitmap = withContext(Dispatchers.IO) {
                    selectedImageUrl?.let { loadBitmapFromUrl(it) }
                }

                if (bitmap != null) {
                    ivProduct.setImageBitmap(bitmap)
                } else {
                    ivProduct.setImageResource(R.drawable.clothes12)
                }

                Log.d("PRODUCT_DETAIL", "상품 상세 조회 성공 productId=$productId, imageUrl=$selectedImageUrl")
            } catch (e: Exception) {
                Log.e("PRODUCT_DETAIL", "상품 상세 조회 실패: ${e.message}", e)
                Toast.makeText(this@ProductDetailActivity, "상품 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickProductImageUrl(
        images: List<ProductImage>?,
        fallbackDisplayAssetImageUrl: String?
    ): String? {
        if (!images.isNullOrEmpty()) {
            val firstImage = images.sortedBy { it.displayOrder }.firstOrNull()

            val originalImageUrl = normalizeImageUrl(firstImage?.originalImageUrl)
            if (!originalImageUrl.isNullOrBlank()) {
                return originalImageUrl
            }

            val removedImageUrl = normalizeImageUrl(firstImage?.imageUrl)
            if (!removedImageUrl.isNullOrBlank()) {
                return removedImageUrl
            }
        }

        return normalizeImageUrl(fallbackDisplayAssetImageUrl)
    }

    private fun createOrEnterChatRoom(productId: Long) {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createOrEnterChatRoom(
                    guestUuid = guestUuid,
                    request = ChatRoomRequest(productId = productId)
                )

                if (response.isSuccessful) {
                    val chatRoomId = response.body()?.result?.chatRoomId
                    if (chatRoomId != null) {
                        val intent = Intent(this@ProductDetailActivity, ChattActivity::class.java)
                        intent.putExtra("chatRoomId", chatRoomId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@ProductDetailActivity, "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "채팅방 생성 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ProductDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDate(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) return "-"

        return try {
            val datePart = rawDate.substring(0, 10)
            val split = datePart.split("-")
            "${split[1]}.${split[2]}"
        } catch (e: Exception) {
            rawDate
        }
    }

    private fun formatPrice(price: Int): String {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원"
    }

    private fun formatCondition(condition: String?): String {
        return when (condition) {
            "BEST" -> "최상"
            "GOOD" -> "양호"
            "NORMAL" -> "보통"
            "BAD" -> "사용감"
            "UNOPENED" -> "미개봉"
            else -> condition ?: "-"
        }
    }

    private fun formatColor(color: String?): String {
        return when (color?.lowercase()) {
            "beige" -> "베이지"
            "black" -> "블랙"
            "white" -> "화이트"
            "brown" -> "브라운"
            "gray" -> "그레이"
            "blue" -> "블루"
            "red" -> "레드"
            "pink" -> "핑크"
            "green" -> "그린"
            "yellow" -> "옐로우"
            else -> color ?: "-"
        }
    }

    private fun normalizeImageUrl(rawPath: String?): String? {
        if (rawPath.isNullOrBlank()) return null

        return when {
            rawPath.startsWith("http://") || rawPath.startsWith("https://") -> rawPath
            rawPath.startsWith("/") -> "$apiBaseUrl$rawPath"
            else -> "$apiBaseUrl/$rawPath"
        }
    }

    private fun loadBitmapFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                doInput = true
                connect()
            }

            val responseCode = connection.responseCode
            Log.d("PRODUCT_DETAIL_IMAGE", "url=$imageUrl, responseCode=$responseCode")

            if (responseCode !in 200..299) {
                connection.disconnect()
                return null
            }

            val stream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(stream)
            stream.close()
            connection.disconnect()
            bitmap
        } catch (e: Exception) {
            Log.e("PRODUCT_DETAIL_IMAGE", "이미지 로드 실패: $imageUrl", e)
            null
        }
    }
}