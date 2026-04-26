package com.example.wish_list.ui

import com.example.wish_list.data.DemoDataContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelAnalyticsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_tracksScreenViewedEvent() {
        val analytics = FakeAnalyticsService()

        WishlistViewModel(
            container = DemoDataContainer(),
            analyticsService = analytics
        )

        val event = analytics.events.firstOrNull { it.name == "screen_viewed" }
        assertTrue(event != null)
        assertEquals("my_wishlists", event?.params?.get("screen_name"))
    }

    @Test
    fun createWishlist_tracksWishlistCreatedEvent() = runTest {
        val analytics = FakeAnalyticsService()
        val viewModel = WishlistViewModel(
            container = DemoDataContainer(),
            analyticsService = analytics
        )

        viewModel.createWishlist(
            title = "Lab test wishlist",
            description = "Created in unit test",
            isShared = true,
            shareCode = "LABTEST42"
        )
        advanceUntilIdle()

        val event = analytics.events.firstOrNull { it.name == "wishlist_created" }
        assertTrue(event != null)
        assertEquals(true, event?.params?.get("is_shared"))
    }

    @Test
    fun saveGift_withInvalidPrice_tracksError() = runTest {
        val analytics = FakeAnalyticsService()
        val viewModel = WishlistViewModel(
            container = DemoDataContainer(),
            analyticsService = analytics
        )

        viewModel.saveGift(
            GiftEditorState(
                wishlistId = "wishlist_1",
                title = "Invalid price gift",
                price = "not_a_number"
            )
        )
        advanceUntilIdle()

        val error = analytics.errors.firstOrNull()
        assertTrue(error != null)
        assertEquals("Price must be a valid number", error?.message)
    }

    @Test
    fun showAbout_switchesScreenToAbout() {
        val viewModel = WishlistViewModel(
            container = DemoDataContainer(),
            analyticsService = FakeAnalyticsService()
        )

        viewModel.showAbout()

        assertEquals(HomeScreen.ABOUT, viewModel.uiState.currentScreen)
    }
}
