package com.isfan17.dicogram.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_DEFAULT_VALUE
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_EMAIL_NAME
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_NAME_NAME
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_TOKEN_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    private val token = stringPreferencesKey(USER_PREF_TOKEN_NAME)
    private val name = stringPreferencesKey(USER_PREF_NAME_NAME)
    private val email = stringPreferencesKey(USER_PREF_EMAIL_NAME)

    fun getUserToken(): Flow<String> = dataStore.data.map { it[token] ?: USER_PREF_DEFAULT_VALUE }
    fun getUserName(): Flow<String> = dataStore.data.map { it[name] ?: USER_PREF_DEFAULT_VALUE }
    fun getUserEmail(): Flow<String> = dataStore.data.map { it[email] ?: USER_PREF_DEFAULT_VALUE }

    suspend fun saveUserPreferences(userToken: String, userName:String, userEmail: String) {
        dataStore.edit { preferences ->
            preferences[token] = userToken
            preferences[name] = userName
            preferences[email] = userEmail
        }
    }

    suspend fun clearUserPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

}