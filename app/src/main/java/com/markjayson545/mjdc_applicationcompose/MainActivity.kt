package com.markjayson545.mjdc_applicationcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.markjayson545.mjdc_applicationcompose.ui.theme.MJDC_ApplicationComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MJDC_ApplicationComposeTheme {
                MainApp()
            }
        }
    }

    @Composable
    fun MainApp() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(navController)
            }
            composable("login") {
                LoginScreen(navController)
            }
            composable("calculator") {
                CalculatorScreen(navController)
            }
        }
    }

    @Composable
    fun HomeScreen(navController: NavController) {
        Scaffold(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Mark Jayson V. Dela Cruz",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.padding(5.dp))
                Text(
                    "Bachelor of Science in Information Technology",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.padding(5.dp))
                Text(
                    "BSIT-3B",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.padding(10.dp))
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        navController.navigate("login")
                    }
                ) {
                    Text("Go to Login")
                }
            }

        }
    }
}