package com.example.campusmarket

import android.os.Bundle
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
    }
}