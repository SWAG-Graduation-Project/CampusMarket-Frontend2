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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarket.data.model.ChatReceiveDto
import com.example.campusmarket.data.model.ChatSendRequest
import com.google.gson.Gson
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

        val title = findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "채팅"

        val backBtn = findViewById<ImageButton>(R.id.backbutton)
        backBtn.setOnClickListener {
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
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
                    messages.forEach { dto ->
                        val isMine = dto.senderId != null && dto.senderId == myMemberId
                        val senderName = if (isMine) "" else (dto.senderNickname ?: "알 수 없음")
                        val content = dto.content ?: ""
                        val time = formatTime(dto.createdAt)
                        messageList.add(ChatMessage(senderName, content, time, isMine))
                    }
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

    private fun connectStomp() {
        stompManager = StompManager("ws://3.36.120.78:8080/api/ws")

        stompManager?.onConnected = {
            stompManager?.subscribe("/sub/chat/$chatRoomId")
        }

        stompManager?.onMessage = { json ->
            try {
                val dto = gson.fromJson(json, ChatReceiveDto::class.java)
                val isMine = dto.senderId != null && dto.senderId == myMemberId
                val senderName = if (isMine) "" else (dto.senderNickname ?: "알 수 없음")
                val content = dto.content ?: ""
                val time = formatTime(dto.createdAt)
                receiveMessageFromSocket(senderName, content, time, isMine)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        stompManager?.onError = { error ->
            runOnUiThread {
                android.widget.Toast.makeText(this, "연결 오류: $error", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        stompManager?.connect()
    }

    private fun formatTime(createdAt: String?): String {
        if (createdAt == null) return getCurrentTimeText()
        return try {
            // "2024-01-01T23:21:00" → "23:21"
            val timePart = createdAt.substringAfter("T").take(5)
            if (timePart.length == 5) timePart else getCurrentTimeText()
        } catch (e: Exception) {
            getCurrentTimeText()
        }
    }

    private fun toggleFab() {
        if (isFabOpen) {
            layoutQuickActions.visibility = View.GONE
        } else {
            layoutQuickActions.visibility = View.VISIBLE
        }
        isFabOpen = !isFabOpen
    }

    private fun showSellCompleteDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_sell_complete)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnConfirm.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

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
        chatAdapter = ChatMessageAdapter(messageList)
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

        btnSuggestDelivery.setOnClickListener { sendMyMessage("사물함 거래를 제안할게요.") }
        btnSuggestDirect.setOnClickListener { sendMyMessage("대면 거래를 제안할게요.") }

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

        btnSubmit.setOnClickListener {
            if (selected.isNotEmpty()) dialog.dismiss()
        }
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
        val uuid = guestUuid
        if (uuid.isNullOrBlank() || chatRoomId == -1L) return

        val request = ChatSendRequest(
            guestUuid = uuid,
            messageType = "TEXT",
            content = text
        )
        val json = gson.toJson(request)
        stompManager?.send("/pub/chat/$chatRoomId", json)
    }

    private fun receiveMessageFromSocket(sender: String, message: String, time: String, isMine: Boolean) {
        val newMessage = ChatMessage(
            senderName = sender,
            message = message,
            time = time,
            isMine = isMine
        )
        runOnUiThread {
            messageList.add(newMessage)
            chatAdapter.notifyItemInserted(messageList.size - 1)
            recyclerChatMessages.scrollToPosition(messageList.size - 1)
        }
    }

    private fun getCurrentTimeText(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    override fun onDestroy() {
        super.onDestroy()
        stompManager?.disconnect()
        stompManager = null
    }
}
