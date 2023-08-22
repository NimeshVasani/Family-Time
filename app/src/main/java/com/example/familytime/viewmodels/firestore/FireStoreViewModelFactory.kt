package com.example.familytime.viewmodels.firestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familytime.repositories.firestore.FireStoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireStoreViewModelFactory @Inject constructor(private val repository: FireStoreRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FireStoreViewModel(repository) as T
    }
}