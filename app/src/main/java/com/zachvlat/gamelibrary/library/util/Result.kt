package com.zachvlat.gamelibrary.library.util

sealed class StoreResult<out T> {
    data class Success<T>(val data: T) : StoreResult<T>()
    data class Error(val code: ErrorCode, val message: String) : StoreResult<Nothing>()
}

enum class ErrorCode {
    AUTH_EXPIRED,
    AUTH_FAILED,
    NETWORK_ERROR,
    TIMEOUT,
    PARSING_ERROR,
    UNKNOWN
}
