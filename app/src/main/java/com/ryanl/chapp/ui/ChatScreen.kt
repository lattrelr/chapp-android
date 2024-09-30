package com.ryanl.chapp.ui

import android.app.Activity
import android.text.Layout
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryanl.chapp.models.Message

private const val TAG = "ChatScreen"

// TODO does recompose keep parameters?
@Composable
fun ChatScreen(
    toUserId: String?,
    toDisplayName: String?,
    chatViewModel: ChatViewModel = viewModel()
) {
    chatViewModel.fetchHistory(toUserId)
    Column {
        ChatHeader(toDisplayName)
        ChatHistory(chatViewModel)
        ChatSend(chatViewModel, toUserId)
    }
}

@Composable
fun ChatHeader(toDisplayName: String?) {
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
        Text(text = "$toDisplayName")
    }
}

@Composable
fun ChatHistory(chatViewModel: ChatViewModel = viewModel()) {
    LazyColumn (
        modifier = Modifier
            .fillMaxHeight(0.9F)
            .fillMaxWidth()
    ) {
        items(chatViewModel.messageHistory) { msg ->
            MessageBubble(msg)
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val myUserId = StoredAppPrefs.getUserId()
    Card() {
        if (message.from == myUserId) {
            Text(
                text = message.text,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        } else {
            Text(
                text = message.text
            )
        }
    }
}

@Composable
fun ChatSend(chatViewModel: ChatViewModel = viewModel(), toUserId: String?) {
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
        IconButton(onClick = { chatViewModel.sendMessage(toUserId) }) {
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
            ChatScreen("1", "Bob")
        }
    }
}