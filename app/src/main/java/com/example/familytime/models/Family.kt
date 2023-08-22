package com.example.familytime.models

import java.io.Serializable

data class Family(
    val familyID: String,
    val members: MutableList<String>,
    val familyName: String,
    val familyLocation: String? = null,
    val familyDescription: String? = null,
    val familyPosts: MutableList<String>? = null,
    val familyProfilePicId: String? = null,
    val pendingJoinRequests: MutableList<String>? = null
) : Serializable ,Comparable<Family>{
    constructor() : this("", mutableListOf(), "", null, null, null, null, null)

    override fun compareTo(other: Family): Int {
        return familyName.compareTo(other.familyName, ignoreCase = true)
    }

}
