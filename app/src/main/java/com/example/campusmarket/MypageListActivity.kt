package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarket.model.Post

class MypageListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        val list = listOf(
            Post("상품1", "15000원", "07.21", 999),
            Post("상품2", "3000원", "07.22", 120),            Post("상품1", "15000원", "07.21", 999),
            Post("상품1", "15000원", "07.21", 999),
            Post("상품1", "15000원", "07.21", 999),
            Post("상품1", "15000원", "07.21", 999),
            Post("상품1", "15000원", "07.21", 999),
            Post("상품1", "15000원", "07.21", 999),
            Post("상품1", "15000원", "07.21", 999),

            )

        recyclerView.adapter = PostAdapter(list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.gohome)?.setOnClickListener { startActivity(Intent(this, MarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.goMymarket)?.setOnClickListener { startActivity(Intent(this, MyMarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gomypage)?.setOnClickListener { startActivity(Intent(this, MypageActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gochat)?.setOnClickListener { startActivity(Intent(this, ChatListActivity::class.java)) }
    }
}