package com.zachvlat.gamelibrary.library.store.gog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GogTokenResponse(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("expires_in") val expiresIn: Long = 0,
    @SerialName("token_type") val tokenType: String = "",
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("session_id") val sessionId: String? = null
)

@Serializable
data class GogUserResponse(
    val id: String = "",
    val username: String = "",
    val email: String? = null,
    val avatar: String? = null
)

@Serializable
data class GogLibraryResponse(
    @SerialName("total_count") val totalCount: Int = 0,
    val items: List<GogLibraryItem> = emptyList(),
    @SerialName("next_page_token") val nextPageToken: String? = null
)

@Serializable
data class GogLibraryItem(
    @SerialName("platform_id") val platformId: String = "",
    @SerialName("external_id") val externalId: String = "",
    val certificate: String? = null,
    @SerialName("owned_since") val ownedSince: Long? = null
)

@Serializable
data class GogGameMetadata(
    val id: String = "",
    val title: Map<String, String> = emptyMap(),
    val summary: Map<String, String>? = null,
    val game: GogGameInfo? = null,
    @SerialName("supported_operating_systems") val supportedOperatingSystems: List<GogOsInfo>? = null,
    val type: String? = null,
    val etag: String? = null
)

@Serializable
data class GogGameInfo(
    val developers: List<GogDeveloper>? = null,
    val genres: List<GogGenre>? = null,
    @SerialName("releaseDate") val releaseDate: String? = null,
    @SerialName("first_release_date") val firstReleaseDate: String? = null,
    @SerialName("visible_in_library") val visibleInLibrary: Boolean? = null,
    val type: String? = null,
    val cover: GogImageUrl? = null,
    @SerialName("square_icon") val squareIcon: GogImageUrl? = null,
    val logo: GogImageUrl? = null,
    val background: GogImageUrl? = null,
    @SerialName("horizontal_artwork") val horizontalArtwork: GogImageUrl? = null
)

@Serializable
data class GogDeveloper(
    val name: String = ""
)

@Serializable
data class GogGenre(
    val name: Map<String, String> = emptyMap()
)

@Serializable
data class GogImageUrl(
    @SerialName("url_format") val urlFormat: String? = null
)

@Serializable
data class GogOsInfo(
    val slug: String = ""
)
