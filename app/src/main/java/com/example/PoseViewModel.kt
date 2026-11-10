package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PoseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PoseRepository = PoseRepository(
        PoseDatabase.getInstance(application).poseDao()
    )

    private val _selectedCategory = MutableStateFlow("All Poses")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isResolvingUrl = MutableStateFlow(false)
    val isResolvingUrl: StateFlow<Boolean> = _isResolvingUrl.asStateFlow()

    private val _urlError = MutableStateFlow<String?>(null)
    val urlError: StateFlow<String?> = _urlError.asStateFlow()

    val allPoses: StateFlow<List<PoseEntity>> = repository.allPoses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredPoses: StateFlow<List<PoseEntity>> = combine(
        repository.allPoses,
        _selectedCategory
    ) { poses, category ->
        when (category) {
            "All Poses" -> poses
            "Favorites" -> poses.filter { it.isFavorite }
            "Recent" -> poses.sortedByDescending { it.timestamp }.take(6)
            else -> poses.filter {
                it.category.equals(category, ignoreCase = true) ||
                    it.tagBadge.equals(category, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(pose: PoseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePose(pose.copy(isFavorite = !pose.isFavorite))
        }
    }

    fun renameOrUpdatePose(
        pose: PoseEntity,
        newName: String,
        newAlpha: Float,
        newCategory: String,
        newTagBadge: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePose(
                pose.copy(
                    name = newName.trim().ifEmpty { pose.name },
                    alpha = newAlpha.coerceIn(0.1f, 1.0f),
                    category = newCategory,
                    tagBadge = newTagBadge
                )
            )
        }
    }

    fun deletePose(pose: PoseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePose(pose)
        }
    }

    fun insertPose(
        name: String,
        imageUri: String,
        alpha: Float = 0.40f,
        category: String = "Portraits",
        tagBadge: String = "Studio",
        isMirrored: Boolean = false,
        quickShortcut: Boolean = true,
        onInserted: (PoseEntity) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = PoseEntity(
                name = name.trim().ifEmpty { "Custom Pose" },
                imageUri = imageUri,
                alpha = alpha.coerceIn(0.1f, 1.0f),
                timestamp = System.currentTimeMillis(),
                isFavorite = true,
                category = category,
                tagBadge = tagBadge,
                isMirrored = isMirrored,
                quickShortcut = quickShortcut
            )
            val id = repository.insertPose(entity)
            val saved = entity.copy(id = id)
            launch(Dispatchers.Main) {
                onInserted(saved)
            }
        }
    }

    fun resolveWebOrPinterestLink(
        rawUrlOrText: String,
        onResolved: (ResolvedPoseMedia) -> Unit
    ) {
        _urlError.value = null
        _isResolvingUrl.value = true
        viewModelScope.launch {
            val result = LinkResolver.resolveSharedUrl(rawUrlOrText, getApplication())
            _isResolvingUrl.value = false
            result.fold(
                onSuccess = { media ->
                    onResolved(media)
                },
                onFailure = { error ->
                    _urlError.value = error.localizedMessage ?: "Failed to resolve image from URL."
                }
            )
        }
    }

    fun clearUrlError() {
        _urlError.value = null
    }
}
