package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.persist.models.Message
import com.ryanl.chapp.persist.Historian
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

    // Strategy to avoid message loss:
    // - Subscribe first
    // - Get the history, which will clear everything first.
    // - History update is locked so subscribe will suspend until we finish updating.
    // - We will keep track of all msg id hashes in a set, in case we got a message
    // - While history was loading, we won't show it twice.
    fun enterChatView(toUserId: String, toUserDisplayName: String) {
        viewModelScope.launch {
            Historian.subscribeUserStatus(toUserId, ::statusCallback)
            messageMutex.withLock {
                messageHistory.clear()
                Historian.subscribeUserText(toUserId, ::textCallback)
                Historian.syncUserHistory(toUserId)
                for (msg in Historian.getConversation(toUserId)) {
                    messageHistory.add(msg)
                    messageHistorySet.add(msg.id)
                }
            }
        }
    }

    fun leaveChatView(toUserId: String) {
        viewModelScope.launch {
            // TODO don't really like these member vars ?
            userOnline.value = false
            Historian.unsubscribeUserStatus(toUserId)
            Historian.unsubscribeUserText(toUserId)
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