package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanReportDao {
    @Query("SELECT * FROM scan_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ScanReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ScanReportEntity): Long

    @Query("SELECT * FROM scan_reports WHERE id = :id")
    suspend fun getReportById(id: Long): ScanReportEntity?

    @Query("DELETE FROM scan_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("DELETE FROM scan_reports")
    suspend fun clearAllReports()
}
