package com.aidetector.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class CameraActivity : AppCompatActivity() {

    private lateinit var resultText: TextView
    private lateinit var capturedImage: ImageView
    private lateinit var captureButton: Button
    private lateinit var tflite: Interpreter
    private lateinit var labels: List<String>

    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        resultText = findViewById(R.id.resultText)
        capturedImage = findViewById(R.id.capturedImage)
        captureButton = findViewById(R.id.captureButton)

        // تحميل نموذج TFLite
        tflite = Interpreter(loadModelFile("model.tflite"))

        // تحميل Labels
        labels = assets.open("labels.txt").bufferedReader().readLines()

        // صلاحية الكاميرا
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }

        captureButton.setOnClickListener {
            captureAndAnalyze()
        }
    }

    private fun captureAndAnalyze() {
        // هنا تضيف الكود اللي يلتقط الصورة من الكاميرا
        val bitmap: Bitmap = getCapturedBitmap() // خذ الصورة من الكاميرا

        capturedImage.setImageBitmap(bitmap)

        // تجهيز الصورة للنموذج
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = preprocessBitmap(resized)

        // مصفوفة خرج النموذج
        val output = Array(1) { FloatArray(labels.size) }

        tflite.run(input, output)

        // عرض النتائج بشكل ذكي
        displayResults(output[0])
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val channel = inputStream.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    private fun preprocessBitmap(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val px = bitmap.getPixel(x, y)
                input[0][y][x][0] = ((px shr 16 and 0xFF) / 255.0f)
                input[0][y][x][1] = ((px shr 8 and 0xFF) / 255.0f)
                input[0][y][x][2] = ((px and 0xFF) / 255.0f)
            }
        }
        return input
    }

    private fun displayResults(predictions: FloatArray) {
        val indexed = predictions.mapIndexed { index, conf -> index to conf }
            .sortedByDescending { it.second }

        val topIndex = indexed[0].first
        val topConf = indexed[0].second
        val topLabel = labels[topIndex]

        if (topLabel == "Space" || topConf < 0.5f) {
            // لو الثقة ضعيفة، نعرض أعلى 3 احتمالات
            val top3 = indexed.take(3)
                .map { "${labels[it.first]} (${String.format("%.2f", it.second)})" }
                .joinToString("\n")
            resultText.text = "احتمالات:\n$top3"
        } else {
            // الثقة عالية، نعرض النتيجة مباشرة
            resultText.text = "$topLabel (${String.format("%.2f", topConf)})"
        }
    }

    private fun getCapturedBitmap(): Bitmap {
        // هنا تحط الكود اللي يجيب الصورة من الكاميرا أو من معرض الصور
        // مؤقتاً نستخدم صورة افتراضية من الموارد
        return BitmapFactory.decodeResource(resources, R.drawable.sample_image)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "الكاميرا مطلوبة!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
