package com.example.aasra.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.tasks.await

class AuthManager {

    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isSignedIn: Boolean
        get() = auth.currentUser != null

    /** True only for a real authority/volunteer login (not the anonymous guest session). */
    val isAuthority: Boolean
        get() = auth.currentUser?.isAnonymous == false

    suspend fun ensureSignedIn(): Boolean {
        return try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signInAuthority(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account exists for this email. Create one in Firebase Console → Authentication → Users."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Wrong email or password."))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Account conflict — try again."))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("No internet connection. Check your network and try again."))
        } catch (e: Exception) {
            val msg = e.message ?: ""
            val friendly = when {
                msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
                    "Email/Password sign-in isn't enabled yet. Turn it on in Firebase Console → Authentication → Sign-in method."
                else -> "Couldn't sign in (${e.javaClass.simpleName}). $msg"
            }
            Result.failure(Exception(friendly))
        }
    }

    suspend fun signOutAuthority() {
        auth.signOut()
        ensureSignedIn()
    }
}