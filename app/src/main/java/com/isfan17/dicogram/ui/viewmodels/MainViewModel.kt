package com.isfan17.dicogram.ui.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.isfan17.dicogram.data.model.Story
import com.isfan17.dicogram.data.repository.DicoGramRepository
import kotlinx.coroutines.launch
import com.isfan17.dicogram.utils.Result

class MainViewModel(private val repository: DicoGramRepository): ViewModel() {
    fun getUserPreferences(property: String): LiveData<String> = repository.getUserPreferences(property).asLiveData()

    fun clearLoginSession() {
        viewModelScope.launch {
            repository.clearUserPreferences()
        }
    }

    private val _locatedStories = MutableLiveData<Result<List<Story>>>()
    val locatedStories: LiveData<Result<List<Story>>> = _locatedStories

    fun getLocatedStories(token: String) {
        viewModelScope.launch {
            _locatedStories.value = Result.Loading
            _locatedStories.value = repository.getLocatedStories(token)
        }
    }

    fun getStoriesPagingData(token: String): LiveData<PagingData<Story>> {
        return repository.getStoriesPagingData(token).cachedIn(viewModelScope)
    }
}