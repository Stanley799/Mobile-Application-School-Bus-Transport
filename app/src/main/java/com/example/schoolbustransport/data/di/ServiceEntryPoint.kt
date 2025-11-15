package com.example.schoolbustransport.data.di

import com.example.schoolbustransport.data.network.ApiService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ServiceEntryPoint {
    fun apiService(): ApiService
}
