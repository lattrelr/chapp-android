package com.ryanl.chapp.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ryanl.chapp.persist.models.History

private const val TAG = "HistoryScreen"

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = viewModel(),
    navController: NavHostController
) {
    DisposableEffect(Unit) {
        historyViewModel.enterUsersView()
        onDispose {
            historyViewModel.leaveUsersView()
        }
    }

    LazyColumn (
        /*modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()*/
    ) {
        items(historyViewModel.historyList) { h ->
            HistoryRow(h, navController)
        }
    }
}

// TODO reuse some of this from users screen?
@Composable
fun HistoryRow(h: History, navController: NavHostController) {
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
            Log.d(TAG, "clicked ${h.displayname}")
            navController.navigate("chat/${h.id}/${h.displayname}")
        }
    ) {
        Row {
            Icon (
                imageVector = Icons.Default.Person,
                contentDescription = "User picture",
                modifier = Modifier.size(60.dp)
            )
            Text(
                text = h.displayname,
                modifier = Modifier
                    .fillMaxWidth(0.8F)
                    .padding(16.dp),
                textAlign = TextAlign.Left,
            )
            // TODO how to get online status (instead of one-by-one?)
            /*if (user.online) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        //.fillMaxWidth()
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Green),
                ) {

                }
            }*/
        }
    }
}