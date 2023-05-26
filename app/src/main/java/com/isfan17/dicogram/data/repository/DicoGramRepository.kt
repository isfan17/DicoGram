package com.isfan17.dicogram.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.isfan17.dicogram.data.local.DicoGramDatabase
import com.isfan17.dicogram.data.mediator.StoryRemoteMediator
import com.isfan17.dicogram.data.model.Story
import com.isfan17.dicogram.data.model.User
import com.isfan17.dicogram.data.network.DicoGramService
import com.isfan17.dicogram.data.network.FileUploadResponse
import com.isfan17.dicogram.utils.Constants.Companion.HTTP_400
import com.isfan17.dicogram.utils.Constants.Companion.HTTP_401
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_EMAIL_NAME
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_NAME_NAME
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_TOKEN_NAME
import com.isfan17.dicogram.data.preferences.UserPreferences
import com.isfan17.dicogram.utils.Result
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class DicoGramRepository(
    private val preferences: UserPreferences,
    private val apiService: DicoGramService,
    private val database: DicoGramDatabase
) {

    fun getUserPreferences(property: String): Flow<String> {
        return when (property) {
            USER_PREF_TOKEN_NAME -> preferences.getUserToken()
            USER_PREF_NAME_NAME -> preferences.getUserName()
            USER_PREF_EMAIL_NAME -> preferences.getUserEmail()
            else -> preferences.getUserToken()
        }
    }

    suspend fun saveUserPreferences(token: String, name: String, email: String) {
        preferences.saveUserPreferences(token, name, email)
    }

    suspend fun clearUserPreferences() {
        preferences.clearUserPreferences()
    }

    suspend fun register (name: String, email: String, password: String): Result<String> {
        return try {
            val response = apiService.register(name, email, password)
            val message = response.message
            Result.Success(message)
        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    when (e.code()) {
                        400 -> Result.Error(HTTP_400)
                        401 -> Result.Error(HTTP_401)
                        else -> Result.Error(e.message.toString())
                    }
                }
                else -> {
                    Result.Error(e.message.toString())
                }
            }
        }
    }

    suspend fun login (email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(email, password)
            val user = response.loginResult
            Result.Success(user)
        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    when (e.code()) {
                        400 -> Result.Error(HTTP_400)
                        401 -> Result.Error(HTTP_401)
                        else -> Result.Error(e.message.toString())
                    }
                }
                else -> {
                    Result.Error(e.message.toString())
                }
            }
        }
    }

    suspend fun getLocatedStories (
        token: String,
        size: Int = 100
    ): Result<List<Story>> {
        return try {
            val response = apiService.getAllLocatedStories(token, size)
            val stories = response.listStory
            Result.Success(stories)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    fun getStoriesPagingData (
        token: String
    ): LiveData<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(
                token = token,
                database = database,
                apiService = apiService
            ),
            pagingSourceFactory = {
                database.storyDao().getAllStories()
            }
        ).liveData
    }

    suspend fun uploadStory (
        token: String,
        imageFile: File,
        description: String,
        lat: String?,
        lng: String?
    ): Result<Boolean> {
        val storyDescription = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )

        return try {
            val response: FileUploadResponse = if (lat != null && lng != null) {
                apiService.uploadStory(
                    token,
                    imageMultipart,
                    storyDescription,
                    lat.toRequestBody("text/plain".toMediaType()),
                    lng.toRequestBody("text/plain".toMediaType())
                )
            } else {
                apiService.uploadStory(token, imageMultipart, storyDescription)
            }
            val isError = response.error
            Result.Success(isError)
        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    when (e.code()) {
                        400 -> Result.Error(HTTP_400)
                        401 -> Result.Error(HTTP_401)
                        else -> Result.Error(e.message.toString())
                    }
                }
                else -> {
                    Result.Error(e.message.toString())
                }
            }
        }
    }

    companion object {
        @Volatile
        private var instance: DicoGramRepository? = null
        fun getInstance(
            preferences: UserPreferences,
            apiService: DicoGramService,
            database: DicoGramDatabase
        ): DicoGramRepository =
            instance ?: synchronized(this) {
                instance ?: DicoGramRepository(preferences, apiService, database)
            }.also { instance = it }
    }
}