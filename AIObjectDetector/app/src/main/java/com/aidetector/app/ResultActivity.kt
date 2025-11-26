package com.aidetector.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.aidetector.app.databinding.ActivityResultBinding
import com.aidetector.app.db.AppDatabase
import com.aidetector.app.db.DetectionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ResultActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityResultBinding
    private lateinit var database: AppDatabase
    private var imagePath: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = AppDatabase.getDatabase(this)
        
        loadResultData()
        setupClickListeners()
    }
    
    private fun loadResultData() {
        imagePath = intent.getStringExtra("image_path")
        val objectName = intent.getStringExtra("object_name") ?: "غير معروف"
        val category = intent.getStringExtra("category") ?: "أخرى"
        val description = intent.getStringExtra("description") ?: ""
        val info = intent.getStringExtra("info") ?: ""
        val confidence = intent.getFloatExtra("confidence", 0f)
        
        // Display image
        imagePath?.let { path ->
            val bitmap = BitmapFactory.decodeFile(path)
            binding.ivCapturedImage.setImageBitmap(bitmap)
        }
        
        // Display data
        binding.tvObjectName.text = objectName
        binding.tvCategory.text = category
        binding.tvDescription.text = description
        binding.tvInfo.text = info
        binding.tvConfidence.text = getString(R.string.confidence, (confidence * 100).toInt())
    }
    
    private fun setupClickListeners() {
        // Share Button
        binding.btnShare.setOnClickListener {
            shareResult()
        }
        
        // Save Button
        binding.btnSave.setOnClickListener {
            saveToDatabase()
        }
        
        // Scan Again Button
        binding.btnScanAgain.setOnClickListener {
            finish()
        }
    }
    
    private fun shareResult() {
        val objectName = binding.tvObjectName.text.toString()
        val description = binding.tvDescription.text.toString()
        
        val shareText = """
            🔍 كاشف الأشياء الذكي
            
            الشيء: $objectName
            الوصف: $description
            
            تم الكشف باستخدام تطبيق كاشف الأشياء الذكي
        """.trimIndent()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            
            // Add image if available
            imagePath?.let { path ->
                val imageFile = File(path)
                if (imageFile.exists()) {
                    val imageUri = FileProvider.getUriForFile(
                        this@ResultActivity,
                        "${packageName}.fileprovider",
                        imageFile
                    )
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_result)))
    }
    
    private fun saveToDatabase() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val entity = DetectionEntity(
                    objectName = binding.tvObjectName.text.toString(),
                    category = binding.tvCategory.text.toString(),
                    description = binding.tvDescription.text.toString(),
                    additionalInfo = binding.tvInfo.text.toString(),
                    imagePath = imagePath ?: "",
                    confidence = intent.getFloatExtra("confidence", 0f),
                    timestamp = System.currentTimeMillis()
                )
                
                withContext(Dispatchers.IO) {
                    database.detectionDao().insert(entity)
                }
                
                Toast.makeText(
                    this@ResultActivity,
                    getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@ResultActivity,
                    getString(R.string.error_unknown),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
