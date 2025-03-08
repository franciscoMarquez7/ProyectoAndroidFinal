package com.example.proyecto_francisco_marquez

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_francisco_marquez.navegacion.AppNavigation
import com.example.proyecto_francisco_marquez.ui.theme.Proyecto_Francisco_MarquezTheme
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de Firebase
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("Firebase", "Error inicializando Firebase: ${e.message}")
        }

        // Verificación de Google Play Services
        checkGooglePlayServices()

        // Verificar usuario autenticado
        try {
            val auth = FirebaseAuth.getInstance()
            Log.d("AuthCheck", "Usuario actual: ${auth.currentUser?.email ?: "No autenticado"}")
        } catch (e: Exception) {
            Log.e("Auth", "Error verificando autenticación: ${e.message}")
        }

        setContent {
            Proyecto_Francisco_MarquezTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }

    private fun checkGooglePlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 9000)?.show()
            } else {
                Log.e("GooglePlayServices", "Este dispositivo no es compatible con Google Play Services")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.makeGooglePlayServicesAvailable(this)
        }
    }
}

