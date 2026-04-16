package com.example.wish_list.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wish_list.core.util.PriceFormatter
import com.example.wish_list.domain.model.GiftItem
import com.example.wish_list.domain.model.GiftItemStatus
import com.example.wish_list.domain.model.GiftPriority
import com.example.wish_list.domain.model.User
import com.example.wish_list.domain.model.Wishlist

@Composable
fun WishlistApp(viewModel: WishlistViewModel) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderSection(
                    users = uiState.users,
                    currentUser = uiState.currentUser,
                    currentScreen = uiState.currentScreen,
                    onUserSelected = viewModel::switchUser,
                    onWishlistsClick = viewModel::showMyWishlists,
                    onPublicClick = viewModel::showPublicWishlist,
                    onReservationsClick = viewModel::showMyReservations
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (uiState.currentScreen) {
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
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun HeaderSection(
    users: List<User>,
    currentUser: User?,
    currentScreen: HomeScreen,
    onUserSelected: (String) -> Unit,
    onWishlistsClick: () -> Unit,
    onPublicClick: () -> Unit,
    onReservationsClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Wish List MVP",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Current user: ${currentUser?.name ?: "Unknown"}",
            style = MaterialTheme.typography.bodyLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            users.forEach { user ->
                FilterChip(
                    selected = currentUser?.id == user.id,
                    onClick = { onUserSelected(user.id) },
                    label = { Text(user.name) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = onWishlistsClick,
                label = { Text(if (currentScreen == HomeScreen.MY_WISHLISTS) "My wishlists *" else "My wishlists") }
            )
            AssistChip(
                onClick = onPublicClick,
                label = { Text(if (currentScreen == HomeScreen.PUBLIC_WISHLIST) "Public view *" else "Public view") }
            )
            AssistChip(
                onClick = onReservationsClick,
                label = { Text(if (currentScreen == HomeScreen.MY_RESERVATIONS) "My reservations *" else "My reservations") }
            )
        }
        HorizontalDivider()
    }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My wishlists", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = onCreateWishlist) {
                Text("Create")
            }
        }
        if (wishlists.isEmpty()) {
            EmptyState("No wishlist yet. Create one to start.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(wishlists, key = { it.id }) { wishlist ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenWishlist(wishlist.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(giftItems, key = { it.id }) { gift ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(gift.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(gift.description ?: "No description")
                            Text("Priority: ${gift.priority.name} - Status: ${gift.status.name}")
                            Text("Price: ${PriceFormatter.formatOrDash(gift.price)}")
                            gift.link?.let {
                                Text(it, color = MaterialTheme.colorScheme.primary)
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
        Text(
            text = if (state.giftItemId == null) "Add gift" else "Edit gift",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") })
        OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Description") })
        OutlinedTextField(value = link, onValueChange = { link = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Link") })
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Text("Priority", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GiftPriority.entries.forEach { itemPriority ->
                FilterChip(
                    selected = priority == itemPriority,
                    onClick = { priority = itemPriority },
                    label = { Text(itemPriority.name) }
                )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = shareCode,
                onValueChange = onShareCodeChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Share code") }
            )
            Button(onClick = onLoad) {
                Text("Open")
            }
        }

        wishlist?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(it.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(it.description ?: "No description")
                    Text(
                        "Opened as ${currentUser?.name ?: "Unknown"}",
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(giftItems, key = { it.id }) { gift ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(gift.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(gift.description ?: "No description")
                            Text("Priority: ${gift.priority.name}")
                            Text("Status: ${gift.status.name}")
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reservationCards, key = { it.reservation.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(item.giftTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Wishlist: ${item.wishlistTitle}")
                            Text("Owner: ${item.ownerName}")
                            Text("Status: ${item.reservation.status.name}")
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
        title = { Text("Create wishlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Description") })
                OutlinedTextField(
                    value = shareCode,
                    onValueChange = { shareCode = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Share code") }
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
