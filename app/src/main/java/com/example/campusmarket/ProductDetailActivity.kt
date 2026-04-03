package com.example.campusmarket

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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

    // 🔥 네비게이션
    private lateinit var goHome: LinearLayout
    private lateinit var goMyMarket: LinearLayout
    private lateinit var goChat: LinearLayout
    private lateinit var goMyPage: LinearLayout

    private val apiService by lazy { RetrofitClient.apiService }

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
        setupBottomNavigation() // 🔥 여기 핵심

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

        // 🔥 NavBar 연결
        goHome = findViewById(R.id.gohome)
        goMyMarket = findViewById(R.id.goMymarket)
        goChat = findViewById(R.id.gochat)
        goMyPage = findViewById(R.id.gomypage)
    }

    // ✅ 여기 중요
    private fun setupBottomNavigation() {

        goHome.setOnClickListener {
            startActivity(Intent(this, MarketActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        goMyMarket.setOnClickListener {
            startActivity(Intent(this, MyMarketActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        goChat.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        goMyPage.setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    // ===== 기존 코드 =====

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

                tvDescription.text = result.description ?: "상품 설명이 없습니다."
                btnLike.text = "관심 ${result.wishCount}"

                val imageUrl = result.images?.firstOrNull()?.originalImageUrl
                    ?: result.displayAssetImageUrl

                val bitmap = withContext(Dispatchers.IO) {
                    imageUrl?.let { loadBitmapFromUrl(it) }
                }

                if (bitmap != null) {
                    ivProduct.setImageBitmap(bitmap)
                } else {
                    ivProduct.setImageResource(R.drawable.clothes12)
                }

            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailActivity, "상품 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createOrEnterChatRoom(productId: Long) {
        val guestUuid = GuestManager.getGuestUuid(this) ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createOrEnterChatRoom(
                    guestUuid = guestUuid,
                    request = ChatRoomRequest(productId = productId)
                )

                val chatRoomId = response.body()?.result?.chatRoomId
                if (chatRoomId != null) {
                    val intent = Intent(this@ProductDetailActivity, ChattActivity::class.java)
                    intent.putExtra("chatRoomId", chatRoomId)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDate(rawDate: String?): String {
        return rawDate?.substring(5, 10)?.replace("-", ".") ?: "-"
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
            else -> "-"
        }
    }

    private fun formatColor(color: String?): String {
        return when (color?.lowercase()) {
            "black" -> "블랙"
            "white" -> "화이트"
            else -> color ?: "-"
        }
    }

    private fun loadBitmapFromUrl(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            BitmapFactory.decodeStream(connection.inputStream)
        } catch (e: Exception) {
            null
        }
    }
}