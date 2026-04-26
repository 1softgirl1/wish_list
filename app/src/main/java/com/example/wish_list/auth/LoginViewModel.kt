package com.example.wish_list.auth

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wish_list.ui.analytics.AnalyticsService
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val authorizedSession: AuthSession? = null
) {
    val isAuthorized: Boolean
        get() = authorizedSession != null
}

class LoginViewModel(
    private val authService: AuthService,
    private val analyticsService: AnalyticsService
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun restoreSessionIfPossible() {
        val session = authService.getSavedSession()
        uiState = uiState.copy(authorizedSession = session)
    }

    fun loginWithYandex(activity: ComponentActivity?) {
        launchLogin(
            provider = AuthProvider.YANDEX,
            action = { authService.loginWithYandex(activity) }
        )
    }

    fun loginWithVk(activity: ComponentActivity?) {
        launchLogin(
            provider = AuthProvider.VK,
            action = { authService.loginWithVk(activity) }
        )
    }

    fun clearMessage() {
        uiState = uiState.copy(message = null)
    }

    fun logout() {
        authService.clearSession()
        uiState = uiState.copy(
            authorizedSession = null,
            isLoading = false,
            message = null
        )
    }

    private fun launchLogin(provider: AuthProvider, action: suspend () -> AuthResult) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, message = null)
            when (val result = action()) {
                is AuthResult.Success -> {
                    authService.saveSession(result.session)
                    analyticsService.trackEvent(
                        name = "user_logged_in",
                        params = mapOf("provider" to provider.name.lowercase())
                    )
                    uiState = uiState.copy(
                        isLoading = false,
                        authorizedSession = result.session,
                        message = null
                    )
                }

                is AuthResult.Error -> {
                    uiState = uiState.copy(isLoading = false, message = result.message)
                }

                AuthResult.Cancelled -> {
                    uiState = uiState.copy(isLoading = false, message = "Authorization cancelled")
                }
            }
        }
    }
}
