package com.example.proyecto_francisco_marquez.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _userState = MutableStateFlow<FirebaseUser?>(null)
    val userState: StateFlow<FirebaseUser?> = _userState

    init {
        _userState.value = auth.currentUser
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _userState.value = auth.currentUser
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _userState.value = auth.currentUser
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al registrarse")
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al enviar el correo de recuperación")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _userState.value = null
    }

    fun loginWithGoogle(result: Intent?, onResult: (Boolean, String?) -> Unit) {
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result).result
            if (account?.idToken != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _userState.value = auth.currentUser
                            onResult(true, null)
                        } else {
                            onResult(false, task.exception?.message ?: "Error al iniciar sesión con Google")
                        }
                    }
            } else {
                onResult(false, "No se obtuvo una cuenta válida")
            }
        } catch (e: Exception) {
            onResult(false, "Error en Google Sign-In: ${e.message}")
        }
    }
}
