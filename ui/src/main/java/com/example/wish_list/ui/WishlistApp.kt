package com.example.wish_list.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.launch

@Composable
fun WishlistApp(
    viewModel: WishlistViewModel,
    displayUserName: String,
    greetingText: String,
    onLogout: () -> Unit
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    displayUserName = displayUserName,
                    currentScreen = uiState.currentScreen,
                    onWishlistsClick = {
                        viewModel.showMyWishlists()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onPublicClick = {
                        viewModel.showPublicWishlist()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onReservationsClick = {
                        viewModel.showMyReservations()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onProfileClick = {
                        viewModel.showProfile()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onAboutClick = {
                        viewModel.showAbout()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onLogout = {
                        coroutineScope.launch { drawerState.close() }
                        onLogout()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    currentScreen = uiState.currentScreen,
                    displayUserName = displayUserName,
                    greetingText = greetingText,
                    onOpenMenu = { coroutineScope.launch { drawerState.open() } }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )
                    )
                    .padding(paddingValues)
            ) {
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

                            HomeScreen.PROFILE -> ProfileScreen(
                                profile = uiState.userProfile
                            )

                            HomeScreen.ABOUT -> AboutUsScreen(
                                onMessage = viewModel::postMessage
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentScreen: HomeScreen,
    displayUserName: String,
    greetingText: String,
    onOpenMenu: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = currentScreen.title(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = displayUserName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onOpenMenu) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Open navigation menu"
                )
            }
        }
    )
}

@Composable
private fun DrawerContent(
    displayUserName: String,
    currentScreen: HomeScreen,
    onWishlistsClick: () -> Unit,
    onPublicClick: () -> Unit,
    onReservationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit
) {
    val selectedScreen = currentScreen.menuScreen()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Wish List MVP",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Signed in as $displayUserName",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NavigationDrawerItem(
            label = { Text("My wishlists") },
            selected = selectedScreen == HomeScreen.MY_WISHLISTS,
            onClick = onWishlistsClick
        )
        NavigationDrawerItem(
            label = { Text("Public view") },
            selected = selectedScreen == HomeScreen.PUBLIC_WISHLIST,
            onClick = onPublicClick
        )
        NavigationDrawerItem(
            label = { Text("My reservations") },
            selected = selectedScreen == HomeScreen.MY_RESERVATIONS,
            onClick = onReservationsClick
        )
        NavigationDrawerItem(
            label = { Text("Profile") },
            selected = selectedScreen == HomeScreen.PROFILE,
            onClick = onProfileClick
        )
        NavigationDrawerItem(
            label = { Text("About us") },
            selected = selectedScreen == HomeScreen.ABOUT,
            onClick = onAboutClick
        )
        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = onLogout
        )
    }
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
private fun ProfileScreen(profile: UserProfile?) {
    if (profile == null) {
        EmptyState("Profile is loading...")
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("User profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("User ID: ${profile.userId}")
                Text("Name: ${profile.name}")
                Text("Email: ${profile.email.ifBlank { "-" }}")
                Text("FCM token: ${profile.fcmToken.ifBlank { "-" }}")
                Text("Updated at: ${profile.updatedAtMillis}")
            }
        }
    }
}

@Composable
private fun AboutUsScreen(
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val officePoint = remember { Point(55.3519, 86.0911) }
    val mapView = remember { MapView(context) }
    val fusedClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }
    var showOpenSettings by remember { mutableStateOf(false) }

    val requestRoute: () -> Unit = {
        val tokenSource = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                val launched = RouteLauncher.launchRoute(
                    context = context,
                    destinationLat = officePoint.latitude,
                    destinationLon = officePoint.longitude,
                    startLat = location?.latitude,
                    startLon = location?.longitude
                )
                if (!launched) onMessage("Не удалось открыть навигатор")
            }
            .addOnFailureListener {
                val launched = RouteLauncher.launchRoute(
                    context = context,
                    destinationLat = officePoint.latitude,
                    destinationLon = officePoint.longitude
                )
                if (!launched) onMessage("Не удалось открыть маршрут")
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedMap ->
        val fineGranted = grantedMap[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = grantedMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            showOpenSettings = false
            requestRoute()
        } else {
            onMessage("Доступ к геолокации отклонен. Строим маршрут без стартовой точки.")
            val launched = RouteLauncher.launchRoute(
                context = context,
                destinationLat = officePoint.latitude,
                destinationLon = officePoint.longitude
            )
            if (!launched) onMessage("Не удалось открыть маршрут")
            val permanentlyDenied = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
            showOpenSettings = permanentlyDenied
        }
    }

    LaunchedEffect(mapView) {
        mapView.mapWindow.map.move(
            CameraPosition(officePoint, 16.0f, 0.0f, 0.0f)
        )
        mapView.mapWindow.map.mapObjects.clear()
        mapView.mapWindow.map.mapObjects.addPlacemark(officePoint)
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }

                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "О нас",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Kuzbass Wish Lab - локальная команда, которая создает удобные цифровые сервисы для планирования подарков и совместных праздников."
        )
        Text(
            text = "Офис: Россия, Кемеровская область - Кузбасс, г. Кемерово, Красная ул., 6",
            color = MaterialTheme.colorScheme.primary
        )

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(vertical = 4.dp),
            factory = { mapView }
        )

        Button(
            onClick = {
                val fineLocationGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val coarseLocationGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (fineLocationGranted || coarseLocationGranted) {
                    showOpenSettings = false
                    requestRoute()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Построить маршрут до офиса")
        }

        if (showOpenSettings) {
            OutlinedButton(
                onClick = {
                    val intent = Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Открыть настройки приложения")
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
