package com.aidetector.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detections")
data class DetectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val objectName: String,
    val category: String,
    val description: String,
    val additionalInfo: String,
    val imagePath: String,
    val confidence: Float,
    val timestamp: Long
)
