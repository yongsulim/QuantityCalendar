// 파일 위치: viewmodel/MainViewModelFactory.kt
package com.example.quantitycalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quantitycalendar.data.DeliveryDao

class MainViewModelFactory(
    private val deliveryDao: DeliveryDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(deliveryDao) as T
    }
}
