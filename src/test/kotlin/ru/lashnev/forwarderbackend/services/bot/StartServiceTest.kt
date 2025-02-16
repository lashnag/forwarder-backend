package ru.lashnev.forwarderbackend.services.bot

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.services.bot.StartService.Companion.WELCOME_MESSAGE
import kotlin.test.assertEquals

class StartServiceTest : BaseIT() {
    @Autowired
    private lateinit var startService: StartService

    @Test
    fun testStartBot() {
        val messageStart = mock<Message>()
        val startUpdate = mock<Update>()
        whenever(startUpdate.message()).thenReturn(messageStart)
        whenever(messageStart.text()).thenReturn(AdminCommand.START.commandName)
        whenever(messageStart.from()).thenReturn(user)

        startService.processUpdates(startUpdate)

        verify(telegramBot).execute(captor.capture())
        assertEquals(WELCOME_MESSAGE, captor.value.entities().parameters["text"])
    }
}
