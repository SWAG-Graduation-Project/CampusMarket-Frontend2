package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyMarketActivity : AppCompatActivity() {

    private lateinit var btnSell: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var navHome: LinearLayout
    private lateinit var navMyMarket: LinearLayout
    private lateinit var navChat: LinearLayout
    private lateinit var navMyPage: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_market)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupRecyclerView()
        setupClickListeners()
        setupBottomNavigation()
    }

    private fun bindViews() {
        btnSell = findViewById(R.id.btn_sell)
        recyclerView = findViewById(R.id.recyclerUserMarketPosts)

        navHome = findViewById(R.id.navHome)
        navMyMarket = findViewById(R.id.navMyMarket)
        navChat = findViewById(R.id.navChat)
        navMyPage = findViewById(R.id.navMyPage)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        // recyclerView.adapter = MarketAdapter(dummyList)
    }

    private fun setupClickListeners() {
        btnSell.setOnClickListener {
            val intent = Intent(this, SellActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        navHome.setOnClickListener {
            startActivity(Intent(this, MarketActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        navMyMarket.setOnClickListener {
            // 현재 페이지라서 이동 안 함
        }

        navChat.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        navMyPage.setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }
}