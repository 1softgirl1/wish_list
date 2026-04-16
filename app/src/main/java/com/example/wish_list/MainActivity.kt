package com.example.wish_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wish_list.core.navigation.AppNavigator
import com.example.wish_list.core.navigation.AppRoutes
import com.example.wish_list.feature.publicwishlist.PublicWishlistFeatureScreen
import com.example.wish_list.feature.reservation.ReservationFeatureScreen
import com.example.wish_list.ui.WishlistFeatureScreen
import com.example.wish_list.ui.theme.Wish_listTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Wish_listTheme {
                val navController = rememberNavController()
                val navigator = remember(navController) {
                    AppNavigatorImpl(navController)
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.WISHLIST
                ) {
                    composable(AppRoutes.WISHLIST) {
                        WishlistFeatureScreen(navigator = navigator)
                    }
                    composable(AppRoutes.PUBLIC_WISHLIST) {
                        PublicWishlistFeatureScreen(navigator = navigator)
                    }
                    composable(AppRoutes.RESERVATIONS) {
                        ReservationFeatureScreen(navigator = navigator)
                    }
                }
            }
        }
    }
}

private class AppNavigatorImpl(
    private val navController: NavHostController
) : AppNavigator {
    override fun openWishlist() {
        navController.navigate(AppRoutes.WISHLIST)
    }

    override fun openPublicWishlist() {
        navController.navigate(AppRoutes.PUBLIC_WISHLIST)
    }

    override fun openReservations() {
        navController.navigate(AppRoutes.RESERVATIONS)
    }

    override fun back() {
        navController.popBackStack()
    }
}
