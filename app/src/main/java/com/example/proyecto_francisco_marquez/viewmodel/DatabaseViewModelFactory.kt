package com.example.proyecto_francisco_marquez.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto_francisco_marquez.data.FirestoreService

class DatabaseViewModelFactory(private val firestoreService: FirestoreService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
            return DatabaseViewModel(firestoreService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
