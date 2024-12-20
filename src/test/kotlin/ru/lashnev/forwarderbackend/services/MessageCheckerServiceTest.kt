package ru.lashnev.forwarderbackend.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MessageCheckerServiceTest {

    private val messageCheckerService = MessageCheckerService()

    @Test
    fun testContainKeyword() {
        val message = "Шла Саша по шоссе и сосала сушку"
        val keyword = "Сушка"

        val result = messageCheckerService.containKeyword(message, setOf(keyword))

        assertThat(result).isTrue()
    }

    @Test
    fun testContainSentenceKeyword() {
        val message = "Шла Саша по шоссе и сосала сушку"
        val keyword = "Сушка шоссе"

        val result = messageCheckerService.containKeyword(message, setOf(keyword))

        assertThat(result).isTrue()
    }

    @Test
    fun testContainFewKeywords() {
        val message = "Шла Саша по шоссе и сосала сушку"
        val keyword1 = "Сушка"
        val keyword2 = "Собака"

        val result = messageCheckerService.containKeyword(message, setOf(keyword1, keyword2))

        assertThat(result).isTrue()
    }

    @Test
    fun testDoesNotContainKeyword() {
        val message = "Шла Саша по шоссе и сосала сушку"
        val keyword = "Собака"

        val result = messageCheckerService.containKeyword(message, setOf(keyword))

        assertThat(result).isFalse()
    }

    @Test
    fun testNotRussianMessageAndKeywords() {
        val message = "London is the capital of Great Britain"
        val keyword = "Capital"

        val result = messageCheckerService.containKeyword(message, setOf(keyword))

        assertThat(result).isTrue()
    }
}