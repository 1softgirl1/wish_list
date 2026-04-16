package com.example.wish_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.wish_list.data.DemoDataContainer
import com.example.wish_list.ui.WishlistApp
import com.example.wish_list.ui.WishlistViewModel
import com.example.wish_list.ui.theme.Wish_listTheme

class MainActivity : ComponentActivity() {
    private val container = DemoDataContainer()

    private val viewModel: WishlistViewModel by lazy { WishlistViewModel(container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Wish_listTheme {
                WishlistApp(viewModel = viewModel)
            }
        }
    }
}
