package com.example.wish_list.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wish_list.data.DemoDataContainer

@Composable
fun WishlistFeatureEntry() {
    val container = remember { DemoDataContainer() }
    val wishlistViewModel: WishlistViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                WishlistViewModel(container)
            }
        }
    )
    WishlistApp(viewModel = wishlistViewModel)
}

