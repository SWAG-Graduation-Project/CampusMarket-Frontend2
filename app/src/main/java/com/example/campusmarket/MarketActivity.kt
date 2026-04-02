package com.example.campusmarket

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.model.MajorCategory
import com.example.campusmarket.data.model.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class MarketActivity : AppCompatActivity() {

    private lateinit var categoryContainer: LinearLayout
    private lateinit var btnAll: Button
    private lateinit var shopSigns: List<TextView>
    private lateinit var shopImages: List<ImageView>

    private var selectedMajorCategoryId: Long? = null

    private val apiService by lazy { RetrofitClient.apiService }
    private val apiBaseUrl = "http://3.36.120.78:8080"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_market)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupTopButtons()
        setupBottomNavigation()

        loadMajorCategories()
        loadStores(page = 0, size = 12)
    }

    private fun bindViews() {
        categoryContainer = findViewById(R.id.categoryContainer)
        btnAll = findViewById(R.id.btnAll)

        shopSigns = listOf(
            findViewById(R.id.tvShopSign1),
            findViewById(R.id.tvShopSign2),
            findViewById(R.id.tvShopSign3),
            findViewById(R.id.tvShopSign4),
            findViewById(R.id.tvShopSign5),
            findViewById(R.id.tvShopSign6),
            findViewById(R.id.tvShopSign7),
            findViewById(R.id.tvShopSign8),
            findViewById(R.id.tvShopSign9),
            findViewById(R.id.tvShopSign10),
            findViewById(R.id.tvShopSign11),
            findViewById(R.id.tvShopSign12)
        )

        shopImages = listOf(
            findViewById(R.id.ivShopItem1),
            findViewById(R.id.ivShopItem2),
            findViewById(R.id.ivShopItem3),
            findViewById(R.id.ivShopItem4),
            findViewById(R.id.ivShopItem5),
            findViewById(R.id.ivShopItem6),
            findViewById(R.id.ivShopItem7),
            findViewById(R.id.ivShopItem8),
            findViewById(R.id.ivShopItem9),
            findViewById(R.id.ivShopItem10),
            findViewById(R.id.ivShopItem11),
            findViewById(R.id.ivShopItem12)
        )
    }

    private fun setupTopButtons() {
        val btnSort = findViewById<Button>(R.id.btnSort)
        btnSort.setOnClickListener {
            val popup = PopupMenu(this, btnSort)
            popup.menuInflater.inflate(R.menu.menu_sort, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.sort_popular -> btnSort.text = "인기순"
                    R.id.sort_latest -> btnSort.text = "최신순"
                    R.id.sort_price_low -> btnSort.text = "조회순"
                }
                true
            }

            popup.show()
        }

        val btnList = findViewById<Button>(R.id.btnList)
        btnList.setOnClickListener {
            startActivity(Intent(this, MarketListActivity::class.java))
        }

        btnAll.setOnClickListener {
            selectCategoryButton(btnAll)
            selectedMajorCategoryId = null
            loadStores(page = 0, size = 12)
        }
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.gohome).setOnClickListener {
            startActivity(Intent(this, MarketActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.goMymarket).setOnClickListener {
            startActivity(Intent(this, MyMarketActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.gomypage).setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.gochat).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }
    }

    private fun loadMajorCategories() {
        lifecycleScope.launch {
            try {
                val response = apiService.getMajorCategories()
                val categories = response.result.majorCategories.sortedBy { it.sortOrder }
                addCategoryButtons(categories)
            } catch (e: Exception) {
                Log.e("CATEGORY_API", "카테고리 조회 실패", e)
            }
        }
    }

    private fun addCategoryButtons(categories: List<MajorCategory>) {
        categories.forEach { category ->
            val button = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dpToPx(36)
                ).apply {
                    marginEnd = dpToPx(8)
                }

                minWidth = 0
                minHeight = 0
                setPadding(dpToPx(16), 0, dpToPx(16), 0)

                text = category.name
                isAllCaps = false
                textSize = 13f
                setTextColor(ContextCompat.getColor(context, R.color.chip_text_default))
                background = ContextCompat.getDrawable(context, R.drawable.bg_chip_white)

                setOnClickListener {
                    selectCategoryButton(this)
                    selectedMajorCategoryId = category.majorCategoryId
                    loadStores(page = 0, size = 12)
                }
            }

            categoryContainer.addView(button)
        }
    }

    private fun loadStores(page: Int = 0, size: Int = 12) {
        lifecycleScope.launch {
            try {
                clearAllStoreViews()

                val response = apiService.getStores(page, size)
                val stores = response.result.stores ?: emptyList()

                Log.d("STORE_API", "상점 조회 성공 - stores.size=${stores.size}")
                updateStoreTables(stores)
            } catch (e: Exception) {
                Log.e("STORE_API", "상점 조회 실패", e)
                clearAllStoreViews()
            }
        }
    }

    private fun updateStoreTables(stores: List<Store>) {
        lifecycleScope.launch {
            val visibleStores = stores.take(12)

            val jobs = visibleStores.mapIndexed { index, store ->
                async(Dispatchers.IO) {
                    val nickname = store.sellerNickname?.takeIf { it.isNotBlank() } ?: "이름없는"
                    val imageUrl = normalizeImageUrl(store.latestProductDisplayAssetImageUrl)
                    val bitmap = imageUrl?.let { loadBitmapFromUrl(it) }

                    Triple(index, nickname, bitmap)
                }
            }

            val results = jobs.awaitAll()

            results.forEach { result ->
                val index = result.first
                val nickname = result.second
                val bitmap = result.third

                if (index < shopSigns.size) {
                    shopSigns[index].text = "${nickname}상점"
                }

                if (index < shopImages.size) {
                    if (bitmap != null) {
                        shopImages[index].setImageBitmap(bitmap)
                    } else {
                        shopImages[index].setImageResource(R.drawable.logo)
                    }
                }

                setupStoreClickListener(index, visibleStores[index])
            }

            for (i in visibleStores.size until 12) {
                shopSigns[i].text = "빈 상점"
                shopImages[i].setImageDrawable(null)
                shopSigns[i].setOnClickListener(null)
                shopImages[i].setOnClickListener(null)
                shopImages[i].isClickable = false
                shopImages[i].isFocusable = false
            }
        }
    }

    private fun setupStoreClickListener(index: Int, store: Store) {
        if (index >= shopSigns.size || index >= shopImages.size) return

        // 닉네임 박스 클릭 -> 유저 상점 이동
        shopSigns[index].setOnClickListener {
            val intent = Intent(this, UserMarketActivity::class.java)
            intent.putExtra("sellerId", store.sellerId)
            startActivity(intent)
        }

        // 이미지 클릭 -> 최신 상품 상세 이동
        shopImages[index].setOnClickListener {
            val latestProductId = store.latestProductId

            if (latestProductId == null || latestProductId == -1L) {
                Log.e(
                    "MARKET_ACTIVITY",
                    "latestProductId가 없어서 상품 상세로 이동할 수 없습니다. sellerId=${store.sellerId}"
                )
                return@setOnClickListener
            }

            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("productId", latestProductId)
            startActivity(intent)
        }

        shopImages[index].isClickable = true
        shopImages[index].isFocusable = true
    }

    private fun clearAllStoreViews() {
        for (i in shopSigns.indices) {
            shopSigns[i].text = "빈 상점"
            shopImages[i].setImageDrawable(null)
            shopSigns[i].setOnClickListener(null)
            shopImages[i].setOnClickListener(null)
            shopImages[i].isClickable = false
            shopImages[i].isFocusable = false
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
            Log.d("IMAGE_LOAD", "url=$imageUrl, responseCode=$responseCode")

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
            Log.e("IMAGE_LOAD", "이미지 로드 실패: $imageUrl", e)
            null
        }
    }

    private fun selectCategoryButton(selectedButton: Button) {
        btnAll.background = ContextCompat.getDrawable(this, R.drawable.bg_chip_white)
        btnAll.setTextColor(ContextCompat.getColor(this, R.color.chip_text_default))

        for (i in 0 until categoryContainer.childCount) {
            val child = categoryContainer.getChildAt(i)
            if (child is Button) {
                child.background = ContextCompat.getDrawable(this, R.drawable.bg_chip_white)
                child.setTextColor(ContextCompat.getColor(this, R.color.chip_text_default))
            }
        }

        selectedButton.background =
            ContextCompat.getDrawable(this, R.drawable.bg_chip_selected)
        selectedButton.setTextColor(
            ContextCompat.getColor(this, android.R.color.white)
        )
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}