package ru.lashnev.forwarderbackend.utils

import com.pengrad.telegrambot.request.SendMessage
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.utils.SendTextUtilService.Companion.MAX_MESSAGE_LENGTH

class SendTextUtilServiceTest : BaseIT() {

    @Autowired
    private lateinit var sendTextUtilService: SendTextUtilService

    @Test
    fun sendVeryLongMessage() {
        val char = 'a'
        val stringBuilder = StringBuilder().apply {
            repeat(MAX_MESSAGE_LENGTH + 10) {
                append(char)
            }
        }

        sendTextUtilService.sendText(12233, stringBuilder.toString())
        verify(telegramBot, times(2)).execute(any<SendMessage>())
    }
}