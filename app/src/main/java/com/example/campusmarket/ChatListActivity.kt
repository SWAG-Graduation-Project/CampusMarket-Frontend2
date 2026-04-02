package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ChatListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvSell: TextView
    private lateinit var tvBuy: TextView
    private var isSellTabActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        recyclerView = findViewById(R.id.recyclerChat)
        recyclerView.layoutManager = LinearLayoutManager(this)

        tvSell = findViewById(R.id.tvSell)
        tvBuy = findViewById(R.id.tvBuy)

        tvSell.setOnClickListener {
            if (!isSellTabActive) {
                isSellTabActive = true
                updateTabStyle()
                loadChatRooms()
            }
        }

        tvBuy.setOnClickListener {
            if (isSellTabActive) {
                isSellTabActive = false
                updateTabStyle()
                loadChatRooms()
            }
        }

        updateTabStyle()
        loadChatRooms()
    }

    private fun updateTabStyle() {
        if (isSellTabActive) {
            tvSell.setTextColor(android.graphics.Color.parseColor("#2B2B2B"))
            tvBuy.setTextColor(android.graphics.Color.parseColor("#8A8A8A"))
        } else {
            tvSell.setTextColor(android.graphics.Color.parseColor("#8A8A8A"))
            tvBuy.setTextColor(android.graphics.Color.parseColor("#2B2B2B"))
        }
    }

    private fun loadChatRooms() {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            recyclerView.adapter = SellingChatAdapter(emptyList()) { _, _ -> }
            return
        }

        lifecycleScope.launch {
            try {
                val response = if (isSellTabActive) {
                    RetrofitClient.apiService.getSellingChatRooms(guestUuid)
                } else {
                    RetrofitClient.apiService.getBuyingChatRooms(guestUuid)
                }

                if (response.isSuccessful) {
                    val chatRooms = response.body()?.result?.chatRooms ?: emptyList()
                    recyclerView.adapter = SellingChatAdapter(chatRooms) { chatRoomId, isSeller ->
                        val intent = Intent(this@ChatListActivity, ChattActivity::class.java)
                        intent.putExtra("chatRoomId", chatRoomId)
                        intent.putExtra("isSeller", isSeller)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this@ChatListActivity, "채팅 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
                    recyclerView.adapter = SellingChatAdapter(emptyList()) { _, _ -> }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ChatListActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                recyclerView.adapter = SellingChatAdapter(emptyList()) { _, _ -> }
            }
        }
    }
}
