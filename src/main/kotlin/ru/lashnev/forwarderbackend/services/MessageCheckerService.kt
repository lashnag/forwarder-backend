package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.models.Properties

@Service
class MessageCheckerService(
    private val lemmatizerService: LemmatizerService,
) {
    fun doesMessageFit(
        message: String,
        searchProperties: Properties,
    ): Boolean =
        (searchProperties.keywords.isEmpty() || containAllWords(message, searchProperties.keywords)) &&
            (searchProperties.maxMoney == null || hasAmountLessThan(message, searchProperties.maxMoney!!))

    private fun containAllWords(
        message: String,
        keywords: List<String>,
    ): Boolean {
        val messageLemmas =
            message
                .split(Regex("[\\s,!?.]+"))
                .filterNot { it.isEmpty() }
                .map { lemmatizerService.normalize(it).lowercase() }
        val keywordsLemmas = keywords.map { lemmatizerService.normalize(it).lowercase() }
        return messageLemmas.containsAll(keywordsLemmas)
    }

    private fun hasAmountLessThan(
        message: String,
        maxAmount: Long,
    ): Boolean {
        val regex = Regex("\\d{3,}")
        val matches =
            regex.findAll(
                message
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", ""),
            )

        for (match in matches) {
            if (match.value.toLong() < maxAmount) {
                return true
            }
        }

        return false
    }
}
