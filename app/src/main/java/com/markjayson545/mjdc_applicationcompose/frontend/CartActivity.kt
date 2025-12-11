package com.markjayson545.mjdc_applicationcompose.frontend

import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.markjayson545.mjdc_applicationcompose.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Parcelize
@Serializable
data class ItemDetails(
    val images: List<Int>,
    val name: String,
    val price: Double,
    val description: String,
    var isChecked: Boolean = false,
    var quantity: Int = 1
) : Parcelable

@OptIn(ExperimentalMaterial3Api::class)
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
                "G915 X",
                1500.00,
                "Logitech G915 X delivers iconic speed, accuracy, and customization with a sleek, ultra-thin design."
            ),
            ItemDetails(
                listOf(
                    R.mipmap.ms_1_foreground,
                    R.mipmap.ms_2_foreground,
                    R.mipmap.ms_3_foreground,
                    R.mipmap.ms_4_foreground
                ),
                "Pro 2 Lightspeed",
                1200.00,
                "Designed with pros and engineered to win, the Logitech G PRO 2 LIGHTSPEED wireless mouse is an symmetric, ambidextrous mouse with customizable magnetic side buttons that delivers ultra-fast, precise performance and customizable style for competitive gamers."
            )
        )
    }
    val logoutDialog = remember { mutableStateOf(false) }
    remember {
        mutableStateOf(
            false
        )
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(
        ),
        topBar = {
            TopAppBar(
                title = { Text("Cart") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            logoutDialog.value = true
                        }
                    ) {
                        Text("Logout")
                    }
                },
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(10.dp)
                .fillMaxSize()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    CartHeader(username)
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
                        val isEnabled = cartItems.any { it.isChecked }
                        Button(
                            enabled = isEnabled,
                            onClick = {
                                val checkedItems = cartItems.filter { it.isChecked }
                                val jsonString = Json.encodeToString(checkedItems)
                                val encodedJson =
                                    URLEncoder.encode(jsonString, StandardCharsets.UTF_8.toString())
                                navController.navigate("checkout/$encodedJson")
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text("Proceed to Checkout")
                        }
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
                onDismissRequest = {
                    logoutDialog.value = false
                },
                title = {
                    Text("Logout")
                },
                text = {
                    Text("Are you sure you want to logout?")
                },
                dismissButton = {
                    Button(
                        onClick = {
                            logoutDialog.value = false
                        }
                    ) {
                        Text("No")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            logoutDialog.value = false
                            navController.popBackStack()
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = isCheck,
                    onCheckedChange = onCheckedChange,
                )
            }
            ImageContainer(
                images = item.images
            )
            Spacer(
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Column(
                Modifier.padding(vertical = 5.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₱${item.price}",
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                )
                Text(
                    text = item.description,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
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
                        Text(
                            "-",
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            textAlign = TextAlign.Center
                        )
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
                        Text(
                            "+",
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            textAlign = TextAlign.Center
                        )
                    }

                }
            }
//            Spacer(modifier = Modifier.weight(1f))
//            Button(
//                onClick = {
//                    currentImage.intValue =
//                        item.images[item.images.indexOf(currentImage.intValue) + 1]
//                    if (item.images.indexOf(currentImage.intValue) == item.images.size - 1) {
//                        currentImage.intValue = item.images[0]
//                    }
//                },
//                modifier = Modifier.align(Alignment.CenterVertically)
//            ) {
//                Text("Next")
//            }
        }
    }
}

@Composable
fun ImageContainer(
    images: List<Int>
) {
    val currentImage = remember { mutableIntStateOf(images[0]) }
    val isButtonsVisible = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .clip(RectangleShape)
    ) {
        // Image fills the entire container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = currentImage.intValue),
                contentDescription = null,
                modifier = Modifier
                    .clickable(
                        true,
                        onClick = {
                            scope.launch {
                                isButtonsVisible.value = !isButtonsVisible.value
                                delay(500)
                                isButtonsVisible.value = !isButtonsVisible.value
                            }
                        }
                    ),
                contentScale = ContentScale.Crop
            )
            Button(
                onClick = {
                    if (images.indexOf(currentImage.intValue) < images.size - 1) {
                        currentImage.intValue =
                            images[images.indexOf(currentImage.intValue) + 1]
                    } else {
                        currentImage.intValue = images[0]
                    }
                }
            ) {
                Text(
                    "Next"
                )
            }
        }

        // Left navigation button overlay
        AnimatedVisibility(
            visible = isButtonsVisible.value,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = {
                    if (images.indexOf(currentImage.intValue) > 0) {
                        currentImage.intValue =
                            images[images.indexOf(currentImage.intValue) - 1]
                    } else {
                        currentImage.intValue = images[images.size - 1]
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.7f
                        )
                    )
                    .fillMaxHeight()
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowLeft,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        }

        // Right navigation button overlay
        AnimatedVisibility(
            visible = isButtonsVisible.value,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = {
                    if (images.indexOf(currentImage.intValue) < images.size - 1) {
                        currentImage.intValue =
                            images[images.indexOf(currentImage.intValue) + 1]
                    } else {
                        currentImage.intValue = images[0]
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.7f
                        )
                    )
                    .fillMaxHeight()
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowRight,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
fun CartHeader(username: String) {
    Column {
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null
                )
                Text(
                    "Logged in as $username",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.padding(5.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
                Image(
                    painter = painterResource(id = R.mipmap.logitech_logo_foreground),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .height(50.dp)
                        .width(150.dp)  // Add explicit width to create rectangle shape
                        .clip(RectangleShape)  // Apply rectangular clipping
                )
                Text(
                    "Logitech G develops award-winning wireless and wired gaming mice and keyboard. Constantly innovating from sensors to shape, find the right one for you."
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    CartScreen(rememberNavController(), "Mark")
}