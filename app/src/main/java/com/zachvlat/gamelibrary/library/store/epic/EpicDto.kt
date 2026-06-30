package com.zachvlat.gamelibrary.library.store.epic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpicTokenResponse(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("expires_in") val expiresIn: Int = 0,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("account_id") val accountId: String? = null,
    @SerialName("token_type") val tokenType: String? = null
)

@Serializable
data class EpicLibraryResponse(
    val records: List<EpicLibraryRecord> = emptyList(),
    @SerialName("responseMetadata") val responseMetadata: EpicResponseMetadata? = null
)

@Serializable
data class EpicResponseMetadata(
    @SerialName("nextCursor") val nextCursor: String? = null
)

@Serializable
data class EpicLibraryRecord(
    val namespace: String = "",
    @SerialName("catalogItemId") val catalogItemId: String? = null,
    @SerialName("appName") val appName: String = "",
    val title: String = "",
    @SerialName("sandboxId") val sandboxId: String? = null
)

@Serializable
data class EpicCatalogResponse(
    val title: String? = null,
    val description: String? = null,
    val developer: String? = null,
    val developerId: String? = null,
    val keyImages: List<EpicKeyImage>? = null,
    val releaseDate: String? = null,
    val categories: List<EpicCategory>? = null,
    @SerialName("customAttributes") val customAttributes: Map<String, EpicCustomAttributeValue>? = null,
    @SerialName("technicalRequirements") val technicalRequirements: EpicTechnicalRequirements? = null
)

@Serializable
data class EpicKeyImage(
    val type: String? = null,
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class EpicCategory(
    val path: String? = null
)

@Serializable
data class EpicCustomAttributeValue(
    val type: String? = null,
    val value: String? = null
)

@Serializable
data class EpicTechnicalRequirements(
    val macos: List<EpicPlatformReq>? = null,
    val windows: List<EpicPlatformReq>? = null
)

@Serializable
data class EpicPlatformReq(
    val minimum: String? = null,
    val recommended: String? = null,
    val title: String? = null
)
