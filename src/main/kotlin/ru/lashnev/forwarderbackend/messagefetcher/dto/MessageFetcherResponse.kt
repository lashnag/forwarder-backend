package ru.lashnev.forwarderbackend.messagefetcher.dto

data class MessageFetcherResponse(
    val messages: LinkedHashMap<Long, String>
)
