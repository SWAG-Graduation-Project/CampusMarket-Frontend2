package com.example.campusmarket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class StompManager(private val url: String) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var subCount = 0

    var onConnected: (() -> Unit)? = null
    var onMessage: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    fun connect() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                ws.send("CONNECT\naccept-version:1.1,1.2\nheart-beat:0,0\n\n\u0000")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                val frame = text.trimEnd('\u0000')
                when {
                    frame.startsWith("CONNECTED") -> onConnected?.invoke()
                    frame.startsWith("MESSAGE") -> {
                        val idx = frame.indexOf("\n\n")
                        if (idx != -1) onMessage?.invoke(frame.substring(idx + 2))
                    }
                    frame.startsWith("ERROR") -> onError?.invoke(frame)
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                onError?.invoke(t.message ?: "WebSocket 연결 실패")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                onDisconnected?.invoke()
            }
        })
    }

    fun subscribe(destination: String) {
        val id = "sub-${++subCount}"
        webSocket?.send("SUBSCRIBE\nid:$id\ndestination:$destination\n\n\u0000")
    }

    fun send(destination: String, jsonBody: String) {
        val len = jsonBody.toByteArray(Charsets.UTF_8).size
        val frame = "SEND\ndestination:$destination\ncontent-type:application/json\ncontent-length:$len\n\n$jsonBody\u0000"
        webSocket?.send(frame)
    }

    fun disconnect() {
        webSocket?.send("DISCONNECT\n\n\u0000")
        webSocket?.close(1000, "disconnect")
        webSocket = null
    }

    fun isConnected() = webSocket != null
}
