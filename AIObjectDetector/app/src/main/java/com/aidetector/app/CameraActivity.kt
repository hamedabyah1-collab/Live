package com.aidetector.app

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.aidetector.app.databinding.ActivityCameraBinding
import com.aidetector.app.ml.AIAnalyzer
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var aiAnalyzer: AIAnalyzer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        aiAnalyzer = AIAnalyzer(this)
        
        // Ensure we have camera permission before starting CameraX
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
        setupClickListeners()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, getString(R.string.permission_camera_title), Toast.LENGTH_SHORT).show()
                // Optionally finish activity if permission denied
            }
        }
    }
    
    private fun setupClickListeners() {
        // Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Flash Toggle
        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }
        
        // Switch Camera
        binding.btnSwitchCamera.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
        }
        
        // Capture Button
        binding.btnCapture.setOnClickListener {
            takePhoto()
        }
        
        // Gallery Button
        binding.btnGallery.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            
            // Image Capture
            imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            
            // Camera Selector
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            
            try {
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
                Toast.makeText(this, getString(R.string.error_camera), Toast.LENGTH_SHORT).show()
            }
            
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun toggleFlash() {
        flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) {
            binding.btnFlash.contentDescription = getString(R.string.flash_on)
            ImageCapture.FLASH_MODE_ON
        } else {
            binding.btnFlash.contentDescription = getString(R.string.flash_off)
            ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = flashMode
    }
    
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        
        // Create output file
        val photoFile = File(
            getOutputDirectory(),
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // Show loading
        showLoading(true)
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    showLoading(false)
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.error_analysis),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo saved: ${photoFile.absolutePath}")
                    analyzeImage(photoFile)
                }
            }
        )
    }
    
    private fun analyzeImage(imageFile: File) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Load bitmap
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                
                // Analyze with AI
                val result = withContext(Dispatchers.IO) {
                    aiAnalyzer.analyzeImage(bitmap)
                }
                
                showLoading(false)
                
                if (result != null) {
                    // Navigate to result screen
                    val intent = Intent(this@CameraActivity, ResultActivity::class.java)
                    intent.putExtra("image_path", imageFile.absolutePath)
                    intent.putExtra("object_name", result.name)
                    intent.putExtra("category", result.category)
                    intent.putExtra("description", result.description)
                    intent.putExtra("info", result.additionalInfo)
                    intent.putExtra("confidence", result.confidence)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.error_analysis),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
                showLoading(false)
                Toast.makeText(
                    this@CameraActivity,
                    getString(R.string.error_analysis),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.analysisOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.tvAnalyzing.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCapture.isEnabled = !show
    }
    
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    
    companion object {
        private const val TAG = "CameraActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CAMERA_PERMISSION = 1001
    }
}
