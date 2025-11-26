package com.aidetector.app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aidetector.app.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Start Camera Button
        binding.btnStartCamera.setOnClickListener {
            requestCameraPermission()
        }
        
        // View History Button
        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        
        // Settings Button
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun requestCameraPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    getString(R.string.permission_camera_message),
                    getString(R.string.grant_permission),
                    getString(R.string.deny_permission)
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    getString(R.string.permission_camera_message),
                    getString(R.string.settings),
                    getString(R.string.deny_permission)
                )
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    startCameraActivity()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_camera_title),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    
    private fun startCameraActivity() {
        startActivity(Intent(this, CameraActivity::class.java))
    }
}
