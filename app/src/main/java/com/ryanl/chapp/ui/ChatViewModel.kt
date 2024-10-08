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
import com.ryanl.chapp.socket.models.StatusMessage
import com.ryanl.chapp.socket.models.TextMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "ChatViewModel"

class ChatViewModel : ViewModel() {
    var currentMessage = mutableStateOf("")
        private set
    var messageHistory = mutableStateListOf<Message>()
        private set
    private val historyMutex = Mutex()
    private val historyMsgIdSet = mutableSetOf<String>()
    private var userId: String? = null
    var userOnline = mutableStateOf(false)
        private set

    // Strategy to avoid message loss:
    // - Subscribe first
    // - Get the history, which will clear everything first.
    // - History update is locked so subscribe will suspend until we finish updating.
    // - We will keep track of all msg id hashes in a set, in case we got a message
    // - While history was loading, we won't show it twice.
    fun enterChatView(fromUserId: String?, toUserId: String?) {
        viewModelScope.launch {
            userId = fromUserId
            // Fetch user data from server
            // TODO maybe rework, don't like making this call but not sure how else to get valid
            // TODO user state
            fromUserId?.let { id ->
                val userData = Api.getUser(id)
                userData?.let { u ->
                    userOnline.value = u.online
                }
            }
            subscribeFromUser(toUserId)
            subscribeFromUser(fromUserId)
            fetchHistory(fromUserId)
            subscribeUserStatus()
        }
    }

    fun leaveChatView(fromUserId: String?, toUserId: String?) {
        viewModelScope.launch {
            // TODO don't really like these member vars ?
            userId = null
            userOnline.value = false
            unsubscribeUserStatus()
            unsubscribeFromUser(toUserId)
            unsubscribeFromUser(fromUserId)
            messageHistory.clear()
            historyMsgIdSet.clear()
        }
    }

    private fun statusCallback(msg: StatusMessage) {
        userId?.let { u ->
            if (u == msg.who) {
                userOnline.value = msg.status == "ONLINE"
            }
        }
    }

    private suspend fun fetchHistory(toUserId: String?) {
        // TODO get what you can from DB, then request IDs past what is in the DB from server.
        // TODO update backend to support this.
        historyMutex.withLock {
            messageHistory.clear()
            historyMsgIdSet.clear()
            toUserId?.let {
                messageHistory.addAll(
                    Api.getConversation(StoredAppPrefs.getUserId(), toUserId)
                )
                historyMsgIdSet.addAll(
                    messageHistory.map { msg ->
                        msg._id
                    }
                )
            }
        }
    }

    private suspend fun subscribeFromUser(fromUser: String?) {
        fromUser?.let {
            WebsocketClient.subscribeFromUser(it) { msg: TextMessage ->
                historyMutex.withLock {
                    if (!historyMsgIdSet.contains(msg._id)) {
                        historyMsgIdSet.add(msg._id)
                        messageHistory.add(Message(msg.text, msg.from, msg.to))
                    }
                }
            }
        }
    }

    private suspend fun unsubscribeFromUser(fromUser: String?) {
        fromUser?.let {
            WebsocketClient.unsubscribeFromUser(it)
        }
    }

    private suspend fun subscribeUserStatus() {
        WebsocketClient.subscribeToStatus(::statusCallback)
    }

    private suspend fun unsubscribeUserStatus() {
        WebsocketClient.unsubscribeFromStatus(::statusCallback)
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