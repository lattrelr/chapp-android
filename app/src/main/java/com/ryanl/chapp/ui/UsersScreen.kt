package com.ryanl.chapp.ui

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryanl.chapp.ChatActivity
import com.ryanl.chapp.UsersActivity
import com.ryanl.chapp.models.User

private const val TAG = "UsersScreen"

@Composable
fun UsersScreen(usersViewModel: UsersViewModel = viewModel()) {
    LazyColumn (
      /*modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight()*/
    ) {
        items(usersViewModel.userList) { user ->
            UserRow(user)
        }
    }
}

@Composable
fun UserRow(user: User) {
    val context = LocalContext.current

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
            Log.d(TAG, "clicked ${user.displayName}")
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("displayName", user.displayName)
            intent.putExtra("id", user.id)
            context.startActivity(intent)
        }
    ) {
        Row {
            Icon (
                imageVector = Icons.Default.Person,
                contentDescription = "User picture",
                modifier = Modifier.size(60.dp)
            )
            Text(
                text = user.displayName,
                modifier = Modifier
                    //.fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UsersPreview() {
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