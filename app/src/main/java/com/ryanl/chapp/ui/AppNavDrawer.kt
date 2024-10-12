package com.ryanl.chapp.ui

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun AppNavDrawer(navController: NavHostController, drawerState: DrawerState, content: @Composable () -> Unit) {
    data class DrawerItem(
        val vector: ImageVector,
        val vectorDescription: String,
        val title: String,
        val route: String
    )
    val drawerItems = listOf<DrawerItem>(
        DrawerItem(Icons.Default.Email, "Email", "Messages", "history"),
        DrawerItem(Icons.Default.Search, "Search", "Find", "users"),
        DrawerItem(Icons.Default.Lock, "Lock", "Logout", "login/true")
    )
    val selectedItem = remember { mutableStateOf(drawerItems[0]) }

    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.vector,
                                contentDescription = item.vectorDescription,
                                modifier = Modifier.size(60.dp),
                            )
                        },
                        label = { Text(item.title) },
                        selected = item == selectedItem.value,
                        onClick = {
                            navController.navigate(item.route);
                            scope.launch { drawerState.close() }
                            selectedItem.value = item
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        //gesturesEnabled = false
    ) {
        content()
    }
}