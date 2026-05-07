package com.example.wish_list.data.repository

import android.util.Log
import com.example.wish_list.data.firestore.toWishlistOrNull
import com.example.wish_list.domain.model.Wishlist
import com.example.wish_list.domain.repository.RealtimeWishlistRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreRealtimeWishlistRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RealtimeWishlistRepository {
    override fun observeWishlistsByOwner(ownerUserId: String): Flow<List<Wishlist>> = callbackFlow {
        val registration = firestore.collection(COLLECTION_WISHLISTS)
            .whereEqualTo(FIELD_OWNER_USER_ID, ownerUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Wishlists listen failed", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val wishlists = snapshot?.documents
                    ?.mapNotNull { it.toWishlistOrNull() }
                    .orEmpty()
                trySend(wishlists)
            }

        awaitClose { registration.remove() }
    }

    private companion object {
        private const val TAG = "FirestoreWishlistRepo"
        private const val COLLECTION_WISHLISTS = "wishlists"
        private const val FIELD_OWNER_USER_ID = "ownerUserId"
    }
}
