package com.example.familytime.viewmodels.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familytime.repositories.chats.ChatsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatsViewModelFactory @Inject constructor(private val repository: ChatsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatsViewModel(repository = repository) as T
    }
}