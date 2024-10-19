package com.ryanl.chapp.persist

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.rxjava3.EmptyResultSetException
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.persist.models.History
import com.ryanl.chapp.persist.models.Message
import com.ryanl.chapp.socket.ConnectionManager
import com.ryanl.chapp.socket.TextProvider
import com.ryanl.chapp.socket.WebsocketClient
import com.ryanl.chapp.socket.models.StatusMessage
import com.ryanl.chapp.socket.models.TextMessage
import com.ryanl.chapp.util.Subscription
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList
import java.util.Queue

/*
This object is the source of truth for chat history.  Instead of updating the db and fetching
from the server in the view, we'll do it here on startup and on changes.  Since both the ChatView
and the HistoryView need up-to-date data, we do it once here instead of every time you open a view.

Architecture is now sort of like this:

            AppDatabase
                ^
                |         --> ChatViewModel
TextProvider-> Historian --|
                |         --> HistoryViewModel
               API

 */
object Historian: Subscription<String, String>("history") {
    private const val TAG = "Historian"
    private val historyMutex = Mutex()
    private lateinit var db: AppDatabase
    // TODO add map of user status? and keep latest status here...(in memory database? - inMemoryDatabaseBuilder)

    private suspend fun syncUserMessages(userId: String): Boolean {
        var startTime: Long = 0

        Log.d(TAG, "Sync user messages for $userId ")

        // Find last message in db
        val lastMsg: Message? = getLastMessage(userId)
        lastMsg?.let { msg ->
            startTime = msg.date
        }

        // Get the messages from the server from last msg and add to db
        val newMessages: List<com.ryanl.chapp.api.models.Message>? =
            Api.getConversationAfter(StoredAppPrefs.getUserId(), userId, startTime)
        if (newMessages == null) {
            Log.e(TAG, "Failed to sync messages for $userId")
            return false
        }

        for (msg in newMessages) {
            addMsgHistory(Message(msg))
        }

        Log.d(TAG, "Done sync user messages for $userId ")
        return true
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
        val u = Api.getUser(userId)
        if (u == null) {
            // We were unable to get user data from the web so nothing to sync
            Log.d(TAG, "Sync user data done for $userId FAILED")
            return false
        }

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

    suspend fun syncUserHistory(userId: String): Boolean {
        historyMutex.withLock {
            // Sync userData second so the last action is adding the user to the db
            if (!syncUserMessages(userId) || !syncUserData(userId)) {
                return false
            }
        }
        notifyAll(userId)
        notifyOne(userId, userId)
        return true
    }

    private suspend fun addMsgHistory(msg: Message) {
        try {
            db.messageDao().insert(msg)
        } catch (e: SQLiteConstraintException) {
            Log.d(TAG, "Message already in history!")
        }
    }

    // TODO move this to syncJob ?
    private suspend fun newTextCallback(msg: TextMessage) {
        Log.d(TAG, "Got text $msg")
        // Check if we already have history for this user (not including the logged in user)
        val userId = if (msg.to == StoredAppPrefs.getUserId()) msg.from else msg.to
        if (!HistorySyncJob.addNewHistory(userId)) {
            historyMutex.withLock {
                addMsgHistory(Message(msg))
            }
        }
    }

    fun start(context: Context) {
        // We need to set the db not-in-a-thread since we will crash if we use it before
        // it is initialized
        db = AppDatabase.getInstance(context)
        kotlinx.coroutines.MainScope().launch {
            TextProvider.subscribe(::newTextCallback)
        }
    }

    fun stop() {
        kotlinx.coroutines.MainScope().launch {
            TextProvider.unsubscribe(::newTextCallback)
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