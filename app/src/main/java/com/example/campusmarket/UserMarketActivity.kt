package com.example.campusmarket

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class UserMarketActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvStoreTitle: TextView
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvOpenDate: TextView
    private lateinit var tvTradeCount: TextView
    private lateinit var ivStoreBanner: ImageView
    private lateinit var recyclerUserMarketPosts: RecyclerView

    private val apiService by lazy { RetrofitClient.apiService }
    private val apiBaseUrl = "http://3.36.120.78:8080"

    private var sellerId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_market)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sellerId = intent.getLongExtra("sellerId", -1L)
        Log.d("USER_MARKET", "전달받은 sellerId = $sellerId")

        bindViews()
        setupViews()
        setupBottomNavigation()
        setupRecyclerView()

        if (sellerId != -1L) {
            loadStoreDetail(sellerId)
        } else {
            Log.e("USER_MARKET", "sellerId가 전달되지 않았습니다.")
            finish()
        }
        if (sellerId != -1L) {
            loadStoreDetail(sellerId)
            loadStoreProducts(sellerId) // 👈 추가
        }
    }
    private fun loadStoreProducts(sellerId: Long) {
        lifecycleScope.launch {
            try {
                Log.d("USER_MARKET", "상품 리스트 요청 sellerId = $sellerId")

                val response = apiService.getStoreProducts(sellerId)
                val products = response.result.products

                recyclerUserMarketPosts.adapter = UserMarketPostAdapter(products) { productId ->
                    val intent = Intent(this@UserMarketActivity, ProductDetailActivity::class.java)
                    intent.putExtra("productId", productId)
                    startActivity(intent)
                }

                Log.d("USER_MARKET", "상품 리스트 조회 성공 size=${products.size}")
            } catch (e: Exception) {
                Log.e("USER_MARKET", "상품 리스트 조회 실패: ${e.message}", e)
            }
        }
    }
    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        tvStoreTitle = findViewById(R.id.tvStoreTitle)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvOpenDate = findViewById(R.id.tvOpenDate)
        tvTradeCount = findViewById(R.id.tvTradeCount)
        ivStoreBanner = findViewById(R.id.ivStoreBanner)
        recyclerUserMarketPosts = findViewById(R.id.recyclerUserMarketPosts)
    }

    private fun setupViews() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.gohome).setOnClickListener {
            startActivity(Intent(this, MarketActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.goMymarket).setOnClickListener {
            startActivity(Intent(this, MyMarketActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.gochat).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.gomypage).setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        recyclerUserMarketPosts.layoutManager = LinearLayoutManager(this)
        recyclerUserMarketPosts.adapter = UserMarketPostAdapter(emptyList()) { productId ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("productId", productId)
            startActivity(intent)
        }
    }

    private fun loadStoreDetail(sellerId: Long) {
        lifecycleScope.launch {
            try {
                Log.d("USER_MARKET", "상점 상세 요청 sellerId = $sellerId")

                val response = apiService.getStoreDetail(sellerId)
                val result = response.result

                val nickname = result.nickname?.takeIf { it.isNotBlank() } ?: "이름없는"
                tvStoreTitle.text = "${nickname}네 상점"

                tvOpenDate.text = "상점 시작 날짜: ${formatDate(result.storeStartAt)}"

                val tradeCount = result.saleCount + result.purchaseCount
                tvTradeCount.text = "거래 횟수: ${tradeCount} 번"

                val profileUrl = normalizeImageUrl(result.profileImageUrl)
                val bitmap = withContext(Dispatchers.IO) {
                    profileUrl?.let { loadBitmapFromUrl(it) }
                }

                if (bitmap != null) {
                    ivProfileImage.setImageBitmap(bitmap)
                } else {
                    ivProfileImage.setImageResource(R.drawable.logo)
                }

                Log.d("USER_MARKET", "상점 상세 조회 성공 sellerId=$sellerId")
            } catch (e: Exception) {
                Log.e("USER_MARKET", "상점 상세 조회 실패: ${e.message}", e)
            }
        }
    }

    private fun formatDate(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) return "-"

        return try {
            rawDate.substring(0, 10).replace("-", ". ")
        } catch (e: Exception) {
            rawDate
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
            Log.d("USER_MARKET_IMAGE", "url=$imageUrl, responseCode=$responseCode")

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
            Log.e("USER_MARKET_IMAGE", "이미지 로드 실패: $imageUrl", e)
            null
        }
    }
}