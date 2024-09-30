package com.ryanl.chapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ryanl.chapp.ui.ChatScreen
import com.ryanl.chapp.ui.TopBarNav
import com.ryanl.chapp.ui.theme.ChappAndroidTheme

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayName = intent.getStringExtra("displayName")
        val userId = intent.getStringExtra("id")
        setContent {
            ChappAndroidTheme {
                Scaffold (
                    topBar = { TopBarNav() }
                ) { innerPadding ->
                    Box(modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()) {
                        ChatScreen(userId, displayName)
                    }
                }
            }
        }
    }
}
