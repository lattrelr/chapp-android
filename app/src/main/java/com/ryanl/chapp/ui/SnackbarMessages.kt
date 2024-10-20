package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun ErrorSnacks(errorViewModel: ErrorViewModel = viewModel()) {
    // TODO ErrorViewModel could handle state...somehow report
    // TODO the errorviewmodel from background tasks
    DisposableEffect(Unit) {
        //errorViewModel.enterView()
        onDispose {
            //errorViewModel.exitView()
        }
    }

    // Errors that cross all screens ->
    // Server not responding
    // Need to login again / Not authenticated
    // No internet connection

    ShowServerIssues()

    //TODO show a login screen overlay window on snackbar instead of going back to
    //TODO the main login screen
}

@Composable
fun ShowServerIssues() {
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = snackbarHostState.showSnackbar(
            message = "Server not responding",
            actionLabel = "Action",
            // Defaults to SnackbarDuration.Short
            duration = SnackbarDuration.Indefinite
        )
        when (result) {
            SnackbarResult.ActionPerformed -> {
                Log.d("SNACKBAR", "ACTION")
                /* Handle snackbar action performed */
            }
            SnackbarResult.Dismissed -> {
                Log.d("SNACKBAR", "DISMISS")
                /* Handle snackbar dismissed */
            }
        }
    }
}