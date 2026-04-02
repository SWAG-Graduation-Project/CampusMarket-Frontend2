package com.example.campusmarket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarket.model.Post


class PostAdapter(private val list: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.tvTitle)
        val date = itemView.findViewById<TextView>(R.id.tvDate)
        val price = itemView.findViewById<TextView>(R.id.tvPrice)
        val likeCount = itemView.findViewById<TextView>(R.id.tvLikeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.title
        holder.price.text = item.price
        holder.date.text = item.date
        holder.likeCount.text = item.likeCount.toString()
    }

    override fun getItemCount(): Int = list.size
}