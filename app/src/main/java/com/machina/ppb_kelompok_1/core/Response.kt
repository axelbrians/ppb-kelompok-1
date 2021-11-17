package com.machina.ppb_kelompok_1.core

sealed class Response<T>(val data: T? = null, val message: String? = null) {

    class Success<T> (data: T, message: String? = null): Response<T>(data, message)

    class Error<T> (message: String?, data: T? = null): Response<T> (data, message)

    class Loading<T>: Response<T>()

}
