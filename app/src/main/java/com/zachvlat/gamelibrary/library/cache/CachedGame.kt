package com.zachvlat.gamelibrary.library.cache

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_games")
data class CachedGame(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "store")
    val store: String,

    @ColumnInfo(name = "app_name")
    val appName: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "developer")
    val developer: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "art_cover")
    val artCover: String?,

    @ColumnInfo(name = "art_square")
    val artSquare: String?,

    @ColumnInfo(name = "art_logo")
    val artLogo: String?,

    @ColumnInfo(name = "art_background")
    val artBackground: String?,

    @ColumnInfo(name = "release_date")
    val releaseDate: String?,

    @ColumnInfo(name = "genres")
    val genres: String?,

    @ColumnInfo(name = "can_run_offline")
    val canRunOffline: Boolean,

    @ColumnInfo(name = "store_url")
    val storeUrl: String?,

    @ColumnInfo(name = "is_linux_native")
    val isLinuxNative: Boolean,

    @ColumnInfo(name = "is_mac_native")
    val isMacNative: Boolean,

    @ColumnInfo(name = "last_fetched_at")
    val lastFetchedAt: Long
)
