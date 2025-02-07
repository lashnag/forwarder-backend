package ru.lashnev.forwarderbackend.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfiguration {

    @Value("\${rest-template.connection-timeout}")
    private lateinit var connectionTimeout: String

    @Bean
    fun getRestTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(connectionTimeout.toInt())
        factory.setReadTimeout(connectionTimeout.toInt())

        return RestTemplate(factory)
    }
}