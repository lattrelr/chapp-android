package com.ryanl.chapp.ui

import android.text.format.DateUtils
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ryanl.chapp.persist.models.History
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "HistoryScreen"

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = viewModel(),
    navController: NavHostController
) {
    BackHandler {
        // Do nothing, prevent logout
    }

    DisposableEffect(Unit) {
        historyViewModel.enterUsersView()
        onDispose {
            historyViewModel.leaveUsersView()
        }
    }

    if (historyViewModel.historyMap.size == 0) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No conversations...")
            Button(onClick = {
                navController.navigate("users")
            }) {
                Text("Find")
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            items(historyViewModel.historyMap.entries.toList()) { h ->
                h.value?.let {
                    HistoryRow(it, navController)
                }
            }
        }
    }
}

// TODO commonize some of this from the chat view?
val formatterCalender = SimpleDateFormat("yyyy-MM-dd", Locale.US)
val formatterTime = SimpleDateFormat("hh:mm a", Locale.US)

private fun isYesterday(d: Date): Boolean {
    return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
}

private fun isToday(d: Date): Boolean {
    return DateUtils.isToday(d.time)
}

// TODO reuse some of this from users screen?
@Composable
fun HistoryRow(h: HistoryViewModel.HistoryListItem, navController: NavHostController) {
    //Log.d(TAG, "$h")

    var dateStr = ""
    h.timestamp?.let { t ->
        dateStr = if (isToday(Date(t))) {
            formatterTime.format(t)
        } else if (isYesterday(Date(t))) {
            "Yesterday"
        } else {
            formatterCalender.format(t)
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            //.fillMaxHeight(.75F)
            .padding(2.dp),
        onClick = {
            Log.d(TAG, "clicked ${h.displayname}")
            navController.navigate("chat/${h.id}/${h.displayname}")
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon (
                imageVector = Icons.Default.Person,
                contentDescription = "User picture",
                modifier = Modifier.size(60.dp),
            )
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(5.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = h.displayname,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateStr,
                    )
                }
                Text(
                    text = "${h.lastMessage ?: ' '}",
                    maxLines = 2,
                )
            }

            // TODO how to get online status (instead of one-by-one?)
        }
    }
}