package com.example.wish_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wish_list.data.DemoDataContainer
import com.example.wish_list.ui.WishlistViewModel
import com.example.wish_list.ui.analytics.AnalyticsService
import javax.inject.Inject

class WishlistViewModelFactory @Inject constructor(
    private val container: DemoDataContainer,
    private val analyticsService: AnalyticsService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WishlistViewModel::class.java)) {
            return WishlistViewModel(container, analyticsService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
