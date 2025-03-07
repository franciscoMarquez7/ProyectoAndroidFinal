package com.example.proyecto_francisco_marquez.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_francisco_marquez.data.FirestoreService
import com.example.proyecto_francisco_marquez.model.UiState
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

sealed class NavigationEvent {
    object None : NavigationEvent()
    data class NavigateToCharacterDetail(val characterId: String) : NavigationEvent()
    data class NavigateToEpisodeDetail(val episodeId: String) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    data class ShowError(val message: String) : NavigationEvent()
    data class ShowSuccess(val message: String) : NavigationEvent()
}

data class AppState(
    val isLoading: Boolean = false,
    val currentScreen: String = "",
    val previousScreen: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

class SharedViewModel : ViewModel() {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val firestoreService = FirestoreService()

    private val _navigationEvent = MutableStateFlow<NavigationEvent>(NavigationEvent.None)
    val navigationEvent: StateFlow<NavigationEvent> = _navigationEvent.asStateFlow()

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    // Cache compartida para datos frecuentemente utilizados
    private val dataCache = ConcurrentHashMap<String, Any>()
    private val cacheValidityDuration = 10 * 60 * 1000L // 10 minutos

    private var sharedJob: Job? = null
    private val debounceDelay = 300L

    init {
        setupErrorHandling()
        setupStateTracking()
    }

    private fun setupErrorHandling() {
        viewModelScope.launch {
            navigationEvent
                .filterIsInstance<NavigationEvent.ShowError>()
                .collect { event ->
                    crashlytics.recordException(Exception(event.message))
                }
        }
    }

    private fun setupStateTracking() {
        viewModelScope.launch {
            appState
                .debounce(debounceDelay)
                .collect { state ->
                    crashlytics.setCustomKey("current_screen", state.currentScreen)
                    crashlytics.setCustomKey("is_loading", state.isLoading)
                    crashlytics.setCustomKey("last_updated", state.lastUpdated)
                }
        }
    }

    fun updateCurrentScreen(screenName: String) {
        _appState.update { currentState ->
            currentState.copy(
                currentScreen = screenName,
                previousScreen = currentState.currentScreen,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    fun setLoading(isLoading: Boolean) {
        _appState.update { it.copy(isLoading = isLoading) }
    }

    fun navigateToCharacterDetail(characterId: String) {
        executeNavigationEvent {
            _navigationEvent.value = NavigationEvent.NavigateToCharacterDetail(characterId)
        }
    }

    fun navigateToEpisodeDetail(episodeId: String) {
        executeNavigationEvent {
            _navigationEvent.value = NavigationEvent.NavigateToEpisodeDetail(episodeId)
        }
    }

    fun navigateBack() {
        executeNavigationEvent {
            _navigationEvent.value = NavigationEvent.NavigateBack
        }
    }

    fun showError(message: String) {
        executeNavigationEvent {
            _navigationEvent.value = NavigationEvent.ShowError(message)
        }
    }

    fun showSuccess(message: String) {
        executeNavigationEvent {
            _navigationEvent.value = NavigationEvent.ShowSuccess(message)
        }
    }

    fun <T> getCachedData(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return dataCache[key]?.let { data ->
            val timestamp = dataCache["${key}_timestamp"] as? Long
            if (timestamp != null && System.currentTimeMillis() - timestamp < cacheValidityDuration) {
                data as T
            } else {
                dataCache.remove(key)
                dataCache.remove("${key}_timestamp")
                null
            }
        }
    }

    fun <T> setCachedData(key: String, data: T) {
        dataCache[key] = data as Any
        dataCache["${key}_timestamp"] = System.currentTimeMillis()
    }

    fun clearCache(key: String? = null) {
        if (key != null) {
            dataCache.remove(key)
            dataCache.remove("${key}_timestamp")
        } else {
            dataCache.clear()
        }
    }

    private fun executeNavigationEvent(event: suspend () -> Unit) {
        sharedJob?.cancel()
        sharedJob = viewModelScope.launch {
            try {
                event()
                delay(debounceDelay)
                resetNavigationEvent()
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _navigationEvent.value = NavigationEvent.ShowError(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetNavigationEvent() {
        _navigationEvent.value = NavigationEvent.None
    }

    override fun onCleared() {
        super.onCleared()
        sharedJob?.cancel()
        clearCache()
    }

    // Métodos de utilidad para compartir datos entre pantallas
    suspend fun shareCharacterWithEpisode(characterId: String, episodeId: String): Boolean {
        return try {
            setLoading(true)
            val result = firestoreService.addCharacterToEpisode(characterId, episodeId)
            when (result) {
                is UiState.Success -> {
                    showSuccess("Personaje añadido al episodio correctamente")
                    true
                }
                is UiState.Error -> {
                    showError(result.message)
                    false
                }
                else -> false
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            showError(e.message ?: "Error al compartir el personaje")
            false
        } finally {
            setLoading(false)
        }
    }

    suspend fun removeCharacterFromEpisode(characterId: String, episodeId: String): Boolean {
        return try {
            setLoading(true)
            val result = firestoreService.removeCharacterFromEpisode(characterId, episodeId)
            when (result) {
                is UiState.Success -> {
                    showSuccess("Personaje eliminado del episodio correctamente")
                    true
                }
                is UiState.Error -> {
                    showError(result.message)
                    false
                }
                else -> false
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            showError(e.message ?: "Error al eliminar el personaje del episodio")
            false
        } finally {
            setLoading(false)
        }
    }
} 