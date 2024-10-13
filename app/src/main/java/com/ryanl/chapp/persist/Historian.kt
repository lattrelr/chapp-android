package com.ryanl.chapp.persist

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.rxjava3.EmptyResultSetException
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.persist.models.History
import com.ryanl.chapp.persist.models.Message
import com.ryanl.chapp.socket.WebsocketClient
import com.ryanl.chapp.socket.models.StatusMessage
import com.ryanl.chapp.socket.models.TextMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
This object is the source of truth for chat history.  Instead of updating the db and fetching
from the server in the view, we'll do it here on startup and on changes.  Since both the ChatView
and the HistoryView need up-to-date data, we do it once here instead of every time you open a view.

Architecture is now sort of like this:

            AppDatabase
                ^
                |         --> ChatViewModel
Websocket --> Historian --|
                |         --> HistoryViewModel
               API

 */
object Historian {
    private const val TAG = "Historian"
    private val historyMutex = Mutex()
    private val subscriberMutex = Mutex()
    private val subscriberMapText: MutableMap<String,suspend (TextMessage) -> Unit> = mutableMapOf()
    private val subscriberMapStatus: MutableMap<String,suspend (StatusMessage) -> Unit> = mutableMapOf()
    private val subscriberSetHistory: MutableSet<suspend (String) -> Unit> = mutableSetOf()
    private lateinit var db: AppDatabase
    // TODO add map of user status, and keep latest status here...(in memory database? - inMemoryDatabaseBuilder)

    private suspend fun syncUserMessages(userId: String) {
        var startTime: Long = 0

        Log.d(TAG, "Sync user messages for $userId ")

        // Find last message in db
        val lastMsg: Message? = getLastMessage(userId)
        lastMsg?.let { msg ->
            startTime = msg.date
        }

        // Get the messages from the server from last msg and add to db
        // TODO add null on failure for conversation
        val newMessages: List<com.ryanl.chapp.api.models.Message> =
            Api.getConversationAfter(StoredAppPrefs.getUserId(), userId, startTime)
        for (msg in newMessages) {
            addMsgHistory(Message(msg))
        }

        Log.d(TAG, "Done sync user messages for $userId ")
    }

    private suspend fun syncUserData(userId: String): Boolean {
        var userHistory = History(userId, displayname = "", username = "")
        var insert = true
        var changed = false

        Log.d(TAG, "Sync user data for $userId ")

        // Fetch from db if user exists already
        db.historyDao().getHistory(userId)?.let {h ->
            userHistory = h
            insert = false
        }

        // Fetch web data for the user
        val userData = Api.getUser(userId)
        userData?.let { u ->
            if (userHistory.displayname != u.displayname) {
                changed = true
                userHistory.displayname = u.displayname
            }

            if (userHistory.username != u.username) {
                changed = true
                userHistory.username = u.username
            }

            // TODO handle u.online

            if (insert) {
                try {
                    db.historyDao().insert(userHistory)
                } catch (e: SQLiteConstraintException) {
                    // TODO shouldn't get here, maybe return false
                    Log.d(TAG, "User $userId already in history!")
                }
            } else if (changed) {
                db.historyDao().update(userHistory)
            }

            Log.d(TAG, "Sync user data done for $userId OK")
            return true
        }

        // We were unable to get user data from the web so nothing to sync
        Log.d(TAG, "Sync user data done for $userId FAILED")
        return false
    }

    // TODO also call when app comes to foreground? Or HistoryScreen opened?
    private suspend fun syncHistory() {
        db.historyDao().getHistories().forEach { h ->
            syncUserHistory(h.id)
        }
    }

    private suspend fun syncUserHistory(userId: String) {
        historyMutex.withLock {
            syncUserData(userId)
            syncUserMessages(userId)
        }
        notifyHistoryChanged(userId)
    }

    private suspend fun addMsgHistory(msg: com.ryanl.chapp.persist.models.Message): Boolean {
        try {
            db.messageDao().insert(msg)
            return true
        } catch (e: SQLiteConstraintException) {
            Log.d(TAG, "Message already in history!")
            return false
        }
    }

    suspend fun addUserHistory(userId: String): Boolean {
        if (getHistory(userId) == null) {
            syncUserHistory(userId)
            return true
        }
        return false
    }

    private suspend fun newTextCallback(msg: TextMessage) {
        // TODO if from new user, sync user and notify history changed...call in new coroutine
        kotlinx.coroutines.MainScope().launch {
            Log.d(TAG, "Got text $msg")
            // Check if we already have history for this user (not including the logged in user)
            val userId = if (msg.to == StoredAppPrefs.getUserId()) msg.from else msg.to
            if (!addUserHistory(userId)) {
                historyMutex.withLock {
                    addMsgHistory(Message(msg))
                }
                notifyHistoryChanged(userId)
            }
        }
        // Keep out of historyMutex so we don't deadlock
        notifyNewText(msg)
    }

    private suspend fun newStatusCallback(msg: StatusMessage) {
        historyMutex.withLock {
            Log.d(TAG, "Got status $msg")
        }
        notifyNewStatus(msg)
    }

    private suspend fun notifyHistoryChanged(userId: String) {
        // TODO subscribed func should use a coroutine if it is long
        Log.d(TAG, "History updated for $userId")
        subscriberMutex.withLock {
            subscriberSetHistory.forEach { cb ->
                cb(userId)
            }
        }
    }

    private suspend fun notifyNewText(msg: TextMessage) {
        subscriberMutex.withLock {
            subscriberMapText[msg.from]?.let {
                Log.d(TAG, "subscriberMapText has ${msg.from}")
                it(msg)
            }
            subscriberMapText[msg.to]?.let {
                Log.d(TAG, "subscriberMapText has ${msg.to}")
                it(msg)
            }
        }
    }

    private suspend fun notifyNewStatus(msg: StatusMessage) {
        subscriberMutex.withLock {
            subscriberMapStatus[msg.who]?.let {
                Log.d(TAG, "subscriberMapStatus has ${msg.who}")
                it(msg)
            }
        }
    }

    suspend fun subscribeUserText(fromUser: String, cb: suspend (TextMessage) -> Unit) {
        Log.d(TAG, "Watching for $fromUser texts")
        subscriberMutex.withLock {
            subscriberMapText[fromUser] = cb;
        }
    }

    suspend fun unsubscribeUserText(fromUser: String) {
        Log.d(TAG, "No longer watching $fromUser texts")
        subscriberMutex.withLock {
            subscriberMapText.remove(fromUser)
        }
    }

    suspend fun subscribeUserStatus(userId: String, cb: suspend (StatusMessage) -> Unit) {
        Log.d(TAG, "Watching for $userId status")
        subscriberMutex.withLock {
            subscriberMapStatus[userId] = cb;
        }
    }

    suspend fun unsubscribeUserStatus(userId: String) {
        Log.d(TAG, "No longer watching $userId status")
        subscriberMutex.withLock {
            subscriberMapStatus.remove(userId)
        }
    }

    suspend fun subscribeHistory(cb: suspend (String) -> Unit) {
        Log.d(TAG, "Watching for history")
        subscriberMutex.withLock {
            subscriberSetHistory.add(cb)
        }
    }

    suspend fun unsubscribeHistory(cb: suspend (String) -> Unit) {
        Log.d(TAG, "No longer watching history")
        subscriberMutex.withLock {
            subscriberSetHistory.remove(cb)
        }
    }

    fun start(context: Context) {
        // We need to set the db not-in-a-thread since we will crash if we use it before
        // it is initialized
        db = AppDatabase.getInstance(context)
        kotlinx.coroutines.MainScope().launch {
            WebsocketClient.subscribeToText(::newTextCallback)
            WebsocketClient.subscribeToStatus(::newStatusCallback)
            syncHistory()
        }
    }

    fun stop() {
        kotlinx.coroutines.MainScope().launch {
            WebsocketClient.unsubscribeFromText(::newTextCallback)
            WebsocketClient.unsubscribeFromStatus(::newStatusCallback)
        }
    }

    suspend fun getConversation(userId: String): List<Message> {
        historyMutex.withLock {
            // TODO can be simplified...we can change messageDao to just look for user, not stored user
            // TODO this database should only have messages to the logged in user...
            return db.messageDao().getConversation(userId, StoredAppPrefs.getUserId())
        }
    }

    suspend fun getHistory(userId: String): History? {
        return db.historyDao().getHistory(userId)
    }

    suspend fun getHistories(): List<History> {
        return db.historyDao().getHistories()
    }

    suspend fun getLastMessage(userId: String): Message? {
        var lastMsg: Message? = null
        try {
            // TODO can be simplified...we can change messageDao to just look for user, not stored user
            // TODO this database should only have messages to the logged in user...
            lastMsg =
                db.messageDao().getLastConversationMessage(StoredAppPrefs.getUserId(), userId)
        } catch (e: EmptyResultSetException) {
            Log.d(TAG, "No messages in history for user $userId")
        }
        return lastMsg
    }
}