package com.ryanl.chapp.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ryanl.chapp.api.models.User

private const val TAG = "UsersScreen"

@Composable
fun UsersScreen(
    usersViewModel: UsersViewModel = viewModel(),
    navController: NavHostController
) {
    BackHandler {
        // Do nothing, prevent logout
    }
    DisposableEffect(Unit) {
        usersViewModel.enterUsersView()
        onDispose {
            usersViewModel.leaveUsersView()
        }
    }

    LazyColumn (
      /*modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight()*/
    ) {
        items(usersViewModel.userMap.entries.toList()) { user ->
            user.value?.let {
                UserRow(it, navController)
            }
        }
    }
}

@Composable
fun UserRow(user: User, navController: NavHostController) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            //.height(100.dp)
            //.fillMaxHeight()
            .padding(2.dp),
        onClick = {
            Log.d(TAG, "clicked ${user.displayname}")
            navController.navigate("chat/${user.id}/${user.displayname}")
        }
    ) {
        Row {
            Icon (
                imageVector = Icons.Default.Person,
                contentDescription = "User picture",
                modifier = Modifier.size(60.dp)
            )
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = user.displayname,
                    modifier = Modifier
                        .fillMaxWidth(0.8F)
                        .padding(16.dp),
                    textAlign = TextAlign.Left,
                )
                if (user.online) {
                    Box(
                        modifier = Modifier
                            .padding(20.dp)
                            .align(Alignment.CenterVertically)
                            //.fillMaxWidth()
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                    ) {

                    }
                }
            }
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun UsersPreview() {
    Scaffold (
        topBar = { TopBarNav() }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            //UsersScreen()
        }
    }
}*/