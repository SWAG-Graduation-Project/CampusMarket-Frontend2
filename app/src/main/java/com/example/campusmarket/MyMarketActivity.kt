package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyMarketActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_market)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnSell = findViewById<Button>(R.id.btn_sell)

        btnSell.setOnClickListener {
            val intent = Intent(this, SellActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerUserMarketPosts)

        val dummyList = listOf(
            "상품 1",
            "상품 2",
            "상품 3",
            "상품 4",
            "상품 4",

            "상품 4",

            "상품 4",
            "상품 4",
            "상품 4",
            "상품 4",


            )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MarketAdapter(dummyList)
    }
}