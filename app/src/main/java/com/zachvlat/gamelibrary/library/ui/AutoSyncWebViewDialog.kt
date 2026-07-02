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
fun AutoSyncWebViewDialog(
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
                        }, "AndroidSteamBridge")

                        webChromeClient = WebChromeClient()

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                            }

                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                if (url.contains("/games/")) {
                                    val steamJs = """
                                        (function() {
                                            if (window.__steamAutoSyncRunning) return;
                                            window.__steamAutoSyncRunning = true;
                                            var maxAttempts = 10;
                                            var attempt = 0;
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
                                                var cards = document.querySelectorAll("div.JeLbcWPaZDg-");
                                                if (cards.length === 0) {
                                                    cards = document.querySelectorAll(".gameListRowItem");
                                                }
                                                if (cards.length === 0 && attempt < maxAttempts) {
                                                    setTimeout(scrape, 2000);
                                                    return;
                                                }
                                                var games = [];
                                                for (var i = 0; i < cards.length; i++) {
                                                    games.push(parseCard(cards[i], i));
                                                }
                                                var result = { games: games, profileUrl: window.location.href };
                                                AndroidSteamBridge.onGamesScraped(JSON.stringify(result));
                                            }
                                            setTimeout(scrape, 2000);
                                        })();
                                    """.trimIndent()
                                    view.evaluateJavascript(steamJs, null)
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
