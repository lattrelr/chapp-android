package com.ryanl.chapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ryanl.chapp.persist.AppDatabase
import com.ryanl.chapp.persist.Historian
import com.ryanl.chapp.persist.StoredAppPrefs
import com.ryanl.chapp.socket.AuthenticationManager
import com.ryanl.chapp.socket.ConnectionManager
import com.ryanl.chapp.ui.AppNavDrawer
import com.ryanl.chapp.ui.AppNavigation
import com.ryanl.chapp.ui.ErrorSnacks
import com.ryanl.chapp.ui.ErrorViewModel
import com.ryanl.chapp.ui.LocalSnackbarHostState
import com.ryanl.chapp.ui.TopBarNav
import com.ryanl.chapp.ui.theme.ChappAndroidTheme
import java.sql.Connection

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StoredAppPrefs.open(applicationContext)
        // TODO stop on destroy?
        Historian.start(applicationContext)
        setContent {
            val navController = rememberNavController()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val snackbarHostState = remember { SnackbarHostState() }

            CompositionLocalProvider(
                values = arrayOf(
                    LocalSnackbarHostState provides snackbarHostState
                )
            ) {
                AppNavDrawer(navController, drawerState) {
                    ChappAndroidTheme {
                        Scaffold(
                            topBar = { TopBarNav(navController, drawerState) },
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                AppNavigation(navController)
                            }
                        }
                    }
                }
                ErrorSnacks()
            }
        }
    }
}

