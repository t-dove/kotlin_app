package com.example.anonymous_chat
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserAuthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userAuth: UserAuth)

    @Query("SELECT * FROM user_auth ORDER BY id DESC LIMIT 1")
    suspend fun getUserAuth(): UserAuth?

    @Query("DELETE FROM user_auth")
    suspend fun deleteAll()
}
