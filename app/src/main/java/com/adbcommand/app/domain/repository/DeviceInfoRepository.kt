package com.adbcommand.app.domain.repository

import com.adbcommand.app.domain.models.DeviceInfo

interface DeviceInfoRepository {
    suspend fun getDeviceInfo(): Result<DeviceInfo>
}