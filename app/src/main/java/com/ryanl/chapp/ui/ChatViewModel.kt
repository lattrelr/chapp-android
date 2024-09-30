package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.models.Message
import com.ryanl.chapp.socket.WebsocketClient

private const val TAG = "ChatViewModel"

class ChatViewModel() : ViewModel() {
    var currentMessage = mutableStateOf("")
        private set
    // TODO we probably want a map of these for each user we have convos with
    var messageHistory = mutableStateListOf<Message>()
        private set

    fun fetchHistory(toUserId: String?) {
        // TODO keep this part sync.
        messageHistory.clear()
        // TODO run this part in a coroutine
        // TODO get what you can from DB, then request IDs past what is in the DB from server.
        // TODO update backend to support this.
        toUserId?.let {
            messageHistory.addAll(
                Api.getConversation(StoredAppPrefs.getUserId(), toUserId)
            )
        }
    }

    fun addHistory(msg: Message) {
        // TODO have websocket call this...
    }

    fun updateMessage(messageContents: String) {
        currentMessage.value = messageContents
    }

    fun sendMessage(toUserId: String?) {
        // TODO send through websocket
        Log.d(TAG, "Send message to $toUserId")
    }
}