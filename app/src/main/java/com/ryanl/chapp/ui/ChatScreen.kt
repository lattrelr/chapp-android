package com.ryanl.chapp.ui

import android.text.format.DateUtils
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryanl.chapp.api.models.Message
import com.ryanl.chapp.persist.StoredAppPrefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private const val TAG = "ChatScreen"
private const val TIMESTAMP_DIFF_HOURS: Long = 2

@Composable
fun ChatScreen(
    toUserId: String?,
    toDisplayName: String?,
    chatViewModel: ChatViewModel = viewModel()
) {
    // Every time we come to the foreground, subscribe to socket, and get history.
    // TODO only get new history and not all history ?  Store old history in room db
    /*LifecycleStartEffect(Unit) {
        Log.d(TAG, "Chat window in foreground")
        chatViewModel.enterChatView(toUserId, StoredAppPrefs.getUserId())
        onStopOrDispose {
            // TODO this runs twice, but shouldn't matter
            chatViewModel.leaveChatView(toUserId, StoredAppPrefs.getUserId())
        }
    }*/

    // TODO does this change with userId ?
    DisposableEffect(/*toUserId*/Unit) {
        if (toUserId != null && toDisplayName != null) {
            chatViewModel.enterChatView(toUserId, toDisplayName)
        }
        onDispose {
            if (toUserId != null) {
                chatViewModel.leaveChatView(toUserId)
            }
        }
    }

    Column {
        ChatHeader(chatViewModel, toDisplayName)
        ChatHistory(chatViewModel)
        ChatSend(chatViewModel, toUserId)
    }
}

@Composable
fun ChatHeader(chatViewModel: ChatViewModel, toDisplayName: String?) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(8.dp),
        verticalAlignment = Alignment.Bottom
        //verticalArrangement = Arrangement.Center,
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon (
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "User picture",
            modifier = Modifier.size(70.dp)
        )
        Text(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Left,
            text = "$toDisplayName",
            modifier = Modifier
                //.fillMaxWidth(0.8F)
                .padding(16.dp),
        )
        if (chatViewModel.userOnline.value) {
            Text(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                color = Color.DarkGray,
                text = "ONLINE",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@Composable
fun ChatHistory(chatViewModel: ChatViewModel = viewModel()) {
    val listState = rememberLazyListState()

    LaunchedEffect(chatViewModel.messageHistory.size) {
        //listState.animateScrollToItem(chatViewModel.messageHistory.size)
        listState.scrollToItem(chatViewModel.messageHistory.size)
    }

    LazyColumn (
        modifier = Modifier
            .fillMaxHeight(0.9F)
            .fillMaxWidth(),
        state = listState,
    ) {
        itemsIndexed(chatViewModel.messageHistory) {idx, msg ->
            var prev: Message? = null
            if (idx > 0) {
                prev = chatViewModel.messageHistory[idx - 1]
            }
            MessageTime(prev, msg)
            MessageBubble(msg)
        }
    }
}

val formatterWithCalender = SimpleDateFormat("hh:mm a' on 'yyyy-MM-dd", Locale.US)
val formatter = SimpleDateFormat("hh:mm a", Locale.US)

private fun isYesterday(d: Date): Boolean {
    return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
}

private fun isToday(d: Date): Boolean {
    return DateUtils.isToday(d.time)
}

@Composable
fun MessageTime(prev: Message?, cur: Message) {
    var diff: Long = TIMESTAMP_DIFF_HOURS
    prev?.let {
        diff = ((cur.date - prev.date) / 1000) / 3600
        Log.d(TAG, "Diff... ${prev.date} ${cur.date} $diff")
    }
    if (diff >= TIMESTAMP_DIFF_HOURS) {
        val dateStr = if (isToday(Date(cur.date))) {
            "Today at ${formatter.format(cur.date)}"
        } else if (isYesterday(Date(cur.date))) {
            "Yesterday at ${formatter.format(cur.date)}"
        } else {
            formatterWithCalender.format(cur.date)
        }

        Text(
            text = dateStr,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
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