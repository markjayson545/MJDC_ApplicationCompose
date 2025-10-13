package com.markjayson545.mjdc_applicationcompose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class ItemDetails(
    val images: List<Int>,
    val name: String,
    val price: Double,
    val description: String,
    var isChecked: Boolean = false,
    var quantity: Int = 1
)

@Composable
fun CartScreen(navController: NavController, username: String) {
    val cartItems = remember {
        mutableStateListOf(
            ItemDetails(
                listOf(
                    R.mipmap.kb_1_foreground,
                    R.mipmap.kb_2_foreground,
                    R.mipmap.kb_3_foreground,
                    R.mipmap.kb_4_foreground
                ),
                "Logitech Keyboard",
                100.0,
                "This is a keyboard 1"
            ),
            ItemDetails(
                listOf(
                    R.mipmap.ms_1_foreground,
                    R.mipmap.ms_2_foreground,
                    R.mipmap.ms_3_foreground,
                    R.mipmap.ms_4_foreground
                ),
                "Logitech Mouse",
                100.0,
                "This is a keyboard 2"
            )
        )
    }
    val logoutDialog = remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(
        )
    ) { paddingValues ->
        LazyColumn(
            Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Welcome, $username",
                    )
                    Button(
                        onClick = {
                            logoutDialog.value = true
                        }
                    ) {
                        Text("Logout")
                    }
                }
            }
            items(cartItems.size) { index ->
                ProductCard(
                    item = cartItems[index],
                    isCheck = cartItems[index].isChecked,
                    onCheckedChange = { isChecked ->
                        cartItems[index] = cartItems[index].copy(isChecked = isChecked)
                    },
                    quantity = cartItems[index].quantity,
                    onQuantityChange = { quantity ->
                        cartItems[index] = cartItems[index].copy(quantity = quantity)
                    }
                )
            }
            item {
                Box(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {

                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("Proceed to Checkout")
                    }
                }
            }
        }
        if (logoutDialog.value) {
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null
                    )
                },
                onDismissRequest = {},
                title = {
                    Text("Logout")
                },
                text = {
                    Text("Are you sure you want to logout?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            navController.navigate("login")
                        }
                    ) {
                        Text("Yes")
                    }
                },
            )
        }
    }
}

@Composable
fun ProductCard(
    item: ItemDetails,
    isCheck: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    quantity: Int = 1,
    onQuantityChange: (Int) -> Unit = {}
) {
    val currentImage = remember { mutableIntStateOf(item.images[0]) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isCheck,
                onCheckedChange = onCheckedChange,
            )
            Image(
                painter = painterResource(id = currentImage.intValue),
                contentDescription = null,
            )
            Column {
                Text(
                    text = item.name,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₱${item.price}",
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            if (quantity > 1) {
                                onQuantityChange(quantity - 1)
                            }
                        }
                    ) {
                        Text("-")
                    }
                    Text(
                        text = quantity.toString(),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    )
                    TextButton(
                        onClick = {
                            onQuantityChange(quantity + 1)
                        }
                    ) {
                        Text("+")
                    }

                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    currentImage.intValue =
                        item.images[item.images.indexOf(currentImage.intValue) + 1]
                    if (item.images.indexOf(currentImage.intValue) == item.images.size - 1) {
                        currentImage.intValue = item.images[0]
                    }
                }
            ) {
                Text("Next")
            }
        }
    }
}

@Preview
@Composable
fun CartScreenPreview() {
    CartScreen(
        rememberNavController(),
        username = "Userma,e"
    )
}