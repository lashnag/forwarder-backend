package ru.lashnev.forwarderbackend.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory

@Configuration
class RestTemplateConfiguration {

    @Value("\${rest-template.connection-timeout}")
    private lateinit var connectionTimeout: String

    @Bean
    fun getRestTemplate(): RestTemplate {
        val defaultUriBuilderFactory = DefaultUriBuilderFactory()
        defaultUriBuilderFactory.encodingMode = DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY

        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(connectionTimeout.toInt())
        factory.setReadTimeout(connectionTimeout.toInt())

        return RestTemplate(factory).apply { setUriTemplateHandler(defaultUriBuilderFactory) }
    }
}