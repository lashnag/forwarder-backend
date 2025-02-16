package ru.lashnev.forwarderbackend.services

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.DefaultResourceLoader
import ru.lashnev.forwarderbackend.BaseIT
import java.util.Base64
import kotlin.test.assertTrue

class OcrClientServiceTest : BaseIT() {
    @Autowired
    private lateinit var ocrClientService: OcrClientService

    @Test
    fun test() {
        val resourceLoader = DefaultResourceLoader()
        val travelRadarImage = resourceLoader.getResource("classpath:images/travelradar.jpg")
        val imageBytes = travelRadarImage.inputStream.use { it.readAllBytes() }
        val base64String = Base64.getEncoder().encodeToString(imageBytes)

        val text = ocrClientService.convertToText(base64String)
        assertTrue { text.contains("Багаж") }
        assertTrue { text.contains("Travel") }
    }
}
