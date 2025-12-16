package com.aidetector.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
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

        if (PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }

        setupClickListeners()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permission_camera_title),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnFlash.setOnClickListener { toggleFlash() }

        binding.btnSwitchCamera.setOnClickListener {
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT
                else
                    CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        binding.btnCapture.setOnClickListener { takePhoto() }

        binding.btnGallery.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                if (cameraProvider?.hasCamera(cameraSelector) == true) {
                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_camera),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Camera error", e)
                Toast.makeText(
                    this,
                    getString(R.string.error_camera),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleFlash() {
        flashMode =
            if (flashMode == ImageCapture.FLASH_MODE_OFF)
                ImageCapture.FLASH_MODE_ON
            else
                ImageCapture.FLASH_MODE_OFF

        imageCapture?.flashMode = flashMode
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return

        val photoFile = File(
            getOutputDirectory(),
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        showLoading(true)

        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Capture failed", exc)
                    showLoading(false)
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.error_analysis),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(
                    output: ImageCapture.OutputFileResults
                ) {
                    analyzeImage(photoFile)
                }
            }
        )
    }

    private fun analyzeImage(imageFile: File) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val bitmap =
                    BitmapFactory.decodeFile(imageFile.absolutePath)

                val result = withContext(Dispatchers.IO) {
                    aiAnalyzer.analyzeImage(bitmap)
                }

                showLoading(false)

                if (result != null) {
                    val intent =
                        Intent(this@CameraActivity, ResultActivity::class.java)
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
                Log.e(TAG, "Analysis error", e)
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
        val v = if (show) View.VISIBLE else View.GONE
        binding.analysisOverlay.visibility = v
        binding.progressBar.visibility = v
        binding.tvAnalyzing.visibility = v
        binding.btnCapture.isEnabled = !show
    }

    private fun getOutputDirectory(): File {
        val dir = externalMediaDirs.firstOrNull()?.let {
            File(it, getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (dir != null && dir.exists()) dir else filesDir
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
