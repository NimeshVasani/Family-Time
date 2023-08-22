package com.example.familytime.viewmodels.auth

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familytime.models.User
import com.example.familytime.other.Resource
import com.example.familytime.repositories.auth.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun signInUserWithFirebase(email: String, password: String): MutableLiveData<Resource<User>> {
        return authRepository.signInWithFirebase(email = email, password = password)
    }

    fun registerUserWithFirebase(
        displayName: String,
        email: String,
        password: String
    ): MutableLiveData<Resource<User>> {
        return authRepository.registerUserWithFirebase(displayName, email, password)
    }

    fun updateProfile(displayName: String): MutableLiveData<Resource<Boolean>> {
        return authRepository.updateProfile(displayName = displayName)
    }

    fun checkLoginSession(): Boolean {
        return authRepository.checkLoginSession()
    }

    fun checkPhoneSession(): Boolean {
        return authRepository.checkPhoneSession()
    }

    fun getCurrentUser(): FirebaseUser {
        return authRepository.getCurrentUser()
    }


    fun verifyPhone(
        phoneNum: String,
        activity: Activity
    ) {
        authRepository.verifyPhone(phoneNum = phoneNum, activity = activity)
    }

    fun initCallbacks(
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (FirebaseException) -> Unit,
        onCodeSent: (String, PhoneAuthProvider.ForceResendingToken) -> Unit
    ) {
        authRepository.initCallbacks(onVerificationCompleted, onVerificationFailed, onCodeSent)
    }

    fun verifyPhoneNumberWithCode(code: String): PhoneAuthCredential {
        return authRepository.verifyPhoneNumberWithCode(code)
    }

    fun logout(){
        authRepository.logout()
    }

}