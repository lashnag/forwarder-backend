package ru.lashnev.forwarderbackend.dto

data class SubscriptionRawDto(
    val subscriber: String,
    val subscription: String,
    val keyword: String,
)
