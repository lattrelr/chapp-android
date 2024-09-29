package com.ryanl.chapp

import android.icu.number.NumberFormatter.UnitWidth
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ryanl.chapp.ui.theme.ChappAndroidTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class MainActivity : ComponentActivity() {
    private var count = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChappAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        //var count2: Int by remember {mutableStateOf(0)}
                        Counter(
                            theNum = count,
                            modifier = Modifier.padding(innerPadding)
                        )
                        ButtonUp() { count.intValue++ }
                        ButtonDown() { count.intValue-- }
                        //ClickCounter(cCounter++) {}
                    }
                }
            }
        }
    }
}

@Composable
fun Counter(theNum: MutableIntState, modifier: Modifier = Modifier) {
    Text(
        text = "Count ${theNum.intValue}",
        modifier = modifier
    )
}

@Composable
fun ButtonUp(onClick: () -> Unit) {
    //var count2: Int by remember {mutableStateOf(0)}
    Button(
        onClick = { onClick() }
    ) {
        Text("Up")
    }
}

@Composable
fun ButtonDown(onClick: () -> Unit) {
    Button(
        onClick = { onClick() }
    ) {
        Text("Down")
    }
}

@Composable
fun ClickCounter(clicks: Int, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Number of $clicks")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChappAndroidTheme {
        Column {
            //Counter(8)
            ButtonUp() {}
            ButtonDown() {}
            ClickCounter(8) {}
        }
    }
}