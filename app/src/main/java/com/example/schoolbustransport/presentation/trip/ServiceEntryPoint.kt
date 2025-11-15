package com.example.schoolbustransport.presentation.trip

import com.example.schoolbustransport.data.network.ApiService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Hilt entry point to resolve ApiService outside of @AndroidEntryPoint scope
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ServiceEntryPoint {
    fun apiService(): ApiService
}
