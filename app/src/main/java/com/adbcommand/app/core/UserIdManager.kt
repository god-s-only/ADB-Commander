package com.adbcommand.app.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserIdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("anonymous_user_id")
    }

    suspend fun getUserId(): String {
        val prefs = context.userDataStore.data.first()
        val existing = prefs[KEY_USER_ID]

        if (!existing.isNullOrBlank()) return existing
        val newId = UUID.randomUUID().toString()
        context.userDataStore.edit { it[KEY_USER_ID] = newId }
        return newId
    }
}