package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class LockerMapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locker_map)
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.gohome)?.setOnClickListener { startActivity(Intent(this, MarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.goMymarket)?.setOnClickListener { startActivity(Intent(this, MyMarketActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gomypage)?.setOnClickListener { startActivity(Intent(this, MypageActivity::class.java)) }
        findViewById<LinearLayout>(R.id.gochat)?.setOnClickListener { startActivity(Intent(this, ChatListActivity::class.java)) }
    }
}
