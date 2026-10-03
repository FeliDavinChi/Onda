package dev.socialmusic.common

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Ready<T>(val value: T) : LoadState<T>
    data object Empty : LoadState<Nothing>
    data object Failed : LoadState<Nothing>
}

enum class VisualEffectLevel { FULL, REDUCED, MINIMAL }
