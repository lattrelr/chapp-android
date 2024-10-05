package com.ryanl.chapp.socket

import android.util.Log
import com.ryanl.chapp.socket.models.Message
import com.ryanl.chapp.socket.models.TextMessage
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object WebsocketClient {
    private const val TAG = "WebsocketClient"
    private const val SERVER_NAME = "10.0.2.2"
    private const val PORT = 30000
    private val subscriberMap: MutableMap<String,(TextMessage) -> Unit> = mutableMapOf()
    private val subscriberMutex = Mutex()
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }
    private var session: DefaultClientWebSocketSession? = null

    // TODO handle server restart closing the socket.

    suspend fun connect(token: String) {
        Log.d(TAG, "Starting...")
        session = client.webSocketSession (
            method = HttpMethod.Get,
            host = SERVER_NAME,
            port = PORT) {
                header("authorization", "bearer $token")
            }

        try {
            session?.incoming?.receiveAsFlow()?.collect() {
                Log.d(TAG, "Data rxed")
                if (it is Frame.Text && it.fin) {
                    try {
                        val msg = Json.decodeFromString<Message>(it.readText())
                        when (msg.type) {
                            "text" -> sendToSubscribers(msg as TextMessage)
                            "status" -> { Log.d(TAG, "$msg") }
                        }
                    } catch (e: SerializationException) {
                        Log.e(TAG, "Message issue $e")
                    }
                }
            }
        } catch(e: Exception) {
            // TODO Handle websocket Exception vs Coroutine exception!
            Log.e(TAG, "Rx closed - $e ${e.message}")
            session?.close()
        }

        Log.d(TAG, "Done!")
    }

    /*fun close() {
        // Not sure what this does, close() seems to work.
        session?.incoming?.cancel()
        Log.e(TAG, "Websocket closed!")
    }*/

    suspend fun sendTextMessage(toUser: String, msgText: String) {
        session?.sendSerialized(
            TextMessage(
                type = "text",
                text = msgText,
                to = toUser,
                from = ""
            )
        )
        Log.d(TAG, "Sent text message")
    }

    suspend fun subscribeFromUser(fromUser: String, cb: (TextMessage) -> Unit) {
        Log.d(TAG, "Watching for $fromUser")
        subscriberMutex.withLock {
            subscriberMap[fromUser] = cb;
        }
    }

    suspend fun unsubscribeFromUser(fromUser: String) {
        Log.d(TAG, "No longer watching $fromUser")
        subscriberMutex.withLock {
            subscriberMap.remove(fromUser)
        }
    }

    private suspend fun sendToSubscribers(msg: TextMessage) {
        subscriberMutex.withLock {
            subscriberMap[msg.from]?.let {
                Log.d(TAG, "subscriberMap has ${msg.from}")
                it(msg)
            }
        }
    }

    // TODO send a notification if no viewmodels are registered? Or if to a user that is not in a registered viewmodel
}