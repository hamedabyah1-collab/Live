package com.aidetector.app.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionDao {
    
    @Query("SELECT * FROM detections ORDER BY timestamp DESC")
    fun getAllDetections(): Flow<List<DetectionEntity>>
    
    @Query("SELECT * FROM detections WHERE id = :id")
    suspend fun getDetectionById(id: Long): DetectionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detection: DetectionEntity): Long
    
    @Update
    suspend fun update(detection: DetectionEntity)
    
    @Delete
    suspend fun delete(detection: DetectionEntity)
    
    @Query("DELETE FROM detections")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM detections")
    suspend fun getCount(): Int
}
