package com.example.wish_list.auth

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.example.wish_list.BuildConfig
import com.vk.id.AccessToken
import com.vk.id.VKID
import com.vk.id.VKIDAuthFail
import com.vk.id.auth.AuthCodeData
import com.vk.id.auth.VKIDAuthCallback
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.concurrent.thread

class ExternalAuthService @Inject constructor(
    private val secureSessionStore: SecureSessionStore
) : AuthService {

    override suspend fun loginWithYandex(activity: ComponentActivity?): AuthResult {
        if (activity == null) {
            return AuthResult.Error("Activity is required for Yandex authorization.")
        }
        if (BuildConfig.YANDEX_CLIENT_ID.isBlank()) {
            return AuthResult.Error("YANDEX_CLIENT_ID is empty. Add it to local.properties.")
        }

        val sdk = YandexAuthSdk.create(YandexAuthOptions(activity))
        return suspendCancellableCoroutine { continuation ->
            val requestKey = "yandex_auth_${UUID.randomUUID()}"
            lateinit var launcher: ActivityResultLauncher<YandexAuthLoginOptions>
            launcher = activity.activityResultRegistry.register(requestKey, sdk.contract) { result ->
                val authResult = when (result) {
                    is YandexAuthResult.Success -> {
                        thread(start = true) {
                            val profile = resolveYandexProfile(result.token.value)
                            val session = AuthSession(
                                token = result.token.value,
                                userName = profile.name,
                                userId = profile.userId,
                                email = profile.email,
                                provider = AuthProvider.YANDEX,
                                expiresAtMillis = System.currentTimeMillis() + (result.token.expiresIn * 1000)
                            )
                            if (continuation.isActive) {
                                continuation.resume(AuthResult.Success(session))
                            }
                            launcher.unregister()
                        }
                        null
                    }

                    is YandexAuthResult.Failure -> {
                        AuthResult.Error(result.exception.message ?: DEFAULT_AUTH_ERROR)
                    }

                    YandexAuthResult.Cancelled -> AuthResult.Cancelled
                    else -> AuthResult.Error(DEFAULT_AUTH_ERROR)
                }

                if (authResult != null && continuation.isActive) {
                    continuation.resume(authResult)
                    launcher.unregister()
                }
            }

            continuation.invokeOnCancellation {
                launcher.unregister()
            }

            launcher.launch(YandexAuthLoginOptions())
        }
    }

    override suspend fun loginWithVk(activity: ComponentActivity?): AuthResult {
        if (activity == null) {
            return AuthResult.Error("Activity is required for VK authorization.")
        }
        if (BuildConfig.VKID_CLIENT_ID.isBlank() || BuildConfig.VKID_CLIENT_SECRET.isBlank()) {
            return AuthResult.Error("VKID_CLIENT_ID or VKID_CLIENT_SECRET is empty in local.properties.")
        }

        return suspendCancellableCoroutine { continuation ->
            val callback = object : VKIDAuthCallback {
                override fun onAuth(accessToken: AccessToken) {
                    val token = accessToken.token
                    val firstName = accessToken.userData.firstName
                    val lastName = accessToken.userData.lastName
                    val userName = listOf(firstName, lastName)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .ifBlank { "VK User" }

                    resumeIfActive(
                        continuation = continuation,
                        result = AuthResult.Success(
                            AuthSession(
                                token = token,
                                userName = userName,
                                userId = "${AuthProvider.VK.name.lowercase()}_${token.take(12)}",
                                email = null,
                                provider = AuthProvider.VK,
                                expiresAtMillis = accessToken.expireTime * 1000
                            )
                        )
                    )
                }

                override fun onAuthCode(data: AuthCodeData, isCompletion: Boolean) {
                    // We do not use auth-code flow in this lab. Successful auth is handled in onAuth.
                }

                override fun onFail(fail: VKIDAuthFail) {
                    val authResult = if (fail is VKIDAuthFail.Canceled) {
                        AuthResult.Cancelled
                    } else {
                        AuthResult.Error(fail.description.ifBlank { DEFAULT_AUTH_ERROR })
                    }
                    resumeIfActive(continuation, authResult)
                }
            }

            VKID.instance.authorize(activity, callback)
        }
    }

    override fun getSavedSession(): AuthSession? {
        return secureSessionStore.load()?.takeIf { it.isValid() }
    }

    override fun saveSession(session: AuthSession) {
        secureSessionStore.save(session)
    }

    override fun clearSession() {
        secureSessionStore.clear()
    }

    private fun resumeIfActive(
        continuation: CancellableContinuation<AuthResult>,
        result: AuthResult
    ) {
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }

    companion object {
        private const val YANDEX_FALLBACK_NAME = "Yandex User"
        private const val YANDEX_USER_INFO_URL = "https://login.yandex.ru/info?format=json"
        private const val DEFAULT_AUTH_ERROR = "Authorization failed"
    }

    private fun resolveYandexProfile(accessToken: String): YandexProfile {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(YANDEX_USER_INFO_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Authorization", "OAuth $accessToken")
            }

            val http = connection ?: return YandexProfile(
                userId = "${AuthProvider.YANDEX.name.lowercase()}_${accessToken.take(12)}",
                name = YANDEX_FALLBACK_NAME,
                email = null
            )
            if (http.responseCode !in 200..299) {
                YandexProfile(
                    userId = "${AuthProvider.YANDEX.name.lowercase()}_${accessToken.take(12)}",
                    name = YANDEX_FALLBACK_NAME,
                    email = null
                )
            } else {
                val body = http.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val userId = json.optString("id")
                    .ifBlank { "${AuthProvider.YANDEX.name.lowercase()}_${accessToken.take(12)}" }
                val email = json.optString("default_email")
                    .ifBlank { json.optString("email") }
                    .ifBlank { null }
                val displayName = json.optString("display_name")
                val resolvedName = if (displayName.isNotBlank()) {
                    displayName
                } else {
                    val realName = json.optString("real_name")
                    if (realName.isNotBlank()) {
                        realName
                    } else {
                        val firstName = json.optString("first_name")
                        val lastName = json.optString("last_name")
                        listOf(firstName, lastName)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                            .ifBlank { YANDEX_FALLBACK_NAME }
                    }
                }
                YandexProfile(
                    userId = userId,
                    name = resolvedName,
                    email = email
                )
            }
        } catch (_: Exception) {
            YandexProfile(
                userId = "${AuthProvider.YANDEX.name.lowercase()}_${accessToken.take(12)}",
                name = YANDEX_FALLBACK_NAME,
                email = null
            )
        } finally {
            connection?.disconnect()
        }
    }

    private data class YandexProfile(
        val userId: String,
        val name: String,
        val email: String?
    )
}
