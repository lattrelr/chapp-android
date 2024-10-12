package com.ryanl.chapp.ui

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.persist.StoredAppPrefs
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.api.models.Message
import com.ryanl.chapp.persist.AppDatabase
import com.ryanl.chapp.persist.models.History
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
    private var userId: String = ""
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
            userId = toUserId
            // Fetch user data from server
            // TODO maybe rework, don't like making this call but not sure how else to get valid
            // TODO user state
            val userData = Api.getUser(toUserId)
            userData?.let { u ->
                userOnline.value = u.online
            }

            subscribeFromUser(toUserId)
            subscribeFromUser(StoredAppPrefs.getUserId())
            syncHistory(toUserId)
            subscribeUserStatus()

            // Record that we have opened a chat window with this user
            // so we can show it in the main history view later
            AppDatabase.getInstance()?.let { db ->
                try {
                    db.historyDao().insert(History(toUserId, toUserDisplayName))
                } catch (e: SQLiteConstraintException) {
                    Log.d(TAG, "User already in history!")
                }
            }
        }
    }

    fun leaveChatView(toUserId: String) {
        viewModelScope.launch {
            // TODO don't really like these member vars ?
            userId = ""
            userOnline.value = false
            unsubscribeUserStatus()
            unsubscribeFromUser(toUserId)
            unsubscribeFromUser(StoredAppPrefs.getUserId())
            messageHistory.clear()
            historyMsgIdSet.clear()
        }
    }

    private fun statusCallback(msg: StatusMessage) {
        if (userId == msg.who) {
            userOnline.value = msg.status == "ONLINE"
        }
    }

    private suspend fun syncHistory(toUserId: String) {
        val db = AppDatabase.getInstance()
        var startTime: Long = 0

        historyMutex.withLock {
            messageHistory.clear()
            historyMsgIdSet.clear()

            // First populate the history from the database
            db?.messageDao()?.let { dao ->
                val conversation = dao.getConversation(toUserId, StoredAppPrefs.getUserId())
                if (conversation.isNotEmpty()) {
                    for (msg in conversation) {
                        messageHistory.add(Message(msg))
                        historyMsgIdSet.add(msg.id)
                        if (msg.date > startTime)
                            startTime = msg.date
                    }
                }
                Log.d(TAG, "Found ${conversation.size} messages in history")
            }

            // Next get the messages from the server
            val newMessages =
                Api.getConversationAfter(StoredAppPrefs.getUserId(), toUserId, startTime)
            for (msg in newMessages) {
                if (!historyMsgIdSet.contains(msg._id)) {
                    messageHistory.add(msg)
                    historyMsgIdSet.add(msg._id)
                    // Write to db
                    // TODO fix model names so it isn't confusing
                    addMsgToDb(com.ryanl.chapp.persist.models.Message(msg))
                }
            }
        }
    }

    private suspend fun addMsgToDb(msg: com.ryanl.chapp.persist.models.Message) {
        try {
            AppDatabase.getInstance()?.messageDao()?.insert(msg)
        } catch (e: SQLiteConstraintException) {
            Log.d(TAG, "Message already in history!")
        }
    }

    private suspend fun subscribeFromUser(fromUser: String) {
        WebsocketClient.subscribeFromUser(fromUser) { msg: TextMessage ->
            historyMutex.withLock {
                if (!historyMsgIdSet.contains(msg._id)) {
                    historyMsgIdSet.add(msg._id)
                    messageHistory.add(Message(msg))
                    viewModelScope.launch {
                        addMsgToDb(com.ryanl.chapp.persist.models.Message(msg))
                    }
                }
            }
        }
    }

    private suspend fun unsubscribeFromUser(fromUser: String) {
        WebsocketClient.unsubscribeFromUser(fromUser)
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