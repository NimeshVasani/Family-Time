package com.example.familytime.repositories.storage

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.example.familytime.other.Resource
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    private val storageRef = storage.reference

    fun saveImageInStorage(
        imageUri: Uri,
        pathName: String
    ): MutableLiveData<Resource<String>> {
        val sendingData: MutableLiveData<Resource<String>> = MutableLiveData()
        val randomChildId = UUID.randomUUID().toString()
        sendingData.value = Resource.Loading()

        storageRef.child(pathName).child("$randomChildId.jpg").apply {

            putFile(imageUri).addOnCompleteListener { task ->
                if (task.isComplete) {
                    if (task.isSuccessful) {
                        sendingData.value = Resource.Success(this.toString())
                    } else {
                        sendingData.value = Resource.Error(task.exception?.message.toString())

                    }
                }
            }
            return sendingData
        }
    }

    fun getStorageReference(): FirebaseStorage {
        return storage
    }
}