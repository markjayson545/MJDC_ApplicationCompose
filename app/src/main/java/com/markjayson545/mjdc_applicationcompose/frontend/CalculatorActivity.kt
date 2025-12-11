package com.markjayson545.mjdc_applicationcompose.frontend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun CalculatorScreen(navController: NavController) {

    val firstNumber = remember { mutableStateOf("") }
    val secondNumber = remember { mutableStateOf("") }
    val result = remember { mutableStateOf("") }
    Scaffold(Modifier.fillMaxSize()) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        navController.navigate("home")
                    }
                ) {
                    Text("Logout")
                }
                CalculatorInput(
                    label = "First Number",
                    value = firstNumber.value,
                    onValueChange = {
                        firstNumber.value = it
                    },
                    icon = Icons.Default.Numbers,
                )
                CalculatorInput(
                    label = "Second Number",
                    value = secondNumber.value,
                    onValueChange = {
                        secondNumber.value = it
                    },
                    icon = Icons.Default.Numbers,
                )

                Spacer(Modifier.padding(10.dp))

                Row {
                    Button(
                        onClick = {
                            result.value =
                                (firstNumber.value.toDouble() + secondNumber.value.toDouble()).toString()
                        }
                    ) {
                        Text("Add")
                    }
                    Button(
                        onClick = {
                            result.value =
                                (firstNumber.value.toDouble() - secondNumber.value.toDouble()).toString()
                        }
                    ) {
                        Text("Subtract")
                    }
                }
                Row {
                    Button(
                        onClick = {
                            result.value =
                                (firstNumber.value.toDouble() * secondNumber.value.toDouble()).toString()
                        }
                    ) {
                        Text("Multiply")
                    }
                    Button(
                        onClick = {
                            result.value =
                                (firstNumber.value.toDouble() / secondNumber.value.toDouble()).toString()
                        }
                    ) {
                        Text("Divide")
                    }
                    Button(
                        onClick = {
                            result.value =
                                (firstNumber.value.toInt() % secondNumber.value.toInt()).toString()
                        }
                    ) {
                        Text("Modulus")
                    }
                }

                Spacer(Modifier.padding(10.dp))
                val isCalculateAll = remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        firstNumber.value = ""
                        secondNumber.value = ""
                        result.value = ""
                        isCalculateAll.value = false
                    }
                ) {
                    Text("Clear")
                }


                Text("Result: ${result.value}")


                Button(
                    onClick = {
                        isCalculateAll.value = true
                    }
                ) {
                    Text("Calculate All")
                }

                if (isCalculateAll.value) {
                    Text(
                        "Addition Result: ${firstNumber.value.toDouble() + secondNumber.value.toDouble()}\n" +
                                "Subtraction Result: ${firstNumber.value.toDouble() - secondNumber.value.toDouble()}\n" +
                                "Multiplication Result: ${firstNumber.value.toDouble() * secondNumber.value.toDouble()}\n" +
                                "Division Result: ${firstNumber.value.toDouble() / secondNumber.value.toDouble()}\n" +
                                "Modulus Result: ${firstNumber.value.toInt() % secondNumber.value.toInt()}"
                    )
                }

            }
        }
    }
}

@Composable
fun CalculatorInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = ""
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(label)
        }
        OutlinedTextField(
            value = value,
            singleLine = true,
            placeholder = {
                Text(placeholder)
            },
            onValueChange = onValueChange
        )
    }
}

@Preview
@Composable
fun CalculatorScreenPreview() {
    CalculatorScreen(rememberNavController())
}