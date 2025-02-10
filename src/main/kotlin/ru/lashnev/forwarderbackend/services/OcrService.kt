package ru.lashnev.forwarderbackend.services

interface OcrService {
    fun convertToText(base64Image: String): String
}