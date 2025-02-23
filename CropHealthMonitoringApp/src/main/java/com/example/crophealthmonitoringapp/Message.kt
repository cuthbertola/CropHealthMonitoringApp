package com.example.crophealthmonitoringapp

data class Message(
    val chatId: String = "",
    val messageText: String = "",
    val senderId: String = "",
    val timestamp: Long = 0
)
