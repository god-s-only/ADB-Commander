package com.adbcommand.app.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.adbcommand.app.core.BASE_URL
import com.adbcommand.app.core.KEY_IS_PRO
import com.adbcommand.app.core.KEY_PAYMENT_INTENT
import com.adbcommand.app.domain.models.UserEntitlement
import com.adbcommand.app.domain.models.UserPlan
import com.adbcommand.app.domain.repository.BillingRepository
import com.adbcommand.app.domain.repository.PaymentIntentResult
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private val Context.billingDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "stripe_billing_prefs")

@Singleton
class StripeBillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingRepository {

    companion object {
        private const val TAG = "StripeBillingRepo"
    }


    override val entitlement: Flow<UserEntitlement> =
        context.billingDataStore.data.map { prefs ->
            UserEntitlement(
                plan             = if (prefs[KEY_IS_PRO] == true) UserPlan.PRO else UserPlan.FREE,
                purchaseToken    = prefs[KEY_PAYMENT_INTENT]
            )
        }


    override suspend fun createPaymentIntent(userId: String): Result<PaymentIntentResult> {
        return try {
            val body = JSONObject().apply { put("userId", userId) }
            val response = post("$BASE_URL/create-payment-intent", body)

            val clientSecret    = response.getString("clientSecret")
            val paymentIntentId = response.getString("paymentIntentId")
            val amount          = response.getLong("amount")
            val currency        = response.getString("currency").uppercase()

            val formattedAmount = when (currency) {
                "NGN" -> "₦${amount / 100}"
                "USD" -> "$${amount / 100}"
                "GBP" -> "£${amount / 100}"
                "EUR" -> "€${amount / 100}"
                else  -> "${amount / 100} $currency"
            }

            Log.d(TAG, "PaymentIntent created: $paymentIntentId")

            Result.success(
                PaymentIntentResult(
                    clientSecret    = clientSecret,
                    paymentIntentId = paymentIntentId,
                    formattedAmount = formattedAmount
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "createPaymentIntent failed", e)
            Result.failure(e)
        }
    }


    override suspend fun verifyPurchase(
        paymentIntentId: String,
        userId: String
    ): Result<UserEntitlement> {
        return try {
            val body     = JSONObject().apply {
                put("paymentIntentId", paymentIntentId)
                put("userId", userId)
            }
            val response = post("$BASE_URL/verify-purchase", body)

            val isPro = response.getBoolean("isPro")
            val entitlement = UserEntitlement(
                plan          = if (isPro) UserPlan.PRO else UserPlan.FREE,
                purchaseToken = paymentIntentId
            )

            if (isPro) saveEntitlement(entitlement)

            Log.d(TAG, "verifyPurchase: isPro=$isPro")
            Result.success(entitlement)
        } catch (e: Exception) {
            Log.e(TAG, "verifyPurchase failed", e)
            Result.failure(e)
        }
    }


    override suspend fun checkEntitlement(userId: String): Result<UserEntitlement> {

        return try {
            val prefs = context.billingDataStore.data
            var savedIntentId   = ""
            var savedIsPro = false

            context.billingDataStore.data.collect { p ->
                savedIntentId = p[KEY_PAYMENT_INTENT] ?: ""
                savedIsPro    = p[KEY_IS_PRO] ?: false
                return@collect
            }

            if (savedIntentId.isNotBlank()) {
                verifyPurchase(savedIntentId, userId)
            } else {
                Result.success(UserEntitlement(plan = UserPlan.FREE))
            }
        } catch (e: Exception) {
            Log.w(TAG, "checkEntitlement network failed, using cache", e)
            Result.success(UserEntitlement(plan = UserPlan.FREE))
        }
    }

    override suspend fun saveEntitlement(entitlement: UserEntitlement) {
        context.billingDataStore.edit { prefs ->
            prefs[KEY_IS_PRO] = entitlement.isPro
            if (entitlement.purchaseToken != null) {
                prefs[KEY_PAYMENT_INTENT] = entitlement.purchaseToken
            }
        }
        Log.d(TAG, "Entitlement saved: isPro=${entitlement.isPro}")
    }

    override suspend fun clearEntitlement() {
        context.billingDataStore.edit { prefs ->
            prefs[KEY_IS_PRO]         = false
            prefs.remove(KEY_PAYMENT_INTENT)
        }
    }

    private fun post(url: String, body: JSONObject): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.apply {
            requestMethod     = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(body.toString())
            writer.flush()
        }

        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream
        else connection.errorStream

        val responseText = stream.bufferedReader().readText()

        if (responseCode !in 200..299) {
            throw Exception("HTTP $responseCode: $responseText")
        }

        return JSONObject(responseText)
    }
}