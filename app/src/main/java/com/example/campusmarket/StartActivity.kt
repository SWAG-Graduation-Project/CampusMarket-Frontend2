package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.model.GuestRequest
import com.example.campusmarket.model.GuestResponse
import com.example.campusmarket.model.GuestResult
import com.example.campusmarket.RetrofitClient
import kotlinx.coroutines.launch
import java.util.UUID

class StartActivity : AppCompatActivity() {

    private lateinit var root: View
    private var guestReady = false

    // 실제 서버 호출
    private val USE_MOCK = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)

        root = findViewById(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkOrCreateGuest()

        root.setOnClickListener {
            if (guestReady) {
                val intent = Intent(this, StartGetInfo::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "잠시만 기다려주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkOrCreateGuest() {
        val pref = getSharedPreferences("app_prefs", MODE_PRIVATE)



        val savedGuestUuid = pref.getString("guestUuid", null)

        if (savedGuestUuid != null) {
            Log.d("API", "이미 저장된 guestUuid 있음: $savedGuestUuid")
            guestReady = true
            return
        }

        val newGuestUuid = UUID.randomUUID().toString()
        Log.d("API", "새 guestUuid 생성: $newGuestUuid")

        if (USE_MOCK) {
            createGuestMock(newGuestUuid)
        } else {
            createGuest(newGuestUuid)
        }
    }

    private fun createGuestMock(guestUuid: String) {
        Log.d("API", "Mock guest 생성 시작")

        val mockResponse = GuestResponse(
            code = "COMMON_200",
            message = "성공입니다.",
            result = GuestResult(
                memberId = 1L,
                guestUuid = guestUuid,
                loginType = "GUEST",
                memberStatus = "ACTIVE",
                isNewMember = true
            ),
            success = true
        )

        handleGuestSuccess(mockResponse.result)

        Toast.makeText(this, "임시 게스트 로그인 성공", Toast.LENGTH_SHORT).show()
        Log.d("API", "Mock 게스트 생성 성공: ${mockResponse.result}")
    }

    private fun createGuest(guestUuid: String) {
        lifecycleScope.launch {
            try {
                Log.d("API", "createGuest 요청 시작")
                Log.d("API", "보내는 guestUuid: $guestUuid")

                val request = GuestRequest(guestUuid = guestUuid)
                val response = RetrofitClient.authApi.createGuest(request)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {
                        handleGuestSuccess(body.result)

                        Toast.makeText(
                            this@StartActivity,
                            "게스트 생성 성공",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d("API", "게스트 생성 성공: ${body.result}")
                    } else {
                        Log.e("API", "응답 실패: ${body?.message}")
                        Toast.makeText(
                            this@StartActivity,
                            "게스트 생성 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e("API", "HTTP 실패: ${response.code()} / ${response.errorBody()?.string()}")
                    Toast.makeText(
                        this@StartActivity,
                        "서버 요청 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("API", "예외 발생", e)
                Toast.makeText(
                    this@StartActivity,
                    "서버 연결 오류",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleGuestSuccess(result: GuestResult) {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putString("guestUuid", result.guestUuid)
            .putLong("memberId", result.memberId)
            .apply()

        guestReady = true
    }
}