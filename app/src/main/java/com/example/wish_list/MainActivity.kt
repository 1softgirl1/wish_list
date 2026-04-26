package com.example.wish_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import com.example.wish_list.data.DemoDataContainer
import com.example.wish_list.auth.ExternalAuthService
import com.example.wish_list.auth.LoginViewModel
import com.example.wish_list.auth.SecureSessionStore
import com.example.wish_list.ui.WishlistApp
import com.example.wish_list.ui.WishlistViewModel
import com.example.wish_list.ui.theme.Wish_listTheme

class MainActivity : ComponentActivity() {
    private val container = DemoDataContainer()
    private val analyticsService = AppMetricaAnalyticsService()
    private val authService by lazy { ExternalAuthService(SecureSessionStore(this)) }
    private val loginViewModel by lazy { LoginViewModel(authService, analyticsService) }

    private val viewModel: WishlistViewModel by lazy {
        WishlistViewModel(
            container = container,
            analyticsService = analyticsService
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Wish_listTheme {
                val loginState = loginViewModel.uiState

                LaunchedEffect(Unit) {
                    loginViewModel.restoreSessionIfPossible()
                }

                if (loginState.isAuthorized) {
                    val userName = loginState.authorizedSession?.userName
                        ?.ifBlank { "User" }
                        ?: "User"
                    WishlistApp(
                        viewModel = viewModel,
                        displayUserName = userName,
                        onLogout = loginViewModel::logout
                    )
                } else {
                    LoginScreen(
                        isLoading = loginState.isLoading,
                        message = loginState.message,
                        onLoginWithYandex = { loginViewModel.loginWithYandex(this) },
                        onLoginWithVk = { loginViewModel.loginWithVk(this) }
                    )
                }
            }
        }
    }
}
