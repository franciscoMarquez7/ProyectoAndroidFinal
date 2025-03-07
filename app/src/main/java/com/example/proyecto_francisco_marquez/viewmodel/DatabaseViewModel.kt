package com.example.proyecto_francisco_marquez.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_francisco_marquez.data.FirestoreService
import com.example.proyecto_francisco_marquez.model.CharacterModel
import com.example.proyecto_francisco_marquez.model.UiState
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import com.example.proyecto_francisco_marquez.data.EpisodesRepository

sealed class DatabaseOperation<T> {
    data class Add<T>(val data: T) : DatabaseOperation<T>()
    data class Update<T>(val id: String, val data: T) : DatabaseOperation<T>()
    data class Delete<T>(val id: String) : DatabaseOperation<T>()
    data class Get<T>(val id: String) : DatabaseOperation<T>()
    class GetAll<T> : DatabaseOperation<T>()
}

sealed class DatabaseEvent {
    object None : DatabaseEvent()
    object Loading : DatabaseEvent()
    object Success : DatabaseEvent()
    data class Error(val message: String) : DatabaseEvent()
}

data class DatabaseState(
    val isLoading: Boolean = false,
    val lastOperation: String = "",
    val lastUpdated: Long = 0L,
    val errorMessage: String? = null
)

class DatabaseViewModel : ViewModel() {
    private val firestoreService = FirestoreService()
    private val episodesRepository = EpisodesRepository(firestoreService)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private val _databaseState = MutableStateFlow(DatabaseState())
    val databaseState: StateFlow<DatabaseState> = _databaseState.asStateFlow()

    private val _databaseEvent = MutableSharedFlow<DatabaseEvent>()
    val databaseEvent = _databaseEvent.asSharedFlow()

    // Cache para operaciones de base de datos
    private val operationCache = ConcurrentHashMap<String, Any>()
    private val operationTimestamps = ConcurrentHashMap<String, Long>()
    private val cacheValidityDuration = 5 * 60 * 1000L // 5 minutos
    private var currentJob: Job? = null

    private val _uiState = MutableStateFlow<UiState<Any>>(UiState.Empty)
    val uiState: StateFlow<UiState<Any>> = _uiState.asStateFlow()

    init {
        setupPeriodicCleanup()
    }

    private fun setupPeriodicCleanup() {
        viewModelScope.launch {
            while (true) {
                delay(300_000) // Limpiar cada 5 minutos
                cleanupStaleData()
            }
        }
    }

    private fun cleanupStaleData() {
        val currentTime = System.currentTimeMillis()
        operationTimestamps.entries.removeIf { (key, timestamp) ->
            val isStale = currentTime - timestamp > cacheValidityDuration
            if (isStale) operationCache.remove(key)
            isStale
        }
    }

    private suspend fun <T> executeOperation(
        operation: DatabaseOperation<T>,
        operationName: String,
        action: suspend () -> UiState<T>
    ) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                _databaseState.value = _databaseState.value.copy(
                    isLoading = true,
                    lastOperation = operationName,
                    errorMessage = null
                )
                _databaseEvent.emit(DatabaseEvent.Loading)

                val cacheKey = "${operationName}_${System.currentTimeMillis()}"
                val result = action()

                when (result) {
                    is UiState.Success -> {
                        operationCache[cacheKey] = result.data as Any
                        operationTimestamps[cacheKey] = System.currentTimeMillis()
                        _databaseEvent.emit(DatabaseEvent.Success)
                    }
                    is UiState.Error -> {
                        crashlytics.recordException(Exception(result.message))
                        _databaseEvent.emit(DatabaseEvent.Error(result.message))
                        _databaseState.value = _databaseState.value.copy(errorMessage = result.message)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _databaseState.value = _databaseState.value.copy(
                    isLoading = false,
                    lastUpdated = System.currentTimeMillis()
                )
            }
        }
    }

    // Operaciones de Personajes
    fun addCharacter(character: CharacterModel) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val characterMap = mapOf(
                    "id" to character.id,
                    "episode_id" to character.episode_id,
                    "name" to character.name,
                    "status" to character.status,
                    "species" to character.species,
                    "imageUrl" to character.imageUrl
                )
                val result = firestoreService.addCharacter(characterMap)
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateCharacter(characterId: String, character: Map<String, Any>) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.updateCharacter(characterId, character)
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteCharacter(characterId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.deleteCharacter(characterId)
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getCharacter(characterId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.getCharacterById(characterId)
                _uiState.value = result
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getAllCharacters() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.getCharacters()
                _uiState.value = result
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Operaciones de Episodios
    fun addEpisode(episode: Map<String, Any>) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.addEpisode(episode)
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateEpisode(episodeId: String, episode: Map<String, Any>) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.updateEpisode(episodeId, episode)
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getEpisode(episodeId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.getEpisodeById(episodeId)
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getAllEpisodes() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.getEpisodes()
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Operaciones de relación Personaje-Episodio
    fun addCharacterToEpisode(characterId: String, episodeId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.addCharacterToEpisode(characterId, episodeId)
                _uiState.value = result
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun removeCharacterFromEpisode(characterId: String, episodeId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.removeCharacterFromEpisode(characterId, episodeId)
                _uiState.value = result
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getCharactersByEpisode(episodeId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.getCharactersByEpisodeId(episodeId)
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteEpisode(episodeId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val result = firestoreService.deleteEpisode(episodeId)
                _uiState.value = result
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun handleError(e: Exception) {
        crashlytics.recordException(e)
        viewModelScope.launch {
            _databaseEvent.emit(DatabaseEvent.Error(e.message ?: "Error desconocido"))
            _databaseState.value = _databaseState.value.copy(
                errorMessage = e.message,
                isLoading = false
            )
        }
    }

    fun getLastOperation(): String = _databaseState.value.lastOperation

    fun getCachedOperation(operationKey: String): Any? {
        val timestamp = operationTimestamps[operationKey] ?: return null
        return if (System.currentTimeMillis() - timestamp < cacheValidityDuration) {
            operationCache[operationKey]
        } else {
            operationCache.remove(operationKey)
            operationTimestamps.remove(operationKey)
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
        operationCache.clear()
        operationTimestamps.clear()
    }
} 