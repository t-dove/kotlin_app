package com.example.anonymous_chat

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("response")
    val response: ResponseData
)
data class ResponseData(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("auth_token")
    val auth_token: String,
    @SerializedName("search_id")
    val search_id: Int,
    @SerializedName("chat_found")
    val chat_found: Boolean,
    @SerializedName("chat_id")
    val chat_id: String,
    @SerializedName("user_name")
    val user_name: String,
    @SerializedName("user_sex")
    val user_sex: String,
    @SerializedName("msg_id")
    val msg_id: Int,
)