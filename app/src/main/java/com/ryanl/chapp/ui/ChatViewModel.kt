package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.persist.models.Message
import com.ryanl.chapp.persist.Historian
import com.ryanl.chapp.persist.HistorySyncJob
import com.ryanl.chapp.socket.StatusProvider
import com.ryanl.chapp.socket.TextProvider
import com.ryanl.chapp.socket.WebsocketClient
import com.ryanl.chapp.socket.models.StatusMessage
import com.ryanl.chapp.socket.models.TextMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "ChatViewModel"

class ChatViewModel : ViewModel() {
    var currentMessage = mutableStateOf("")
        private set
    val messageHistory = mutableStateListOf<Message>()
    private val messageHistorySet = mutableSetOf<String>()
    private val messageMutex = Mutex()
    var userOnline = mutableStateOf(false)
        private set

    // - We will keep track of all msg id hashes in a set, in case we got a message
    // - While history was loading, we won't show it twice.
    // TODO remove display name var
    fun enterChatView(toUserId: String, toUserDisplayName: String) {
        viewModelScope.launch {
            StatusProvider.subscribe(toUserId, ::statusCallback)
            TextProvider.subscribe(toUserId, ::textCallback)
            Historian.subscribe(toUserId) {
                addMessagesFromHistory(toUserId)
            }
            if (!HistorySyncJob.addNewHistory(toUserId)) {
                addMessagesFromHistory(toUserId)
            }
        }
    }

    private suspend fun addMessagesFromHistory(toUserId: String) {
        messageMutex.withLock {
            messageHistory.clear()
            for (msg in Historian.getConversation(toUserId)) {
                messageHistory.add(msg)
                messageHistorySet.add(msg.id)
            }
        }
    }

    fun leaveChatView(toUserId: String) {
        viewModelScope.launch {
            userOnline.value = false
            StatusProvider.unsubscribe(toUserId)
            TextProvider.unsubscribe(toUserId)
            messageHistory.clear()
        }
    }

    private fun statusCallback(msg: StatusMessage) {
        userOnline.value = msg.status == "ONLINE"
    }

    private suspend fun textCallback(msg: TextMessage) {
        messageMutex.withLock {
            if (!messageHistorySet.contains(msg._id)) {
                messageHistory.add(Message(msg))
                messageHistorySet.add(msg._id)
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