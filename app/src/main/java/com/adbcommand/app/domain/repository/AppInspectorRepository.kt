package com.adbcommand.app.domain.repository

import com.adbcommand.app.domain.models.AppInspection

interface AppInspectorRepository {

    suspend fun inspectApp(packageName: String): Result<AppInspection>
}