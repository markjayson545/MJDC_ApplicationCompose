package com.markjayson545.mjdc_applicationcompose.frontend

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WebviewScreen(navController: NavController) {
    val urlTextFieldValue = remember { mutableStateOf("") }
    val url = remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize()) {
        Column {
//            Row(
//                modifier = Modifier.padding(start = 10.dp, top = 15.dp, end = 10.dp),
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.Search,
//                    contentDescription = null
//                )
//                Text(
//                    "Search: ",
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                value = urlTextFieldValue.value,
                onValueChange = {
                    urlTextFieldValue.value = it
                },
                label = {
                    Text("Enter url to load...")
                },
                placeholder = {
                    Text("e.g., https://www.google.com/")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ManageSearch,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            url.value = urlTextFieldValue.value
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                )
            )

            if (url.value.isNotEmpty()) {
                AndroidView(factory = {
                    WebView(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                    }
                }, update = { webView ->
                    var url = url.value.trim()

                    // Add http:// prefix if no protocol is specified
                    if (url.isNotEmpty() && !url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://$url"
                    }

                    // Only load if URL is not empty
                    if (url.isNotEmpty()) {
                        webView.loadUrl(url)
                    }
                })
            } else {
                Box(
                    Modifier.fillMaxSize()
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ManageSearch,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                                .size(50.dp)
                        )
                        Text(
                            "Enter a URL to load",
                            textAlign = TextAlign.Center
                        )
                    }

                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun WebviewScreenPreview() {
    WebviewScreen(rememberNavController())
}