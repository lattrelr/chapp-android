package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.models.User
import com.ryanl.chapp.socket.WebsocketClient
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

class UsersViewModel : ViewModel() {
    var userList = mutableStateListOf<User>()
        private set

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                userList.addAll(Api.getUsers())
            } catch (e: Exception) {
                // TODO we don't want to catch the cancellationException, be specific
                Log.e(TAG, "Get Users FAILED - ${e.message}")
            }
        }
    }
}
