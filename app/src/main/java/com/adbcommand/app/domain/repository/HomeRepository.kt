package com.adbcommand.app.domain.repository

interface HomeRepository {

    suspend fun getDeviceIp(): Result<String>

    suspend fun getPairingPort(): Result<String>

    suspend fun getAdbPort(): Result<String>

    suspend fun generatePairingCode(): Result<String>

    suspend fun testConnection(): Result<String>
}