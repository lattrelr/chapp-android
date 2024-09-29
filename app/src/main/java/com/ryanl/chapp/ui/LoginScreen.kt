package com.ryanl.chapp.ui

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryanl.chapp.UsersActivity

private const val tag = "LoginScreen"

@Composable
fun LoginScreen(loginViewModel: LoginViewModel = viewModel()) {
    // val lifecycleOwner = LocalLifecycleOwner.current
    // val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    /*DisposableEffect (lifecycleState) {
        // lifecycleState == Lifecycle.State.RESUMED
        onDispose {
            if (lifecycleState == Lifecycle.State.RESUMED)
                loginViewModel.reset()
            Log.d(tag, "State is $lifecycleState")
        }
    }*/

    // Printing this causes recomposition on lifecycle state
    // changes, which causes us to do bad things.
    // Log.d(tag, "State is $lifecycleState")

    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier.padding(8.dp)
        ) {
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
        }
        Row (
            modifier = Modifier.padding(8.dp)
        ) {
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") }
            )
        }
        Button (
            //modifier = Modifier.fillMaxWidth(),
            onClick = {
                loginViewModel.doLogin(username, password)
            }
        ) {
            Text(text = "Login")
        }
    }

    // Check for active token
    loginViewModel.check()
    // If logged in, lets move to the next screen
    if (loginViewModel.loggedInState) {
        // TODO this be ugly
        // Make sure we start next recompose with false
        // so we don't launch again.
        loginViewModel.reset()
        username = ""
        password = ""

        Log.d(tag, "Logged in OK")
        // TODO go to user list
        // but eventually go to HistoryActivity (or whatever)
        // -- which will have previous conversation list
        context.startActivity(Intent(context, UsersActivity::class.java))
    }
}