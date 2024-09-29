package com.ryanl.chapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Card
import com.ryanl.chapp.ui.LoginScreen
import com.ryanl.chapp.ui.theme.ChappAndroidTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChappAndroidTheme {
                LoginScreen()
            }
        }
    }
}