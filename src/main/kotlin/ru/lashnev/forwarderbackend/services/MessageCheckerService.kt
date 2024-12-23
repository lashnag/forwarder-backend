package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import org.tartarus.snowball.ext.RussianStemmer
import org.tartarus.snowball.ext.EnglishStemmer

@Service
class MessageCheckerService {
    fun containKeyword(message: String, keywords: Set<String>): Boolean {
        val messageLemmas = message.split(Regex("[\\s,!?.]+"))
            .filterNot { it.isEmpty() }
            .map { it.stem() }
            .map { it.lowercase() }


        keywords.forEach { keyword ->
            val keywordLemmas = keyword.split(Regex("[\\s,!?.]+"))
                .filterNot { it.isEmpty() }
                .map { it.stem() }
                .map { it.lowercase() }
            if (messageLemmas.containsAll(keywordLemmas)) {
                return true
            }
        }
        return false
    }

    private fun String.stem(): String {
        return if (isRussian(this)) {
            russianStemmer.current = this
            russianStemmer.stem()
            russianStemmer.current
        } else {
            englishStemmer.current = this
            englishStemmer.stem()
            englishStemmer.current
        }
    }

    private fun isRussian(message: String): Boolean {
        val regex = Regex("[а-яА-ЯёЁ]+")
        return regex.containsMatchIn(message)
    }

    companion object {
        val russianStemmer = RussianStemmer()
        val englishStemmer = EnglishStemmer()
    }
}