package com.adbcommand.app.domain.usecase.deviceinfo

import com.adbcommand.app.domain.models.DeviceInfo
import com.adbcommand.app.domain.repository.DeviceInfoRepository
import jakarta.inject.Inject

class GetDeviceInfoUseCase @Inject constructor(
    private val repository: DeviceInfoRepository
) {
    suspend operator fun invoke(): Result<DeviceInfo> =
        repository.getDeviceInfo()
}
