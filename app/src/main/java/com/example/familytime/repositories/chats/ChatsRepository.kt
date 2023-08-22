package com.example.familytime.repositories.chats


import android.widget.NumberPicker.OnValueChangeListener
import androidx.lifecycle.MutableLiveData
import com.example.familytime.models.LocationData
import com.example.familytime.models.UserChatting
import com.example.familytime.other.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import javax.inject.Inject

class ChatsRepository @Inject constructor(private val database: FirebaseDatabase) {
    private val chatRef = database.getReference("chats")

    fun saveChatInRealTimeDatabase(
        userChatting: UserChatting, userId1: String
    ): MutableLiveData<Resource<String>> {

        val chatData: MutableLiveData<Resource<String>> = MutableLiveData()
        chatRef.child(userId1).push().apply {
            setValue(userChatting)
                .addOnSuccessListener {
                    chatData.value = Resource.Success(key.toString())
                }.addOnFailureListener {
                    chatData.value = Resource.Error(it.message.toString())
                }

        }

        return chatData
    }

    fun getChatFromRealtimeDatabase(
        userId1: String
    ): MutableLiveData<MutableList<UserChatting>> {

        val userChattingList: MutableLiveData<MutableList<UserChatting>> = MutableLiveData()
        val list = mutableListOf<UserChatting>()

        chatRef.child(userId1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    list.clear()

                    for (chatSnapshot in snapshot.children) {
                        list.add(chatSnapshot.getValue<UserChatting>()!!)
                        // Do something with each chat
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        userChattingList.value = list
        return userChattingList
    }

    fun setUsersLiveLocation(
        userId: String,
        userLocation: LocationData
    ): MutableLiveData<Resource<Boolean>> {
        val returnData: MutableLiveData<Resource<Boolean>> = MutableLiveData()
        database.getReference("location").child(userId).setValue(userLocation)
            .addOnSuccessListener {
                returnData.value = Resource.Success(true)
            }.addOnFailureListener {
                returnData.value = Resource.Error(it.message.toString())
            }
        return returnData
    }

    fun getUserLiveLocation(userId: String): MutableLiveData<Resource<LocationData>> {
        val returnData: MutableLiveData<Resource<LocationData>> = MutableLiveData()
        database.getReference("location").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        returnData.value = Resource.Success(snapshot.getValue<LocationData>())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        return returnData
    }

}