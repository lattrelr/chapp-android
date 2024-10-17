package com.ryanl.chapp.socket

import android.net.http.UrlRequest.Status
import android.util.Log
import com.ryanl.chapp.socket.models.Message
import com.ryanl.chapp.socket.models.StatusMessage
import com.ryanl.chapp.socket.models.TextMessage
import com.ryanl.chapp.util.Subscription
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.EOFException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

object WebsocketClient {
    private const val TAG = "WebsocketClient"
    private const val SERVER_NAME = "10.0.2.2"
    private const val PORT = 30000
    val statusSub = Subscription<suspend (StatusMessage) -> Unit, StatusMessage>("Status")
    val textSub = Subscription<suspend (TextMessage) -> Unit, TextMessage>("Text")
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }
    private var session: DefaultClientWebSocketSession? = null
    private var wsJob: Job? = null

    fun closeSocket() {
        kotlinx.coroutines.MainScope().launch {
            wsJob?.cancelAndJoin()
        }
    }

    fun runForever(token: String) {
        kotlinx.coroutines.MainScope().launch {
            wsJob?.cancelAndJoin()
            wsJob = kotlinx.coroutines.MainScope().launch {
                try {
                    while (true) {
                        connectAndReceive(token)
                        Log.e(TAG, "Try to reconnect...")
                    }
                } catch (e: CancellationException) {
                    Log.e(TAG, "Closed. Job cancelled.")
                    session?.close()
                }
                Log.d(TAG, "runForever done")
            }
        }
    }

    private suspend fun connectAndReceive(token: String) {
        Log.d(TAG, "Starting connect loop...")
        try {
            session = client.webSocketSession(
                method = HttpMethod.Get,
                host = SERVER_NAME,
                port = PORT
            ) {
                header("authorization", "bearer $token")
            }
        } catch (e: Exception) {
            when(e) {
                is EOFException, is IOException -> {
                    Log.e(TAG, "Connect failed, let's try again in a few")
                    delay(5000)
                    return
                }
                else -> throw e
            }
        }

        session?.incoming?.receiveAsFlow()?.collect() {
            Log.d(TAG, "Data rxed")
            when (it) {
                is Frame.Text -> handleDataMessage(it)
                is Frame.Ping -> { Log.e(TAG, "Ping frame is unhandled") }
                is Frame.Pong -> { Log.e(TAG, "Pong frame is unhandled") }
                is Frame.Binary -> { Log.e(TAG, "Binary frame is unhandled") }
                is Frame.Close -> { Log.e(TAG, "Close frame is unhandled") }
            }
        }
        Log.e(TAG, "Exiting rx loop. Server offline?")
    }

    private suspend fun handleDataMessage(textFrame: Frame.Text) {
        try {
            val msg = Json.decodeFromString<Message>(textFrame.readText())
            when (msg.type) {
                "text" -> textSub.notifyAll(msg as TextMessage)
                "status" -> statusSub.notifyAll(msg as StatusMessage)
            }
        } catch (e: SerializationException) {
            Log.e(TAG, "Message issue $e")
        }
    }

    suspend fun sendTextMessage(toUser: String, msgText: String) {
        session?.sendSerialized(
            TextMessage(
                type = "text",
                text = msgText,
                to = toUser,
                from = "",
                _id = "",
                date = 0,
            )
        )
        Log.d(TAG, "Sent text message")
    }

    // TODO send a notification if no viewmodels are registered? Or if to a user that is not in a registered viewmodel
}