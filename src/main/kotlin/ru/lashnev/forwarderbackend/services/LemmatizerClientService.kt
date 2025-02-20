package ru.lashnev.forwarderbackend.services

import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.ApiProperties

@Service
@Primary
class LemmatizerClientService(
    private val restTemplate: RestTemplate,
    private val apiProperties: ApiProperties,
) : LemmatizerService {
    @Cacheable("lemmatizationCache")
    override fun normalize(word: String): String =
        restTemplate.postForEntity(apiProperties.lemmatizationUrl, Request(word), Response::class.java).body!!.lemmatized

    data class Request(
        val word: String,
    )

    data class Response(
        val lemmatized: String,
    )
}
