package com.example.familytime.models

import com.google.firebase.Timestamp
import java.io.Serializable

data class Posts(
    val sharedByUserName: String,
    val postUid: String,
    val createdBy: String,
    val postsUri: String? = null,
    val timestamp: Timestamp? = null,
    val description: String? = null,
    val location: String? = null
) : Serializable {
    constructor() : this("", "", "", null, null, null, null)
}
