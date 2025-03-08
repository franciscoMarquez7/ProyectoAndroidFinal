package com.example.proyecto_francisco_marquez.utils

import android.content.Context
import android.content.Intent
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

        googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient?.signInIntent ?: Intent()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
        return try {
            googleSignInClient?.signOut()
            intent
        } catch (e: ApiException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}
