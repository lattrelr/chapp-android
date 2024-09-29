package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ryanl.chapp.api.Api
import com.ryanl.chapp.models.User
import com.ryanl.chapp.socket.WebsocketClient

private const val TAG = "LoginViewModel"

class UsersViewModel : ViewModel() {
    var userList = mutableStateListOf<User>()
        private set

    init {
        userList.addAll(Api.getUsers())
        userList.forEach { user ->
            Log.d(TAG, "User $user")
        }
    }
}
