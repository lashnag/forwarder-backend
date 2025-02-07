package ru.lashnev.forwarderbackend.services

interface LemmatizerService {
    fun normalize(sentence: String): String
}