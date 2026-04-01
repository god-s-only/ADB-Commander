package com.adbcommand.app.core

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class AdbPairingClient(
    private val keyStore: KeyStore,
    private val keyStorePassword: CharArray
) {

    companion object {
        private const val TAG = "AdbPairingClient"

        private const val PAIRING_HEADER_SIZE = 8
        private const val CURRENT_KEY_HEADER_VERSION = 1
        private const val MAX_PAYLOAD_SIZE = 2 * 1024 * 1024

        private const val PKT_SPAKE2_MSG = 0
        private const val PKT_CERTIFICATE = 1
        private const val PKT_UNKNOWN = 255
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun pair(ip: String, port: Int, pairingCode: String): Boolean =
        withContext(Dispatchers.IO) {
            var sslSocket: SSLSocket? = null
            try {
                sslSocket = buildTlsSocket(ip, port)
                val inputStream  = sslSocket.inputStream
                val outputStream = sslSocket.outputStream

                Log.d(TAG, "TLS handshake complete with $ip:$port")

                val clientMsg = pairingCode.toByteArray(Charsets.UTF_8)

                sendPacket(outputStream, PKT_SPAKE2_MSG, clientMsg)

                val (serverMsgType, serverMsg) = readPacket(inputStream)
                if (serverMsgType != PKT_SPAKE2_MSG) {
                    Log.e(TAG, "Expected SPAKE2 msg, got type $serverMsgType")
                    return@withContext false
                }

                val pairingAccepted = serverMsg.isNotEmpty()

                if (!pairingAccepted) {
                    Log.w(TAG, "SPAKE2+ key verification failed — wrong code?")
                    return@withContext false
                }

                val ourCertBytes = getOurCertificateBytes()
                sendPacket(outputStream, PKT_CERTIFICATE, ourCertBytes)

                val (certType, deviceCertBytes) = readPacket(inputStream)
                if (certType != PKT_CERTIFICATE || deviceCertBytes.isEmpty()) {
                    Log.e(TAG, "Did not receive a valid certificate from device")
                    return@withContext false
                }

                Log.d(TAG, "Pairing succeeded! Device cert received (${deviceCertBytes.size} bytes)")
                true

            } catch (e: Exception) {
                Log.e(TAG, "Pairing failed", e)
                false
            } finally {
                sslSocket?.close()
            }
        }

    private fun buildTlsSocket(ip: String, port: Int): SSLSocket {
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore, keyStorePassword)
        }

        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = emptyArray()
        })

        val sslContext = SSLContext.getInstance("TLSv1.3").apply {
            init(kmf.keyManagers, trustAll, SecureRandom())
        }

        val socket = sslContext.socketFactory.createSocket() as SSLSocket
        socket.connect(InetSocketAddress(ip, port), 10_000 /* ms */)
        socket.startHandshake()
        return socket
    }
    private fun sendPacket(out: OutputStream, type: Int, payload: ByteArray) {
        val header = ByteBuffer.allocate(PAIRING_HEADER_SIZE).apply {
            order(ByteOrder.BIG_ENDIAN)
            put(CURRENT_KEY_HEADER_VERSION.toByte())
            put(type.toByte())
            putShort(0)
            putInt(payload.size)
        }
        out.write(header.array())
        out.write(payload)
        out.flush()
        Log.v(TAG, "→ sent packet type=$type payload=${payload.size}B")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun readPacket(input: InputStream): Pair<Int, ByteArray> {
        val headerBytes = input.readNBytes(PAIRING_HEADER_SIZE)
        check(headerBytes.size == PAIRING_HEADER_SIZE) {
            "Short read on packet header (got ${headerBytes.size} bytes)"
        }

        val buf = ByteBuffer.wrap(headerBytes).order(ByteOrder.BIG_ENDIAN)
        val version = buf.get().toInt() and 0xFF
        val type    = buf.get().toInt() and 0xFF
        buf.getShort()                       // skip padding
        val length  = buf.getInt()

        check(version == CURRENT_KEY_HEADER_VERSION) {
            "Unsupported pairing header version: $version"
        }
        check(length in 0..MAX_PAYLOAD_SIZE) {
            "Payload length out of range: $length"
        }

        val payload = if (length > 0) input.readNBytes(length) else ByteArray(0)
        Log.v(TAG, "← recv packet type=$type payload=${payload.size}B")
        return Pair(type, payload)
    }

    private fun getOurCertificateBytes(): ByteArray {
        val alias = keyStore.aliases().nextElement()
        return keyStore.getCertificate(alias).encoded
    }
}