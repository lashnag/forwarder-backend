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
import kotlin.test.assertTrue

class ChangelogServiceTest : BaseIT() {
    @Autowired
    private lateinit var changelogService: ChangelogService

    @Test
    fun testShowChangelog() {
        val messageChangelog = mock<Message>()
        val changelogUpdate = mock<Update>()
        whenever(changelogUpdate.message()).thenReturn(messageChangelog)
        whenever(messageChangelog.text()).thenReturn(AdminCommand.CHANGELOG.commandName)
        whenever(messageChangelog.from()).thenReturn(user)

        changelogService.processUpdates(changelogUpdate)

        verify(telegramBot).execute(captor.capture())
        assertTrue(captor.value.entities().parameters["text"].toString().contains("Changelog"))
    }
}