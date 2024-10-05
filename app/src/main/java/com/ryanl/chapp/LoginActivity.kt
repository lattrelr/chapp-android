package com.ryanl.chapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ryanl.chapp.ui.LoginScreen
import com.ryanl.chapp.ui.theme.ChappAndroidTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StoredAppPrefs.open(applicationContext)
        setContent {
            ChappAndroidTheme {
                LoginScreen()
            }
        }
    }
}