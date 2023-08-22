package com.example.familytime.repositories.firestore

import androidx.lifecycle.MutableLiveData
import com.example.familytime.models.Family
import com.example.familytime.models.Posts
import com.example.familytime.models.User
import com.example.familytime.other.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class FireStoreRepository @Inject constructor(
    firebaseAuth: FirebaseAuth,
    private val fireStore: FirebaseFirestore
) {
    private val currentUser = firebaseAuth.currentUser


    fun addIntoUsersList(user: User): MutableLiveData<Resource<Boolean>> {
        val returnData: MutableLiveData<Resource<Boolean>> = MutableLiveData()
        returnData.value = Resource.Loading()

        fireStore.collection("users").document(user.uId!!)
            .set(user)
            .addOnCompleteListener { task ->
                if (task.isComplete) {
                    if (task.isSuccessful) {
                        returnData.value = Resource.Success(true)
                    } else {
                        returnData.value = Resource.Error(task.exception?.message.toString())
                    }
                } else {
                    returnData.value = Resource.Error(task.exception?.message.toString())
                }

            }
        return returnData
    }

    fun getUserList(): MutableLiveData<Resource<MutableList<User>>> {

        val userList: MutableLiveData<Resource<MutableList<User>>> = MutableLiveData()
        val list: MutableList<User> = mutableListOf()
        userList.value = Resource.Loading()

        if (currentUser != null) {
            fireStore.collection("users").get().addOnSuccessListener { querySnapshot ->
                for (user in querySnapshot) {
                    val uid = user.id
                    val name = user.getString("name")
                    val email = user.getString("email")
                    list.add(
                        User(
                            name = name.toString(),
                            email = email.toString(),
                            uId = uid
                        )
                    )
                }

                list.remove(
                    User(
                        currentUser.displayName!!,
                        currentUser.email!!,
                        currentUser.uid
                    )
                )
                userList.value = Resource.Success(list)
            }.addOnFailureListener {
                userList.value = Resource.Error(it.message.toString())
            }
        }
        return userList
    }

    fun searchUserByName(userName: String): MutableLiveData<Resource<MutableList<User>>> {
        val userList: MutableLiveData<Resource<MutableList<User>>> = getUserList()
        val filteredUsersList: MutableLiveData<Resource<MutableList<User>>> = MutableLiveData()
        val list: MutableList<User> = mutableListOf()
        filteredUsersList.value = Resource.Loading()

        userList.observeForever { resource ->
            when (resource) {
                is Resource.Success -> {
                    for (user in userList.value!!.data!!) {
                        if (user.name == userName) {
                            list.add(user)
                        }
                    }
                    filteredUsersList.value = Resource.Success(list)
                }

                is Resource.Error -> {
                    filteredUsersList.value = Resource.Error(userList.value!!.message.toString())
                }

                is Resource.Loading -> {
                    filteredUsersList.value = Resource.Loading()
                }

                else -> {}
            }
        }
        return filteredUsersList

    }


    fun saveFamilies(family: Family): MutableLiveData<Resource<Family>> {
        val returnData: MutableLiveData<Resource<Family>> = MutableLiveData()
        returnData.value = Resource.Loading()

        fireStore.collection("families").document(family.familyID)
            .set(family)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    returnData.value = Resource.Success(family)
                } else {
                    returnData.value = Resource.Error(task.exception?.message.toString())
                }

            }

        return returnData
    }

    fun searchUserByUserId(userId: String): MutableLiveData<Resource<User>> {
        val filteredUsersList: MutableLiveData<Resource<User>> = MutableLiveData()
        filteredUsersList.value = Resource.Loading()
        fireStore.collection("users").document(userId).get().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                val user: User = task.result.toObject(User::class.java)!!
                filteredUsersList.value = Resource.Success(user)

            } else {
                filteredUsersList.value = Resource.Error(task.exception?.message.toString())
            }
        }

        return filteredUsersList
    }


    fun getCurrentUser(): MutableLiveData<Resource<User>> {


        val returnData: MutableLiveData<Resource<User>> = MutableLiveData()
        if (currentUser != null) {
            searchUserByUserId(currentUser.uid).observeForever { resources ->
                when (resources) {
                    is Resource.Success -> {
                        returnData.value = Resource.Success(resources.data!!)
                    }

                    is Resource.Error -> {
                        returnData.value = Resource.Error(resources.message.toString())

                    }

                    is Resource.Loading -> {
                        returnData.value = Resource.Loading()
                    }

                }

            }

        } else {
            returnData.value = Resource.Error("something went Wrong")
        }
        return returnData
    }

    fun addFamilyIntoUserDocument(
        familyId: String
    ): MutableLiveData<Resource<Boolean>> {
        val returnData: MutableLiveData<Resource<Boolean>> = MutableLiveData()
        val currentUser: MutableLiveData<Resource<User>> = getCurrentUser()
        returnData.value = Resource.Loading()
        currentUser.observeForever { resource ->
            when (resource) {
                is Resource.Success -> {
                    val userId = resource.data?.uId!!
                    fireStore.collection("users").document(userId)
                        .update("families", FieldValue.arrayUnion(familyId))
                        .addOnCompleteListener { task ->
                            if (task.isComplete) {
                                if (task.isSuccessful) {
                                    returnData.value =
                                        Resource.Success(true)
                                } else {

                                    returnData.value =
                                        Resource.Error(task.exception?.message.toString())
                                }
                            }
                        }
                }

                is Resource.Error -> {
                    returnData.value =
                        Resource.Error(resource.message.toString())
                }

                is Resource.Loading -> {
                    returnData.value = Resource.Loading()
                }
            }

        }

        return returnData
    }


    fun getFamilyById(familyId: String): MutableLiveData<Resource<Family>> {
        val returnData: MutableLiveData<Resource<Family>> = MutableLiveData()
        returnData.value = Resource.Loading()
        fireStore.collection("families").document(familyId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Convert the documentSnapshot to a Family object
                    val family: Family? = documentSnapshot.toObject(Family::class.java)

                    if (family != null) {
                        returnData.value = Resource.Success(family)
                    } else {
                        // Handle the case where the family document is null or conversion fails
                        returnData.value = Resource.Error("Failed to convert Family data")
                    }
                } else {
                    // Handle the case where the family document does not exist
                    returnData.value = Resource.Error("Family not found")
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while fetching the family document
                returnData.value = Resource.Error(exception.message.toString())
            }

        return returnData
    }

    fun shareNewPosts(posts: Posts): MutableLiveData<Resource<String>> {
        val returnData: MutableLiveData<Resource<String>> = MutableLiveData()

        fireStore.collection("posts").document(posts.postUid).set(posts)
            .addOnCompleteListener { task ->
                returnData.value = Resource.Loading()
                if (task.isSuccessful) {
                    returnData.value = Resource.Success(posts.postUid)
                } else {
                    returnData.value = Resource.Error(task.exception?.message.toString())
                }

            }
        return returnData
    }

    fun updatePostUid(
        postsId: String,
        families: MutableList<String>
    ): MutableLiveData<Resource<Boolean>> {
        val returnData: MutableLiveData<Resource<Boolean>> = MutableLiveData()
        val currentUser: MutableLiveData<Resource<User>> = getCurrentUser()
        returnData.value = Resource.Loading()
        currentUser.observeForever { resources ->
            when (resources) {
                is Resource.Success -> {
                    val userId = resources.data?.uId!!
                    fireStore.collection("users").document(userId)
                        .update("userPosts", FieldValue.arrayUnion(postsId))
                        .addOnCompleteListener { task ->
                            if (task.isComplete) {
                                if (task.isSuccessful) {
                                    if (families.isNotEmpty()) {
                                        for (family in families) {
                                            fireStore.collection("families").document(family)
                                                .update(
                                                    "familyPosts",
                                                    FieldValue.arrayUnion(postsId)
                                                )
                                                .addOnCompleteListener {
                                                    returnData.value = Resource.Success(true)
                                                }
                                        }
                                    } else {
                                        returnData.value = Resource.Success(true)
                                    }
                                } else {
                                    returnData.value =
                                        Resource.Error(task.exception?.message.toString())
                                }
                            }
                        }
                }

                is Resource.Error -> {
                    returnData.value =
                        Resource.Error(resources.message.toString())
                }

                is Resource.Loading -> {

                    returnData.value =
                        Resource.Loading()
                }
            }
        }
        return returnData
    }


    fun getAllFamiliesPostsIds(familiesList: MutableList<String>): MutableLiveData<Resource<MutableList<String>>> {
        val listOfPosts: MutableList<String> = mutableListOf()
        val returnData: MutableLiveData<Resource<MutableList<String>>> = MutableLiveData()
        if (familiesList.isNotEmpty()) {
            for (familyId in familiesList) {
                getFamilyById(familyId).observeForever { resources ->
                    when (resources) {
                        is Resource.Success -> {
                            if (!resources.data!!.familyPosts.isNullOrEmpty()) {
                                val postsId: MutableList<String> = resources.data.familyPosts!!
                                listOfPosts.addAll(postsId)
                                returnData.value = Resource.Success(listOfPosts)
                            }
                        }

                        is Resource.Error -> {
                            returnData.value =
                                Resource.Error(resources.message.toString())
                        }

                        is Resource.Loading -> {
                            returnData.value =
                                Resource.Loading()
                        }

                    }
                }
            }
        }

        return returnData
    }

    fun getPostsByTimeStamp(postIds: MutableList<String>): MutableLiveData<Resource<MutableList<Posts>>> {
        val resultLiveData = MutableLiveData<Resource<MutableList<Posts>>>()

        // Initialize an empty list to store the retrieved posts
        val postsList = mutableListOf<Posts>()

        // Loop through the list of post IDs
        resultLiveData.value = Resource.Loading()
        for (postId in postIds) {
            fireStore.collection("posts").document(postId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val post = documentSnapshot.toObject(Posts::class.java)
                    if (post != null) {
                        postsList.add(post)
                    }

                    // If all posts are retrieved, sort and emit the result
                    if (postsList.size == postIds.size) {
                        val sortedPostsList = postsList.sortedByDescending { it.timestamp }.toSet()
                        resultLiveData.value = Resource.Success(sortedPostsList.toMutableList())
                    }
                }
                .addOnFailureListener { exception ->
                    resultLiveData.value = Resource.Error(exception.message.toString())
                }
        }

        return resultLiveData
    }

    fun getSpecificFamilyPost(familyId: String): MutableLiveData<Resource<MutableList<String>>> {
        val listOfPosts: MutableList<String> = mutableListOf()
        val returnData: MutableLiveData<Resource<MutableList<String>>> = MutableLiveData()
        getFamilyById(familyId).observeForever { resources ->
            when (resources) {
                is Resource.Success -> {
                    if (!resources.data!!.familyPosts.isNullOrEmpty()) {
                        val postsId: MutableList<String> = resources.data.familyPosts!!
                        listOfPosts.addAll(postsId)
                        returnData.value = Resource.Success(listOfPosts)

                    } else {
                        returnData.value =
                            Resource.Error("no posts in")
                    }
                }

                is Resource.Error -> {
                    returnData.value =
                        Resource.Error(resources.message.toString())
                }

                is Resource.Loading -> {
                    returnData.value =
                        Resource.Loading()
                }

            }
        }
        return returnData
    }


    fun updateFamilyJoinReq(
        userId: String,
        familyId: String,
        operation: Boolean
    ): MutableLiveData<Resource<Boolean>> {
        val returnData: MutableLiveData<Resource<Boolean>> = MutableLiveData()
        returnData.value =
            Resource.Loading()
        if (operation) {
            fireStore.collection("users").document(userId)
                .update("pendingFamilyJoinReq", FieldValue.arrayUnion(familyId))
                .addOnCompleteListener { task ->
                    if (task.isComplete) {
                        if (task.isSuccessful) {
                            returnData.value = Resource.Success(true)
                        } else {
                            returnData.value =
                                Resource.Error(task.exception?.message.toString())
                        }
                    }
                }
        } else {
            fireStore.collection("users").document(userId)
                .update("pendingFamilyJoinReq", FieldValue.arrayRemove(familyId))
                .addOnCompleteListener { task ->
                    if (task.isComplete) {
                        if (task.isSuccessful) {
                            returnData.value = Resource.Success(true)
                        } else {
                            returnData.value =
                                Resource.Error(task.exception?.message.toString())
                        }
                    }
                }
        }
        return returnData
    }

    fun updateFamilyMemberList(
        familyId: String,
        userId: String
    ): MutableLiveData<Resource<String>> {
        val returnData: MutableLiveData<Resource<String>> = MutableLiveData()
        returnData.value =
            Resource.Loading()
        fireStore.collection("families").document(familyId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnCompleteListener { task ->
                if (task.isComplete) {
                    if (task.isSuccessful) {
                        returnData.value = Resource.Success(userId)
                    } else {
                        returnData.value =
                            Resource.Error(task.exception?.message.toString())
                    }
                }
            }
        return returnData
    }

    fun updateProfile(finalUserName: String, photoUri: String?): MutableLiveData<Resource<Boolean>> {
        val returnData: MutableLiveData<Resource<Boolean>> = MutableLiveData()

        getCurrentUser().observeForever { resources ->
            when (resources) {
                is Resource.Success -> {
                    fireStore.collection("users").document(resources.data!!.uId!!)
                        .update("name", finalUserName)
                        .addOnCompleteListener { task ->
                            if (task.isComplete) {
                                if (task.isSuccessful) {
                                    if (!photoUri.isNullOrBlank()) {
                                        fireStore.collection("users")
                                            .document(resources.data!!.uId!!)
                                            .update("profilePic", photoUri)
                                            .addOnCompleteListener { task ->
                                                if (task.isComplete) {
                                                    if (task.isSuccessful) {
                                                        returnData.value = Resource.Success(true)
                                                    } else {
                                                        returnData.value =
                                                            Resource.Error(task.exception?.message.toString())
                                                    }
                                                }
                                            }
                                    } else
                                        returnData.value = Resource.Success(true)
                                } else {
                                    returnData.value =
                                        Resource.Error(task.exception?.message.toString())
                                }
                            }
                        }
                }

                is Resource.Error -> {

                    returnData.value =
                        Resource.Error(resources.message.toString())
                }

                is Resource.Loading -> {
                    returnData.value = Resource.Loading()
                }

            }
        }
        return returnData
    }


}