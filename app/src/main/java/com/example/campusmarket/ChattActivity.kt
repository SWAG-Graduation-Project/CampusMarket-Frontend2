package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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
import com.example.campusmarket.data.model.ChatReceiveDto
import com.example.campusmarket.data.model.ChatSendRequest
import com.example.campusmarket.data.model.ProposalRequest
import com.example.campusmarket.data.model.ProposalRespondRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class ChattActivity : AppCompatActivity() {

    private lateinit var recyclerChatMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnSuggestDelivery: TextView
    private lateinit var btnSuggestDirect: TextView
    private lateinit var btnCheck: ImageButton
    private lateinit var btnDown: ImageButton
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatMessageAdapter
    private lateinit var btnReport: Button

    private lateinit var btnFabMain: ImageButton
    private lateinit var layoutQuickActions: LinearLayout
    private lateinit var burgerbar: ImageView
    private lateinit var chatSettingOverlay: View
    private lateinit var chatSettingPanel: View
    private lateinit var btnCloseSetting: ImageView
    private lateinit var settingDim: View
    private var isFabOpen = false

    private var chatRoomId: Long = -1L
    private var myMemberId: Long? = null
    private var guestUuid: String? = null
    private var isSeller: Boolean = false
    private var stompManager: StompManager? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chatt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        chatRoomId = intent.getLongExtra("chatRoomId", -1L)
        guestUuid = GuestManager.getGuestUuid(this)
        myMemberId = GuestManager.getMemberId(this)
        isSeller = intent.getBooleanExtra("isSeller", false)

        val title = findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "채팅"

        val backBtn = findViewById<ImageButton>(R.id.backbutton)
        backBtn.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
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

        initViews()
        initRecyclerView()
        setupListeners()

        btnFabMain.setOnClickListener { toggleFab() }

        // 판매자가 아니면 제안 버튼 숨김
        if (!isSeller) layoutQuickActions.visibility = View.GONE

        if (chatRoomId != -1L) {
            loadPreviousMessages()
        }
    }

    private fun loadPreviousMessages() {
        val uuid = guestUuid ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getChatMessages(uuid, chatRoomId)
                if (response.isSuccessful) {
                    val messages = response.body()?.result?.messages ?: emptyList()
                    messages.forEach { dto -> messageList.add(dtoToChatMessage(dto)) }
                    chatAdapter.notifyDataSetChanged()
                    if (messageList.isNotEmpty()) {
                        recyclerChatMessages.scrollToPosition(messageList.size - 1)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            connectStomp()
        }
    }

    private fun dtoToChatMessage(dto: ChatReceiveDto): ChatMessage {
        val isMine = dto.senderId != null && dto.senderId == myMemberId
        val senderName = if (isMine) "" else (dto.senderNickname ?: "알 수 없음")
        val time = formatTime(dto.createdAt)
        val type = dto.messageType ?: "TEXT"

        // PROPOSAL 메시지: metadata에서 proposalId/proposalType/proposalStatus 파싱
        if (type == "PROPOSAL" || type.endsWith("_PROPOSAL")) {
            val (proposalId, proposalType, proposalStatus) = parseProposalMeta(dto.metadata)
            android.util.Log.d("PROPOSAL", "type=$type metadata=${dto.metadata} proposalId=$proposalId proposalType=$proposalType proposalStatus=$proposalStatus")
            return ChatMessage(
                senderName = senderName,
                message = dto.content ?: "",
                time = time,
                isMine = isMine,
                messageType = "PROPOSAL",
                proposalId = proposalId,
                proposalType = proposalType,
                proposalStatus = proposalStatus ?: "PENDING",
                metadata = dto.metadata
            )
        }

        return ChatMessage(
            senderName = senderName,
            message = dto.content ?: "",
            time = time,
            isMine = isMine,
            messageType = type,
            metadata = dto.metadata
        )
    }

    private fun parseProposalMeta(metadata: String?): Triple<Long?, String?, String?> {
        if (metadata.isNullOrBlank()) return Triple(null, null, "PENDING")
        return try {
            val obj = gson.fromJson(metadata, JsonObject::class.java)
            val id = if (obj.has("proposalId")) obj.get("proposalId").asLong else null
            val type = if (obj.has("proposalType")) obj.get("proposalType").asString else null
            val status = if (obj.has("proposalStatus")) obj.get("proposalStatus").asString else "PENDING"
            Triple(id, type, status)
        } catch (e: Exception) {
            Triple(null, null, "PENDING")
        }
    }

    private fun connectStomp() {
        stompManager = StompManager("ws://3.36.120.78:8080/api/ws")

        stompManager?.onConnected = {
            stompManager?.subscribe("/sub/chat/$chatRoomId")
        }

        stompManager?.onMessage = { json ->
            android.util.Log.d("WS_RAW", "received: $json")
            try {
                val dto = gson.fromJson(json, ChatReceiveDto::class.java)
                val msg = dtoToChatMessage(dto)
                runOnUiThread {
                    messageList.add(msg)
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerChatMessages.scrollToPosition(messageList.size - 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        stompManager?.onError = { error ->
            runOnUiThread {
                Toast.makeText(this, "연결 오류: $error", Toast.LENGTH_SHORT).show()
            }
        }

        stompManager?.connect()
    }

    private fun sendProposal(proposalType: String) {
        val uuid = guestUuid ?: return
        if (chatRoomId == -1L) return
        layoutQuickActions.visibility = View.GONE
        isFabOpen = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createProposal(
                    uuid, chatRoomId, ProposalRequest(proposalType)
                )
                if (!response.isSuccessful || response.body()?.success != true) {
                    Toast.makeText(this@ChattActivity, "제안 전송 실패", Toast.LENGTH_SHORT).show()
                }
                // 성공 시 WebSocket으로 메시지가 push됨 → onMessage에서 자동 처리
            } catch (e: Exception) {
                Toast.makeText(this@ChattActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun respondToProposal(proposalId: Long, accept: Boolean) {
        val uuid = guestUuid ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.respondToProposal(
                    uuid, chatRoomId, proposalId, ProposalRespondRequest(accept)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val status = if (accept) "ACCEPTED" else "REJECTED"
                    // 기존 proposal 메시지의 상태 업데이트 (버튼 비활성화)
                    val idx = messageList.indexOfLast {
                        it.messageType == "PROPOSAL" && it.proposalId == proposalId
                    }
                    if (idx != -1) {
                        messageList[idx] = messageList[idx].copy(proposalStatus = status)
                        runOnUiThread { chatAdapter.notifyItemChanged(idx) }
                    }
                    if (accept) {
                        val typeLabel = response.body()?.result?.proposalType
                            ?.let { if (it == "LOCKER") "사물함" else "대면" } ?: ""
                        runOnUiThread {
                            Toast.makeText(this@ChattActivity, "${typeLabel} 거래가 성사되었습니다!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 수락 시 WebSocket으로 TIMETABLE_SHARE / SYSTEM 메시지 push됨
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ChattActivity, "응답 처리 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ChattActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatTime(createdAt: String?): String {
        if (createdAt == null) return getCurrentTimeText()
        return try {
            val timePart = createdAt.substringAfter("T").take(5)
            if (timePart.length == 5) timePart else getCurrentTimeText()
        } catch (e: Exception) {
            getCurrentTimeText()
        }
    }

    private fun toggleFab() {
        if (isSeller) {
            isFabOpen = !isFabOpen
            layoutQuickActions.visibility = if (isFabOpen) View.VISIBLE else View.GONE
        }
    }

    private fun showSellCompleteDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_sell_complete)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun initViews() {
        recyclerChatMessages = findViewById(R.id.recyclerChatMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnSuggestDelivery = findViewById(R.id.btnSuggestDelivery)
        btnSuggestDirect = findViewById(R.id.btnSuggestDirect)
        btnFabMain = findViewById(R.id.btnFabMain)
        layoutQuickActions = findViewById(R.id.layoutQuickActions)
        btnCheck = findViewById(R.id.btnCheck)
        btnDown = findViewById(R.id.btnDown)
        burgerbar = findViewById(R.id.burgerbar)
        chatSettingOverlay = findViewById(R.id.chatSettingOverlay)
        chatSettingPanel = findViewById(R.id.chatSettingPanel)
        btnCloseSetting = findViewById(R.id.btnCloseSetting)
        settingDim = findViewById(R.id.settingDim)
        btnReport = findViewById(R.id.btnReport)
    }

    private fun initRecyclerView() {
        chatAdapter = ChatMessageAdapter(
            items = messageList,
            isSeller = isSeller,
            onProposalAccept = { proposalId -> respondToProposal(proposalId, true) },
            onProposalReject = { proposalId -> respondToProposal(proposalId, false) }
        )
        recyclerChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = false
        }
        recyclerChatMessages.adapter = chatAdapter
    }

    private fun setupListeners() {
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMyMessage(text)
                etMessage.text.clear()
            }
        }

        // 판매자: API 호출로 제안 전송
        btnSuggestDelivery.setOnClickListener { sendProposal("LOCKER") }
        btnSuggestDirect.setOnClickListener { sendProposal("FACE_TO_FACE") }

        burgerbar.setOnClickListener { openSettingPanel() }
        btnCloseSetting.setOnClickListener { closeSettingPanel() }
        settingDim.setOnClickListener { closeSettingPanel() }
        btnCheck.setOnClickListener { showSellCompleteDialog() }
        btnReport.setOnClickListener { showReportDialog() }
    }

    private fun showReportDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_report)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnBack = dialog.findViewById<Button>(R.id.btnBack)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmitReport)
        val option1 = dialog.findViewById<TextView>(R.id.option1)
        val option2 = dialog.findViewById<TextView>(R.id.option2)
        val option3 = dialog.findViewById<TextView>(R.id.option3)
        val option4 = dialog.findViewById<TextView>(R.id.option4)

        var selected = ""
        option1.setOnClickListener { selected = "선입금 요구" }
        option2.setOnClickListener { selected = "외부 메신저 유도" }
        option3.setOnClickListener { selected = "욕설 / 비하" }
        option4.setOnClickListener { selected = "기타" }

        btnSubmit.setOnClickListener { if (selected.isNotEmpty()) dialog.dismiss() }
        btnBack.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun openSettingPanel() {
        chatSettingOverlay.visibility = View.VISIBLE
        chatSettingPanel.post {
            chatSettingPanel.translationX = chatSettingPanel.width.toFloat()
            chatSettingPanel.animate().translationX(0f).setDuration(250).start()
        }
    }

    private fun closeSettingPanel() {
        chatSettingPanel.animate()
            .translationX(chatSettingPanel.width.toFloat())
            .setDuration(250)
            .withEndAction { chatSettingOverlay.visibility = View.GONE }
            .start()
    }

    private fun sendMyMessage(text: String) {
        val uuid = guestUuid ?: return
        if (chatRoomId == -1L) return
        val request = ChatSendRequest(guestUuid = uuid, messageType = "TEXT", content = text)
        stompManager?.send("/pub/chat/$chatRoomId", gson.toJson(request))
    }

    private fun getCurrentTimeText(): String {
        val cal = java.util.Calendar.getInstance()
        return String.format("%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
    }

    override fun onDestroy() {
        super.onDestroy()
        stompManager?.disconnect()
        stompManager = null
    }
}
