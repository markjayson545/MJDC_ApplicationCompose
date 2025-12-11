package com.markjayson545.mjdc_applicationcompose.frontend

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

@Composable
fun WebviewLocalHostScreen(navController: NavController) {
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
//        var url = url.value.trim()
//
//        // Add http:// prefix if no protocol is specified
//        if (url.isNotEmpty() && !url.startsWith("http://") && !url.startsWith("https://")) {
//            url = "https://$url"
//        }
//
//        // Only load if URL is not empty
//        if (url.isNotEmpty()) {
//            webView.loadUrl(url)
//        }
        webView.loadUrl("http://192.168.0.192/DocuTracker/")
    })
}