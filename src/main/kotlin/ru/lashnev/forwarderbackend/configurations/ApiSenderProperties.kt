package ru.lashnev.forwarderbackend.configurations

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ru.lashnev.forwarder.api")
data class ApiSenderProperties(
    val uri: String
)
