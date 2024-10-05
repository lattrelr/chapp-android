package com.ryanl.chapp.ui

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryanl.chapp.StoredAppPrefs
import com.ryanl.chapp.api.models.Message

private const val TAG = "ChatScreen"

@Composable
fun ChatScreen(
    toUserId: String?,
    toDisplayName: String?,
    chatViewModel: ChatViewModel = viewModel()
) {
    // Every time we come to the foreground, subscribe to socket, and get history.
    // TODO only get new history and not all history ?  Store old history in room db
    LifecycleStartEffect(Unit) {
        Log.d(TAG, "Chat window in foreground")
        // TODO deal with any races here...if we get history and messages at the same time
        chatViewModel.subscribeFromUser(toUserId)
        chatViewModel.subscribeFromUser(StoredAppPrefs.getUserId())
        chatViewModel.fetchHistory(toUserId)
        onStopOrDispose {
            // TODO this runs twice, but shouldn't matter
            chatViewModel.unsubscribeFromUser(toUserId)
            chatViewModel.unsubscribeFromUser(StoredAppPrefs.getUserId())
            chatViewModel.clearHistory()
        }
    }

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
    val listState = rememberLazyListState()

    LaunchedEffect(chatViewModel.messageHistory.size) {
        listState.animateScrollToItem(chatViewModel.messageHistory.size)
    }

    LazyColumn (
        modifier = Modifier
            .fillMaxHeight(0.9F)
            .fillMaxWidth(),
        state = listState,
    ) {
        items(chatViewModel.messageHistory) { msg ->
            MessageBubble(msg)
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val myUserId = StoredAppPrefs.getUserId()
    Column (
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Card(
            modifier = (if (message.from == myUserId)
                Modifier.align(Alignment.End) else
                Modifier.align(Alignment.Start))
                .padding(5.dp),
            colors = if (message.from == myUserId)
                CardDefaults.cardColors(Color.Green) else
                CardDefaults.cardColors(Color.Gray)
        ) {
            Text(
                text = message.text,
                modifier = Modifier
                    .widthIn(0.dp, 300.dp)
                    .padding(10.dp)
            )
        }
    }
}

@Composable
fun ChatSend(chatViewModel: ChatViewModel = viewModel(), toUserId: String?) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

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
        IconButton(onClick = {
            chatViewModel.sendMessage(toUserId)
            focusManager.clearFocus()
            //keyboardController?.hide()
        }) {
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