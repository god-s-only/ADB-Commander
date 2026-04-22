package com.adbcommand.app.core

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

const val BASE_URL = ""
val KEY_IS_PRO = booleanPreferencesKey("stripe_is_pro")
val KEY_PAYMENT_INTENT = stringPreferencesKey("stripe_payment_intent_id")
val KEY_USER_ID = stringPreferencesKey("stripe_user_id")