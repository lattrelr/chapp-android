package com.ryanl.chapp.ui

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private const val TAG = "ChatScreen"

@Composable
fun ChatScreen(chatViewModel: ChatViewModel = viewModel()) {
    // TODO how to get intent to/from viewModel properly...
    // maybe launched effect, on created?
    val context = LocalContext.current
    var username: String? = "No name"
    var userId: String? = "No ID"
    try {
        val intent = (context as Activity).intent
        username = intent.getStringExtra("displayName")
        userId = intent.getStringExtra("id")
    } catch (e: Exception) {

    }

    Column {
        ChatHeader(username)
        ChatHistory()
        ChatSend(chatViewModel, userId)
    }
}

@Composable
fun ChatHeader(username: String?) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(8.dp),
        //verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon (
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "User picture",
            modifier = Modifier.size(40.dp)
        )
        Text(text = "$username")
    }
}

@Composable
fun ChatHistory() {
    Column (
        modifier = Modifier
            .fillMaxHeight(0.9F)
            .fillMaxWidth()
    ) {

    }
}

@Composable
fun ChatSend(chatViewModel: ChatViewModel = viewModel(), userId: String?) {
    Row (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TextField(
            value = chatViewModel.currentMessage.value,
            onValueChange = { chatViewModel.updateMessage(it) },
            modifier = Modifier.fillMaxWidth(0.85F)
        )
        IconButton(onClick = { chatViewModel.sendMessage(userId) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Send,
                contentDescription = "User picture",
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    Scaffold (
        topBar = { TopBarNav() }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            ChatScreen()
        }
    }
}