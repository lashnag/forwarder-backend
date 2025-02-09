package ru.lashnev.forwarderbackend.services

interface LemmatizerService {
    fun normalize(word: String): String
}