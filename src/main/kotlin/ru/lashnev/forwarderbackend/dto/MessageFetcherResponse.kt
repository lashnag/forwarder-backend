package ru.lashnev.forwarderbackend.dto

data class MessageFetcherResponse(
    val messages: LinkedHashMap<Long, String>
)
