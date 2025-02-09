package ru.lashnev.forwarderbackend.configurations

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("telegram-forwarder-sender")
data class TelegramForwarderSenderProperties(
    val getMessageUrl: String,
    val lemmatizationUrl: String,
)