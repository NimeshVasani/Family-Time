package com.example.familytime.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familytime.repositories.auth.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthViewModelFactory @Inject constructor(private var repository: AuthRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository = repository) as T
    }
}