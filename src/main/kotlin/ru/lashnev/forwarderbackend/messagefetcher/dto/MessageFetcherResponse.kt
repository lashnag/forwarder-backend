package ru.lashnev.forwarderbackend.messagefetcher.dto

data class MessageFetcherResponse(
    val messages: List<String>,
    val lastMessageId: Long,
)
