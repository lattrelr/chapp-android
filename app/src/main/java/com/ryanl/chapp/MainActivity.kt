package com.ryanl.chapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ryanl.chapp.persist.AppDatabase
import com.ryanl.chapp.persist.StoredAppPrefs
import com.ryanl.chapp.ui.AppNavigation
import com.ryanl.chapp.ui.TopBarNav
import com.ryanl.chapp.ui.theme.ChappAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StoredAppPrefs.open(applicationContext)
        AppDatabase.getInstance(applicationContext)
        setContent {
            val navController = rememberNavController()
            ChappAndroidTheme {
                Scaffold (
                    topBar = { TopBarNav(navController) }
                ) { innerPadding ->
                    Box(modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()) {
                        AppNavigation(navController)
                    }
                }
            }
        }
    }
}