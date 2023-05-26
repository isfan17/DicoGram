package com.isfan17.dicogram.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.isfan17.dicogram.data.local.DicoGramDatabase
import com.isfan17.dicogram.data.network.DicoGramNetwork
import com.isfan17.dicogram.data.preferences.UserPreferences
import com.isfan17.dicogram.data.repository.DicoGramRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user")

object Injection {
    fun provideRepository(context: Context): DicoGramRepository {
        val preferences = UserPreferences.getInstance(context.dataStore)
        val apiService = DicoGramNetwork.dicoGramService
        val database = DicoGramDatabase.getDatabase(context)
        return DicoGramRepository.getInstance(preferences, apiService, database)
    }
}