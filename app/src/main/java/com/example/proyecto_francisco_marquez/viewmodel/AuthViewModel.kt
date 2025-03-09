package com.example.proyecto_francisco_marquez.viewmodel

import android.content.Intent
import android.util.Log
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _userState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val userState: StateFlow<FirebaseUser?> = _userState

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _userState.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _userState.value = result.user
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error de autenticación: ${e.message}")
                onError(e.message ?: "Error de autenticación")
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
        viewModelScope.launch {
            try {
                auth.signOut()
                // No necesitamos actualizar _userState aquí porque el AuthStateListener lo hará
                Log.d("AuthViewModel", "Sesión cerrada exitosamente")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al cerrar sesión: ${e.message}")
            }
        }
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    val credential = GoogleAuthProvider.getCredential(token, null)
                    try {
                        val result = auth.signInWithCredential(credential).await()
                        _userState.value = result.user
                        Log.d("AuthViewModel", "Google Sign-In exitoso: ${result.user?.email}")
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Error en Firebase Auth: ${e.message}")
                        _userState.value = null
                        throw Exception("Error de autenticación con Firebase: ${e.message}")
                    }
                } ?: run {
                    Log.e("AuthViewModel", "Token de Google es null")
                    _userState.value = null
                    throw Exception("No se pudo obtener el token de Google")
                }
            } catch (e: ApiException) {
                Log.e("AuthViewModel", "Google Sign-In falló con código: ${e.statusCode}")
                _userState.value = null
                val errorMessage = when (e.statusCode) {
                    12500 -> "Error de configuración del proyecto en Google Cloud"
                    10 -> "Error de configuración de SHA-1 en Firebase"
                    7 -> "Red no disponible"
                    else -> "Error en inicio de sesión con Google (código: ${e.statusCode})"
                }
                throw Exception(errorMessage)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error general: ${e.message}")
                _userState.value = null
                throw e
            }
        }
    }
}
