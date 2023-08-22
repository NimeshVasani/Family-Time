package com.example.familytime.models

import com.google.firebase.Timestamp
import java.io.Serializable

data class UserChatting(
    val sender: String,
    val receiver: String,
    val time: String? = null,
    val isRead: Boolean = false,
    val type: String? = null,
    val senderProfilePic: String? = null,
    val message: String? = null,
    val imageId: String? = null,
    val audioId: String? = null,
    val videoId: String? = null,
    val otherFilesId: String? = null
) : Serializable {
    constructor() : this("", "", null, false, null, null, null, null, null, null)

}
