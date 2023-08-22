package com.example.familytime.viewmodels.chats


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familytime.models.LocationData
import com.example.familytime.models.UserChatting
import com.example.familytime.other.Resource
import com.example.familytime.repositories.chats.ChatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(private val repository: ChatsRepository) : ViewModel() {
    fun saveChatInRealTimeDatabase(
        userChatting: UserChatting,
        userId1: String,
    ): MutableLiveData<Resource<String>> {
        return repository.saveChatInRealTimeDatabase(userChatting, userId1)
    }

    fun loadLast20Chats(
        userId1: String,
    ): MutableLiveData<MutableList<UserChatting>> {
        return repository.getChatFromRealtimeDatabase(userId1)
    }

    fun setUsersLiveLocation(
        userId: String,
        userLocation: LocationData
    ): MutableLiveData<Resource<Boolean>> {
        return repository.setUsersLiveLocation(userId, userLocation)
    }

    fun getUserLiveLocation(userId: String): MutableLiveData<Resource<LocationData>> {
        return repository.getUserLiveLocation(userId)
    }
}