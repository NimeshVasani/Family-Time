package com.example.familytime.models

import android.location.Location
import com.google.firebase.database.Exclude
import java.io.Serializable

data class User(
    var name: String? = null,
    var email: String? = null,
    var uId: String? = null,
    var profilePic: String? = null,
    val families: MutableList<String>? = null,
    val friends: MutableList<String>? = null,
    val userPosts: MutableList<String>? = null,
    val pendingFriendRequest: MutableList<String>? = null,
    val pendingFamilyJoinReq: MutableList<String>? = null
) : Serializable {

    @Exclude
    var isAuthenticated: Boolean = false

    @Exclude
    var isNew: Boolean = false

    @Exclude
    var isCreated: Boolean = false
}
