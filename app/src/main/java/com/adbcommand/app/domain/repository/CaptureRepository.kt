package com.adbcommand.app.domain.repository

import com.adbcommand.app.domain.models.CapturedScreenshot
import com.adbcommand.app.domain.models.RecordingSession
import kotlinx.coroutines.flow.Flow

interface CaptureRepository {

    suspend fun takeScreenshot(): Result<CapturedScreenshot>

    suspend fun shareScreenshot(filePath: String): Result<Unit>

    suspend fun saveToGallery(filePath: String): Result<String>

    fun startRecording(): Flow<Long>

    suspend fun stopRecording(): Result<RecordingSession>

    suspend fun shareRecording(filePath: String): Result<Unit>
}