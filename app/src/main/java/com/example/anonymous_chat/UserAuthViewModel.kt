package com.example.anonymous_chat
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
class UserAuthViewModel(application: Application) : AndroidViewModel(application) {


    private val repository: UserAuthRepository
    private val userAuthDao: UserAuthDao = AppDatabase.getDatabase(application).userAuthDao()

    init {
        val userAuthDao = AppDatabase.getDatabase(application).userAuthDao()

        repository = UserAuthRepository(userAuthDao)
    }

    fun insertUserAuth(token: String) {
        viewModelScope.launch {
            repository.insertUserAuth(UserAuth(access_token = token))
        }
    }

    fun deleteAllUserAuths() {
        viewModelScope.launch {
            userAuthDao.deleteAll()
        }
    }
    fun getUserAuth() = liveData {
        val userAuth = repository.getUserAuth()
        emit(userAuth)
    }
}

