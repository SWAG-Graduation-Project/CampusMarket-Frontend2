package com.example.campusmarket

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarket.data.model.UserMarketProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

class UserMarketPostAdapter(
    private val items: List<UserMarketProduct>,
    private val onProductImageClick: (Long) -> Unit
) : RecyclerView.Adapter<UserMarketPostAdapter.UserMarketPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserMarketPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_market_post, parent, false)
        return UserMarketPostViewHolder(view, onProductImageClick)
    }

    override fun onBindViewHolder(holder: UserMarketPostViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class UserMarketPostViewHolder(
        itemView: View,
        private val onProductImageClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)

        private var imageJob: Job? = null

        fun bind(item: UserMarketProduct) {
            tvTitle.text = item.name
            tvDate.text = formatDate(item.createdAt)
            tvPrice.text = if (item.isFree) "무료 나눔" else formatPrice(item.price)




            ivProduct.setOnClickListener {
                onProductImageClick(item.productId)
            }

            imageJob?.cancel()
            ivProduct.setImageDrawable(null)

            val imageUrl = item.thumbnailImageUrl

            if (!imageUrl.isNullOrBlank()) {
                imageJob = CoroutineScope(Dispatchers.Main).launch {
                    val bitmap = withContext(Dispatchers.IO) {
                        loadBitmapFromUrl(imageUrl)
                    }

                    if (bitmap != null) {
                        ivProduct.setImageBitmap(bitmap)
                    } else {
                        ivProduct.setImageResource(R.drawable.clothes12)
                    }
                }
            } else {
                ivProduct.setImageResource(R.drawable.clothes12)
            }
        }

        private fun formatDate(rawDate: String?): String {
            if (rawDate.isNullOrBlank()) return "-"

            return try {
                val datePart = rawDate.substring(0, 10)
                val split = datePart.split("-")
                "${split[1]}.${split[2]}"
            } catch (e: Exception) {
                rawDate
            }
        }

        private fun formatPrice(price: Int): String {
            return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원"
        }

        private fun loadBitmapFromUrl(imageUrl: String): Bitmap? {
            return try {
                val url = URL(imageUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    doInput = true
                    connect()
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    connection.disconnect()
                    return null
                }

                val stream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(stream)
                stream.close()
                connection.disconnect()
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }
}