package com.zachvlat.gamelibrary.library.model

import kotlinx.serialization.Serializable

@Serializable
enum class Store {
    EPIC,
    GOG,
    AMAZON,
    STEAM,
    ITCH,
    EA,
    MANUAL
}
