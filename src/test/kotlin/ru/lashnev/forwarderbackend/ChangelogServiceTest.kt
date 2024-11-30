package ru.lashnev.forwarderbackend

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.services.ChangelogService
import kotlin.test.assertTrue

class ChangelogServiceTest : BaseIT() {
    @Autowired
    private lateinit var changelogService: ChangelogService

    @MockBean
    private lateinit var telegramBot: TelegramBot

    @Test
    fun testStartBot() {
        val messageCreateSubscription = mock(Message::class.java)
        val createUpdate = mock(Update::class.java)
        `when`(createUpdate.message()).thenReturn(messageCreateSubscription)
        `when`(messageCreateSubscription.text()).thenReturn(AdminCommand.CHANGELOG.commandName)
        `when`(messageCreateSubscription.from()).thenReturn(user)

        changelogService.processUpdates(createUpdate)

        verify(telegramBot).execute(captor.capture())
        assertTrue(captor.value.entities().parameters["text"].toString().contains("Changelog"))
    }
}