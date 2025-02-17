package ru.lashnev.forwarderbackend.dto

data class Mail(
    val text: String,
    val userName: String? = null,
)
