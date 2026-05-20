package com.example.wish_list.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.wish_list.core.util.PriceFormatter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.wish_list.domain.model.GiftItem
import com.example.wish_list.domain.model.GiftItemStatus
import com.example.wish_list.domain.model.GiftPriority
import com.example.wish_list.domain.model.User
import com.example.wish_list.domain.model.UserProfile
import com.example.wish_list.domain.model.Wishlist
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun WishlistApp(
    viewModel: WishlistViewModel,
    displayUserName: String,
    greetingText: String,
    onLogout: () -> Unit,
    onOpenAboutUs: () -> Unit
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    if (uiState.showCreateWishlistDialog) {
        CreateWishlistDialog(
            onDismiss = viewModel::closeCreateWishlistDialog,
            onConfirm = viewModel::createWishlist
        )
    }

        Scaffold(
            topBar = {
                AppTopBar(
                    currentScreen = uiState.currentScreen,
                    displayUserName = displayUserName,
                    appTitle = greetingText
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                BottomPillBar(
                    currentScreen = uiState.currentScreen,
                    onWishlists = viewModel::showMyWishlists,
                    onPublic = viewModel::showPublicWishlist,
                    onFriends = viewModel::showMyReservations,
                    onProfile = viewModel::showProfile
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(paddingValues)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp, start = 12.dp)
                        .size(180.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                                )
                            ),
                            shape = RoundedCornerShape(999.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 8.dp)
                        .size(220.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0f)
                                )
                            ),
                            shape = RoundedCornerShape(999.dp)
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        AnimatedContent(targetState = uiState.currentScreen, label = "screen_transition") { screen ->
                            when (screen) {
                            HomeScreen.MY_WISHLISTS -> MyWishlistsScreen(
                                wishlists = uiState.myWishlists,
                                onCreateWishlist = viewModel::openCreateWishlistDialog,
                                onOpenWishlist = viewModel::openWishlist
                            )

                            HomeScreen.WISHLIST_DETAILS -> WishlistDetailsScreen(
                                wishlist = uiState.selectedWishlist,
                                giftItems = uiState.selectedGiftItems,
                                onAddGift = viewModel::openGiftEditorForNew,
                                onEditGift = viewModel::openGiftEditorForEdit
                            )

                            HomeScreen.GIFT_EDITOR -> uiState.giftEditorState?.let { editorState ->
                                GiftEditorScreen(
                                    state = editorState,
                                    onBack = viewModel::closeGiftEditor,
                                    onSave = viewModel::saveGift
                                )
                            }

                            HomeScreen.PUBLIC_WISHLIST -> PublicWishlistScreen(
                                shareCode = uiState.publicShareCode,
                                wishlist = uiState.publicWishlist,
                                ownerName = uiState.publicWishlistOwnerName,
                                giftItems = uiState.publicGiftItems,
                                currentUser = uiState.currentUser,
                                onShareCodeChanged = viewModel::updatePublicShareCode,
                                onLoad = viewModel::loadPublicWishlistFromInput,
                                onReserve = viewModel::reserveGift
                            )

                            HomeScreen.MY_RESERVATIONS -> MyReservationsScreen(
                                reservationCards = uiState.reservationCards,
                                onCancelReservation = viewModel::cancelReservation,
                                onMarkGifted = viewModel::markGiftAsGifted
                            )

                            HomeScreen.PROFILE -> ProfileScreen(
                                profile = uiState.userProfile,
                                currentUser = uiState.currentUser,
                                authorizedUserId = uiState.authorizedUserId,
                                authorizedUserName = uiState.authorizedUserName,
                                authorizedUserEmail = uiState.authorizedUserEmail,
                                displayUserName = displayUserName,
                                onOpenAbout = onOpenAboutUs,
                                onLogout = onLogout
                            )

                            HomeScreen.ABOUT -> AboutUsRedirectScreen(
                                onOpenAboutUs = onOpenAboutUs,
                                onReturnToProfile = viewModel::showProfile
                            )
                        }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
            }
        }
}

@Composable
private fun AboutUsRedirectScreen(
    onOpenAboutUs: () -> Unit,
    onReturnToProfile: () -> Unit
) {
    LaunchedEffect(Unit) {
        onOpenAboutUs()
        onReturnToProfile()
    }
    EmptyState("Opening About us...")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentScreen: HomeScreen,
    displayUserName: String,
    appTitle: String
) {
    val resolvedTitle = appTitle.ifBlank { "dreamboard" }
    TopAppBar(
        title = {
            Column {
                Text(
                    text = resolvedTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentScreen.title(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {}
    )
}

private fun HomeScreen.menuScreen(): HomeScreen =
    when (this) {
        HomeScreen.WISHLIST_DETAILS,
        HomeScreen.GIFT_EDITOR -> HomeScreen.MY_WISHLISTS
        else -> this
    }

private fun HomeScreen.title(): String =
    when (this) {
        HomeScreen.MY_WISHLISTS -> "My wishlists"
        HomeScreen.WISHLIST_DETAILS -> "Wishlist details"
        HomeScreen.GIFT_EDITOR -> "Gift editor"
        HomeScreen.PUBLIC_WISHLIST -> "Public view"
        HomeScreen.MY_RESERVATIONS -> "My reservations"
        HomeScreen.PROFILE -> "Profile"
        HomeScreen.ABOUT -> "About us"
    }

@Composable
private fun MyWishlistsScreen(
    wishlists: List<Wishlist>,
    onCreateWishlist: () -> Unit,
    onOpenWishlist: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FrostedCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("My wishlists", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${wishlists.size} collections", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onCreateWishlist) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("New")
                }
            }
        }
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            FrostedCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Create and share wishlists in a couple of taps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (wishlists.isEmpty()) {
            EmptyState("No wishlist yet. Create one to start.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wishlists, key = { it.id }) { wishlist ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenWishlist(wishlist.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(wishlist.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(wishlist.description ?: "No description")
                            Text(
                                text = "Share code: ${wishlist.shareCode} - ${if (wishlist.isShared) "shared" else "private"}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistDetailsScreen(
    wishlist: Wishlist?,
    giftItems: List<GiftItem>,
    onAddGift: () -> Unit,
    onEditGift: (String) -> Unit
) {
    if (wishlist == null) {
        EmptyState("Wishlist was not loaded.")
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(wishlist.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(wishlist.description ?: "No description")
                Text(
                    text = "Share code: ${wishlist.shareCode}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = onAddGift) {
                Text("Add gift")
            }
        }
        if (giftItems.isEmpty()) {
            EmptyState("No gifts added to this wishlist yet.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(giftItems, key = { it.id }) { gift ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(gift.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(gift.description ?: "No description")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GiftPriorityTag(priority = gift.priority)
                                GiftStatusTag(status = gift.status)
                            }
                            Text("Price: ${PriceFormatter.formatOrDash(gift.price)}")
                            gift.link?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(onClick = { onEditGift(gift.id) }) {
                                Text("Edit")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GiftEditorScreen(
    state: GiftEditorState,
    onBack: () -> Unit,
    onSave: (GiftEditorState) -> Unit
) {
    var title by remember(state) { mutableStateOf(state.title) }
    var description by remember(state) { mutableStateOf(state.description) }
    var link by remember(state) { mutableStateOf(state.link) }
    var price by remember(state) { mutableStateOf(state.price) }
    var priority by remember(state) { mutableStateOf(state.priority) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FrostedCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (state.giftItemId == null) "Add gift" else "Edit gift",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") }, shape = RoundedCornerShape(20.dp), colors = OutlinedTextFieldDefaults.colors())
                OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Description") }, shape = RoundedCornerShape(20.dp), colors = OutlinedTextFieldDefaults.colors())
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Link") },
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                Text("Priority", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GiftPriority.entries.forEach { itemPriority ->
                        SelectablePriorityTag(
                            priority = itemPriority,
                            selected = priority == itemPriority,
                            onClick = { priority = itemPriority }
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    onSave(
                        state.copy(
                            title = title,
                            description = description,
                            link = link,
                            price = price,
                            priority = priority
                        )
                    )
                }
            ) {
                Text("Save")
            }
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun PublicWishlistScreen(
    shareCode: String,
    wishlist: Wishlist?,
    ownerName: String?,
    giftItems: List<GiftItem>,
    currentUser: User?,
    onShareCodeChanged: (String) -> Unit,
    onLoad: () -> Unit,
    onReserve: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Public wishlist by link", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Demo share codes: ALICE2026, BOBTECH", color = MaterialTheme.colorScheme.primary)
        FrostedCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = shareCode,
                    onValueChange = onShareCodeChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("Share code") },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                Button(onClick = onLoad) {
                    Text("Open")
                }
            }
        }

        wishlist?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(it.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(it.description ?: "No description")
                    Text(
                        text = "Owner: ${ownerName ?: it.ownerUserId}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (wishlist == null) {
            EmptyState("Enter a share code to load a shared wishlist.")
        } else if (giftItems.isEmpty()) {
            EmptyState("No visible gifts in this shared wishlist.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(giftItems, key = { it.id }) { gift ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(gift.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(gift.description ?: "No description")
                            GiftPriorityTag(priority = gift.priority)
                            GiftStatusTag(status = gift.status)
                            if (gift.status == GiftItemStatus.AVAILABLE) {
                                Button(onClick = { onReserve(gift.id) }) {
                                    Text("Reserve")
                                }
                            } else {
                                Text("Already taken", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyReservationsScreen(
    reservationCards: List<ReservationCardState>,
    onCancelReservation: (String) -> Unit,
    onMarkGifted: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("My reservations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (reservationCards.isEmpty()) {
            EmptyState("You have no reservations yet.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reservationCards, key = { it.reservation.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(item.giftTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Wishlist: ${item.wishlistTitle}")
                            Text("Owner: ${item.ownerName}")
                            ReservationStatusTag(status = item.reservation.status.name)
                            if (item.reservation.status.name == "ACTIVE") {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(onClick = { onMarkGifted(item.reservation.id) }) {
                                        Text("Mark gifted")
                                    }
                                    OutlinedButton(onClick = { onCancelReservation(item.reservation.id) }) {
                                        Text("Cancel")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GiftStatusTag(status: GiftItemStatus) {
    val (label, color) = when (status) {
        GiftItemStatus.AVAILABLE -> "Available" to MaterialTheme.colorScheme.secondary
        GiftItemStatus.RESERVED -> "Reserved" to MaterialTheme.colorScheme.tertiary
        GiftItemStatus.GIFTED -> "Gifted" to MaterialTheme.colorScheme.primary
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun GiftPriorityTag(priority: GiftPriority) {
    val (label, color) = when (priority) {
        GiftPriority.LOW -> "Low" to MaterialTheme.colorScheme.secondary
        GiftPriority.MEDIUM -> "Medium" to MaterialTheme.colorScheme.tertiary
        GiftPriority.HIGH -> "High" to MaterialTheme.colorScheme.primary
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f))
    ) {
        Text(
            text = "Priority: $label",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun SelectablePriorityTag(
    priority: GiftPriority,
    selected: Boolean,
    onClick: () -> Unit
) {
    val (_, color) = when (priority) {
        GiftPriority.LOW -> "Low" to MaterialTheme.colorScheme.secondary
        GiftPriority.MEDIUM -> "Medium" to MaterialTheme.colorScheme.tertiary
        GiftPriority.HIGH -> "High" to MaterialTheme.colorScheme.primary
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) color.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, if (selected) color.copy(alpha = 0.75f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ReservationStatusTag(status: String) {
    val normalized = status.uppercase()
    val (label, color) = when (normalized) {
        "ACTIVE" -> "Active" to MaterialTheme.colorScheme.tertiary
        "GIFTED" -> "Gifted" to MaterialTheme.colorScheme.primary
        "CANCELLED" -> "Cancelled" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> status to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ProfileScreen(
    profile: UserProfile?,
    currentUser: User?,
    authorizedUserId: String?,
    authorizedUserName: String?,
    authorizedUserEmail: String?,
    displayUserName: String,
    onOpenAbout: () -> Unit,
    onLogout: () -> Unit
) {
    val resolvedName = profile?.name?.takeIf { it.isNotBlank() }
        ?: authorizedUserName?.takeIf { it.isNotBlank() }
        ?: currentUser?.name?.takeIf { it.isNotBlank() }
        ?: displayUserName
    val resolvedEmail = profile?.email?.takeIf { it.isNotBlank() }
        ?: authorizedUserEmail?.takeIf { it.isNotBlank() }
        ?: "Not provided"
    val resolvedUserId = profile?.userId?.takeIf { it.isNotBlank() }
        ?: authorizedUserId?.takeIf { it.isNotBlank() }
        ?: currentUser?.id?.takeIf { it.isNotBlank() }
        ?: "Unknown"
    val resolvedToken = profile?.fcmToken?.takeIf { it.isNotBlank() } ?: "-"
    val resolvedUpdatedAt = profile?.updatedAtMillis?.takeIf { it > 0L }?.toString() ?: "-"

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FrostedCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Name: $resolvedName")
                Text("Email: $resolvedEmail")
                Text("User ID: $resolvedUserId")
                Text("FCM token: $resolvedToken")
                Text("Updated at: $resolvedUpdatedAt")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onOpenAbout, modifier = Modifier.weight(1f)) {
                Text("About us")
            }
            OutlinedButton(onClick = onLogout, modifier = Modifier.weight(1f)) {
                Text("Logout")
            }
        }
    }
}

@Composable
private fun CreateWishlistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var shareCode by remember { mutableStateOf("") }
    var isShared by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Create wishlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") }, shape = RoundedCornerShape(20.dp), colors = OutlinedTextFieldDefaults.colors())
                OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Description") }, shape = RoundedCornerShape(20.dp), colors = OutlinedTextFieldDefaults.colors())
                OutlinedTextField(
                    value = shareCode,
                    onValueChange = { shareCode = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Share code") },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                FilterChip(
                    selected = isShared,
                    onClick = { isShared = !isShared },
                    label = { Text(if (isShared) "Shared" else "Private") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, description, isShared, shareCode) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BottomPillBar(
    currentScreen: HomeScreen,
    onWishlists: () -> Unit,
    onPublic: () -> Unit,
    onFriends: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BottomTabItem(
                    selected = currentScreen.menuScreen() == HomeScreen.MY_WISHLISTS,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = onWishlists
                )
                BottomTabItem(
                    selected = currentScreen.menuScreen() == HomeScreen.PUBLIC_WISHLIST,
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    onClick = onPublic
                )
                BottomTabItem(
                    selected = currentScreen.menuScreen() == HomeScreen.MY_RESERVATIONS,
                    icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                    onClick = onFriends
                )
                BottomTabItem(
                    selected = currentScreen.menuScreen() == HomeScreen.PROFILE,
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    onClick = onProfile
                )
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
        }
    }
}

@Composable
private fun FrostedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

