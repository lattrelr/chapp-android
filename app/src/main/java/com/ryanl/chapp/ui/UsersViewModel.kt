package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.persist.StoredAppPrefs
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.api.models.User
import com.ryanl.chapp.socket.WebsocketClient
import com.ryanl.chapp.socket.models.StatusMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "LoginViewModel"

class UsersViewModel : ViewModel() {
    var userList = mutableStateListOf<User>()
        private set
    private val statusMutex = Mutex()

    private suspend fun statusCallback(msg: StatusMessage) {
        statusMutex.withLock {
            for ((idx, user) in userList.withIndex()) {
                if (user.id == msg.who) {
                    userList[idx] = userList[idx].copy(online = (msg.status == "ONLINE"))
                    break
                }
            }
        }
    }

    fun enterUsersView() {
        viewModelScope.launch {
            WebsocketClient.subscribeToStatus(::statusCallback)
            fetchUsers()
        }
    }

    fun leaveUsersView() {
        viewModelScope.launch {
            WebsocketClient.unsubscribeFromStatus(::statusCallback)
        }
    }

    private suspend fun fetchUsers() {
        statusMutex.withLock {
            userList.clear()
            try {
                Api.getUsers().forEach { user ->
                    if (user.id != StoredAppPrefs.getUserId()) {
                        userList.add(user)
                    }
                }
            } catch (e: Exception) {
                // TODO we don't want to catch the cancellationException, be specific
                Log.e(TAG, "Get Users FAILED - ${e.message}")
            }
        }
    }
}
