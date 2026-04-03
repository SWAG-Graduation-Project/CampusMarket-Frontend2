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

            val dummyList = listOf(
                MarketItem("까만 바지", "15,000원", R.drawable.pants),
                MarketItem("운동화", "50,000원", R.drawable.shoes2),
                MarketItem("까만 자켓", "18,000원", R.drawable.jaket),
                MarketItem("초록 자켓", "10,000원", R.drawable.jaket3),
                MarketItem("까만 자켓", "20,000원", R.drawable.jaket),
                MarketItem("초록 자켓", "13,000원", R.drawable.jaket3),
                MarketItem("까만 자켓", "17,000원", R.drawable.jaket),
                MarketItem("초록 자켓", "11,000원", R.drawable.jaket3),
                MarketItem("까만 자켓", "19,000원", R.drawable.jaket),
                MarketItem("초록 자켓", "14,000원", R.drawable.jaket3)
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