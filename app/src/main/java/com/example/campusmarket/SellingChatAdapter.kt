package com.example.campusmarket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusmarket.data.model.SellingChatRoom

class SellingChatAdapter(
    private val itemList: List<SellingChatRoom>,
    private val onClick: (chatRoomId: Long) -> Unit
) : RecyclerView.Adapter<SellingChatAdapter.ViewHolder>() {

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

        holder.titleTop.text = item.productName ?: "상품 없음"
        holder.productName.text = if (item.isSeller) {
            item.buyerNickname ?: "구매자"
        } else {
            item.sellerNickname ?: "판매자"
        }
        holder.date.text = formatDate(item.lastMessageAt)
        holder.preview.text = item.lastMessageContent ?: ""

        Glide.with(holder.thumbnail.context)
            .load(item.productThumbnailUrl)
            .placeholder(R.drawable.chat_brawn)
            .error(R.drawable.chat_brawn)
            .into(holder.thumbnail)

        holder.itemView.setOnClickListener { onClick(item.chatRoomId) }
    }

    override fun getItemCount(): Int = itemList.size

    private fun formatDate(dateString: String?): String {
        if (dateString == null) return ""
        return try {
            // "2024-07-21T23:21:00" → "07.21"
            val datePart = dateString.substringBefore("T")
            val parts = datePart.split("-")
            if (parts.size >= 3) "${parts[1]}.${parts[2]}" else dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
