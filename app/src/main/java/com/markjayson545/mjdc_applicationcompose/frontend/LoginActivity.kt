package com.markjayson545.mjdc_applicationcompose.frontend

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loginAttempts by remember { mutableIntStateOf(5) }
    val usernameTextFieldValue = remember { mutableStateOf("") }
    val passwordTextFieldValue = remember { mutableStateOf("") }

    val timer = remember { mutableIntStateOf(5) }
    val isLoginEnabled = remember { mutableStateOf(true) }

    Scaffold(Modifier.fillMaxSize()) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "User Login",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.padding(20.dp))
                LoginInput(
                    label = "Username",
                    value = usernameTextFieldValue.value,
                    onValueChange = {
                        usernameTextFieldValue.value = it
                    },
                    icon = Icons.Default.Person,
                    placeholder = "Enter your username"
                )
                Spacer(Modifier.padding(10.dp))
                LoginInput(
                    label = "Password",
                    value = passwordTextFieldValue.value,
                    onValueChange = {
                        passwordTextFieldValue.value = it
                    },
                    icon = Icons.Default.Key,
                    isPassword = true,
                    placeholder = "Enter your password"
                )
                Text(
                    "Login attempts left: $loginAttempts",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(vertical = 10.dp)
                )
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = isLoginEnabled.value,
                    onClick = {
                        if (usernameTextFieldValue.value == "MarkJay" && passwordTextFieldValue.value == "markjay") {
                            val username = usernameTextFieldValue.value
                            usernameTextFieldValue.value = ""
                            passwordTextFieldValue.value = ""
                            navController.navigate("cart/$username")
                        } else {
                            loginAttempts--
                            if (loginAttempts == 0) {
                                Toast.makeText(
                                    context,
                                    "Login attempts exceeded",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Start Timer
                                scope.launch {
                                    isLoginEnabled.value = false
                                    timer.intValue = 5
                                    for (i in 5 downTo 1) {
                                        timer.intValue = i
                                        delay(1000)
                                    }
                                    loginAttempts = 5
                                    isLoginEnabled.value = true
                                }

                                return@Button
                            }
                            Toast.makeText(
                                context,
                                "Invalid username or password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text(text = if (loginAttempts > 0) "Login" else "Wait ${timer.intValue}")
                }
            }
            Text(
                "Copyright © 2025 Mark Jayson V. Dela Cruz\nAll rights reserved.",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoginInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    isPassword: Boolean = false,
    placeholder: String = ""
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            Spacer(Modifier.padding(5.dp))
            Text(label)
        }
        Spacer(Modifier.padding(2.dp))
        OutlinedTextField(
            value = value,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true,
            label = {
                Text(label)
            },
            placeholder = {
                Text(placeholder)
            },
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(rememberNavController())
}