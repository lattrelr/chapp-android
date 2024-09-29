package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ryanl.chapp.socket.WebsocketClient

private const val TAG = "ChatViewModel"

class ChatViewModel : ViewModel() {
    var currentMessage = mutableStateOf("")
        private set

    init {
    }

    fun updateMessage(messageContents: String) {
        currentMessage.value = messageContents
    }

    fun sendMessage(userId: String?) {
        Log.d(TAG, "Send message to $userId")
    }
}