package com.example.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.repository.LandlordRepository

class LandlordViewModelFactory(private val repository: LandlordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LandlordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LandlordViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
