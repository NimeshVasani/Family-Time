package com.example.familytime.di

import com.example.familytime.repositories.auth.AuthRepository
import com.example.familytime.repositories.chats.ChatsRepository
import com.example.familytime.repositories.firestore.FireStoreRepository
import com.example.familytime.repositories.storage.StorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {

    // provide firebase instances

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseFireStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    //provides repositories

    @Singleton
    @Provides
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepository(firebaseAuth)
    }

    @Singleton
    @Provides
    fun provideChatsRepository(firebaseDatabase: FirebaseDatabase): ChatsRepository {
        return ChatsRepository(firebaseDatabase)
    }


    @Singleton
    @Provides
    fun provideFireStoreRepository(
        firebaseAuth: FirebaseAuth,
        fireStore: FirebaseFirestore
    ): FireStoreRepository {
        return FireStoreRepository(firebaseAuth, fireStore)
    }


    @Singleton
    @Provides
    fun provideFirebaseStorageRepository(
        firebaseStorage: FirebaseStorage
    ): StorageRepository {
        return StorageRepository(firebaseStorage)
    }
}