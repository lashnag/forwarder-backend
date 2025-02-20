package ru.lashnev.forwarderbackend.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.models.Properties

class MessageCheckerServiceTest : BaseIT() {
    @Autowired
    private lateinit var messageCheckerService: MessageCheckerService

    @Test
    fun testDoesMessageFit() {
        val message = "Шла Саша по шоссе и сосала сушку"
        val keyword = "Сушка"

        val result = messageCheckerService.doesMessageFit(message, Properties(keywords = mutableListOf(keyword)))

        assertThat(result).isTrue()
    }

    @Test
    fun testDoesMessageFitAFewKeyword() {
        val message = "Шла Саша по шоссе и сосала сушку"
        val keyword1 = "Сушка"
        val keyword2 = "шоссе"

        val result = messageCheckerService.doesMessageFit(message, Properties(keywords = mutableListOf(keyword1, keyword2)))

        assertThat(result).isTrue()
    }

    @Test
    fun testDoesNotMessageFitAFewKeywords() {
        val message = "Шла Саша по шоссе и сосала сушку"
        val keyword1 = "Сушка"
        val keyword2 = "Собака"

        val result = messageCheckerService.doesMessageFit(message, Properties(keywords = mutableListOf(keyword1, keyword2)))

        assertThat(result).isFalse()
    }

    @Test
    fun testDoesNotFitMessage() {
        val message = "Шла Саша по шоссе и сосала сушку"
        val keyword = "Собака"

        val result = messageCheckerService.doesMessageFit(message, Properties(keywords = mutableListOf(keyword)))

        assertThat(result).isFalse()
    }

    @Test
    fun testDoesMessageFitNotRussianMessageAndKeywords() {
        val message = "London is the capital of Great Britain"
        val keyword = "Capital"

        val result = messageCheckerService.doesMessageFit(message, Properties(keywords = mutableListOf(keyword)))

        assertThat(result).isTrue()
    }

    @Test
    fun testDoesMessageFitMaxMoney() {
        val message = "Москва - Краснодар 1000 рублей"

        val result = messageCheckerService.doesMessageFit(message, Properties(maxMoney = 1500))

        assertThat(result).isTrue()
    }

    @Test
    fun testDoesMessageWithSpaceFitMaxMoney() {
        val message = "Москва - Краснодар 1 000 рублей"

        val result = messageCheckerService.doesMessageFit(message, Properties(maxMoney = 1500))

        assertThat(result).isTrue()
    }

    @Test
    fun testDoesMessageWithCommaFitMaxMoney() {
        val message = "Москва - Краснодар 1,000 рублей"

        val result = messageCheckerService.doesMessageFit(message, Properties(maxMoney = 1500))

        assertThat(result).isTrue()
    }

    @Test
    fun testDoesNotMessageFitMaxMoney() {
        val message = "Москва - Краснодар 1500 рублей"

        val result = messageCheckerService.doesMessageFit(message, Properties(maxMoney = 1000))

        assertThat(result).isFalse()
    }

    @Test
    fun testDoesMessageFitByKeywordAndMaxMoney() {
        val message = "Москва - Краснодар 1000 рублей"
        val keyword = "москва"

        val result = messageCheckerService.doesMessageFit(message, Properties(maxMoney = 2000, keywords = mutableListOf(keyword)))

        assertThat(result).isTrue()
    }

    @Test
    fun testSentenceWithOtherDeclensionFitKeyword() {
        val message = "Подешевело! Прямые рейсы из Египта в Тюмень, Челябинск и Н.Новгород от 5600₽ в одну сторону"
        val keyword = "египет"

        val result = messageCheckerService.doesMessageFit(message, Properties(keywords = mutableListOf(keyword)))

        assertThat(result).isTrue()
    }
}
