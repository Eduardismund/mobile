package com.example.examprep.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.examprep.model.Course
import com.google.gson.Gson
import okhttp3.*

class CourseWebSocketClient(
    private val onCourseAdded: (Course) -> Unit
) {
    private val TAG = "CourseWebSocket"
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()
    private var wsUrl: String? = null
    private var retryCount = 0
    private val maxRetries = 5
    private val handler = Handler(Looper.getMainLooper())
    private var isManualDisconnect = false

    fun connect(url: String) {
        wsUrl = url
        isManualDisconnect = false
        attemptConnection()
    }

    private fun attemptConnection() {
        val url = wsUrl ?: return
        Log.d(TAG, "Attempting to connect to WebSocket: $url (attempt ${retryCount + 1})")

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected successfully!")
                retryCount = 0 // Reset retry count on successful connection
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received WebSocket message: $text")
                try {
                    val course = gson.fromJson(text, Course::class.java)
                    Log.d(TAG, "Parsed course: ${course.name}")
                    onCourseAdded(course)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing course: ${e.message}", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: code=$code, reason=$reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: code=$code, reason=$reason")
                if (!isManualDisconnect) {
                    scheduleReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket connection failed: ${t.message}", t)
                Log.e(TAG, "Response: ${response?.code} ${response?.message}")
                if (!isManualDisconnect) {
                    scheduleReconnect()
                }
            }
        })
    }

    private fun scheduleReconnect() {
        if (retryCount >= maxRetries) {
            Log.e(TAG, "Max retries ($maxRetries) reached. Giving up.")
            return
        }

        retryCount++
        val delayMs = (1000L * retryCount).coerceAtMost(10000L) // 1s, 2s, 3s, ... max 10s
        Log.d(TAG, "Scheduling reconnect in ${delayMs}ms (attempt $retryCount/$maxRetries)")

        handler.postDelayed({
            attemptConnection()
        }, delayMs)
    }

    fun disconnect() {
        isManualDisconnect = true
        handler.removeCallbacksAndMessages(null) // Cancel any pending reconnects
        webSocket?.close(1000, "App closed")
        webSocket = null
        Log.d(TAG, "WebSocket disconnected")
    }
}
