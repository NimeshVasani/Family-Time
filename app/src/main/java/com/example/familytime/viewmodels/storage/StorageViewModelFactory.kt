package com.example.familytime.viewmodels.storage


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familytime.repositories.storage.StorageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageViewModelFactory @Inject constructor(private val repository: StorageRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StorageViewModel(repository) as T
    }
}
