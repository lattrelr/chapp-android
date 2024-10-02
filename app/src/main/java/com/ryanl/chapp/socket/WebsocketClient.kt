package com.ryanl.chapp.socket

import android.util.Log
import androidx.compose.material3.Text
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.close
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json

object WebsocketClient {
    private const val TAG = "WebsocketClient"
    private const val SERVER_NAME = "10.0.2.2"
    private const val PORT = 30000
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }
    private var session: DefaultClientWebSocketSession? = null

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
                Log.d(TAG, "Websocket message rxed")
            }
        } catch(e: Exception) {
            Log.e(TAG, "Websocket rx closed")
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
                text = msgText,
                to = toUser
            )
        )
        Log.d(TAG, "Websocket sent text message")
    }

    // TODO ViewModel that want's updates should register
    // somewhere in here
}