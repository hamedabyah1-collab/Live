package com.aidetector.app

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aidetector.app.databinding.ActivityHistoryBinding
import com.aidetector.app.db.AppDatabase
import com.aidetector.app.db.DetectionEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: DetectionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = AppDatabase.getDatabase(this)
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeDetections()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = DetectionAdapter(
            onItemClick = { detection ->
                // TODO: Show detail dialog or navigate to detail screen
            },
            onDeleteClick = { detection ->
                deleteDetection(detection)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }
    }
    
    private fun setupClickListeners() {
        binding.fabClearHistory.setOnClickListener {
            showClearHistoryDialog()
        }
    }
    
    private fun observeDetections() {
        lifecycleScope.launch {
            database.detectionDao().getAllDetections().collectLatest { detections ->
                if (detections.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    adapter.submitList(detections)
                }
            }
        }
    }
    
    private fun deleteDetection(detection: DetectionEntity) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_item))
            .setMessage("هل تريد حذف هذا العنصر؟")
            .setPositiveButton("حذف") { _, _ ->
                lifecycleScope.launch {
                    database.detectionDao().delete(detection)
                    Toast.makeText(
                        this@HistoryActivity,
                        getString(R.string.deleted_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
    
    private fun showClearHistoryDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_history))
            .setMessage("هل تريد حذف جميع العناصر من السجل؟")
            .setPositiveButton("حذف الكل") { _, _ ->
                lifecycleScope.launch {
                    database.detectionDao().deleteAll()
                    Toast.makeText(
                        this@HistoryActivity,
                        getString(R.string.deleted_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
}

class DetectionAdapter(
    private val onItemClick: (DetectionEntity) -> Unit,
    private val onDeleteClick: (DetectionEntity) -> Unit
) : RecyclerView.Adapter<DetectionAdapter.ViewHolder>() {
    
    private var detections = listOf<DetectionEntity>()
    
    fun submitList(list: List<DetectionEntity>) {
        detections = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detection, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(detections[position])
    }
    
    override fun getItemCount() = detections.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivDetectionImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvDetectionName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvDetectionCategory)
        private val tvTime: TextView = itemView.findViewById(R.id.tvDetectionTime)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        
        fun bind(detection: DetectionEntity) {
            tvName.text = detection.objectName
            tvCategory.text = detection.category
            tvTime.text = formatTimestamp(detection.timestamp)
            
            // Load image
            val imageFile = File(detection.imagePath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                ivImage.setImageBitmap(bitmap)
            }
            
            itemView.setOnClickListener {
                onItemClick(detection)
            }
            
            btnDelete.setOnClickListener {
                onDeleteClick(detection)
            }
        }
        
        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60_000 -> "الآن"
                diff < 3600_000 -> "منذ ${diff / 60_000} دقيقة"
                diff < 86400_000 -> "منذ ${diff / 3600_000} ساعة"
                diff < 604800_000 -> "منذ ${diff / 86400_000} يوم"
                else -> {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }
}
