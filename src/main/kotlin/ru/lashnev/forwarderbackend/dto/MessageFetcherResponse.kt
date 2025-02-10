package ru.lashnev.forwarderbackend.dto

data class MessageFetcherResponse(
    val messages: LinkedHashMap<Long, Message>
)

data class Message(
    val text: String,
    val image: String? = null,
)
