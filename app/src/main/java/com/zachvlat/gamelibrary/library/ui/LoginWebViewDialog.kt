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
    var steamScrapedGamesJson by remember { mutableStateOf<String?>(null) }
    var jsInjected by remember { mutableStateOf(false) }

    fun handleScrapedJson(json: String) {
        if (codeHandled) return
        codeHandled = true
        if (store == Store.STEAM || store == Store.ITCH) {
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

    LaunchedEffect(steamScrapedGamesJson) {
        val json = steamScrapedGamesJson
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
                                settings.userAgentString = settings.userAgentString + " GameLibrary/1.0"

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
                                    Store.STEAM -> {
                                        class SteamBridge {
                                            @JavascriptInterface
                                            fun onGamesScraped(json: String) {
                                                steamScrapedGamesJson = json
                                            }
                                        }
                                        addJavascriptInterface(SteamBridge(), "AndroidSteamBridge")
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
                                            Store.STEAM -> {
                                                if (!url.contains("/games/")) {
                                                    jsInjected = false
                                                }
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
                                        if (store == Store.STEAM && url.contains("/games/") && !jsInjected) {
                                            jsInjected = true
                                            val steamJs = """
                                                (function() {
                                                    var maxAttempts = 10;
                                                    var attempt = 0;
                                                    var allGames = [];
                                                    function findLinks(card, keyword) {
                                                        var links = card.querySelectorAll("a");
                                                        for (var j = 0; j < links.length; j++) {
                                                            if (links[j].href.indexOf(keyword) !== -1) return links[j].href;
                                                        }
                                                        return null;
                                                    }
                                                    function parseCard(card, index) {
                                                        var storeLink = card.querySelector('a[href*="store.steampowered.com/app/"]');
                                                        var storeUrl = storeLink ? storeLink.href : null;
                                                        var anyAppLink = card.querySelector('a[href*="/app/"]');
                                                        var appIdMatch = anyAppLink ? anyAppLink.href.match(/\/app\/(\d+)/) : null;
                                                        var appId = appIdMatch ? appIdMatch[1] : null;
                                                        var name = null;
                                                        var nameLink = card.querySelector('a[href*="/app/"]');
                                                        if (nameLink) {
                                                            name = nameLink.textContent ? nameLink.textContent.trim() : null;
                                                        }
                                                        var imgEl = card.querySelector("img");
                                                        if (!name && imgEl) {
                                                            name = imgEl.alt || imgEl.title || null;
                                                        }
                                                        var pictureImg = card.querySelector("picture img");
                                                        var pictureSource = card.querySelector("picture source");
                                                        var headerImage = pictureImg ? pictureImg.src : (imgEl ? imgEl.src : null);
                                                        var libraryImage = pictureSource ? pictureSource.srcset : null;
                                                        var achievementLink = card.querySelector('a[href*="tab=achievements"]');
                                                        var achievementText = null;
                                                        if (achievementLink && achievementLink.parentElement) {
                                                            var sp = achievementLink.parentElement.querySelector("span");
                                                            if (sp) achievementText = sp.textContent.trim();
                                                        }
                                                        var earned = null;
                                                        var total = null;
                                                        if (achievementText && achievementText.indexOf("/") !== -1) {
                                                            var x = achievementText.split("/");
                                                            earned = parseInt(x[0], 10);
                                                            total = parseInt(x[1], 10);
                                                        }
                                                        var progressBar = card.querySelector('[style*="--percent"]');
                                                        var percent = progressBar ? parseFloat(progressBar.style.getPropertyValue("--percent")) : null;
                                                        var user = null;
                                                        if (achievementLink) {
                                                            var u = achievementLink.href.match(/steamcommunity\.com\/(?:id|profiles)\/([^/]+)/);
                                                            user = u ? u[1] : null;
                                                        }
                                                        return {
                                                            appId: appId || ("game_" + index),
                                                            name: name,
                                                            storeUrl: storeUrl,
                                                            headerImage: headerImage,
                                                            libraryImage: libraryImage,
                                                            achievements: { earned: earned, total: total, percent: percent },
                                                            community: { user: user, myAchievements: findLinks(card, "tab=achievements"), globalAchievements: findLinks(card, "/stats/"), groups: findLinks(card, "/search/groups/") },
                                                            links: { forums: findLinks(card, "/forum/"), officialWebsite: findLinks(card, "/appofficialsite/"), news: findLinks(card, "/news/") }
                                                        };
                                                    }
                                                    function scrape() {
                                                        attempt++;
                                                        console.log("[GameShelf Steam] Attempt " + attempt + "/" + maxAttempts);
                                                        var cards = document.querySelectorAll("div.JeLbcWPaZDg-");
                                                        if (cards.length === 0) {
                                                            cards = document.querySelectorAll(".gameListRowItem");
                                                        }
                                                        if (cards.length === 0 && attempt < maxAttempts) {
                                                            setTimeout(scrape, 2000);
                                                            return;
                                                        }
                                                        console.log("[GameShelf Steam] Found " + cards.length + " cards");
                                                        var games = [];
                                                        for (var i = 0; i < cards.length; i++) {
                                                            games.push(parseCard(cards[i], i));
                                                        }
                                                        console.log("[GameShelf Steam] Scraped " + games.length + " games");
                                                        var result = { games: games, profileUrl: window.location.href };
                                                        AndroidSteamBridge.onGamesScraped(JSON.stringify(result));
                                                    }
                                                    setTimeout(scrape, 2000);
                                                })();
                                            """.trimIndent()
                                            view.evaluateJavascript(steamJs, null)
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
