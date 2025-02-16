package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.ApiProperties

@Service
class OcrClientService(
    private val restTemplate: RestTemplate,
    private val apiProperties: ApiProperties,
) : OcrService {
    override fun convertToText(base64Image: String): String {
        val response = restTemplate.postForEntity(apiProperties.ocrUrl, Request(base64Image), Response::class.java).body!!
        return "${response.ruText} ${response.enText}"
    }

    data class Request(
        val base64Image: String,
    )

    data class Response(
        val ruText: String,
        val enText: String,
    )
}
