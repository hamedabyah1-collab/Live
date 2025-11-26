package com.aidetector.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.aidetector.app.models.DetectionResult
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.Base64

class GeminiAnalyzer(private val context: Context) {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    
    // Replace with your actual Gemini API key
    // For production, store this securely (e.g., in BuildConfig or secure storage)
    private val apiKey = "YOUR_GEMINI_API_KEY_HERE"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$apiKey"
    
    suspend fun getDetailedInfo(
        objectName: String,
        confidence: Float,
        bitmap: Bitmap
    ): DetectionResult? = withContext(Dispatchers.IO) {
        try {
            // Convert bitmap to base64
            val base64Image = bitmapToBase64(bitmap)
            
            // Create prompt in Arabic
            val prompt = """
                قم بتحليل هذه الصورة وتقديم معلومات تفصيلية عن الشيء الموجود فيها.
                الشيء المكتشف: $objectName
                
                يرجى تقديم المعلومات التالية بتنسيق JSON:
                {
                    "name": "الاسم بالعربية",
                    "category": "الفئة (حيوان، نبات، طعام، شيء، مركبة، مبنى، أو أخرى)",
                    "description": "وصف تفصيلي بالعربية (2-3 جمل)",
                    "additionalInfo": "معلومات إضافية ومثيرة للاهتمام بالعربية"
                }
                
                تأكد من أن جميع المعلومات باللغة العربية ودقيقة.
            """.trimIndent()
            
            // Create request body
            val requestBody = createRequestBody(prompt, base64Image)
            
            val request = Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Gemini response: $responseBody")
                
                val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
                val content = geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (content != null) {
                    parseJsonResponse(content, confidence)
                } else {
                    createFallbackResult(objectName, confidence)
                }
            } else {
                Log.e(TAG, "Gemini API error: ${response.code}")
                createFallbackResult(objectName, confidence)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Gemini analysis failed", e)
            createFallbackResult(objectName, confidence)
        }
    }
    
    private fun createRequestBody(prompt: String, base64Image: String): RequestBody {
        val json = """
            {
                "contents": [{
                    "parts": [
                        {"text": "$prompt"},
                        {
                            "inline_data": {
                                "mime_type": "image/jpeg",
                                "data": "$base64Image"
                            }
                        }
                    ]
                }],
                "generationConfig": {
                    "temperature": 0.4,
                    "topK": 32,
                    "topP": 1,
                    "maxOutputTokens": 2048
                }
            }
        """.trimIndent()
        
        return json.toRequestBody("application/json".toMediaType())
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.getEncoder().encodeToString(bytes)
    }
    
    private fun parseJsonResponse(jsonText: String, confidence: Float): DetectionResult {
        return try {
            // Extract JSON from markdown code blocks if present
            val cleanJson = jsonText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val parsed = gson.fromJson(cleanJson, GeminiObjectInfo::class.java)
            
            DetectionResult(
                name = parsed.name,
                category = parsed.category,
                description = parsed.description,
                additionalInfo = parsed.additionalInfo,
                confidence = confidence
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON response", e)
            createFallbackResult("Unknown", confidence)
        }
    }
    
    private fun createFallbackResult(objectName: String, confidence: Float): DetectionResult {
        return DetectionResult(
            name = objectName,
            category = "أخرى",
            description = "تم اكتشاف $objectName في الصورة.",
            additionalInfo = "للحصول على معلومات أكثر تفصيلاً، تأكد من الاتصال بالإنترنت.",
            confidence = confidence
        )
    }
    
    // Data classes for Gemini API response
    private data class GeminiResponse(
        val candidates: List<Candidate>?
    )
    
    private data class Candidate(
        val content: Content?
    )
    
    private data class Content(
        val parts: List<Part>?
    )
    
    private data class Part(
        val text: String?
    )
    
    private data class GeminiObjectInfo(
        val name: String,
        val category: String,
        val description: String,
        @SerializedName("additionalInfo")
        val additionalInfo: String
    )
    
    companion object {
        private const val TAG = "GeminiAnalyzer"
    }
}
