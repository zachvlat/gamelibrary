package com.zachvlat.gamelibrary.library.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zachvlat.gamelibrary.library.store.steam.SteamConstants
import com.zachvlat.gamelibrary.library.model.Store

@Composable
fun LoginWebViewDialog(
    store: Store,
    authUrl: String,
    onDismiss: () -> Unit,
    onCodeReceived: (String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("$store Login") }
    var codeHandled by remember { mutableStateOf(false) }
    var steamPendingCode by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(min = 300.dp, max = 800.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AndroidView(
                        factory = { context ->
                            @SuppressLint("SetJavaScriptEnabled")
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.userAgentString = settings.userAgentString + " GameLibrary/1.0"

                                webChromeClient = object : WebChromeClient() {
                                    override fun onReceivedTitle(view: WebView, t: String?) {
                                        title = t ?: "$store Login"
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                        isLoading = true
                                        tryHandleUrl(url, view)
                                    }

                                    override fun onPageFinished(view: WebView, url: String) {
                                        isLoading = false
                                        if (store == Store.STEAM && steamPendingCode != null && url.startsWith("https://store.steampowered.com")) {
                                            val code = steamPendingCode!!
                                            steamPendingCode = null
                                            codeHandled = true
                                            onDismiss()
                                            onCodeReceived(code)
                                            return
                                        }
                                        tryHandleUrl(url, view)
                                    }

                                    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                                        tryHandleUrl(url, view)
                                    }

                                    private fun tryHandleUrl(url: String, webView: WebView? = null) {
                                        if (codeHandled) return
                                        android.util.Log.d("LoginWebView", "[${store.name}] Nav: $url")
                                        val code = checkForAuthCode(url)
                                        if (code != null) {
                                            if (store == Store.STEAM) {
                                                if (steamPendingCode == null) {
                                                    steamPendingCode = code
                                                    webView?.loadUrl("https://store.steampowered.com")
                                                }
                                            } else {
                                                codeHandled = true
                                                onDismiss()
                                                onCodeReceived(code)
                                            }
                                        } else if (store == Store.EPIC && url.contains("/id/api/redirect")) {
                                            tryReadEpicCodeFromPage(webView)
                                        }
                                    }

                                    private fun tryReadEpicCodeFromPage(webView: WebView?) {
                                        if (webView == null) return
                                        val js = """
                                            (function(){
                                              try {
                                                var el = document.body || document.documentElement;
                                                var text = el ? (el.innerText || el.textContent || '') : '';
                                                return JSON.parse(text).authorizationCode || '';
                                              } catch(e) { return ''; }
                                            })()
                                        """.trimIndent()
                                        webView.evaluateJavascript(js) { result ->
                                            if (codeHandled || result == null || result == "null" || result.length < 2) return@evaluateJavascript
                                            val code = result.substring(1, result.length - 1)
                                            if (code.isNotEmpty()) {
                                                android.util.Log.d("LoginWebView", "[${store.name}] Got code from page body")
                                                codeHandled = true
                                                onDismiss()
                                                onCodeReceived(code)
                                            }
                                        }
                                    }

                                    private fun checkForAuthCode(url: String): String? {
                                        val params = url.split("?").lastOrNull()?.split("&") ?: return null
                                        return when (store) {
                                            Store.GOG -> {
                                                if (url.contains("embed.gog.com/on_login_success")) {
                                                    params.firstOrNull { it.startsWith("code=") }
                                                        ?.removePrefix("code=")
                                                } else null
                                            }
                                            Store.EPIC -> {
                                                params.firstOrNull { it.startsWith("authorizationCode=") }
                                                    ?.removePrefix("authorizationCode=")
                                            }
                                            Store.AMAZON -> {
                                                params.firstOrNull { it.startsWith("openid.oa2.authorization_code=") }
                                                    ?.removePrefix("openid.oa2.authorization_code=")
                                            }
                                            Store.STEAM -> {
                                                if (url.startsWith(SteamConstants.REDIRECT_URL.split("?").first())) {
                                                    params.firstOrNull { it.startsWith("openid.claimed_id=") }
                                                        ?.removePrefix("openid.claimed_id=")
                                                        ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                                                } else null
                                            }
                                        }
                                    }
                                }

                                loadUrl(authUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
