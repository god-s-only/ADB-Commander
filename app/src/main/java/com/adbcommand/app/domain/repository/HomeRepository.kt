package com.adbcommand.app.domain.repository

interface HomeRepository {
    suspend fun getDeviceIp(): Result<String>
}