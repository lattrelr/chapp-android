package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryanl.chapp.ErrorReporter
import kotlinx.coroutines.launch

@Composable
fun ErrorSnacks(errorViewModel: ErrorViewModel = viewModel()) {
    val errUiState by errorViewModel.uiState.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current

    DisposableEffect(Unit) {
        errorViewModel.enterView()
        onDispose {
            errorViewModel.exitView()
        }
    }

    if (errorViewModel.lastTempError.intValue != errorViewModel.NO_ERROR) {
        val e = ErrorReporter.ErrorTypes.fromInt(errorViewModel.lastTempError.intValue)
        if (e == ErrorReporter.ErrorTypes.SEND_FAILED) {
            ShowTempMessage(errorViewModel, e, "Failed to send message")
        }
    } else {
        if (errUiState.internetError) {
            ShowPermMessage("No internet connection")
        } /*else if (errUiState.serverError) {
            ShowPermMessage("Server not responding")
        }*/ else if (errUiState.authError) {
            // TODO give link to login screen (overlay?)
            ShowPermMessage("Access denied. Log in again.")
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }

    }

    //TODO does the snackbar go away when error is false
    //TODO auth error should not be shown from the login screen
}

@Composable
fun ShowPermMessage(errMsg: String) {
    val snackbarHostState = LocalSnackbarHostState.current
    LaunchedEffect(Unit) {
        snackbarHostState.showSnackbar(
            message = errMsg,
            duration = SnackbarDuration.Indefinite
        )
    }
}

@Composable
fun ShowTempMessage(errorViewModel: ErrorViewModel, e: ErrorReporter.ErrorTypes, errMsg: String) {
    val snackbarHostState = LocalSnackbarHostState.current
    LaunchedEffect(Unit) {
        val result = snackbarHostState.showSnackbar(
            message = errMsg,
            duration = SnackbarDuration.Short
        )
        when (result) {
            SnackbarResult.ActionPerformed -> { }
            SnackbarResult.Dismissed -> {
                errorViewModel.errorDismissed(e)
            }
        }
    }
}

@Composable
fun ShowServerIssues() {
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = snackbarHostState.showSnackbar(
            message = "Server not responding",
            //actionLabel = "Action",
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