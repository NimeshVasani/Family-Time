package com.example.familytime.viewmodels.firestore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familytime.models.Family
import com.example.familytime.models.Posts
import com.example.familytime.models.User
import com.example.familytime.other.Resource
import com.example.familytime.repositories.firestore.FireStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FireStoreViewModel @Inject constructor(private val repository: FireStoreRepository) :
    ViewModel() {


    fun addIntoUsersList(user: User): MutableLiveData<Resource<Boolean>> {
        return repository.addIntoUsersList(user)
    }

    fun getUserList(): MutableLiveData<Resource<MutableList<User>>> {
        return repository.getUserList()
    }

    fun searchUserByName(userName: String): MutableLiveData<Resource<MutableList<User>>> {
        return repository.searchUserByName(userName)
    }

    fun saveFamilies(family: Family): MutableLiveData<Resource<Family>> {
        return repository.saveFamilies(family)
    }

    fun searchUserByUserId(userId: String): MutableLiveData<Resource<User>> {
        return repository.searchUserByUserId(userId)
    }


    fun getCurrentUser(): MutableLiveData<Resource<User>> {
        return repository.getCurrentUser()
    }

    fun addFamilyIntoUserDocument(
        familyId: String
    ): MutableLiveData<Resource<Boolean>> {
        return repository.addFamilyIntoUserDocument(familyId = familyId)
    }

    fun getFamilyById(familyId: String): MutableLiveData<Resource<Family>> {
        return repository.getFamilyById(familyId)
    }

    fun updatePostUid(
        postsId: String,
        families: MutableList<String>
    ): MutableLiveData<Resource<Boolean>> {
        return repository.updatePostUid(postsId, families)
    }

    fun shareNewPosts(posts: Posts): MutableLiveData<Resource<String>> {
        return repository.shareNewPosts(posts)
    }

    fun getAllFamiliesPosts(familiesList: MutableList<String>): MutableLiveData<Resource<MutableList<String>>> {
        return repository.getAllFamiliesPostsIds(familiesList)
    }

    fun getPostsByTimeStamp(postIds: MutableList<String>): MutableLiveData<Resource<MutableList<Posts>>> {
        return repository.getPostsByTimeStamp(postIds)
    }

    fun getSpecificFamilyPost(familyId: String): MutableLiveData<Resource<MutableList<String>>> {
        return repository.getSpecificFamilyPost(familyId)
    }

    fun updateFamilyJoinReq(
        userId: String,
        familyId: String,
        operation: Boolean
    ): MutableLiveData<Resource<Boolean>> {
        return repository.updateFamilyJoinReq(userId, familyId, operation)
    }

    fun updateFamilyMemberList(
        familyId: String,
        userId: String
    ): MutableLiveData<Resource<String>> {
        return repository.updateFamilyMemberList(familyId, userId)
    }

    fun updateProfile(
        finalUserName: String,
        photoUri: String?
    ): MutableLiveData<Resource<Boolean>> {
        return repository.updateProfile(finalUserName, photoUri)
    }

}