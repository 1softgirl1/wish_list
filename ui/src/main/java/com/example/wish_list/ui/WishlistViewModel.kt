package com.example.wish_list.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.wish_list.core.base.BaseViewModel
import com.example.wish_list.core.constant.CoreConstants
import com.example.wish_list.data.DemoDataContainer
import com.example.wish_list.domain.exception.DomainException
import com.example.wish_list.domain.model.GiftItem
import com.example.wish_list.domain.model.GiftItemStatus
import com.example.wish_list.domain.model.GiftPriority
import com.example.wish_list.domain.model.Reservation
import com.example.wish_list.domain.model.User
import com.example.wish_list.domain.model.UserProfile
import com.example.wish_list.domain.model.Wishlist
import com.example.wish_list.domain.repository.RealtimeWishlistRepository
import com.example.wish_list.domain.usecase.gift.AddGiftItemUseCase
import com.example.wish_list.domain.usecase.gift.GetGiftItemsForWishlistUseCase
import com.example.wish_list.domain.usecase.gift.UpdateGiftItemUseCase
import com.example.wish_list.domain.usecase.reservation.CancelReservationUseCase
import com.example.wish_list.domain.usecase.reservation.GetMyReservationsUseCase
import com.example.wish_list.domain.usecase.reservation.MarkGiftAsGiftedUseCase
import com.example.wish_list.domain.usecase.reservation.ReserveGiftItemUseCase
import com.example.wish_list.domain.usecase.wishlist.CreateWishlistUseCase
import com.example.wish_list.domain.usecase.wishlist.GetMyWishlistsUseCase
import com.example.wish_list.domain.usecase.wishlist.GetWishlistByShareCodeUseCase
import com.example.wish_list.domain.usecase.wishlist.GetWishlistDetailsUseCase
import com.example.wish_list.ui.analytics.AnalyticsService
import com.example.wish_list.ui.analytics.NoOpAnalyticsService
import kotlinx.coroutines.flow.collect

enum class HomeScreen {
    MY_WISHLISTS,
    WISHLIST_DETAILS,
    GIFT_EDITOR,
    PUBLIC_WISHLIST,
    MY_RESERVATIONS,
    PROFILE,
    ABOUT
}

data class GiftEditorState(
    val wishlistId: String,
    val giftItemId: String? = null,
    val title: String = CoreConstants.EMPTY_TEXT,
    val description: String = CoreConstants.EMPTY_TEXT,
    val link: String = CoreConstants.EMPTY_TEXT,
    val price: String = CoreConstants.EMPTY_TEXT,
    val priority: GiftPriority = GiftPriority.MEDIUM
)

data class ReservationCardState(
    val reservation: Reservation,
    val giftTitle: String,
    val wishlistTitle: String,
    val ownerName: String
)

data class WishlistUiState(
    val users: List<User> = emptyList(),
    val currentUser: User? = null,
    val currentScreen: HomeScreen = HomeScreen.MY_WISHLISTS,
    val myWishlists: List<Wishlist> = emptyList(),
    val selectedWishlist: Wishlist? = null,
    val selectedGiftItems: List<GiftItem> = emptyList(),
    val giftEditorState: GiftEditorState? = null,
    val showCreateWishlistDialog: Boolean = false,
    val publicShareCode: String = "",
    val publicWishlist: Wishlist? = null,
    val publicGiftItems: List<GiftItem> = emptyList(),
    val reservationCards: List<ReservationCardState> = emptyList(),
    val greetingText: String = "Добро пожаловать!",
    val userProfile: UserProfile? = null,
    val message: String? = null,
    val isLoading: Boolean = false
)

class WishlistViewModel(
    private val container: DemoDataContainer,
    private val analyticsService: AnalyticsService = NoOpAnalyticsService
) : BaseViewModel() {
    private val userRepository = container.userRepository
    private val wishlistRepository = container.wishlistRepository
    private val giftItemRepository = container.giftItemRepository
    private val reservationRepository = container.reservationRepository

    private val getMyWishlistsUseCase = GetMyWishlistsUseCase(userRepository, wishlistRepository)
    private val createWishlistUseCase = CreateWishlistUseCase(userRepository, wishlistRepository)
    private val getWishlistDetailsUseCase = GetWishlistDetailsUseCase(userRepository, wishlistRepository)
    private val addGiftItemUseCase = AddGiftItemUseCase(userRepository, wishlistRepository, giftItemRepository)
    private val updateGiftItemUseCase = UpdateGiftItemUseCase(userRepository, wishlistRepository, giftItemRepository)
    private val getGiftItemsForWishlistUseCase =
        GetGiftItemsForWishlistUseCase(userRepository, wishlistRepository, giftItemRepository)
    private val getWishlistByShareCodeUseCase = GetWishlistByShareCodeUseCase(wishlistRepository)
    private val reserveGiftItemUseCase =
        ReserveGiftItemUseCase(userRepository, wishlistRepository, giftItemRepository, reservationRepository)
    private val cancelReservationUseCase =
        CancelReservationUseCase(userRepository, giftItemRepository, reservationRepository)
    private val markGiftAsGiftedUseCase =
        MarkGiftAsGiftedUseCase(userRepository, giftItemRepository, reservationRepository)
    private val getMyReservationsUseCase = GetMyReservationsUseCase(userRepository, reservationRepository)
    private val realtimeWishlistRepository: RealtimeWishlistRepository? = container.realtimeWishlistRepository
    private var shouldUseFirestoreRealtime = false
    private var observedOwnerUserId: String? = null

    var uiState by mutableStateOf(WishlistUiState())
        private set

    init {
        analyticsService.trackEvent(
            name = "screen_viewed",
            params = mapOf("screen_name" to HomeScreen.MY_WISHLISTS.name.lowercase())
        )
        refreshAppData()
    }

    fun dismissMessage() {
        uiState = uiState.copy(message = null)
    }

    fun applyRemoteConfig(greetingText: String, enableFirestoreRealtime: Boolean) {
        uiState = uiState.copy(greetingText = greetingText)
        shouldUseFirestoreRealtime = enableFirestoreRealtime
        val currentUserId = uiState.currentUser?.id ?: return
        ensureRealtimeSubscription(currentUserId)
    }

    fun showMyWishlists() {
        uiState = uiState.copy(currentScreen = HomeScreen.MY_WISHLISTS, message = null)
    }

    fun showPublicWishlist() {
        uiState = uiState.copy(currentScreen = HomeScreen.PUBLIC_WISHLIST, message = null)
    }

    fun showMyReservations() {
        uiState = uiState.copy(currentScreen = HomeScreen.MY_RESERVATIONS, message = null)
    }

    fun showAbout() {
        uiState = uiState.copy(currentScreen = HomeScreen.ABOUT, message = null)
    }

    fun showProfile() {
        uiState = uiState.copy(currentScreen = HomeScreen.PROFILE, message = null)
    }

    fun applyUserProfile(profile: UserProfile?) {
        uiState = uiState.copy(userProfile = profile)
    }

    fun postMessage(message: String) {
        uiState = uiState.copy(message = message)
    }

    fun openCreateWishlistDialog() {
        uiState = uiState.copy(showCreateWishlistDialog = true)
    }

    fun closeCreateWishlistDialog() {
        uiState = uiState.copy(showCreateWishlistDialog = false)
    }

    fun updatePublicShareCode(value: String) {
        uiState = uiState.copy(publicShareCode = value)
    }

    fun switchUser(userId: String) {
        launchAction {
            userRepository.switchCurrentUser(userId)
            refreshSnapshot()
            val selectedWishlistId = uiState.selectedWishlist?.id
            if (selectedWishlistId != null) {
                runCatching { loadWishlistDetails(selectedWishlistId) }
                    .onFailure {
                        uiState = uiState.copy(
                            currentScreen = HomeScreen.MY_WISHLISTS,
                            selectedWishlist = null,
                            selectedGiftItems = emptyList(),
                            giftEditorState = null
                        )
                    }
            }
            if (uiState.publicShareCode.isNotBlank()) {
                runCatching { loadPublicWishlist(uiState.publicShareCode) }
            }
        }
    }

    fun createWishlist(title: String, description: String, isShared: Boolean, shareCode: String) {
        launchAction {
            createWishlistUseCase(
                title = title,
                description = description,
                isShared = isShared,
                shareCode = shareCode
            )
            analyticsService.trackEvent(
                name = "wishlist_created",
                params = mapOf("is_shared" to isShared)
            )
            refreshSnapshot()
            uiState = uiState.copy(
                currentScreen = HomeScreen.MY_WISHLISTS,
                showCreateWishlistDialog = false,
                message = "Wishlist created"
            )
        }
    }

    fun openWishlist(wishlistId: String) {
        launchAction {
            loadWishlistDetails(wishlistId)
            uiState = uiState.copy(currentScreen = HomeScreen.WISHLIST_DETAILS)
        }
    }

    fun openGiftEditorForNew() {
        val wishlist = uiState.selectedWishlist ?: return
        uiState = uiState.copy(
            currentScreen = HomeScreen.GIFT_EDITOR,
            giftEditorState = GiftEditorState(wishlistId = wishlist.id)
        )
    }

    fun openGiftEditorForEdit(giftItemId: String) {
        launchAction {
            val wishlist = uiState.selectedWishlist ?: return@launchAction
            val gift = giftItemRepository.getGiftItemById(giftItemId) ?: return@launchAction
            uiState = uiState.copy(
                currentScreen = HomeScreen.GIFT_EDITOR,
                giftEditorState = GiftEditorState(
                    wishlistId = wishlist.id,
                    giftItemId = gift.id,
                    title = gift.title,
                    description = gift.description.orEmpty(),
                    link = gift.link.orEmpty(),
                    price = gift.price?.toString().orEmpty(),
                    priority = gift.priority
                )
            )
        }
    }

    fun closeGiftEditor() {
        uiState = uiState.copy(currentScreen = HomeScreen.WISHLIST_DETAILS, giftEditorState = null)
    }

    fun saveGift(editorState: GiftEditorState) {
        launchAction {
            val parsedPrice = editorState.price.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            if (editorState.price.isNotBlank() && parsedPrice == null) {
                throw DomainException("Price must be a valid number")
            }

            if (editorState.giftItemId == null) {
                addGiftItemUseCase(
                    wishlistId = editorState.wishlistId,
                    title = editorState.title,
                    description = editorState.description,
                    link = editorState.link,
                    price = parsedPrice,
                    priority = editorState.priority
                )
            } else {
                updateGiftItemUseCase(
                    giftItemId = editorState.giftItemId,
                    title = editorState.title,
                    description = editorState.description,
                    link = editorState.link,
                    price = parsedPrice,
                    priority = editorState.priority
                )
            }

            loadWishlistDetails(editorState.wishlistId)
            uiState = uiState.copy(
                currentScreen = HomeScreen.WISHLIST_DETAILS,
                giftEditorState = null,
                message = if (editorState.giftItemId == null) "Gift added" else "Gift updated"
            )
        }
    }

    fun loadPublicWishlistFromInput() {
        launchAction {
            loadPublicWishlist(uiState.publicShareCode)
            uiState = uiState.copy(currentScreen = HomeScreen.PUBLIC_WISHLIST)
        }
    }

    fun reserveGift(giftItemId: String) {
        launchAction {
            reserveGiftItemUseCase(giftItemId)
            refreshSnapshot()
            if (uiState.publicShareCode.isNotBlank()) {
                loadPublicWishlist(uiState.publicShareCode)
            }
            uiState = uiState.copy(message = "Gift reserved")
        }
    }

    fun cancelReservation(reservationId: String) {
        launchAction {
            cancelReservationUseCase(reservationId)
            refreshSnapshot()
            if (uiState.publicShareCode.isNotBlank()) {
                runCatching { loadPublicWishlist(uiState.publicShareCode) }
            }
            uiState = uiState.copy(message = "Reservation cancelled")
        }
    }

    fun markGiftAsGifted(reservationId: String) {
        launchAction {
            markGiftAsGiftedUseCase(reservationId)
            refreshSnapshot()
            if (uiState.publicShareCode.isNotBlank()) {
                runCatching { loadPublicWishlist(uiState.publicShareCode) }
            }
            uiState = uiState.copy(message = "Gift marked as gifted")
        }
    }

    private fun refreshAppData() {
        launchAction {
            refreshSnapshot()
        }
    }

    private suspend fun refreshSnapshot() {
        val users = userRepository.getAllUsers()
        val currentUser = userRepository.getCurrentUser()
        val wishlists = getMyWishlistsUseCase()
        val reservations = getMyReservationsUseCase()
        val reservationCards = reservations.mapNotNull { reservation ->
            val gift = giftItemRepository.getGiftItemById(reservation.giftItemId) ?: return@mapNotNull null
            val wishlist = wishlistRepository.getWishlistById(gift.wishlistId) ?: return@mapNotNull null
            val owner = userRepository.getUserById(wishlist.ownerUserId) ?: return@mapNotNull null
            ReservationCardState(
                reservation = reservation,
                giftTitle = gift.title,
                wishlistTitle = wishlist.title,
                ownerName = owner.name
            )
        }

        uiState = uiState.copy(
            users = users,
            currentUser = currentUser,
            myWishlists = wishlists,
            reservationCards = reservationCards
        )
        ensureRealtimeSubscription(currentUser?.id)
    }

    private fun ensureRealtimeSubscription(ownerUserId: String?) {
        if (!shouldUseFirestoreRealtime) return
        val repository = realtimeWishlistRepository ?: return
        if (ownerUserId.isNullOrBlank() || observedOwnerUserId == ownerUserId) return

        observedOwnerUserId = ownerUserId
        launchInScope {
            repository.observeWishlistsByOwner(ownerUserId).collect { wishlists ->
                uiState = uiState.copy(myWishlists = wishlists)
            }
        }
    }

    private suspend fun loadWishlistDetails(wishlistId: String) {
        val wishlist = getWishlistDetailsUseCase(wishlistId)
        val giftItems = getGiftItemsForWishlistUseCase(wishlistId)
        uiState = uiState.copy(
            selectedWishlist = wishlist,
            selectedGiftItems = giftItems
        )
    }

    private suspend fun loadPublicWishlist(shareCode: String) {
        val wishlist = getWishlistByShareCodeUseCase(shareCode)
        val giftItems = giftItemRepository.getGiftItemsByWishlistId(wishlist.id)
            .filter { it.status != GiftItemStatus.GIFTED }

        uiState = uiState.copy(
            publicWishlist = wishlist,
            publicGiftItems = giftItems,
            publicShareCode = shareCode
        )
    }

    private fun launchAction(block: suspend () -> Unit) {
        launchInScope {
            uiState = uiState.copy(isLoading = true)
            runCatching { block() }
                .onFailure { throwable ->
                    analyticsService.trackError(
                        message = throwable.message ?: CoreConstants.DEFAULT_ERROR_MESSAGE,
                        error = throwable
                    )
                    uiState = uiState.copy(message = throwable.message ?: CoreConstants.DEFAULT_ERROR_MESSAGE)
                }
            uiState = uiState.copy(isLoading = false)
        }
    }
}

