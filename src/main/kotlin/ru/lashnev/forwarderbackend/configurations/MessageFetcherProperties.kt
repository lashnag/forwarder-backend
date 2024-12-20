package ru.lashnev.forwarderbackend.configurations

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("message-fetcher")
data class MessageFetcherProperties(
    val url: String
)