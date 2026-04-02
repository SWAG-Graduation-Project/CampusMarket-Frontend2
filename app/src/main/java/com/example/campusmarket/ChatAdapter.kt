package com.example.campusmarket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val itemList: List<ChatPost>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTop: TextView = view.findViewById(R.id.tvTitleTop)
        val productName: TextView = view.findViewById(R.id.tvProductName)
        val date: TextView = view.findViewById(R.id.tvDate)
        val preview: TextView = view.findViewById(R.id.tvPreview)
        val thumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        holder.titleTop.text = item.title
        holder.productName.text = item.productName
        holder.date.text = item.date
        holder.preview.text = item.preview
        holder.thumbnail.setImageResource(item.imageRes)
    }

    override fun getItemCount(): Int = itemList.size
}