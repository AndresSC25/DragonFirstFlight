package com.redcrow.dragonsfirstflight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun App() {
    var jugando by remember { mutableStateOf(false) }

    MaterialTheme {
        if (jugando) {
            GameScreen(
                onVolverAlMenu = { jugando = false }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dragon's First Flight",
                    style = MaterialTheme.typography.headlineSmall
                )

                Button(onClick = { jugando = true }) {
                    Text("Jugar")
                }
            }
        }
    }
}
