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
    private var userId: String? = null
    var userOnline = mutableStateOf(false)
        private set

    // Strategy to avoid message loss:
    // - Subscribe first
    // - Get the history, which will clear everything first.
    // - History update is locked so subscribe will suspend until we finish updating.
    // - We will keep track of all msg id hashes in a set, in case we got a message
    // - While history was loading, we won't show it twice.
    fun enterChatView(toUserId: String?, toUserDisplayName: String?) {
        viewModelScope.launch {
            userId = toUserId
            // Fetch user data from server
            // TODO maybe rework, don't like making this call but not sure how else to get valid
            // TODO user state
            toUserId?.let { id ->
                val userData = Api.getUser(id)
                userData?.let { u ->
                    userOnline.value = u.online
                }
            }

            subscribeFromUser(userId)
            subscribeFromUser(StoredAppPrefs.getUserId())
            fetchHistory(userId)
            subscribeUserStatus()

            // Record that we have opened a chat window with this user
            // so we can show it in the main history view later
            if (toUserId != null && toUserDisplayName != null) {
                AppDatabase.getInstance()?.let { db ->
                    try {
                        db.historyDao().insert(History(toUserId, toUserDisplayName))
                    } catch (e: SQLiteConstraintException) {
                        Log.d(TAG, "User already in history!")
                    }
                }
            }
        }
    }

    fun leaveChatView(toUserId: String?) {
        viewModelScope.launch {
            // TODO don't really like these member vars ?
            userId = null
            userOnline.value = false
            unsubscribeUserStatus()
            unsubscribeFromUser(toUserId)
            unsubscribeFromUser(StoredAppPrefs.getUserId())
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

    private suspend fun syncHistory() {
        //viewModelScope.launch {
            // TODO add all messages in messageHistory not in db to db
            // TODO add all message not in messageHistory from db to messageHistory
        //}
        // Fetch from db first
        // val db = AppDatabase.getInstance()
        /*db?.messageDao()?.getMessages(user1, user2)?.let { hist ->
            hist.forEach{ h ->
                messageHistory(Message(h.))
            }
        }
        historyMsgIdSet.update ( --- )
        */
    }

    private suspend fun fetchHistory(toUserId: String?) {
        historyMutex.withLock {
            messageHistory.clear()
            historyMsgIdSet.clear()
            toUserId?.let {
                // Fetch history from the db first
                syncHistory()
                // TODO only add messages not in msgIdSet
                // TODO only get messages after the last message in history
                messageHistory.addAll(
                    Api.getConversation(StoredAppPrefs.getUserId(), toUserId)
                )
                historyMsgIdSet.addAll(
                    messageHistory.map { msg ->
                        msg._id
                    }
                )
                // Update db with any messages we might have been missing
                viewModelScope.launch {
                    syncHistory()
                }
            }
        }
        // Make sure the db is up to date
        syncHistory()
    }

    private suspend fun subscribeFromUser(fromUser: String?) {
        fromUser?.let {
            WebsocketClient.subscribeFromUser(it) { msg: TextMessage ->
                historyMutex.withLock {
                    if (!historyMsgIdSet.contains(msg._id)) {
                        historyMsgIdSet.add(msg._id)
                        messageHistory.add(Message(msg.text, msg.from, msg.to, msg.date))
                        viewModelScope.launch {
                            syncHistory()
                        }
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