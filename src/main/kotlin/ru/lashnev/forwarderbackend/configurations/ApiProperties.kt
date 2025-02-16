package ru.lashnev.forwarderbackend.configurations

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("api")
data class ApiProperties(
    val getMessageUrl: String,
    val joinGroupUrl: String,
    val lemmatizationUrl: String,
    val ocrUrl: String,
)
