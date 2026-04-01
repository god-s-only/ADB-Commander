package com.adbcommand.app.core

import android.content.Context
import android.util.Base64
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

class AdbKeyStoreManager(private val context: Context) {

    companion object {
        private const val TAG = "AdbKeyStoreManager"
        private const val KEY_STORE_FILE = "adb_identity.p12"
        private const val ALIAS = "adb_client_key"
        private val PASSWORD = "adb_ks_pass".toCharArray()
    }

    val keyStorePassword: CharArray get() = PASSWORD
    val keyStore: KeyStore by lazy { loadOrCreate() }


    private fun loadOrCreate(): KeyStore {
        val ksFile = context.getFileStreamPath(KEY_STORE_FILE)
        val ks = KeyStore.getInstance("PKCS12")

        if (ksFile.exists()) {
            try {
                context.openFileInput(KEY_STORE_FILE).use { fis ->
                    ks.load(fis, PASSWORD)
                    Log.d(TAG, "Loaded existing key store")
                    return ks
                }
            } catch (e: Exception) {
                Log.w(TAG, "Corrupted key store — regenerating", e)
                ksFile.delete()
            }
        }

        ks.load(null, PASSWORD)
        generateAndStore(ks)
        saveKeyStore(ks)
        Log.d(TAG, "Created new ADB identity key pair")
        return ks
    }

    private fun generateAndStore(ks: KeyStore) {
        val kpg = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
        val kp  = kpg.generateKeyPair()

        val cert = buildSelfSignedCertificate(kp.private, kp.public)

        ks.setKeyEntry(ALIAS, kp.private, PASSWORD, arrayOf<Certificate>(cert))
    }
    @Suppress("UNCHECKED_CAST")
    private fun buildSelfSignedCertificate(
        privateKey: PrivateKey,
        publicKey: java.security.PublicKey
    ): java.security.cert.Certificate {
        return try {
            val subject = X500Principal("CN=ADB Commander, O=AdbCommandApp")

            // Attempt Bouncy-Castle path (available via Android's conscrypt)
            val x500NameClass   = Class.forName("org.bouncycastle.asn1.x500.X500Name")
            val toX500Name      = x500NameClass.getConstructor(String::class.java)
            val subjectName     = toX500Name.newInstance("CN=ADB Commander")

            val contentSignerBuilderClass =
                Class.forName("org.bouncycastle.operator.jcajce.JcaContentSignerBuilder")
            val contentSignerBuilder =
                contentSignerBuilderClass.getConstructor(String::class.java)
                    .newInstance("SHA256WithRSA")

            val signer = contentSignerBuilderClass
                .getMethod("build", java.security.PrivateKey::class.java)
                .invoke(contentSignerBuilder, privateKey)

            val certBuilderClass =
                Class.forName("org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder")
            val certBuilder = certBuilderClass.getConstructors().first().newInstance(
                subject,
                java.math.BigInteger.valueOf(System.currentTimeMillis()),
                Date(System.currentTimeMillis() - 1000L),
                Date(System.currentTimeMillis() + 10L * 365 * 24 * 60 * 60 * 1000),
                subject,
                publicKey
            )

            val holderClass = Class.forName("org.bouncycastle.cert.X509CertificateHolder")
            val holder = certBuilderClass
                .getMethod("build", Class.forName("org.bouncycastle.operator.ContentSigner"))
                .invoke(certBuilder, signer)

            val converterClass =
                Class.forName("org.bouncycastle.cert.jcajce.JcaX509CertificateConverter")
            val converter = converterClass.getDeclaredConstructor().newInstance()
            converterClass.getMethod("getCertificate", holderClass)
                .invoke(converter, holder) as java.security.cert.Certificate

        } catch (e: Exception) {

            Log.w(TAG, "BC path unavailable; using stub certificate — DO NOT SHIP", e)
            StubCertificate(publicKey)
        }
    }

    private fun saveKeyStore(ks: KeyStore) {
        context.openFileOutput(KEY_STORE_FILE, Context.MODE_PRIVATE).use { fos ->
            ks.store(fos, PASSWORD)
        }
    }

    fun exportPublicKeyForAdb(): String {
        val cert  = keyStore.getCertificate(ALIAS)
        val b64   = Base64.encodeToString(cert.publicKey.encoded, Base64.NO_WRAP)
        return "$b64 AdbCommander"
    }
}

private class StubCertificate(private val pub: java.security.PublicKey) :
    java.security.cert.Certificate("STUB") {
    override fun getEncoded() = pub.encoded
    override fun verify(key: java.security.PublicKey) = Unit
    override fun verify(key: java.security.PublicKey, sigProvider: String) = Unit
    override fun toString() = "StubCertificate"
    override fun getPublicKey() = pub
}