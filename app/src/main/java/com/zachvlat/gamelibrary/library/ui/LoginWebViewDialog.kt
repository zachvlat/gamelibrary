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
    var scrapedGamesJson by remember { mutableStateOf<String?>(null) }
    var eaScrapedJson by remember { mutableStateOf<String?>(null) }
    var ubisoftScrapedJson by remember { mutableStateOf<String?>(null) }
    var jsInjected by remember { mutableStateOf(false) }

    val ubisoftScrapeJs = """
        (function() {
            window.__ubisoftScrapingStarted = false;
            async function expandAndScrape() {
                // 1. Wait for game cards to appear (up to 20s)
                for (var w = 0; w < 20; w++) {
                    var c = document.querySelectorAll('[class*="GameCard_card"]').length;
                    console.log('[UBISOFT-SCRAPE] wait ' + w + ': ' + c + ' cards');
                    if (c > 0) break;
                    await new Promise(function(r) { setTimeout(r, 1000); });
                }
                // 2. Repeatedly click "See more" until gone or "See less" appears (max 30 clicks)
                for (var i = 0; i < 30; i++) {
                    var btn = null;
                    var buttons = document.querySelectorAll('button');
                    for (var j = 0; j < buttons.length; j++) {
                        var txt = buttons[j].textContent.trim().toLowerCase();
                        if (txt === 'see more' || txt === 'show more' || txt === 'load more' || txt === 'view more') {
                            btn = buttons[j];
                            break;
                        }
                    }
                    if (!btn) {
                        var allBtns = document.querySelectorAll('button, [role="button"]');
                        for (var k = 0; k < allBtns.length; k++) {
                            var bt = allBtns[k].textContent.trim().toLowerCase();
                            if (bt.includes('more') || bt.includes('load') || bt.includes('expand')) {
                                btn = allBtns[k];
                                console.log('[UBISOFT-SCRAPE] fallback: ' + bt);
                                break;
                            }
                        }
                    }
                    if (!btn) break;
                    var cardBefore = document.querySelectorAll('[class*="GameCard_card"]').length;
                    btn.click();
                    await new Promise(function(r) { setTimeout(r, 2000); });
                    var cardAfter = document.querySelectorAll('[class*="GameCard_card"]').length;
                    console.log('[UBISOFT-SCRAPE] clicked, cards: ' + cardBefore + ' -> ' + cardAfter);
                }
                // 3. Scrape all visible cards
                var games = [];
                document.querySelectorAll('[class*="GameCard_card"]').forEach(function(card) {
                    var nameEl = card.querySelector('[class*="GameCard_mobileHeading"]');
                    var imgEl = card.querySelector('img');
                    var stats = card.querySelectorAll('[class*="GameCard_gameCard"]');
                    var playTime = '';
                    var lastPlayed = '';
                    var platform = '';
                    stats.forEach(function(stat) {
                        var label = stat.querySelector('p:first-child');
                        var value = stat.querySelector('[class*="GameCard_mobileHeading"]');
                        if (label && value) {
                            var labelText = label.textContent.trim();
                            var valueText = value.textContent.trim();
                            if (labelText === 'Played time') playTime = valueText;
                            else if (labelText === 'Last played') lastPlayed = valueText;
                            else if (labelText === 'Platform') platform = valueText;
                        }
                    });
                    var name = nameEl ? nameEl.textContent.trim() : '';
                    var cover = imgEl ? imgEl.src : '';
                    if (name) {
                        games.push({ name: name, cover: cover, playTime: playTime, lastPlayed: lastPlayed, platform: platform });
                    }
                });
                console.log('[UBISOFT-SCRAPE] scraped ' + games.length + ' games');
                if (games.length > 0) {
                    AndroidUbisoftBridge.onGamesScraped(JSON.stringify(games));
                }
            }
            expandAndScrape();
        })();
    """.trimIndent()

    fun handleScrapedJson(json: String) {
        if (codeHandled) return
        codeHandled = true
        if (store == Store.ITCH || store == Store.EA || store == Store.UBISOFT) {
            CookieManager.getInstance().flush()
        }
        onDismiss()
        onCodeReceived(json)
    }

    LaunchedEffect(scrapedGamesJson) {
        val json = scrapedGamesJson
        if (json != null && !codeHandled) {
            handleScrapedJson(json)
        }
    }

    LaunchedEffect(eaScrapedJson) {
        val json = eaScrapedJson
        if (json != null && !codeHandled) {
            handleScrapedJson(json)
        }
    }

    LaunchedEffect(ubisoftScrapedJson) {
        val json = ubisoftScrapedJson
        if (json != null && !codeHandled) {
            handleScrapedJson(json)
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

                                when (store) {
                                    Store.ITCH -> {
                                        class ItchBridge {
                                            @JavascriptInterface
                                            fun onGamesScraped(json: String) {
                                                scrapedGamesJson = json
                                            }
                                        }
                                        addJavascriptInterface(ItchBridge(), "AndroidItchBridge")
                                    }
                                    Store.EA -> {
                                        class EaBridge {
                                            @JavascriptInterface
                                            fun onJsonReceived(json: String) {
                                                eaScrapedJson = json
                                            }
                                        }
                                        addJavascriptInterface(EaBridge(), "AndroidEaBridge")
                                    }
                                    Store.UBISOFT -> {
                                        class UbisoftBridge {
                                            @JavascriptInterface
                                            fun onGamesScraped(json: String) {
                                                ubisoftScrapedJson = json
                                            }
                                        }
                                        addJavascriptInterface(UbisoftBridge(), "AndroidUbisoftBridge")
                                    }
                                    else -> {}
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onReceivedTitle(view: WebView, t: String?) {
                                        title = t ?: "$store Login"
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                        isLoading = true
                                        when (store) {
                                            Store.ITCH -> {
                                                if (!url.startsWith("https://itch.io/my-purchases")) {
                                                    jsInjected = false
                                                }
                                            }
                                            Store.EA -> {
                                                jsInjected = false
                                            }
                                            Store.UBISOFT -> {
                                                jsInjected = false
                                            }
                                            else -> {}
                                        }
                                        tryHandleUrl(url, view)
                                    }

                                    override fun onPageFinished(view: WebView, url: String) {
                                        isLoading = false
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
                                        if (store == Store.EA && url.startsWith("https://myaccount.ea.com/am/data/1/order-history") && !jsInjected) {
                                            jsInjected = true
                                            val eaJs = """
                                                (function() {
                                                    try {
                                                        var text = document.body ? (document.body.innerText || document.body.textContent || '') : '';
                                                        if (text.length > 0) {
                                                            AndroidEaBridge.onJsonReceived(text);
                                                        }
                                                    } catch(e) { }
                                                })();
                                            """.trimIndent()
                                            view.evaluateJavascript(eaJs, null)
                                        }
                                        if (store == Store.UBISOFT && url.startsWith("https://www.ubisoft.com/en-gb/account/games-activity") && !jsInjected) {
                                            jsInjected = true
                                            view.evaluateJavascript(ubisoftScrapeJs, null)
                                        }
                                        tryHandleUrl(url, view)
                                    }

                                    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                                        if (store == Store.UBISOFT && url.startsWith("https://www.ubisoft.com/en-gb/account/games-activity") && !jsInjected) {
                                            jsInjected = true
                                            view.evaluateJavascript(ubisoftScrapeJs, null)
                                        }
                                        tryHandleUrl(url, view)
                                    }

                                    private fun tryHandleUrl(url: String, webView: WebView? = null) {
                                        if (codeHandled) return
                                        android.util.Log.d("LoginWebView", "[${store.name}] Nav: $url")
                                        val code = checkForAuthCode(url)
                                        if (code != null) {
                                            codeHandled = true
                                            onDismiss()
                                            onCodeReceived(code)
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
                                            Store.STEAM -> null
                                            Store.ITCH -> null
                                            Store.EA -> null
                                            Store.MANUAL -> null
                                            Store.UBISOFT -> null
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
