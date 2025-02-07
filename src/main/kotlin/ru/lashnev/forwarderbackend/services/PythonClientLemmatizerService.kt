package ru.lashnev.forwarderbackend.services

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.utils.runCommandAndWaitResult

@Service
@Primary
class PythonClientLemmatizerService(private val restTemplate: RestTemplate) : LemmatizerService {
    init {
        val pipInstallCommand = arrayOf("pip", "install", "-r", "requirements.txt")
        runCommandAndWaitResult(pipInstallCommand)
        val pythonCommand = arrayOf("python", "src/main/python/lemmatizer_server.py")
        Runtime.getRuntime().exec(pythonCommand)
    }

    override fun normalize(sentence: String): String {
        return restTemplate.postForEntity("http://127.0.0.1:4892/lemmatize", Request(sentence), Response::class.java).body!!.lemmatized
    }

    data class Request(val sentence: String)
    data class Response(val lemmatized: String)
}