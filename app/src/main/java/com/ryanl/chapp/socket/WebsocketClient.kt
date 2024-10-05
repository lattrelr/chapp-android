package com.ryanl.chapp.socket

import android.util.Log
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
        Log.d(TAG, "Websocket starting...")
        session = client.webSocketSession (
            method = HttpMethod.Get,
            host = SERVER_NAME,
            port = PORT) {
                header("authorization", "bearer $token")
            }

        try {
            session?.incoming?.receiveAsFlow()?.collect() {
                Log.d(TAG, "Websocket data rxed")
                if (it is Frame.Text && it.fin) {
                    Log.d(TAG, "Websocket message rxed")
                    try {
                        sendToSubscribers(it)
                    } catch (e: SerializationException) {
                        Log.e(TAG, "Message issue $e")
                    }
                }
            }
        } catch(e: Exception) {
            Log.e(TAG, "Websocket rx closed - $e ${e.message}")
            session?.close()
        }

        Log.d(TAG, "Websocket done!")
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
        Log.d(TAG, "Websocket sent text message")
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

    private suspend fun sendToSubscribers(frame: Frame.Text) {
        val msg = Json.decodeFromString<TextMessage>(frame.readText())
        if (msg.type == "text") {
            subscriberMutex.withLock {
                subscriberMap[msg.from]?.let {
                    Log.d(TAG, "subscriberMap has ${msg.from}")
                    it(msg)
                }
            }
        } else {
            Log.d(TAG, "sendToSubscribers type is ${msg.type}")
        }
    }

    // TODO send a notification if no viewmodels are registered? Or if to a user that is not in a registered viewmodel
}