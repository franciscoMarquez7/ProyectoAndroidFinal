package com.example.proyecto_francisco_marquez.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class InicioConGoogle : ActivityResultContract<Unit, Intent?>() {

    private var googleSignInClient: GoogleSignInClient? = null

    override fun createIntent(context: Context, input: Unit): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("262603989783-v701ebi3au08lifp0m457eao9l7rqi3t.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso).apply {
            signOut() // Aseguramos que no hay sesión previa
        }
        
        val signInIntent = googleSignInClient?.signInIntent ?: Intent()
        Log.d("InicioConGoogle", "Intent de inicio de sesión creado")
        return signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
        Log.d("InicioConGoogle", "Parseando resultado. ResultCode: $resultCode")
        
        return when (resultCode) {
            android.app.Activity.RESULT_OK -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        Log.d("InicioConGoogle", "Cuenta de Google obtenida exitosamente: ${account.email}")
                        intent
                    } else {
                        Log.e("InicioConGoogle", "La cuenta de Google es null")
                        null
                    }
                } catch (e: ApiException) {
                    Log.e("InicioConGoogle", "Error de Google Sign In: código ${e.statusCode}: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("InicioConGoogle", "Error procesando resultado: ${e.message}")
                    null
                }
            }
            android.app.Activity.RESULT_CANCELED -> {
                Log.d("InicioConGoogle", "Inicio de sesión cancelado por el usuario")
                null
            }
            else -> {
                Log.e("InicioConGoogle", "Resultado inesperado. ResultCode: $resultCode")
                null
            }
        }
    }
}
