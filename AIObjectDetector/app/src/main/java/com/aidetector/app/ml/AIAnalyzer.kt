package com.aidetector.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.aidetector.app.models.DetectionResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AIAnalyzer(private val context: Context) {
    
    private val imageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
        ImageLabeling.getClient(options)
    }
    
    private val geminiAnalyzer by lazy {
        GeminiAnalyzer(context)
    }
    
    suspend fun analyzeImage(bitmap: Bitmap): DetectionResult? {
        return try {
            // Step 1: Use ML Kit for initial object detection
            val mlKitLabels = detectWithMLKit(bitmap)
            
            if (mlKitLabels.isEmpty()) {
                Log.w(TAG, "No labels detected by ML Kit")
                return null
            }
            
            val topLabel = mlKitLabels.first()
            Log.d(TAG, "Top label: ${topLabel.text} (${topLabel.confidence})")
            
            // Step 2: Use Gemini AI for detailed analysis
            val detailedResult = geminiAnalyzer.getDetailedInfo(
                objectName = topLabel.text,
                confidence = topLabel.confidence,
                bitmap = bitmap
            )
            
            detailedResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed", e)
            null
        }
    }
    
    private suspend fun detectWithMLKit(bitmap: Bitmap): List<com.google.mlkit.vision.label.ImageLabel> {
        return suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            
            imageLabeler.process(image)
                .addOnSuccessListener { labels ->
                    continuation.resume(labels)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }
    
    fun close() {
        imageLabeler.close()
    }
    
    companion object {
        private const val TAG = "AIAnalyzer"
    }
}
