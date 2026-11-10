package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poses")
data class PoseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val imageUri: String,
    val alpha: Float = 0.40f,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val category: String = "Portraits",
    val tagBadge: String = "Studio",
    val isMirrored: Boolean = false,
    val quickShortcut: Boolean = true
)
