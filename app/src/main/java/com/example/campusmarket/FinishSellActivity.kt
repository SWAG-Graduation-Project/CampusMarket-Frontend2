package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FinishSellActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finish_sell)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.gohome)?.setOnClickListener { startActivity(Intent(this, MarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.goMymarket)?.setOnClickListener { startActivity(Intent(this, MyMarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gomypage)?.setOnClickListener { startActivity(Intent(this, MypageActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gochat)?.setOnClickListener { startActivity(Intent(this, ChatListActivity::class.java)) }
    }
}