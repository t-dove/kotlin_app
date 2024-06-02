package com.example.anonymous_chat

class UserAuthRepository(private val userAuthDao: UserAuthDao) {
    suspend fun insertUserAuth(userAuth: UserAuth) {
        userAuthDao.insert(userAuth)
    }

    suspend fun getUserAuth(): UserAuth? {
        return userAuthDao.getUserAuth()
    }
}
