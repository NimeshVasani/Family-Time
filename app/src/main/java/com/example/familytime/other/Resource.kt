package com.example.familytime.other

import javax.annotation.Nullable

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(@Nullable data: T?) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}