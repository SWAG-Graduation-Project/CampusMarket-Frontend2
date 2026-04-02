package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.jvm.java

class MyMarketDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_market_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<Button>(R.id.backbutton)

        btnBack.setOnClickListener {
            val intent = Intent(this, MyMarketActivity::class.java)
            startActivity(intent)
        }

        val FinishButton = findViewById<Button>(R.id.change_finish)

        FinishButton.setOnClickListener {
            val intent = Intent(this, FinishSellActivity::class.java)
            startActivity(intent)
        }
    }
}
