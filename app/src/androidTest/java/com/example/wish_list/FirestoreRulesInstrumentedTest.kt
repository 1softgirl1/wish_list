package com.example.wish_list

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirestoreRulesInstrumentedTest {

    @Test
    fun userCanReadAndWriteOwnProfile_only() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Tasks.await(auth.signInAnonymously(), 30, TimeUnit.SECONDS)
        }
        val uid = requireNotNull(auth.currentUser?.uid) { "FirebaseAuth uid is null" }
        val db = FirebaseFirestore.getInstance()

        val ownDoc = db.collection(COLLECTION_USERS).document(uid)
        val profile = mapOf(
            "name" to "Rules Test User",
            "email" to "rules-test@example.com",
            "fcmToken" to "test-token",
            "updatedAt" to System.currentTimeMillis()
        )

        Tasks.await(ownDoc.set(profile), 30, TimeUnit.SECONDS)
        val ownSnapshot = Tasks.await(ownDoc.get(), 30, TimeUnit.SECONDS)
        assertTrue("Own profile should exist", ownSnapshot.exists())
        assertEquals("Rules Test User", ownSnapshot.getString("name"))
    }

    @Test
    fun userCannotReadAnotherUserProfile() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Tasks.await(auth.signInAnonymously(), 30, TimeUnit.SECONDS)
        }
        val uid = requireNotNull(auth.currentUser?.uid) { "FirebaseAuth uid is null" }
        val db = FirebaseFirestore.getInstance()

        val otherUserId = "other_${uid.takeLast(8)}_${UUID.randomUUID()}"
        val foreignDoc = db.collection(COLLECTION_USERS).document(otherUserId)

        try {
            Tasks.await(foreignDoc.get(), 30, TimeUnit.SECONDS)
            throw AssertionError("Expected PERMISSION_DENIED when reading another user profile")
        } catch (e: ExecutionException) {
            val cause = e.cause
            if (cause is FirebaseFirestoreException) {
                assertEquals(
                    FirebaseFirestoreException.Code.PERMISSION_DENIED,
                    cause.code
                )
            } else {
                throw e
            }
        }
    }

    private companion object {
        private const val COLLECTION_USERS = "users"
    }
}
