package com.isfan17.dicogram.ui.viewmodels

import androidx.lifecycle.*
import com.isfan17.dicogram.data.model.User
import com.isfan17.dicogram.data.repository.DicoGramRepository
import kotlinx.coroutines.launch
import com.isfan17.dicogram.utils.Result

class AuthViewModel(private val repository: DicoGramRepository): ViewModel() {

    /**
     * USER PREFERENCES
     */
    private val _emailEntry = MutableLiveData<String>()
    val emailEntry: LiveData<String> = _emailEntry

    fun getUserPreferences(property: String): LiveData<String> = repository.getUserPreferences(property).asLiveData()

    fun saveLoginSession(token: String, name: String, email: String) {
        viewModelScope.launch {
            repository.saveUserPreferences(token, name, email)
        }
    }

    /**
     * LOGIN
     */
    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _emailEntry.value = email
            _loginResult.value = Result.Loading
            _loginResult.value = repository.login(email, password)
        }
    }

    /**
     * REGISTER
     */
    private val _registerResult = MutableLiveData<Result<String>>()
    val registerResult: LiveData<Result<String>> = _registerResult

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerResult.value = Result.Loading
            _registerResult.value = repository.register(name, email, password)
        }
    }

}