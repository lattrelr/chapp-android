package com.ryanl.chapp.persist

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object HistorySyncJob {
    private var syncJob: Job? = null
    private val syncChannel: Channel<String> = Channel(UNLIMITED)
    private val syncSet = mutableSetOf<String>()

    suspend fun start() {
        syncJob?.cancelAndJoin()
        syncJob = kotlinx.coroutines.MainScope().launch {
            while (true) {
                val nextId = getNext()
                if (!Historian.syncUserHistory(nextId)) {
                    // Delay and try again until cancelled.
                    delay(5000)
                    add(nextId)
                }
            }
        }
    }

    suspend fun stop() {
        syncJob?.cancelAndJoin()
    }

    suspend fun add(userId: String) {
        if (!syncSet.contains(userId)) {
            syncSet.add(userId)
            syncChannel.send(userId)
        }
    }

    private suspend fun getNext(): String {
        val userId = syncChannel.receive()
        syncSet.remove(userId)
        return userId
    }

    suspend fun addNewHistory(userId: String): Boolean{
        if (Historian.getHistory(userId) == null) {
            add(userId)
            return true
        }
        return false
    }

    // TODO start/stop on foreground?
    private suspend fun onConnectionChanged(connected: Boolean) {
        if (connected) {
            start()
            Historian.getHistories().forEach { h ->
                add(h.id)
            }
        } else {
            stop()
        }
    }
}
