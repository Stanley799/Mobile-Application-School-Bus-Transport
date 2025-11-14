package com.example.schoolbustransport.data.di

import com.example.schoolbustransport.data.repository.AuthRepositoryImpl
import com.example.schoolbustransport.data.repository.TripRepositoryImpl
import com.example.schoolbustransport.domain.repository.AuthRepository
import com.example.schoolbustransport.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(tripRepositoryImpl: TripRepositoryImpl): TripRepository
}
