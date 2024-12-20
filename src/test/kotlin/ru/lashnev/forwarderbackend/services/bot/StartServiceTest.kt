package ru.lashnev.forwarderbackend.services.bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.services.bot.StartService.Companion.WELCOME_MESSAGE
import kotlin.test.assertEquals

class StartServiceTest : BaseIT() {
    @Autowired
    private lateinit var startService: StartService

    @MockBean
    private lateinit var telegramBot: TelegramBot

    @Test
    fun testStartBot() {
        val messageCreateSubscription = mock(Message::class.java)
        val createUpdate = mock(Update::class.java)
        `when`(createUpdate.message()).thenReturn(messageCreateSubscription)
        `when`(messageCreateSubscription.text()).thenReturn(AdminCommand.START.commandName)
        `when`(messageCreateSubscription.from()).thenReturn(user)

        startService.processUpdates(createUpdate)

        verify(telegramBot).execute(captor.capture())
        assertEquals(WELCOME_MESSAGE, captor.value.entities().parameters["text"])
    }
}