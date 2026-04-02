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
import com.example.campusmarket.data.LockerDataSource
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

    // POST /proposals 응답에서 저장 (판매자) 또는 GET /proposals 조회 (구매자)
    private var pendingProposalId: Long? = null
    private var pendingProposalType: String? = null

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
            // 현재 PENDING 제안 조회 (구매자가 proposalId를 알기 위해)
            try {
                val propResp = RetrofitClient.apiService.getProposals(uuid, chatRoomId)
                val pending = propResp.body()?.result?.firstOrNull { it.proposalStatus == "PENDING" }
                if (pending != null) {
                    pendingProposalId = pending.proposalId
                    pendingProposalType = pending.proposalType
                    android.util.Log.d("PROPOSAL", "loaded pending proposalId=$pendingProposalId type=$pendingProposalType")
                }
            } catch (e: Exception) {
                android.util.Log.w("PROPOSAL", "getProposals failed: ${e.message}")
            }

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

        // PROPOSAL 감지: messageType 이름 or metadata에 proposalId/proposalType 포함 여부로 판단
        val isProposal = type.contains("PROPOSAL", ignoreCase = true) ||
            (!dto.metadata.isNullOrBlank() && (
                dto.metadata.contains("proposalId", ignoreCase = true) ||
                dto.metadata.contains("proposalType", ignoreCase = true)
            ))

        android.util.Log.d("MSG_TYPE", "messageType=$type isProposal=$isProposal metadata=${dto.metadata}")

        if (isProposal) {
            val (parsedId, parsedType, parsedStatus) = parseProposalMeta(dto.metadata)
            // metadata에 없으면 저장된 pendingProposalId 사용 (판매자: POST 응답, 구매자: GET 조회)
            val proposalId = parsedId ?: pendingProposalId
            val proposalType = parsedType ?: pendingProposalType
            val proposalStatus = parsedStatus ?: "PENDING"
            android.util.Log.d("PROPOSAL", "proposalId=$proposalId proposalType=$proposalType proposalStatus=$proposalStatus")
            return ChatMessage(
                senderName = senderName,
                message = dto.content ?: "",
                time = time,
                isMine = isMine,
                messageType = "PROPOSAL",
                proposalId = proposalId,
                proposalType = proposalType,
                proposalStatus = proposalStatus,
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
            // proposalId: 여러 키 이름 시도
            val id = listOf("proposalId", "proposal_id", "id").firstNotNullOfOrNull { key ->
                if (obj.has(key)) obj.get(key).asLong else null
            }
            val type = listOf("proposalType", "proposal_type", "type").firstNotNullOfOrNull { key ->
                if (obj.has(key)) obj.get(key).asString else null
            }
            val status = listOf("proposalStatus", "proposal_status", "status").firstNotNullOfOrNull { key ->
                if (obj.has(key)) obj.get(key).asString else null
            } ?: "PENDING"
            android.util.Log.d("PROPOSAL_PARSE", "meta=$metadata → id=$id type=$type status=$status")
            Triple(id, type, status)
        } catch (e: Exception) {
            android.util.Log.e("PROPOSAL_PARSE", "parse error: ${e.message}, meta=$metadata")
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
                android.util.Log.d("PROPOSAL", "sending proposal: type=$proposalType chatRoomId=$chatRoomId")
                val response = RetrofitClient.apiService.createProposal(
                    uuid, chatRoomId, ProposalRequest(proposalType)
                )
                android.util.Log.d("PROPOSAL", "response code=${response.code()} body=${response.body()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    // 판매자: POST 응답에서 proposalId 저장
                    val result = response.body()?.result
                    pendingProposalId = result?.proposalId
                    pendingProposalType = result?.proposalType
                    android.util.Log.d("PROPOSAL", "stored pendingProposalId=$pendingProposalId")
                } else {
                    Toast.makeText(this@ChattActivity, "제안 전송 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                }
                // 성공 시 WebSocket으로 메시지가 push됨 → onMessage에서 자동 처리
            } catch (e: Exception) {
                android.util.Log.e("PROPOSAL", "exception: ${e.message}", e)
                Toast.makeText(this@ChattActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun respondToProposal(proposalId: Long, accept: Boolean) {
        val uuid = guestUuid ?: return
        // proposalId가 0이면 저장된 pendingProposalId로 대체
        val actualProposalId = if (proposalId == 0L) pendingProposalId ?: run {
            Toast.makeText(this, "제안 ID를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        } else proposalId
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.respondToProposal(
                    uuid, chatRoomId, actualProposalId, ProposalRespondRequest(accept)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val status = if (accept) "ACCEPTED" else "REJECTED"
                    // 기존 proposal 메시지의 상태 업데이트 (버튼 비활성화)
                    val idx = messageList.indexOfLast { it.messageType == "PROPOSAL" }
                    if (idx != -1) {
                        messageList[idx] = messageList[idx].copy(proposalStatus = status)
                        runOnUiThread { chatAdapter.notifyItemChanged(idx) }
                    }
                    // 처리 완료 후 초기화
                    pendingProposalId = null
                    pendingProposalType = null
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

    private fun showLockerImagePopup(metadata: String?) {
        android.util.Log.d("LOCKER_POPUP", "metadata=$metadata")
        if (metadata.isNullOrBlank()) {
            Toast.makeText(this, "사물함 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val obj = gson.fromJson(metadata, JsonObject::class.java)
            android.util.Log.d("LOCKER_POPUP", "keys=${obj.keySet()}")

            val building = listOf("lockerBuilding", "building", "buildingName").firstNotNullOfOrNull { key ->
                obj.get(key)?.asString?.takeIf { it.isNotBlank() }
            }
            val floor = listOf("lockerFloor", "floor").firstNotNullOfOrNull { key ->
                val el = obj.get(key) ?: return@firstNotNullOfOrNull null
                runCatching {
                    if (el.isJsonPrimitive) {
                        val p = el.asJsonPrimitive
                        if (p.isNumber) p.asInt else p.asString.trim().toInt()
                    } else null
                }.getOrNull()
            } ?: 1

            android.util.Log.d("LOCKER_POPUP", "building=$building floor=$floor")

            if (building == null) {
                Toast.makeText(this, "사물함 위치 정보가 없습니다", Toast.LENGTH_SHORT).show()
                return
            }

            // LockerDataSource에서 해당 건물 데이터 조회
            val group = LockerDataSource.lockerList.firstOrNull {
                it.buildingName == building && it.floor == floor
            }
            val loungeExists = LockerDataSource.loungeImageList.any {
                it.buildingName == building && it.floor == floor
            }
            android.util.Log.d("LOCKER_POPUP", "group=$group loungeExists=$loungeExists")

            if (!loungeExists) {
                // 해당 건물의 지도 이미지가 없으면 사물함 정보를 텍스트 다이얼로그로 표시
                val lockerName = obj.get("lockerName")?.asString ?: ""
                val major = listOf("lockerMajor", "major").firstNotNullOfOrNull { k -> obj.get(k)?.asString } ?: ""
                val lockerGroup = listOf("lockerGroup").firstNotNullOfOrNull { k ->
                    runCatching { obj.get(k)?.asInt }.getOrNull()
                } ?: 0
                val row = listOf("lockerRow", "row").firstNotNullOfOrNull { k ->
                    runCatching { obj.get(k)?.asInt }.getOrNull()
                } ?: 0
                val col = listOf("lockerCol", "col").firstNotNullOfOrNull { k ->
                    runCatching { obj.get(k)?.asInt }.getOrNull()
                } ?: 0
                val info = buildString {
                    if (lockerName.isNotBlank()) appendLine("사물함: $lockerName")
                    appendLine("건물: $building")
                    appendLine("층: ${floor}층")
                    if (major.isNotBlank()) appendLine("구역: $major")
                    if (lockerGroup > 0) appendLine("그룹: ${lockerGroup}번")
                    if (row > 0 && col > 0) append("위치: ${row}행 ${col}열")
                }
                android.app.AlertDialog.Builder(this)
                    .setTitle("사물함 위치")
                    .setMessage(info.trim())
                    .setPositiveButton("확인", null)
                    .show()
                return
            }

            val major = listOf("lockerMajor", "major").firstNotNullOfOrNull { k ->
                obj.get(k)?.asString?.takeIf { it.isNotBlank() }
            }
            val lockerGroupNum = listOf("lockerGroup", "groupNumber").firstNotNullOfOrNull { k ->
                runCatching { obj.get(k)?.asInt }.getOrNull()
            }

            val imageIndex = group?.imageIndex ?: 1
            LockerGroupPopupDialogFragment(
                buildingName = building,
                floor = floor,
                imageIndex = imageIndex,
                loungeImages = LockerDataSource.loungeImageList,
                lockerGroups = LockerDataSource.lockerList,
                onLockerGroupSelected = { /* 보기 전용 */ },
                highlightGroupNumber = lockerGroupNum,
                highlightMajor = major
            ).show(supportFragmentManager, "lockerViewPopup")

        } catch (e: Exception) {
            android.util.Log.e("LOCKER_POPUP", "exception: ${e.message}", e)
            Toast.makeText(this, "사물함 정보를 표시할 수 없습니다", Toast.LENGTH_SHORT).show()
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
            onProposalReject = { proposalId -> respondToProposal(proposalId, false) },
            onLockerCheck = { metadata -> showLockerImagePopup(metadata) }
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
