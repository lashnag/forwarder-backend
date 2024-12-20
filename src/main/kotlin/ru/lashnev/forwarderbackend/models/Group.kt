package ru.lashnev.forwarderbackend.models

data class Group(
    val name: String,
    val lastMessageId: Long,
    val invalid: Boolean = false,
)
