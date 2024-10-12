package com.ryanl.chapp.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.persist.AppDatabase
import com.ryanl.chapp.persist.models.History
import com.ryanl.chapp.socket.WebsocketClient
import kotlinx.coroutines.launch

private const val TAG = "HistoryViewModel"

class HistoryViewModel : ViewModel() {
    var historyList = mutableStateListOf<History>()
        private set

    fun enterUsersView() {
        viewModelScope.launch {
            historyList.clear()
            val db = AppDatabase.getInstance()
            db?.historyDao()?.getHistories()?.let { hist ->
                historyList.addAll(hist)
            }
        }
    }

    fun leaveUsersView() {
        viewModelScope.launch {

        }
    }
}