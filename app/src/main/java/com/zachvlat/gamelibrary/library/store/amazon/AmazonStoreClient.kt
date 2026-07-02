package com.zachvlat.gamelibrary.library.store.amazon

import android.util.Base64
import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import com.zachvlat.gamelibrary.library.util.AmazonConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class AmazonStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    override val store: Store = Store.AMAZON

    private var currentSerial: String? = null
    private var currentVerifier: String? = null

    override suspend fun isLoggedIn(): Boolean {
        return tokenStorage.getToken(store.name, "access_token") != null
    }

    override suspend fun getLoginData(): LoginData {
        val serial = UUID.randomUUID().toString().replace("-", "").uppercase()
        currentSerial = serial

        val clientIdRaw = "${serial}#${AmazonConstants.DEVICE_TYPE}"
        val clientId = clientIdRaw.encodeToByteArray().joinToString("") { "%02x".format(it) }

        val verifierBytes = ByteArray(32)
        SecureRandom().nextBytes(verifierBytes)
        val codeVerifier = Base64.encodeToString(verifierBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        currentVerifier = codeVerifier

        val digest = MessageDigest.getInstance("SHA-256")
        val challengeBytes = digest.digest(codeVerifier.toByteArray(Charsets.UTF_8))
        val codeChallenge = Base64.encodeToString(challengeBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

        tokenStorage.saveToken(store.name, "code_verifier", codeVerifier)
        tokenStorage.saveToken(store.name, "serial", serial)
        tokenStorage.saveToken(store.name, "client_id", clientId)

        println("[Amazon] Serial: $serial")
        println("[Amazon] ClientId (hex): $clientId")
        println("[Amazon] Verifier: $codeVerifier")
        println("[Amazon] Challenge: $codeChallenge")

        val authUrl = buildString {
            append(AmazonConstants.AUTH_BASE_URL)
            append("?openid.ns=http://specs.openid.net/auth/2.0")
            append("&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select")
            append("&openid.identity=http://specs.openid.net/auth/2.0/identifier_select")
            append("&openid.mode=checkid_setup")
            append("&openid.oa2.scope=device_auth_access")
            append("&openid.ns.oa2=http://www.amazon.com/ap/ext/oauth/2")
            append("&openid.oa2.response_type=code")
            append("&openid.oa2.code_challenge_method=S256")
            append("&openid.oa2.client_id=device:${URLEncoder.encode(clientId, "UTF-8")}")
            append("&language=en_US")
            append("&marketPlaceId=${AmazonConstants.MARKETPLACE_ID}")
            append("&openid.return_to=https://www.amazon.com")
            append("&openid.pape.max_auth_age=0")
            append("&openid.assoc_handle=${AmazonConstants.ASSOC_HANDLE}")
            append("&pageId=${AmazonConstants.ASSOC_HANDLE}")
            append("&openid.oa2.code_challenge=$codeChallenge")
        }

        println("[Amazon] Auth URL: $authUrl")
        return LoginData(url = authUrl)
    }

    override suspend fun completeLogin(authCode: String): Boolean {
        val code = if (authCode.contains("openid.oa2.authorization_code=")) {
            val params = authCode.split("?").lastOrNull()?.split("&") ?: listOf(authCode)
            params.firstOrNull { it.startsWith("openid.oa2.authorization_code=") }
                ?.removePrefix("openid.oa2.authorization_code=") ?: authCode
        } else {
            authCode
        }

        val codeVerifier = tokenStorage.getToken(store.name, "code_verifier")
            ?: return false
        val serial = tokenStorage.getToken(store.name, "serial")
            ?: return false
        val clientId = tokenStorage.getToken(store.name, "client_id")
            ?: return false

        return try {
            val registerRequest = AmazonRegisterRequest(
                authData = AmazonAuthData(
                    authorizationCode = code,
                    clientId = clientId,
                    codeVerifier = codeVerifier
                ),
                registrationData = AmazonRegistrationData(
                    deviceSerial = serial
                ),
                requestedExtensions = listOf("customer_info", "device_info"),
                requestedTokenType = listOf("bearer", "mac_dms")
            )

            val response: HttpResponse = httpClient.post(AmazonConstants.REGISTER_URL) {
                contentType(ContentType.Application.Json)
                setBody(registerRequest)
            }

            if (!response.status.isSuccess()) return false

            val registerResponse: AmazonRegisterResponse = response.body()
            val bearer = registerResponse.response?.success?.tokens?.bearer
                ?: return false

            val deviceSerial = registerResponse.response?.success?.extensions?.deviceInfo?.deviceSerialNumber
            if (deviceSerial != null) {
                tokenStorage.saveToken(store.name, "device_serial", deviceSerial)
            }

            tokenStorage.saveToken(store.name, "access_token", bearer.accessToken)
            tokenStorage.saveToken(store.name, "refresh_token", bearer.refreshToken)
            tokenStorage.saveToken(store.name, "expires_in", bearer.expiresIn.toString())
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun refreshLibrary(): List<GameInfo> {
        val accessToken = tokenStorage.getToken(store.name, "access_token")
            ?: throw IllegalStateException("Not authenticated with Amazon")

        return try {
            val serial = tokenStorage.getToken(store.name, "device_serial")
                ?: tokenStorage.getToken(store.name, "serial") ?: ""
            val hardwareHash = MessageDigest.getInstance("SHA-256")
                .digest(serial.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
                .uppercase()

            fetchAndMapEntitlements(accessToken, hardwareHash)
        } catch (e: Exception) {
            val refreshed = tryRefreshToken()
            if (refreshed) {
                val newToken = tokenStorage.getToken(store.name, "access_token") ?: throw e
                val serial = tokenStorage.getToken(store.name, "device_serial")
                    ?: tokenStorage.getToken(store.name, "serial") ?: ""
                val hardwareHash = MessageDigest.getInstance("SHA-256")
                    .digest(serial.toByteArray(Charsets.UTF_8))
                    .joinToString("") { "%02x".format(it) }
                    .uppercase()
                fetchAndMapEntitlements(newToken, hardwareHash)
            } else {
                throw e
            }
        }
    }

    override suspend fun logout() {
        tokenStorage.clearTokens(store.name)
    }

    private suspend fun tryRefreshToken(): Boolean {
        val refreshToken = tokenStorage.getToken(store.name, "refresh_token") ?: return false
        return try {
            val request = AmazonRefreshRequest(
                sourceToken = refreshToken
            )
            val response: HttpResponse = httpClient.post(AmazonConstants.TOKEN_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (!response.status.isSuccess()) return false
            val refreshResponse: AmazonRefreshResponse = response.body()
            tokenStorage.saveToken(store.name, "access_token", refreshResponse.accessToken)
            tokenStorage.saveToken(store.name, "expires_in", refreshResponse.expiresIn.toString())
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun fetchAndMapEntitlements(
        accessToken: String,
        hardwareHash: String
    ): List<GameInfo> {
        val games = mutableListOf<GameInfo>()
        var nextToken: String? = null
        var syncPoint: String? = null
        var page = 1

        do {
            println("[Amazon] Fetching entitlements page $page...")
            val request = AmazonEntitlementsRequest(
                syncPoint = syncPoint,
                nextToken = nextToken,
                hardwareHash = hardwareHash
            )

            val response: HttpResponse = httpClient.post(AmazonConstants.ENTITLEMENTS_URL) {
                header("X-Amz-Target", "com.amazon.animusdistributionservice.entitlement.AnimusEntitlementsService.GetEntitlements")
                header("x-amzn-token", accessToken)
                header("UserAgent", "com.amazon.agslauncher.win/3.0.9202.1")
                header("Content-Encoding", "amz-1.0")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (!response.status.isSuccess()) {
                throw IllegalStateException("Failed to fetch entitlements: ${response.status}")
            }

            val entitlementsResponse: AmazonEntitlementsResponse = response.body()
            val pageGames = mutableListOf<GameInfo>()

            for (entitlement in entitlementsResponse.entitlements) {
                val product = entitlement.product ?: continue
                val detail = product.productDetail
                val details = detail?.details
                val keyImages = mutableMapOf<String, String>().apply {
                    detail?.iconUrl?.let { put("icon", it) }
                    details?.backgroundUrl1?.let { put("background", it) }
                    details?.logoUrl?.let { put("logo", it) }
                }.ifEmpty { null }

                pageGames.add(
                    GameInfo(
                        store = Store.AMAZON,
                        appName = product.id,
                        title = product.title.ifBlank { "Unknown" },
                        developer = details?.developer,
                        description = details?.shortDescription,
                        artCover = details?.backgroundUrl2,
                        artSquare = keyImages?.get("icon"),
                        artLogo = keyImages?.get("logo"),
                        artBackground = keyImages?.get("background"),
                        releaseDate = details?.releaseDate,
                        genres = details?.genres?.takeIf { it.isNotEmpty() },
                        canRunOffline = false,
                        storeUrl = null,
                        isLinuxNative = false,
                        isMacNative = false
                    )
                )
            }

            games.addAll(pageGames)
            println("[Amazon] Page $page: ${pageGames.size} entitlements (total: ${games.size})")
            nextToken = entitlementsResponse.nextToken
            syncPoint = entitlementsResponse.syncPoint
            page++
        } while (nextToken != null)

        println("[Amazon] Done — ${games.size} games loaded")
        return games
    }
}
