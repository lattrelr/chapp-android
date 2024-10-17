package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
    val userMap = mutableStateMapOf<String, User?>()
    private val statusMutex = Mutex()

    private suspend fun statusCallback(msg: StatusMessage) {
        statusMutex.withLock {
            if (userMap.contains(msg.who)) {
                userMap[msg.who] = userMap[msg.who]?.copy(online = (msg.status == "ONLINE"))
            }
        }
    }

    fun enterUsersView() {
        viewModelScope.launch {
            // TODO subscribe to historian instead of websocket.
            WebsocketClient.statusSub.subscribe(::statusCallback)
            fetchUsers()
        }
    }

    fun leaveUsersView() {
        viewModelScope.launch {
            WebsocketClient.statusSub.unsubscribe(::statusCallback)
        }
    }

    private suspend fun fetchUsers() {
        statusMutex.withLock {
            userMap.clear()
            try {
                Api.getUsers().forEach { user ->
                    if (user.id != StoredAppPrefs.getUserId()) {
                        userMap[user.id] = user
                    }
                }
            } catch (e: Exception) {
                // TODO we don't want to catch the cancellationException, be specific
                Log.e(TAG, "Get Users FAILED - ${e.message}")
            }
        }
    }
}
