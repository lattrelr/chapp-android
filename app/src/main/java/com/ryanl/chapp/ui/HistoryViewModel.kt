package com.ryanl.chapp.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.persist.AppDatabase
import com.ryanl.chapp.persist.Historian
import com.ryanl.chapp.persist.StoredAppPrefs
import com.ryanl.chapp.persist.models.History
import com.ryanl.chapp.persist.models.Message
import com.ryanl.chapp.socket.WebsocketClient
import kotlinx.coroutines.launch

private const val TAG = "HistoryViewModel"

class HistoryViewModel : ViewModel() {
    data class HistoryListItem(
        val id: String,
        val displayname: String,
        val lastMessage: String?,
        val timestamp: Long?
    )

    var historyList = mutableStateListOf<HistoryListItem>()
        private set

    fun enterUsersView() {
        viewModelScope.launch {
            historyList.clear()
            for (h in Historian.getHistory()) {
                val lastMsg: Message? = Historian.getLastMessage(h.id)
                historyList.add(HistoryListItem(
                    h.id,
                    h.displayname,
                    lastMsg?.text,
                    lastMsg?.date
                ))
            }
        }
    }

    fun leaveUsersView() {
        viewModelScope.launch {

        }
    }
}