package com.ryanl.chapp.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.persist.Historian
import com.ryanl.chapp.persist.models.History
import com.ryanl.chapp.persist.models.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "HistoryViewModel"

class HistoryViewModel : ViewModel() {
    data class HistoryListItem(
        val id: String,
        val displayname: String,
        val lastMessage: String?,
        val timestamp: Long?
    )

    val historyMap = mutableStateMapOf<String, HistoryListItem?>()
    private val historyMutex = Mutex()

    fun enterUsersView() {
        // TODO subscribe, then sync, then update in cb.  Get history can go away?
        viewModelScope.launch {
            historyMutex.withLock {
                historyMap.clear()
                Historian.subscribeHistory(::onHistoryChanged)
                for (history in Historian.getHistories()) {
                    updateHistory(history)
                }
            }
        }
    }

    fun leaveUsersView() {
        viewModelScope.launch {
            Historian.unsubscribeHistory(::onHistoryChanged)
        }
    }

    private suspend fun updateHistory(history: History) {
        val lastMsg: Message? = Historian.getLastMessage(history.id)
        val item = HistoryListItem (
            history.id,
            history.displayname,
            lastMsg?.text,
            lastMsg?.date
        )
        historyMap[history.id] = item
    }

    private suspend fun onHistoryChanged(userId: String) {
        viewModelScope.launch {
            val history = Historian.getHistory(userId)
            history?.let{ h ->
                historyMutex.withLock {
                    updateHistory(h)
                }
            }
        }
    }

    // TODO displayname won't update until we restart the app probably
    // TODO eventually a status update should include user changed notifications
}