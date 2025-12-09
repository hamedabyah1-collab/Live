package com.aidetector.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aidetector.app.databinding.ActivityMainBinding
import com.guolindev.permissionx.PermissionX

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // زر "ابدأ الكشف"
        binding.btnStartDetection.setOnClickListener {
            requestPermissions()
        }

        // زر "السجل"
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // زر "الإعدادات"
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun requestPermissions() {
        // تحديد الأذونات المطلوبة
        val permissionsList = mutableListOf(Manifest.permission.CAMERA)

        // إضافة إذن التخزين حسب إصدار Android
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // Android 9 (Pie) وأقل
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (Tiramisu) وأعلى
            permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            // Android 10, 11, 12 لا تحتاج إلى إذن تخزين صريح لحفظ الملفات في المجلدات الخاصة بالتطبيق
        }

        PermissionX.init(this)
            .permissions(permissionsList)
            .onExplainRequestBefore { scope, deniedList ->
                // عرض رسالة توضيحية قبل طلب الأذونات
                scope.showRequestReasonDialog(
                    deniedList,
                    getString(R.string.permission_explanation), // "يحتاج التطبيق إلى هذه الأذونات ليعمل بشكل صحيح."
                    getString(R.string.ok),
                    getString(R.string.cancel)
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    // جميع الأذونات ممنوحة، ابدأ نشاط الكاميرا
                    startActivity(Intent(this, CameraActivity::class.java))
                } else {
                    // لم يتم منح جميع الأذونات
                    Toast.makeText(
                        this,
                        getString(R.string.permission_denied_message) + deniedList.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
