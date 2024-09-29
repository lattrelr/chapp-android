package com.ryanl.chapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.ryanl.chapp.ui.ChatScreen
import com.ryanl.chapp.ui.TopBarNav
import com.ryanl.chapp.ui.UsersScreen
import com.ryanl.chapp.ui.theme.ChappAndroidTheme

class UsersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChappAndroidTheme {
                Scaffold (
                    topBar = { TopBarNav() }
                ) { innerPadding ->
                    Box(modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()) {
                        UsersScreen()
                    }
                }
            }
        }
    }
}