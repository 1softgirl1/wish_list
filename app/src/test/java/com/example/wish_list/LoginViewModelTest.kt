package com.example.wish_list

import androidx.activity.ComponentActivity
import com.example.wish_list.auth.AuthProvider
import com.example.wish_list.auth.AuthResult
import com.example.wish_list.auth.AuthService
import com.example.wish_list.auth.AuthSession
import com.example.wish_list.auth.LoginViewModel
import com.example.wish_list.ui.analytics.AnalyticsService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun loginWithYandex_success_savesSession_andTracksEvent() = runTest {
        val authService = FakeAuthService()
        val analytics = FakeAnalyticsService()
        val expectedSession = AuthSession(
            token = "yandex_token",
            userName = "Yandex User",
            provider = AuthProvider.YANDEX,
            expiresAtMillis = System.currentTimeMillis() + 3_600_000
        )
        authService.yandexResult = AuthResult.Success(expectedSession)

        val viewModel = LoginViewModel(authService, analytics)
        viewModel.loginWithYandex(null)
        advanceUntilIdle()

        assertEquals(expectedSession, viewModel.uiState.authorizedSession)
        assertEquals(expectedSession, authService.lastSavedSession)
        assertTrue(analytics.events.any { it.name == "user_logged_in" && it.params["provider"] == "yandex" })
    }

    @Test
    fun loginWithVk_cancelled_doesNotSaveSession() = runTest {
        val authService = FakeAuthService()
        val analytics = FakeAnalyticsService()
        authService.vkResult = AuthResult.Cancelled

        val viewModel = LoginViewModel(authService, analytics)
        viewModel.loginWithVk(null)
        advanceUntilIdle()

        assertNull(viewModel.uiState.authorizedSession)
        assertNull(authService.lastSavedSession)
        assertTrue(viewModel.uiState.message?.contains("cancel", ignoreCase = true) == true)
        assertFalse(analytics.events.any { it.name == "user_logged_in" })
    }

    @Test
    fun restoreSessionIfPossible_usesOnlyValidSavedSession() {
        val validSession = AuthSession(
            token = "token",
            userName = "VK User",
            provider = AuthProvider.VK,
            expiresAtMillis = System.currentTimeMillis() + 10_000
        )
        val authService = FakeAuthService().apply { storedSession = validSession }
        val viewModel = LoginViewModel(authService, FakeAnalyticsService())

        viewModel.restoreSessionIfPossible()

        assertNotNull(viewModel.uiState.authorizedSession)
        assertEquals(validSession, viewModel.uiState.authorizedSession)
    }

    @Test
    fun restoreSessionIfPossible_skipsExpiredSession() {
        val expiredSession = AuthSession(
            token = "token",
            userName = "VK User",
            provider = AuthProvider.VK,
            expiresAtMillis = System.currentTimeMillis() - 1_000
        )
        val authService = FakeAuthService().apply { storedSession = expiredSession }
        val viewModel = LoginViewModel(authService, FakeAnalyticsService())

        viewModel.restoreSessionIfPossible()

        assertNull(viewModel.uiState.authorizedSession)
    }

    @Test
    fun logout_clearsSavedSession_andUnauthorizesUser() {
        val validSession = AuthSession(
            token = "token",
            userName = "Yandex User",
            provider = AuthProvider.YANDEX,
            expiresAtMillis = System.currentTimeMillis() + 10_000
        )
        val authService = FakeAuthService().apply { storedSession = validSession }
        val viewModel = LoginViewModel(authService, FakeAnalyticsService())
        viewModel.restoreSessionIfPossible()

        viewModel.logout()

        assertNull(viewModel.uiState.authorizedSession)
        assertTrue(authService.clearSessionCalled)
        assertNull(authService.storedSession)
    }
}

private class FakeAuthService : AuthService {
    var yandexResult: AuthResult = AuthResult.Error("No result configured")
    var vkResult: AuthResult = AuthResult.Error("No result configured")
    var storedSession: AuthSession? = null
    var lastSavedSession: AuthSession? = null
    var clearSessionCalled: Boolean = false

    override suspend fun loginWithYandex(activity: ComponentActivity?): AuthResult = yandexResult

    override suspend fun loginWithVk(activity: ComponentActivity?): AuthResult = vkResult

    override fun getSavedSession(): AuthSession? = storedSession?.takeIf { it.isValid() }

    override fun saveSession(session: AuthSession) {
        lastSavedSession = session
        storedSession = session
    }

    override fun clearSession() {
        clearSessionCalled = true
        storedSession = null
    }
}

private class FakeAnalyticsService : AnalyticsService {
    data class Event(val name: String, val params: Map<String, Any>)
    val events = mutableListOf<Event>()

    override fun trackEvent(name: String, params: Map<String, Any>) {
        events += Event(name, params)
    }

    override fun trackError(message: String, error: Throwable?) = Unit
}
