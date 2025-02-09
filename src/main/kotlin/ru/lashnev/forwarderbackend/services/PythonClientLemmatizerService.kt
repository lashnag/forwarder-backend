package ru.lashnev.forwarderbackend.services

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.ApiProperties

@Service
@Primary
class PythonClientLemmatizerService(
    private val restTemplate: RestTemplate,
    private val senderProperties: ApiProperties,
) : LemmatizerService {
    override fun normalize(word: String): String {
        return restTemplate.postForEntity(senderProperties.lemmatizationUrl, Request(word), Response::class.java).body!!.lemmatized
    }

    data class Request(val word: String)
    data class Response(val lemmatized: String)
}