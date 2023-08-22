package com.example.familytime.models

import java.io.Serializable

data class CommonForRequest(
    val uid: String,
    val profilePic: String,
    val name: String
) : Serializable, Comparable<CommonForRequest> {
    override fun compareTo(other: CommonForRequest): Int {
        return name.compareTo(other.name, ignoreCase = true)
    }


}