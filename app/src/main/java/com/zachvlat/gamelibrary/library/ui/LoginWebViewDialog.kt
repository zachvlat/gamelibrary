package com.zachvlat.gamelibrary.library.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.JavascriptInterface
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
import androidx.compose.runtime.LaunchedEffect
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
    var scrapedGamesJson by remember { mutableStateOf<String?>(null) }
    var jsInjected by remember { mutableStateOf(false) }

    LaunchedEffect(scrapedGamesJson) {
        val json = scrapedGamesJson
        if (json != null && !codeHandled) {
            codeHandled = true
            onDismiss()
            onCodeReceived(json)
        }
    }

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

                                if (store == Store.ITCH) {
                                    class ItchBridge {
                                        @JavascriptInterface
                                        fun onGamesScraped(json: String) {
                                            scrapedGamesJson = json
                                        }
                                    }
                                    addJavascriptInterface(ItchBridge(), "AndroidItchBridge")
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onReceivedTitle(view: WebView, t: String?) {
                                        title = t ?: "$store Login"
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                        isLoading = true
                                        if (store == Store.ITCH && !url.startsWith("https://itch.io/my-purchases")) {
                                            jsInjected = false
                                        }
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
                                        if (store == Store.ITCH && url.startsWith("https://itch.io/my-purchases") && !jsInjected) {
                                            jsInjected = true
                                            val itchJs = """
                                                (function() {
                                                    if (window.__itchScrapingStarted) return;
                                                    window.__itchScrapingStarted = true;
                                                    let seen = new Map();
                                                    let lastCount = 0;
                                                    let sameCount = 0;
                                                    async function scan() {
                                                        document.querySelectorAll(".game_cell").forEach(function(cell) {
                                                            let id = cell.dataset.game_id;
                                                            let title = cell.querySelector(".game_title a")?.innerText?.trim();
                                                            let url = cell.querySelector(".game_title a")?.href;
                                                            let author = cell.querySelector(".game_author a")?.innerText?.trim();
                                                            let cover = cell.querySelector(".game_thumb img")?.src || cell.querySelector(".game_thumb img")?.dataset?.lazy_src;
                                                            if (id && !seen.has(id)) {
                                                                seen.set(id, { id: id, title: title, url: url, author: author, cover: cover });
                                                            }
                                                        });
                                                        window.scrollTo(0, document.body.scrollHeight);
                                                        let count = seen.size;
                                                        if (count === lastCount) { sameCount++; } else { sameCount = 0; }
                                                        lastCount = count;
                                                        if (sameCount >= 3) {
                                                            AndroidItchBridge.onGamesScraped(JSON.stringify(Array.from(seen.values())));
                                                            return;
                                                        }
                                                        setTimeout(scan, 1500);
                                                    }
                                                    scan();
                                                })();
                                            """.trimIndent()
                                            view.evaluateJavascript(itchJs, null)
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
                                            Store.ITCH -> null
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
