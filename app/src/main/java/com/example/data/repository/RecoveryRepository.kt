package com.example.data.repository

import com.example.data.local.ScanReportDao
import com.example.data.local.ScanReportEntity
import kotlinx.coroutines.flow.Flow

class RecoveryRepository(private val scanReportDao: ScanReportDao) {
    val allReports: Flow<List<ScanReportEntity>> = scanReportDao.getAllReports()

    suspend fun saveReport(report: ScanReportEntity): Long {
        return scanReportDao.insertReport(report)
    }

    suspend fun getReport(id: Long): ScanReportEntity? {
        return scanReportDao.getReportById(id)
    }

    suspend fun deleteReport(id: Long) {
        scanReportDao.deleteReportById(id)
    }

    suspend fun clearHistory() {
        scanReportDao.clearAllReports()
    }
}
