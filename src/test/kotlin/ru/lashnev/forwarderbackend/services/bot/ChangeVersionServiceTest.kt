package ru.lashnev.forwarderbackend.services.bot

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.services.bot.ChangeVersionService.Companion.ALREADY_V2_MESSAGE
import ru.lashnev.forwarderbackend.services.bot.ChangeVersionService.Companion.CHANGE_VERSION_SUCCESS
import kotlin.test.assertEquals

class ChangeVersionServiceTest : BaseIT() {
    @Autowired
    private lateinit var changeVersionService: ChangeVersionService

    @Test
    @Sql("/sql/subscriber_v1.sql")
    fun testChangeVersion() {
        val messageChangeVersion = mock<Message>()
        val changeVersionUpdate = mock<Update>()
        whenever(changeVersionUpdate.message()).thenReturn(messageChangeVersion)
        whenever(messageChangeVersion.text()).thenReturn(AdminCommand.CHANGE_VERSION_V2.commandName)
        whenever(messageChangeVersion.from()).thenReturn(user)

        changeVersionService.processUpdates(changeVersionUpdate)

        verify(telegramBot).execute(captor.capture())
        assertEquals(CHANGE_VERSION_SUCCESS, captor.value.entities().parameters["text"])
        val subscriber = subscribersDao.getSubscriber(user.username())
        assertThat(subscriber?.chatId).isNotNull
    }

    @Test
    @Sql("/sql/subscriber_v2.sql")
    fun testVersionAlreadyChanged() {
        val messageChangeVersion = mock<Message>()
        val changeVersionUpdate = mock<Update>()
        whenever(changeVersionUpdate.message()).thenReturn(messageChangeVersion)
        whenever(messageChangeVersion.text()).thenReturn(AdminCommand.CHANGE_VERSION_V2.commandName)
        whenever(messageChangeVersion.from()).thenReturn(user)
        val messageResponse = mock<SendResponse>()
        whenever(messageResponse.errorCode()).thenReturn(0)
        whenever(telegramBot.execute(any<SendMessage>())).thenReturn(messageResponse)

        changeVersionService.processUpdates(changeVersionUpdate)

        verify(telegramBot).execute(captor.capture())
        assertEquals(ALREADY_V2_MESSAGE, captor.value.entities().parameters["text"])
    }
}