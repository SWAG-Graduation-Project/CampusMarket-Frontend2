package com.example.campusmarket

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.model.CreateProductImageRequest
import com.example.campusmarket.data.model.CreateProductRequest
import com.example.campusmarket.data.model.ProductDraftRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class SellDetailActivity : AppCompatActivity() {

    private lateinit var backButton: Button
    private lateinit var btnAiRemove: Button
    private lateinit var btnSell: Button
    private lateinit var btnPriceDirect: Button
    private lateinit var btnPriceShare: Button

    private lateinit var spMajorCategory: Spinner
    private lateinit var spSubCategory: Spinner
    private lateinit var spCondition: Spinner
    private lateinit var spColor: Spinner

    private lateinit var chipCategoryMain: Button
    private lateinit var chipCategorySub: Button
    private lateinit var chipCondition: Button
    private lateinit var chipColor: Button

    private lateinit var etTitle: EditText
    private lateinit var etPrice: EditText
    private lateinit var etDescription: EditText
    private lateinit var photoContainer: LinearLayout

    private var imageList: ArrayList<String> = arrayListOf()
    private var tempImageIds: List<Long> = emptyList()
    private var originalImageUrls: List<String> = emptyList()
    private var isFree: Boolean = false

    data class CategoryOption(
        val id: Long,
        val label: String
    ) {
        override fun toString(): String = label
    }

    private val majorCategories = listOf(
        CategoryOption(1L, "디지털기기"),
        CategoryOption(2L, "전공책/교재"),
        CategoryOption(3L, "문구/학용품"),
        CategoryOption(4L, "패션"),
        CategoryOption(5L, "생활용품"),
        CategoryOption(6L, "자취/원룸용품"),
        CategoryOption(7L, "뷰티/미용"),
        CategoryOption(8L, "스포츠/취미"),
        CategoryOption(9L, "티켓/이용권"),
        CategoryOption(10L, "생활가전"),
        CategoryOption(11L, "식품/소모품"),
        CategoryOption(12L, "기타")
    )

    private val subCategoryMap = mapOf(
        1L to listOf(
            CategoryOption(1L, "노트북"),
            CategoryOption(2L, "태블릿"),
            CategoryOption(3L, "휴대폰"),
            CategoryOption(4L, "스마트워치"),
            CategoryOption(5L, "이어폰/헤드폰"),
            CategoryOption(6L, "키보드/마우스"),
            CategoryOption(7L, "충전기/케이블"),
            CategoryOption(8L, "모니터"),
            CategoryOption(9L, "기타 전자기기")
        ),
        2L to listOf(
            CategoryOption(10L, "전공서적"),
            CategoryOption(11L, "교양서적"),
            CategoryOption(12L, "문제집"),
            CategoryOption(13L, "자격증 교재"),
            CategoryOption(14L, "어학 교재"),
            CategoryOption(15L, "필기노트 / 제본자료"),
            CategoryOption(16L, "기타 도서")
        ),
        3L to listOf(
            CategoryOption(17L, "필기구"),
            CategoryOption(18L, "노트 / 다이어리"),
            CategoryOption(19L, "파일 / 바인더"),
            CategoryOption(20L, "계산기"),
            CategoryOption(21L, "독서대"),
            CategoryOption(22L, "필통"),
            CategoryOption(23L, "기타 학용품")
        ),
        4L to listOf(
            CategoryOption(24L, "아우터"),
            CategoryOption(25L, "상의"),
            CategoryOption(26L, "하의"),
            CategoryOption(27L, "원피스 / 스커트"),
            CategoryOption(28L, "신발"),
            CategoryOption(29L, "가방"),
            CategoryOption(30L, "모자"),
            CategoryOption(31L, "액세서리"),
            CategoryOption(32L, "기타 패션잡화")
        ),
        5L to listOf(
            CategoryOption(33L, "수납용품"),
            CategoryOption(34L, "침구 / 쿠션"),
            CategoryOption(35L, "조명"),
            CategoryOption(36L, "거울"),
            CategoryOption(37L, "청소용품"),
            CategoryOption(38L, "세탁용품"),
            CategoryOption(39L, "욕실용품"),
            CategoryOption(40L, "기타 생활용품")
        ),
        6L to listOf(
            CategoryOption(41L, "행거"),
            CategoryOption(42L, "선반"),
            CategoryOption(43L, "테이블"),
            CategoryOption(44L, "의자"),
            CategoryOption(45L, "소형가전"),
            CategoryOption(46L, "주방도구"),
            CategoryOption(47L, "식기 / 컵"),
            CategoryOption(48L, "전기장판 / 히터"),
            CategoryOption(49L, "기타 자취용품")
        ),
        7L to listOf(
            CategoryOption(50L, "화장품"),
            CategoryOption(51L, "향수"),
            CategoryOption(52L, "헤어기기"),
            CategoryOption(53L, "미용소품"),
            CategoryOption(54L, "네일용품"),
            CategoryOption(55L, "기타 뷰티용품")
        ),
        8L to listOf(
            CategoryOption(56L, "운동기구"),
            CategoryOption(57L, "요가 / 필라테스 용품"),
            CategoryOption(58L, "자전거 / 킥보드 용품"),
            CategoryOption(59L, "악기"),
            CategoryOption(60L, "게임용품"),
            CategoryOption(61L, "피규어 / 굿즈"),
            CategoryOption(62L, "기타 취미용품")
        ),
        9L to listOf(
            CategoryOption(63L, "공연 / 전시"),
            CategoryOption(64L, "영화"),
            CategoryOption(65L, "스터디룸 / 공간이용권"),
            CategoryOption(66L, "헬스장 / 운동권"),
            CategoryOption(67L, "기타 이용권")
        ),
        10L to listOf(
            CategoryOption(68L, "드라이기"),
            CategoryOption(69L, "고데기"),
            CategoryOption(70L, "선풍기"),
            CategoryOption(71L, "가습기"),
            CategoryOption(72L, "전자레인지"),
            CategoryOption(73L, "밥솥"),
            CategoryOption(74L, "청소기"),
            CategoryOption(75L, "기타 소형가전")
        ),
        11L to listOf(
            CategoryOption(76L, "미개봉 식품"),
            CategoryOption(77L, "음료"),
            CategoryOption(78L, "영양제"),
            CategoryOption(79L, "생필품"),
            CategoryOption(80L, "기타 소모품")
        ),
        12L to listOf(
            CategoryOption(81L, "분류 어려운 상품"),
            CategoryOption(82L, "직접 입력용 임시 카테고리")
        )
    )

    private val conditionDisplayList = listOf(
        "UNOPENED",
        "LIKE_NEW",
        "GOOD",
        "USED"
    )

    private val colorDisplayList = listOf(
        "검정",
        "흰색",
        "회색",
        "베이지",
        "갈색",
        "빨강",
        "파랑",
        "초록",
        "노랑",
        "핑크",
        "보라",
        "기타"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sell_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        receiveIntentData()
        setupSelectors()
        setupChipButtons()
        setupButtons()
        renderImages(imageList)
        setupBottomNavigation()

        if (tempImageIds.isNotEmpty()) {
            loadDraftData()
        }
    }

    private fun bindViews() {
        backButton = findViewById(R.id.backbutton)
        btnAiRemove = findViewById(R.id.btnAiRemove)
        btnSell = findViewById(R.id.btnSell)
        btnPriceDirect = findViewById(R.id.btnPriceDirect)
        btnPriceShare = findViewById(R.id.btnPriceShare)

        spMajorCategory = findViewById(R.id.spMajorCategory)
        spSubCategory = findViewById(R.id.spSubCategory)
        spCondition = findViewById(R.id.spCondition)
        spColor = findViewById(R.id.spColor)

        chipCategoryMain = findViewById(R.id.chipCategoryMain)
        chipCategorySub = findViewById(R.id.chipCategorySub)
        chipCondition = findViewById(R.id.chipCondition)
        chipColor = findViewById(R.id.chipColor)

        etTitle = findViewById(R.id.etTitle)
        etPrice = findViewById(R.id.etPrice)
        etDescription = findViewById(R.id.etDescription)
        photoContainer = findViewById(R.id.photoContainer)
    }

    private fun receiveIntentData() {
        imageList = intent.getStringArrayListExtra("imageList") ?: arrayListOf()
        tempImageIds = intent.getLongArrayExtra("tempImageIds")?.toList() ?: emptyList()
        originalImageUrls = intent.getStringArrayListExtra("originalImageUrls") ?: arrayListOf()

        Log.d("SELL_DETAIL", "received imageList=$imageList")
        Log.d("SELL_DETAIL", "received tempImageIds=$tempImageIds")
        Log.d("SELL_DETAIL", "received originalImageUrls=$originalImageUrls")

        if (tempImageIds.isEmpty()) {
            Toast.makeText(this, "이미지 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSelectors() {
        val majorAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            majorCategories
        )
        majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMajorCategory.adapter = majorAdapter

        val conditionAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            conditionDisplayList
        )
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCondition.adapter = conditionAdapter

        val colorAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            colorDisplayList
        )
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spColor.adapter = colorAdapter

        if (majorCategories.isNotEmpty()) {
            updateSubCategorySpinner(majorCategories.first().id, null)
        }

        spMajorCategory.setSelection(0)
        spCondition.setSelection(0)
        spColor.setSelection(0)

        updateCategoryChips()
        updateConditionChip()
        updateColorChip()
    }

    private fun setupChipButtons() {
        chipCategoryMain.setOnClickListener {
            showMajorCategoryDialog()
        }

        chipCategorySub.setOnClickListener {
            val selectedMajor = spMajorCategory.selectedItem as? CategoryOption
            if (selectedMajor == null) {
                Toast.makeText(this, "먼저 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                showSubCategoryDialog(selectedMajor.id)
            }
        }

        chipCondition.setOnClickListener {
            showConditionDialog()
        }

        chipColor.setOnClickListener {
            showColorDialog()
        }
    }

    private fun updateSubCategorySpinner(majorCategoryId: Long, selectedSubCategoryId: Long?) {
        val subCategories = subCategoryMap[majorCategoryId].orEmpty()

        val subAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            subCategories
        )
        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSubCategory.adapter = subAdapter

        if (subCategories.isEmpty()) {
            Log.e("SELL_DETAIL", "No subcategories for majorCategoryId=$majorCategoryId")
            chipCategorySub.text = "세부 카테고리"
            return
        }

        val targetIndex = if (selectedSubCategoryId != null) {
            subCategories.indexOfFirst { it.id == selectedSubCategoryId }
        } else {
            0
        }

        spSubCategory.setSelection(if (targetIndex >= 0) targetIndex else 0)
        updateCategoryChips()
    }

    private fun showMajorCategoryDialog() {
        val labels = majorCategories.map { it.label }.toTypedArray()
        val currentIndex = spMajorCategory.selectedItemPosition

        AlertDialog.Builder(this)
            .setTitle("카테고리 선택")
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                spMajorCategory.setSelection(which)
                val selectedMajor = majorCategories[which]
                updateSubCategorySpinner(selectedMajor.id, null)
                updateCategoryChips()
                dialog.dismiss()
            }
            .show()
    }

    private fun showSubCategoryDialog(majorCategoryId: Long) {
        val subCategories = subCategoryMap[majorCategoryId].orEmpty()
        if (subCategories.isEmpty()) {
            Toast.makeText(this, "선택 가능한 세부 카테고리가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = subCategories.map { it.label }.toTypedArray()
        val currentIndex = spSubCategory.selectedItemPosition

        AlertDialog.Builder(this)
            .setTitle("세부 카테고리 선택")
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                spSubCategory.setSelection(which)
                updateCategoryChips()
                dialog.dismiss()
            }
            .show()
    }

    private fun showConditionDialog() {
        val labels = conditionDisplayList.toTypedArray()
        val currentIndex = spCondition.selectedItemPosition

        AlertDialog.Builder(this)
            .setTitle("상태 선택")
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                spCondition.setSelection(which)
                updateConditionChip()
                dialog.dismiss()
            }
            .show()
    }

    private fun showColorDialog() {
        val labels = colorDisplayList.toTypedArray()
        val currentIndex = spColor.selectedItemPosition

        AlertDialog.Builder(this)
            .setTitle("색깔 선택")
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                spColor.setSelection(which)
                updateColorChip()
                dialog.dismiss()
            }
            .show()
    }

    private fun updateCategoryChips() {
        val selectedMajor = spMajorCategory.selectedItem as? CategoryOption
        val selectedSub = spSubCategory.selectedItem as? CategoryOption

        chipCategoryMain.text = selectedMajor?.label ?: "카테고리"
        chipCategorySub.text = selectedSub?.label ?: "세부 카테고리"

        chipCategoryMain.setBackgroundResource(R.drawable.bg_chip_selected)
        chipCategorySub.setBackgroundResource(R.drawable.bg_chip_unselected)
    }

    private fun updateConditionChip() {
        val condition = spCondition.selectedItem?.toString()?.ifBlank { "상태" } ?: "상태"
        chipCondition.text = condition
        chipCondition.setBackgroundResource(R.drawable.bg_chip_selected)
    }

    private fun updateColorChip() {
        val color = spColor.selectedItem?.toString()?.ifBlank { "색깔" } ?: "색깔"
        chipColor.text = color

        when (color) {
            "노랑" -> chipColor.setBackgroundResource(R.drawable.bg_chip_selected_yellow)
            else -> chipColor.setBackgroundResource(R.drawable.bg_chip_selected)
        }
    }

    private fun setupButtons() {
        backButton.setOnClickListener {
            finish()
        }

        btnAiRemove.setOnClickListener {
            if (tempImageIds.isEmpty()) {
                Toast.makeText(this, "임시 이미지 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                loadDraftData()
            }
        }

        btnPriceDirect.setOnClickListener {
            isFree = false
            updatePriceModeUi()
        }

        btnPriceShare.setOnClickListener {
            isFree = true
            updatePriceModeUi()
        }

        btnSell.setOnClickListener {
            submitProduct()
        }

        updatePriceModeUi()
    }

    private fun updatePriceModeUi() {
        if (isFree) {
            btnPriceDirect.setBackgroundResource(R.drawable.bg_price_gray)
            btnPriceShare.setBackgroundResource(R.drawable.bg_price_black)
            etPrice.setText("0")
            etPrice.isEnabled = false
            etPrice.alpha = 0.6f
        } else {
            btnPriceDirect.setBackgroundResource(R.drawable.bg_price_black)
            btnPriceShare.setBackgroundResource(R.drawable.bg_price_gray)
            if (etPrice.text.toString().trim() == "0") {
                etPrice.setText("")
            }
            etPrice.isEnabled = true
            etPrice.alpha = 1.0f
        }
    }

    private fun loadDraftData() {
        val guestUuid = GuestManager.getGuestUuid(this)
        val memberId = GuestManager.getMemberId(this)

        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "게스트 정보가 없습니다. 처음 화면부터 시작해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (memberId == null) {
            Toast.makeText(this, "회원 정보가 없습니다. 처음 화면부터 시작해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProductDraftRequest(
            tempImageIds = tempImageIds
        )

        Log.d("DRAFT_AUTO", "tempImageIds=$tempImageIds")
        Log.d("DRAFT_AUTO", "request=$request")
        Log.d("DRAFT_AUTO", "guestUuid=$guestUuid")
        Log.d("DRAFT_AUTO", "memberId=$memberId")

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.productImageApi.createProductDraft(
                    guestUuid = guestUuid,
                    memberId = memberId,
                    request = request
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("DRAFT_AUTO", "body=$body")

                    val result = body?.result

                    if (body?.success == true && result != null) {
                        setSpinnerByCategoryId(result.majorCategoryId)
                        setSpinnerBySubCategoryId(result.majorCategoryId, result.subCategoryId)
                        setSpinnerByValue(spCondition, mapConditionValue(result.productCondition))
                        setSpinnerByValue(spColor, mapColorValue(result.color))

                        etTitle.setText(result.productName ?: "")
                        etDescription.setText(result.description ?: "")

                        updateCategoryChips()
                        updateConditionChip()
                        updateColorChip()

                        Log.d("DRAFT_AUTO", "condition=${result.productCondition}")
                        Log.d("DRAFT_AUTO", "color=${result.color}")
                        Log.d("DRAFT_AUTO", "majorCategoryId=${result.majorCategoryId}")
                        Log.d("DRAFT_AUTO", "subCategoryId=${result.subCategoryId}")
                        Log.d("DRAFT_AUTO", "auto fill success: $result")

                        Toast.makeText(
                            this@SellDetailActivity,
                            "AI 추천값을 불러왔습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e(
                            "DRAFT_AUTO",
                            "success=${body?.success}, result=${body?.result}, message=${body?.message}"
                        )
                        Toast.makeText(
                            this@SellDetailActivity,
                            body?.message ?: "AI 추천값 불러오기 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorText = response.errorBody()?.string()
                    Log.e("DRAFT_AUTO", "code=${response.code()}")
                    Log.e("DRAFT_AUTO", "error=$errorText")

                    Toast.makeText(
                        this@SellDetailActivity,
                        "AI 추천값 불러오기 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("DRAFT_AUTO", "exception=${e.message}", e)
                Toast.makeText(
                    this@SellDetailActivity,
                    "AI 추천값 불러오기 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setSpinnerByCategoryId(majorCategoryId: Long?) {
        if (majorCategoryId == null) return

        val targetIndex = majorCategories.indexOfFirst { it.id == majorCategoryId }
        if (targetIndex >= 0) {
            spMajorCategory.setSelection(targetIndex)
        }
    }

    private fun setSpinnerBySubCategoryId(majorCategoryId: Long?, subCategoryId: Long?) {
        if (majorCategoryId == null) return
        updateSubCategorySpinner(majorCategoryId, subCategoryId)
    }

    private fun setSpinnerByValue(spinner: Spinner, value: String) {
        val adapter = spinner.adapter ?: return
        for (index in 0 until adapter.count) {
            if (adapter.getItem(index).toString().equals(value, ignoreCase = true)) {
                spinner.setSelection(index)
                return
            }
        }
        Log.w("SELL_DETAIL", "Spinner value not matched: $value")
    }

    private fun mapConditionValue(serverValue: String?): String {
        return when (serverValue?.trim()?.uppercase()) {
            "UNOPENED" -> "UNOPENED"
            "LIKE_NEW" -> "LIKE_NEW"
            "GOOD" -> "GOOD"
            "USED" -> "USED"
            else -> "GOOD"
        }
    }

    private fun mapColorValue(serverValue: String?): String {
        return when (serverValue?.trim()?.uppercase()) {
            "BLACK", "검정", "블랙" -> "검정"
            "WHITE", "흰색", "화이트" -> "흰색"
            "GRAY", "GREY", "회색" -> "회색"
            "BEIGE", "베이지" -> "베이지"
            "BROWN", "갈색" -> "갈색"
            "RED", "빨강", "레드" -> "빨강"
            "BLUE", "파랑", "블루" -> "파랑"
            "GREEN", "초록", "그린" -> "초록"
            "YELLOW", "노랑", "옐로우" -> "노랑"
            "PINK", "핑크" -> "핑크"
            "PURPLE", "보라", "퍼플" -> "보라"
            else -> "기타"
        }
    }

    private fun submitProduct() {
        val guestUuid = GuestManager.getGuestUuid(this)

        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "게스트 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedMajor = spMajorCategory.selectedItem as? CategoryOption
        val selectedSub = spSubCategory.selectedItem as? CategoryOption

        if (selectedMajor == null || selectedSub == null) {
            Toast.makeText(this, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val priceText = etPrice.text.toString().trim()

        if (title.isBlank()) {
            Toast.makeText(this, "상품명을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val price = if (isFree) 0 else priceText.toIntOrNull() ?: 0

        val request = CreateProductRequest(
            majorCategoryId = selectedMajor.id,
            subCategoryId = selectedSub.id,
            name = title,
            brand = "",
            color = spColor.selectedItem?.toString() ?: "기타",
            productCondition = spCondition.selectedItem?.toString() ?: "GOOD",
            description = description,
            price = price,
            isFree = isFree,
            images = imageList.mapIndexed { index, url ->
                CreateProductImageRequest(
                    imageUrl = url,
                    originalImageUrl = originalImageUrls.getOrNull(index)?.takeIf { it.isNotBlank() }
                )
            }
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.productImageApi.createMyStoreProduct(
                    guestUuid = guestUuid,
                    request = request
                )

                val responseBody = response.body()
                val errorText = response.errorBody()?.string()

                Log.d("DEBUG", "request=$request")
                Log.d("DEBUG", "code=${response.code()}")
                Log.d("DEBUG", "body=$responseBody")
                Log.d("DEBUG", "error=$errorText")

                if (response.isSuccessful) {
                    if (responseBody?.success == true) {
                        Toast.makeText(
                            this@SellDetailActivity,
                            "상품 등록 완료",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this@SellDetailActivity, MarketActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@SellDetailActivity,
                            responseBody?.message ?: "등록 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@SellDetailActivity,
                        "서버 오류: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("DEBUG", "exception=${e.message}", e)
                Toast.makeText(this@SellDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderImages(images: List<String>) {
        photoContainer.removeAllViews()

        if (images.isEmpty()) {
            Log.e("SELL_DETAIL", "renderImages: no images")
            return
        }

        images.forEach { imagePath ->
            Log.d("SELL_DETAIL", "render imagePath=$imagePath")

            val frame = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(100),
                    dpToPx(100)
                ).apply {
                    marginEnd = dpToPx(8)
                }
                setBackgroundResource(R.drawable.bg_photo_upload_box)
            }

            val imageView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            frame.addView(imageView)
            photoContainer.addView(frame)

            when {
                imagePath.startsWith("content://") -> {
                    imageView.setImageURI(Uri.parse(imagePath))
                }

                imagePath.startsWith("http://") || imagePath.startsWith("https://") -> {
                    loadImageFromUrl(imageView, imagePath)
                }

                imagePath.startsWith("/uploads/") -> {
                    val finalUrl = "http://3.36.120.78:8080$imagePath"
                    loadImageFromUrl(imageView, finalUrl)
                }

                else -> {
                    Log.e("SELL_DETAIL", "unsupported image path: $imagePath")
                }
            }
        }
    }

    private fun loadImageFromUrl(imageView: ImageView, imageUrl: String) {
        lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val url = URL(imageUrl)
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 10000
                        readTimeout = 10000
                        doInput = true
                        connect()
                    }

                    val responseCode = connection.responseCode
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
            } catch (e: Exception) {
                Log.e("SELL_DETAIL", "image load error=$imageUrl", e)
            }
        }
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.gohome)?.setOnClickListener { startActivity(Intent(this, MarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.goMymarket)?.setOnClickListener { startActivity(Intent(this, MyMarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gomypage)?.setOnClickListener { startActivity(Intent(this, MypageActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gochat)?.setOnClickListener { startActivity(Intent(this, ChatListActivity::class.java)) }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}