package com.aidetector.app.models

data class DetectionResult(
    val name: String,
    val category: String,
    val description: String,
    val additionalInfo: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)
