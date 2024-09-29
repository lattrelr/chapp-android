package com.ryanl.chapp.ui

import android.app.Activity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

private const val TAG = "ChatScreen"

@Composable
fun ChatScreen(chatViewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    val intent = (context as Activity).intent
    Text(text="Chat Screen for ${intent.getStringExtra("displayName")}")
}