package com.example.crophealthmonitoringapp

data class ChatSummary(
    val chatId: String,
    val participantName: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long
)
