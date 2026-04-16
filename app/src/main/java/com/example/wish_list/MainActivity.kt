package com.example.wish_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.wish_list.ui.WishlistFeatureEntry
import com.example.wish_list.ui.theme.Wish_listTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Wish_listTheme {
                WishlistFeatureEntry()
            }
        }
    }
}
