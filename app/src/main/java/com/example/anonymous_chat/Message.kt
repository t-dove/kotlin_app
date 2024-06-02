package com.example.anonymous_chat


data class Message(
    val id: Int,
    val text: String,
    val date: String,
    val isIncoming: Boolean
)
