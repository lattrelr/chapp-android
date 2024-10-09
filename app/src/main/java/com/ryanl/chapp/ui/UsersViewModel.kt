package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.StoredAppPrefs
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.api.models.User
import com.ryanl.chapp.socket.WebsocketClient
import com.ryanl.chapp.socket.models.StatusMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

class UsersViewModel : ViewModel() {
    var userList = mutableStateListOf<User>()
        private set

    private suspend fun statusCallback(msg: StatusMessage) {
        for ((idx, user) in userList.withIndex()) {
            if (user.id == msg.who) {
                userList[idx] = userList[idx].copy(online=(msg.status == "ONLINE"))
                break
            }
        }
    }

    fun subscribeUserStatus() {
        viewModelScope.launch {
            WebsocketClient.subscribeToStatus(::statusCallback)
        }
    }

    fun unsubscribeUserStatus() {
        viewModelScope.launch {
            WebsocketClient.unsubscribeFromStatus(::statusCallback)
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
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
