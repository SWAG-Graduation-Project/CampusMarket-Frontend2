package com.example.campusmarket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatMessageAdapter(
    private val items: List<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_OTHER = 0
        private const val TYPE_ME = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isMine) TYPE_ME else TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ME) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_me, parent, false)
            MyMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_other, parent, false)
            OtherMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is MyMessageViewHolder -> holder.bind(item)
            is OtherMessageViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class MyMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMyMessage: TextView = itemView.findViewById(R.id.tvMyMessage)
        private val tvMyTime: TextView = itemView.findViewById(R.id.tvMyTime)

        fun bind(item: ChatMessage) {
            tvMyMessage.text = item.message
            tvMyTime.text = item.time
        }
    }

    class OtherMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvOtherMessage: TextView = itemView.findViewById(R.id.tvOtherMessage)
        private val tvOtherTime: TextView = itemView.findViewById(R.id.tvOtherTime)

        fun bind(item: ChatMessage) {
            tvSenderName.text = item.senderName
            tvOtherMessage.text = item.message
            tvOtherTime.text = item.time
        }
    }
}