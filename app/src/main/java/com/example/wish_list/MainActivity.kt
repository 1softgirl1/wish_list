package com.example.wish_list

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.wish_list.auth.AuthSession
import com.example.wish_list.auth.LoginViewModel
import com.example.wish_list.auth.SecureSessionStore
import com.example.wish_list.firebase.AppNotificationHelper
import com.example.wish_list.firebase.FcmTokenRepository
import com.example.wish_list.firebase.RemoteConfigService
import com.example.wish_list.firebase.UserProfileRepository
import com.example.wish_list.ui.WishlistApp
import com.example.wish_list.ui.WishlistViewModel
import com.example.wish_list.ui.theme.Wish_listTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var remoteConfigService: RemoteConfigService

    @Inject
    lateinit var secureSessionStore: SecureSessionStore

    @Inject
    lateinit var userProfileRepository: UserProfileRepository

    @Inject
    lateinit var fcmTokenRepository: FcmTokenRepository

    @Inject
    lateinit var wishlistViewModelFactory: WishlistViewModelFactory

    private val loginViewModel: LoginViewModel by viewModels()
    private val viewModel: WishlistViewModel by viewModels { wishlistViewModelFactory }

    private var profileObserverJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppNotificationHelper(this).ensureChannel()
        requestNotificationsPermissionIfNeeded()
        handlePushIntent(intent?.extras)
        fetchAndStoreCurrentFcmToken()
        fetchAndApplyRemoteConfig()
        enableEdgeToEdge()

        setContent {
            Wish_listTheme {
                val loginState = loginViewModel.uiState

                LaunchedEffect(Unit) {
                    loginViewModel.restoreSessionIfPossible()
                }

                if (loginState.isAuthorized) {
                    LaunchedEffect(loginState.authorizedSession?.userId) {
                        val session = loginState.authorizedSession ?: return@LaunchedEffect
                        syncUserProfile(session)
                        observeUserProfile(session.userId)
                    }
                    val userName = loginState.authorizedSession?.userName
                        ?.ifBlank { "User" }
                        ?: "User"
                    WishlistApp(
                        viewModel = viewModel,
                        displayUserName = userName,
                        greetingText = viewModel.uiState.greetingText,
                        onLogout = loginViewModel::logout,
                        onOpenHybridComposeDemo = {
                            startActivity(Intent(this, HybridComposeActivity::class.java))
                        }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlePushIntent(intent.extras)
    }

    override fun onResume() {
        super.onResume()
        fetchAndApplyRemoteConfig()
    }

    private fun handlePushIntent(extras: Bundle?) {
        val fromPush = extras?.keySet()?.isNotEmpty() == true
        if (!fromPush) return
        val shareCode = extras?.getString("shareCode") ?: extras?.getString("share_code")
        if (!shareCode.isNullOrBlank()) {
            viewModel.updatePublicShareCode(shareCode)
            viewModel.loadPublicWishlistFromInput()
        }
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun fetchAndStoreCurrentFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            task.result?.let { token ->
                fcmTokenRepository.saveToken(token)
                secureSessionStore.load()?.let { session ->
                    userProfileRepository.saveOrUpdateProfile(session, token)
                }
            }
        }
    }

    private fun syncUserProfile(session: AuthSession) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = if (task.isSuccessful) task.result else null
            userProfileRepository.saveOrUpdateProfile(session, token)
        }
    }

    private fun observeUserProfile(userId: String) {
        profileObserverJob?.cancel()
        profileObserverJob = lifecycleScope.launch {
            runCatching {
                userProfileRepository.observeUserProfile(userId).collect { profile ->
                    viewModel.applyUserProfile(profile)
                }
            }.onFailure {
                viewModel.postMessage("Profile sync is unavailable. Check Firestore rules.")
            }
        }
    }

    private fun fetchAndApplyRemoteConfig() {
        lifecycleScope.launch {
            val remoteConfig = remoteConfigService.fetchAndActivate(BuildConfig.DEBUG)
            viewModel.applyRemoteConfig(
                greetingText = remoteConfig.greetingText,
                enableFirestoreRealtime = remoteConfig.isFirestoreRealtimeEnabled
            )
        }
    }

    private companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
    }
}
