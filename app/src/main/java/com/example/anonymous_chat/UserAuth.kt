package com.example.anonymous_chat
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_auth")
data class UserAuth(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val access_token: String
)
