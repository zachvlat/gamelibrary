package com.zachvlat.gamelibrary.library.store.amazon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AmazonRegisterRequest(
    @SerialName("auth_data") val authData: AmazonAuthData,
    @SerialName("registration_data") val registrationData: AmazonRegistrationData,
    @SerialName("requested_extensions") val requestedExtensions: List<String>,
    @SerialName("requested_token_type") val requestedTokenType: List<String>,
    @SerialName("user_context_map") val userContextMap: Map<String, String> = emptyMap()
)

@Serializable
data class AmazonAuthData(
    @SerialName("authorization_code") val authorizationCode: String,
    @SerialName("client_domain") val clientDomain: String = "DeviceLegacy",
    @SerialName("client_id") val clientId: String,
    @SerialName("code_algorithm") val codeAlgorithm: String = "SHA-256",
    @SerialName("code_verifier") val codeVerifier: String,
    @SerialName("use_global_authentication") val useGlobalAuthentication: Boolean = false
)

@Serializable
data class AmazonRegistrationData(
    @SerialName("app_name") val appName: String = "AGSLauncher for Windows",
    @SerialName("app_version") val appVersion: String = "1.0.0",
    @SerialName("device_model") val deviceModel: String = "Windows",
    @SerialName("device_name") val deviceName: String? = null,
    @SerialName("device_serial") val deviceSerial: String,
    @SerialName("device_type") val deviceType: String = "A2UMVHOX7UP4V7",
    @SerialName("domain") val domain: String = "Device",
    @SerialName("os_version") val osVersion: String = "10.0.19044.0"
)

@Serializable
data class AmazonRegisterResponse(
    val response: AmazonRegisterResponseData? = null
)

@Serializable
data class AmazonRegisterResponseData(
    val success: AmazonRegisterSuccess? = null
)

@Serializable
data class AmazonRegisterSuccess(
    val tokens: AmazonTokens? = null,
    val extensions: AmazonExtensions? = null
)

@Serializable
data class AmazonTokens(
    val bearer: AmazonBearerToken? = null
)

@Serializable
data class AmazonBearerToken(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("refresh_token") val refreshToken: String = "",
    @SerialName("expires_in") val expiresIn: Int = 0
)

@Serializable
data class AmazonExtensions(
    @SerialName("customer_info") val customerInfo: AmazonCustomerInfo? = null,
    @SerialName("device_info") val deviceInfo: AmazonDeviceInfo? = null
)

@Serializable
data class AmazonCustomerInfo(
    @SerialName("user_id") val userId: String = "",
    @SerialName("given_name") val givenName: String = ""
)

@Serializable
data class AmazonDeviceInfo(
    @SerialName("device_serial_number") val deviceSerialNumber: String = ""
)

@Serializable
data class AmazonRefreshRequest(
    @SerialName("source_token") val sourceToken: String,
    @SerialName("source_token_type") val sourceTokenType: String = "refresh_token",
    @SerialName("requested_token_type") val requestedTokenType: String = "access_token",
    @SerialName("app_name") val appName: String = "AGSLauncher for Windows",
    @SerialName("app_version") val appVersion: String = "1.0.0"
)

@Serializable
data class AmazonRefreshResponse(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("expires_in") val expiresIn: Int = 0
)

@Serializable
data class AmazonEntitlementsRequest(
    @SerialName("Operation") val operation: String = "GetEntitlements",
    @SerialName("clientId") val clientId: String = "Sonic",
    @SerialName("syncPoint") val syncPoint: String? = null,
    @SerialName("nextToken") val nextToken: String? = null,
    @SerialName("maxResults") val maxResults: Int = 50,
    @SerialName("productIdFilter") val productIdFilter: String? = null,
    @SerialName("keyId") val keyId: String = "d5dc8b8b-86c8-4fc4-ae93-18c0def5314d",
    @SerialName("hardwareHash") val hardwareHash: String
)

@Serializable
data class AmazonEntitlementsResponse(
    val entitlements: List<AmazonEntitlement> = emptyList(),
    @SerialName("nextToken") val nextToken: String? = null,
    @SerialName("syncPoint") val syncPoint: String? = null
)

@Serializable
data class AmazonEntitlement(
    val id: String = "",
    val product: AmazonProduct? = null
)

@Serializable
data class AmazonProduct(
    val id: String = "",
    val title: String = "",
    @SerialName("productDetail") val productDetail: AmazonProductDetail? = null
)

@Serializable
data class AmazonProductDetail(
    @SerialName("iconUrl") val iconUrl: String? = null,
    val details: AmazonDetails? = null
)

@Serializable
data class AmazonDetails(
    @SerialName("backgroundUrl1") val backgroundUrl1: String? = null,
    @SerialName("backgroundUrl2") val backgroundUrl2: String? = null,
    val developer: String? = null,
    val genres: List<String>? = null,
    @SerialName("logoUrl") val logoUrl: String? = null,
    val publisher: String? = null,
    @SerialName("releaseDate") val releaseDate: String? = null,
    @SerialName("shortDescription") val shortDescription: String? = null,
    val screenshots: List<String>? = null
)
