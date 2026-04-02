package com.example.campusmarket

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarket.data.model.FreeSlot
import com.google.gson.Gson
import com.google.gson.JsonObject

class ChatMessageAdapter(
    private val items: List<ChatMessage>,
    private val isSeller: Boolean,
    var onProposalAccept: ((proposalId: Long) -> Unit)? = null,
    var onProposalReject: ((proposalId: Long) -> Unit)? = null,
    var onLockerCheck: ((metadata: String?) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_OTHER = 0
        const val TYPE_ME = 1
        const val TYPE_OFFER_NOTICE = 2
        const val TYPE_SYSTEM_TEXT = 3
        const val TYPE_IMAGE_ME = 4
        const val TYPE_IMAGE_OTHER = 5
        const val TYPE_FREE_SLOTS = 6
        const val TYPE_LOCKER_SHARE_ME = 7
        const val TYPE_LOCKER_SHARE_OTHER = 8
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item.messageType) {
            "PROPOSAL", "LOCKER_PROPOSAL", "FACE_TO_FACE_PROPOSAL" -> TYPE_OFFER_NOTICE
            "TIMETABLE_SHARE" -> if (item.isMine) TYPE_IMAGE_ME else TYPE_IMAGE_OTHER
            "LOCKER_SHARE" -> if (item.isMine) TYPE_LOCKER_SHARE_ME else TYPE_LOCKER_SHARE_OTHER
            "SYSTEM" -> {
                if (!item.metadata.isNullOrBlank() && item.metadata.contains("freeSlots"))
                    TYPE_FREE_SLOTS
                else
                    TYPE_SYSTEM_TEXT
            }
            else -> if (item.isMine) TYPE_ME else TYPE_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ME -> MyMessageViewHolder(inflater.inflate(R.layout.item_chat_me, parent, false))
            TYPE_OFFER_NOTICE -> OfferNoticeViewHolder(inflater.inflate(R.layout.item_chat_offer_notice, parent, false))
            TYPE_SYSTEM_TEXT -> SystemTextViewHolder(inflater.inflate(R.layout.item_chat_no_locker, parent, false))
            TYPE_IMAGE_ME -> ImageMeViewHolder(inflater.inflate(R.layout.item_chat_image_me, parent, false))
            TYPE_IMAGE_OTHER -> ImageOtherViewHolder(inflater.inflate(R.layout.item_chat_image_other, parent, false))
            TYPE_FREE_SLOTS -> FreeSlotsViewHolder(inflater.inflate(R.layout.item_chat_free_slots, parent, false))
            TYPE_LOCKER_SHARE_ME -> LockerShareMeViewHolder(inflater.inflate(R.layout.item_chat_locker_share_me, parent, false))
            TYPE_LOCKER_SHARE_OTHER -> LockerShareOtherViewHolder(inflater.inflate(R.layout.item_chat_locker_share_other, parent, false))
            else -> OtherMessageViewHolder(inflater.inflate(R.layout.item_chat_other, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is MyMessageViewHolder -> holder.bind(item)
            is OtherMessageViewHolder -> holder.bind(item)
            is OfferNoticeViewHolder -> holder.bind(item)
            is SystemTextViewHolder -> holder.bind(item)
            is ImageMeViewHolder -> holder.bind(item)
            is ImageOtherViewHolder -> holder.bind(item)
            is FreeSlotsViewHolder -> holder.bind(item)
            is LockerShareMeViewHolder -> holder.bind(item)
            is LockerShareOtherViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // ── TEXT: 내 메시지 ──
    inner class MyMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMyMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvMyTime)
        fun bind(item: ChatMessage) {
            tvMessage.text = item.message
            tvTime.text = item.time
        }
    }

    // ── TEXT: 상대방 메시지 ──
    inner class OtherMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvOtherMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvOtherTime)
        fun bind(item: ChatMessage) {
            tvSender.text = item.senderName
            tvMessage.text = item.message
            tvTime.text = item.time
        }
    }

    // ── 거래 제안 알림 ──
    inner class OfferNoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNotice: TextView = itemView.findViewById(R.id.tvOfferNotice)
        private val btnAccept: ImageButton = itemView.findViewById(R.id.btnOfferAccept)
        private val btnReject: ImageButton = itemView.findViewById(R.id.btnOfferReject)

        fun bind(item: ChatMessage) {
            val typeLabel = if (item.proposalType == "LOCKER") "사물함" else "대면"
            tvNotice.text = "판매자가 $typeLabel 거래를 제안했습니다"

            // null은 PENDING으로 간주, ACCEPTED/REJECTED일 때만 비활성화
            val isResolved = item.proposalStatus == "ACCEPTED" || item.proposalStatus == "REJECTED"
            // 판매자는 수락/거절 불가, 구매자만 가능
            val canRespond = !isSeller && !isResolved
            btnAccept.isEnabled = canRespond
            btnReject.isEnabled = canRespond
            btnAccept.alpha = if (canRespond) 1f else 0.4f
            btnReject.alpha = if (canRespond) 1f else 0.4f

            btnAccept.setOnClickListener {
                if (!canRespond) return@setOnClickListener
                val id = item.proposalId
                if (id == null) {
                    android.util.Log.e("PROPOSAL", "proposalId is null. metadata=${item.metadata}")
                    android.widget.Toast.makeText(itemView.context, "제안 정보를 불러올 수 없습니다", android.widget.Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                onProposalAccept?.invoke(id)
            }
            btnReject.setOnClickListener {
                if (!canRespond) return@setOnClickListener
                val id = item.proposalId
                if (id == null) {
                    android.util.Log.e("PROPOSAL", "proposalId is null. metadata=${item.metadata}")
                    android.widget.Toast.makeText(itemView.context, "제안 정보를 불러올 수 없습니다", android.widget.Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                onProposalReject?.invoke(id)
            }
        }
    }

    // ── SYSTEM 텍스트 메시지 (예: "매칭이 되는 시간이 없습니다!") ──
    inner class SystemTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNotice: TextView = itemView.findViewById(R.id.tvNoLockerNotice)
        fun bind(item: ChatMessage) {
            if (item.message.isNotBlank()) tvNotice.text = item.message
        }
    }

    // ── TIMETABLE_SHARE: 내가 보낸 시간표 ──
    inner class ImageMeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivMyImage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvMyImageTime)
        fun bind(item: ChatMessage) {
            tvTime.text = item.time
            val slots = parseClasses(item.metadata)
            ivImage.setImageBitmap(drawTimetableBitmap(ivImage, slots, Color.parseColor("#64B5F6")))
        }
    }

    // ── TIMETABLE_SHARE: 상대방이 보낸 시간표 ──
    inner class ImageOtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivOtherImage)
        private val tvSender: TextView = itemView.findViewById(R.id.tvOtherImageSenderName)
        private val tvTime: TextView = itemView.findViewById(R.id.tvOtherImageTime)
        fun bind(item: ChatMessage) {
            tvSender.text = item.senderName
            tvTime.text = item.time
            val slots = parseClasses(item.metadata)
            ivImage.setImageBitmap(drawTimetableBitmap(ivImage, slots, Color.parseColor("#64B5F6")))
        }
    }

    // ── SYSTEM 시간표 (freeSlots) ──
    inner class FreeSlotsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivTimetable: ImageView = itemView.findViewById(R.id.ivTimetable)

        fun bind(item: ChatMessage) {
            val slots = parseFreeSlots(item.metadata)
            ivTimetable.setImageBitmap(drawTimetableBitmap(ivTimetable, slots, Color.parseColor("#81C784")))
        }

        private fun parseFreeSlots(metadata: String?): List<FreeSlot> {
            if (metadata.isNullOrBlank()) return emptyList()
            return try {
                val json = Gson().fromJson(metadata, JsonObject::class.java)
                val arr = json.getAsJsonArray("freeSlots") ?: return emptyList()
                arr.map { el ->
                    val obj = el.asJsonObject
                    FreeSlot(
                        day = obj.get("day").asString,
                        start_time = obj.get("start_time").asString,
                        end_time = obj.get("end_time").asString
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    // ── LOCKER_SHARE: 내가 보낸 사물함 정보 ──
    inner class LockerShareMeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMyMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvMyTime)
        private val btnCheck: android.widget.Button = itemView.findViewById(R.id.btnCheckLocker)
        fun bind(item: ChatMessage) {
            tvMessage.text = formatLockerText(item.metadata, item.message)
            tvTime.text = item.time
            btnCheck.setOnClickListener { onLockerCheck?.invoke(item.metadata) }
        }
    }

    // ── LOCKER_SHARE: 상대방이 보낸 사물함 정보 ──
    inner class LockerShareOtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvOtherMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvOtherTime)
        private val btnCheck: android.widget.Button = itemView.findViewById(R.id.btnCheckLocker)
        fun bind(item: ChatMessage) {
            tvSender.text = item.senderName
            tvMessage.text = formatLockerText(item.metadata, item.message)
            tvTime.text = item.time
            btnCheck.setOnClickListener { onLockerCheck?.invoke(item.metadata) }
        }
    }

    private fun formatLockerText(metadata: String?, fallback: String): String {
        if (metadata.isNullOrBlank()) return fallback
        return try {
            val obj = Gson().fromJson(metadata, JsonObject::class.java)
            val building = listOf("lockerBuilding", "building", "buildingName")
                .firstNotNullOfOrNull { k -> obj.get(k)?.asString?.takeIf { it.isNotBlank() } }
                ?: return fallback
            val floor = listOf("lockerFloor", "floor")
                .firstNotNullOfOrNull { k ->
                    val el = obj.get(k) ?: return@firstNotNullOfOrNull null
                    runCatching {
                        if (el.isJsonPrimitive) {
                            val p = el.asJsonPrimitive
                            if (p.isNumber) p.asInt else p.asString.trim().toInt()
                        } else null
                    }.getOrNull()
                } ?: return fallback
            val major = listOf("lockerMajor", "major")
                .firstNotNullOfOrNull { k -> obj.get(k)?.asString?.takeIf { it.isNotBlank() } } ?: ""
            val group = listOf("lockerGroup", "groupNumber")
                .firstNotNullOfOrNull { k -> runCatching { obj.get(k)?.asInt }.getOrNull() } ?: 0
            val row = listOf("lockerRow", "row")
                .firstNotNullOfOrNull { k -> runCatching { obj.get(k)?.asInt }.getOrNull() } ?: 0
            val col = listOf("lockerCol", "col")
                .firstNotNullOfOrNull { k -> runCatching { obj.get(k)?.asInt }.getOrNull() } ?: 0

            buildString {
                append("$building ${floor}층")
                if (major.isNotBlank()) append(" $major")
                if (group > 0) append(" ${group}그룹")
                if (row > 0) append(" ${row}행")
                if (col > 0) append(" ${col}열")
            }
        } catch (e: Exception) {
            fallback
        }
    }

    // ── 공용 헬퍼: metadata.classes 파싱 ──
    private fun parseClasses(metadata: String?): List<FreeSlot> {
        if (metadata.isNullOrBlank()) return emptyList()
        return try {
            val json = Gson().fromJson(metadata, JsonObject::class.java)
            val arr = json.getAsJsonArray("classes") ?: return emptyList()
            arr.map { el ->
                val obj = el.asJsonObject
                FreeSlot(
                    day = obj.get("day").asString,
                    start_time = obj.get("start_time").asString,
                    end_time = obj.get("end_time").asString
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── 공용 헬퍼: 시간표 비트맵 그리기 ──
    private fun drawTimetableBitmap(view: ImageView, slots: List<FreeSlot>, slotColor: Int): Bitmap {
        val density = view.context.resources.displayMetrics.density
        val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
        val startHour = 9
        val endHour = 24
        val hours = endHour - startHour

        val labelW = (36 * density).toInt()
        val cellW = (38 * density).toInt()
        val cellH = (22 * density).toInt()
        val headerH = (22 * density).toInt()

        val bmpW = labelW + dayLabels.size * cellW
        val bmpH = headerH + hours * cellH

        val bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 슬롯 채우기
        paint.color = slotColor
        for (slot in slots) {
            val dayIdx = dayLabels.indexOf(slot.day)
            if (dayIdx == -1) continue
            val startMin = parseMinutes(slot.start_time)
            val endMin = parseMinutes(slot.end_time)
            val clampStart = maxOf(startMin, startHour * 60)
            val clampEnd = minOf(endMin, endHour * 60)
            if (clampStart >= clampEnd) continue
            val left = (labelW + dayIdx * cellW + 1).toFloat()
            val right = (labelW + (dayIdx + 1) * cellW - 1).toFloat()
            val top = headerH + (clampStart - startHour * 60) / 60f * cellH
            val bottom = headerH + (clampEnd - startHour * 60) / 60f * cellH
            canvas.drawRect(left, top, right, bottom, paint)
        }

        // 격자선
        paint.color = Color.parseColor("#CCCCCC")
        paint.strokeWidth = 1f
        for (i in 0..hours) {
            val y = (headerH + i * cellH).toFloat()
            canvas.drawLine(0f, y, bmpW.toFloat(), y, paint)
        }
        for (i in 0..dayLabels.size) {
            val x = (labelW + i * cellW).toFloat()
            canvas.drawLine(x, 0f, x, bmpH.toFloat(), paint)
        }

        // 요일 헤더
        paint.color = Color.parseColor("#222222")
        paint.textSize = 9 * density
        paint.textAlign = Paint.Align.CENTER
        for ((i, day) in dayLabels.withIndex()) {
            val x = (labelW + i * cellW + cellW / 2).toFloat()
            val y = headerH / 2f + paint.textSize / 3
            canvas.drawText(day, x, y, paint)
        }

        // 시간 레이블 (1시간마다)
        paint.textSize = 7.5f * density
        paint.textAlign = Paint.Align.RIGHT
        for (h in startHour until endHour) {
            val y = (headerH + (h - startHour) * cellH + paint.textSize).toFloat()
            canvas.drawText("%02d".format(h), (labelW - 2 * density), y, paint)
        }

        return bitmap
    }

    private fun parseMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + (if (parts.size > 1) parts[1].toInt() else 0)
    }
}
