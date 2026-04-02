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



class MarketListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_market_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val btnMarket = findViewById<Button>(R.id.btnmarket)

        btnMarket.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            startActivity(intent)
        }





            val recyclerView = findViewById<RecyclerView>(R.id.recyclerMarketList)

            // 더미 데이터
            val dummyList = listOf(
                "상품 1",
                "상품 2",
                "상품 3",
                "상품 4",
                "상품 4",

                "상품 4",

                "상품 5",
                "상품 5"
            )

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = MarketAdapter(dummyList)
        setupBottomNavigation()
        }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.gohome)?.setOnClickListener { startActivity(Intent(this, MarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.goMymarket)?.setOnClickListener { startActivity(Intent(this, MyMarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gomypage)?.setOnClickListener { startActivity(Intent(this, MypageActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gochat)?.setOnClickListener { startActivity(Intent(this, ChatListActivity::class.java)) }
    }
}