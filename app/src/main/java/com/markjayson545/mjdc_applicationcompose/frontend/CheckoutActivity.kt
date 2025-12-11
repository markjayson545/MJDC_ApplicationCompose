package com.markjayson545.mjdc_applicationcompose.frontend

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.markjayson545.mjdc_applicationcompose.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController, products: List<ItemDetails>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(Modifier.padding(10.dp)) {
                var total = 0.00
                items(
                    products.size
                ) { index ->
                    ItemDetails(products[index])
                    total += products[index].price * products[index].quantity
                    Spacer(
                        Modifier.padding(vertical = 4.dp)
                    )
                }
                item {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Total: ₱$total",
                            style = MaterialTheme.typography.bodyLargeEmphasized,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ItemDetails(itemDetails: ItemDetails) {
    Card {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painter = painterResource(id = itemDetails.images[0]),
                contentDescription = null
            )
            Column {
                Text(
                    text = itemDetails.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₱${itemDetails.price} x ${itemDetails.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = "₱${itemDetails.price * itemDetails.quantity}",
                style = MaterialTheme.typography.bodyLargeEmphasized,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun CheckoutScreenPreview() {
    val item = ItemDetails(
        listOf(
            R.mipmap.kb_1_foreground,
            R.mipmap.kb_2_foreground,
            R.mipmap.kb_3_foreground,
            R.mipmap.kb_4_foreground
        ),
        "Logitech Keyboard",
        100.0,
        "This is a keyboard 1"
    )
    val items = listOf(
        item,
        item,
        item,
        item,
        item,
    )
    CheckoutScreen(rememberNavController(), items)
}