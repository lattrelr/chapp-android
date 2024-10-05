package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.StoredAppPrefs
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.api.models.Message
import com.ryanl.chapp.socket.WebsocketClient
import kotlinx.coroutines.launch

private const val TAG = "ChatViewModel"

class ChatViewModel() : ViewModel() {
    var currentMessage = mutableStateOf("")
        private set
    var messageHistory = mutableStateListOf<Message>()
        private set

    fun clearHistory() {
        messageHistory.clear()
    }

    fun fetchHistory(toUserId: String?) {
        // TODO get what you can from DB, then request IDs past what is in the DB from server.
        // TODO update backend to support this.
        clearHistory()
        viewModelScope.launch {
            toUserId?.let {
                messageHistory.addAll(
                    Api.getConversation(StoredAppPrefs.getUserId(), toUserId)
                )
            }
        }
    }

    fun subscribeFromUser(fromUser: String?) {
        viewModelScope.launch {
            fromUser?.let {
                WebsocketClient.subscribeFromUser(it) { msg ->
                    messageHistory.add(Message(msg.text, msg.from, msg.to))
                }
            }
        }
    }

    fun unsubscribeFromUser(fromUser: String?) {
        viewModelScope.launch {
            fromUser?.let {
                WebsocketClient.unsubscribeFromUser(it)
            }
        }
    }

    fun updateMessage(messageContents: String) {
        currentMessage.value = messageContents
    }

    fun sendMessage(toUserId: String?) {
        Log.d(TAG, "Send message to $toUserId")
        viewModelScope.launch {
            toUserId?.let {
                WebsocketClient.sendTextMessage(it, currentMessage.value)
                currentMessage.value = ""
            }
        }
    }
}