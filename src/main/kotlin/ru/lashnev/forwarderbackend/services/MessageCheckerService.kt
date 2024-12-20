package ru.lashnev.forwarderbackend.services

import com.github.demidko.aot.WordformMeaning.lookupForMeanings
import org.springframework.stereotype.Service

@Service
class MessageCheckerService {
    fun containKeyword(message: String, keywords: Set<String>): Boolean {
        val messageLemmas = message.split(Regex("[\\s,!?.]+"))
            .filterNot { it.isEmpty() }
            .map {
                val wordParts = lookupForMeanings(it)
                if (wordParts.isEmpty()) it else wordParts[0].lemma.toString()
            }
            .map { it.lowercase() }


        keywords.forEach { keyword ->
            val keywordLemmas = keyword.split(Regex("[\\s,!?.]+"))
                .filterNot { it.isEmpty() }
                .map {
                    val wordParts = lookupForMeanings(it)
                    if (wordParts.isEmpty()) it else wordParts[0].lemma.toString()
                }
                .map { it.lowercase() }
            if (messageLemmas.containsAll(keywordLemmas)) {
                return true
            }
        }
        return false
    }
}