package ru.lashnev.forwarderbackend.services

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.models.Properties

@Service
class MessageCheckerService(private val lemmatizerService: LemmatizerService) {
    fun doesMessageFit(message: String, searchProperties: Properties): Boolean {
        val cleanMessage = removeMarkdown(message)
        return (searchProperties.keywords.isEmpty() || containAllWords(cleanMessage, searchProperties.keywords))
            && (searchProperties.maxMoney == null || hasAmountLessThan(cleanMessage, searchProperties.maxMoney!!))
    }

    private fun containAllWords(message: String, keywords: List<String>): Boolean {
        val messageLemmas = lemmatizerService.normalize(message).split(Regex("[\\s,!?.]+"))
            .filterNot { it.isEmpty() }
            .map { it.lowercase() }
        val keywordsLemmas = keywords.map { lemmatizerService.normalize(it).lowercase() }
        return messageLemmas.containsAll(keywordsLemmas)
    }

    private fun hasAmountLessThan(message: String, maxAmount: Long): Boolean {
        val regex = Regex("\\d{3,}")
        val matches = regex.findAll(
            message
                .replace(" ", "")
                .replace(".", "")
                .replace(",", "")
        )

        for (match in matches) {
            if (match.value.toLong() < maxAmount) {
                return true
            }
        }

        return false
    }

    private fun removeMarkdown(text: String): String {
        val options = MutableDataSet()
        val parser = Parser.builder(options).build()
        val renderer = HtmlRenderer.builder(options).build()

        val document: Node = parser.parse(text)

        return renderer.render(document).replace(Regex("<.*?>"), "")
    }
}