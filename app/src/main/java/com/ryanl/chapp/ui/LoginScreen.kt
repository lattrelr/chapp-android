package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

private const val tag = "LoginScreen"

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    navController: NavHostController
) {
    var username by rememberSaveable { mutableStateOf("ryanl") }
    var password by rememberSaveable { mutableStateOf("password") }
    var firstLaunch by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Log.d(tag, "Launched!")
        if (firstLaunch) {
            firstLaunch = false
            // Auto login on start up only
            loginViewModel.checkForActiveSession() {
                navController.navigate("users")
            }
        } else {
            loginViewModel.logout()
        }
    }

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
                loginViewModel.doLogin(username, password) { success ->
                    if (success) {
                        username = ""
                        password = ""
                        navController.navigate("users")
                    }
                    // TODO display message on failure
                }
            }
        ) {
            Text(text = "Login")
        }
    }
}