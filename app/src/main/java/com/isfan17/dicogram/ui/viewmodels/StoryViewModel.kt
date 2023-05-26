package com.isfan17.dicogram.ui.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.*
import com.isfan17.dicogram.data.repository.DicoGramRepository
import com.isfan17.dicogram.utils.Helper.rotateBitmap
import com.isfan17.dicogram.utils.Result
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class StoryViewModel(private val repository: DicoGramRepository): ViewModel() {
    fun getUserPreferences(property: String): LiveData<String> = repository.getUserPreferences(property).asLiveData()

    private val _isStoryUploaded = MutableLiveData<Result<Boolean>>()
    val isStoryUploaded: LiveData<Result<Boolean>> = _isStoryUploaded

    fun uploadStory(
        token: String,
        imageFile: File,
        isBackCamera: Int,
        description: String,
        storyLat: String?,
        storyLng: String?
    ) {
        viewModelScope.launch {
            _isStoryUploaded.value = Result.Loading
            _isStoryUploaded.value = repository.uploadStory(
                token,
                reduceFileImage(imageFile, isBackCamera),
                description,
                storyLat,
                storyLng
            )
        }
    }

    private fun reduceFileImage(file: File, isBackCamera: Int): File {
        val bitmap = rotateBitmap(
            BitmapFactory.decodeFile(file.path),
            isBackCamera
        )

        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000000)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }
}