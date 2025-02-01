package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import org.tartarus.snowball.ext.RussianStemmer
import org.tartarus.snowball.ext.EnglishStemmer
import ru.lashnev.forwarderbackend.models.Properties

@Service
class MessageCheckerService {
    fun doesMessageFit(message: String, searchProperties: Properties): Boolean {
        return (searchProperties.keywords.isEmpty() || containAllWords(message, searchProperties.keywords))
            && (searchProperties.maxMoney == null || hasAmountLessThan(message, searchProperties.maxMoney!!))
    }

    private fun containAllWords(message: String, keywords: List<String>): Boolean {
        val messageLemmas = message.split(Regex("[\\s,!?.]+"))
            .filterNot { it.isEmpty() }
            .map { it.stem() }
            .map { it.lowercase() }
        val keywordsLemmas = keywords.map { it.stem().lowercase() }
        return messageLemmas.containsAll(keywordsLemmas)
    }

    private fun hasAmountLessThan(message: String, maxAmount: Long): Boolean {
        val regex = Regex("\\d{2,}")
        val matches = regex.findAll(message)

        for (match in matches) {
            if (match.value.toLong() < maxAmount) {
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