package com.zachvlat.gamelibrary.library.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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

@Composable
fun ItchAutoSyncWebViewDialog(
    url: String,
    onDismiss: () -> Unit,
    onGamesScraped: (String) -> Unit
) {
    var result by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(result) {
        val json = result
        if (json != null) {
            onDismiss()
            onGamesScraped(json)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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

                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onGamesScraped(json: String) {
                                result = json
                            }
                        }, "AndroidItchBridge")

                        webChromeClient = WebChromeClient()

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                if (url.startsWith("https://itch.io/my-purchases")) {
                                    val itchJs = """
                                        (function() {
                                            if (window.__itchAutoSyncRunning) return;
                                            window.__itchAutoSyncRunning = true;
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
                                                    var result = { games: Array.from(seen.values()), purchasesUrl: window.location.href };
                                                    AndroidItchBridge.onGamesScraped(JSON.stringify(result));
                                                    return;
                                                }
                                                setTimeout(scan, 1500);
                                            }
                                            scan();
                                        })();
                                    """.trimIndent()
                                    view.evaluateJavascript(itchJs, null)
                                }
                            }
                        }

                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
