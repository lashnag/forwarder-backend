package ru.lashnev.forwarderbackend.services.bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.services.bot.ChangeVersionService.Companion.ALREADY_V2_MESSAGE
import ru.lashnev.forwarderbackend.services.bot.ChangeVersionService.Companion.CHANGE_VERSION_SUCCESS
import kotlin.test.assertEquals

class ChangeVersionServiceTest : BaseIT() {
    @Autowired
    private lateinit var changeVersionService: ChangeVersionService

    @MockBean
    private lateinit var telegramBot: TelegramBot

    @Test
    @Sql("/sql/subscriber_v1.sql")
    fun testChangeVersion() {
        val messageCreateSubscription = mock(Message::class.java)
        val createUpdate = mock(Update::class.java)
        `when`(createUpdate.message()).thenReturn(messageCreateSubscription)
        `when`(messageCreateSubscription.text()).thenReturn(AdminCommand.CHANGE_VERSION_V2.commandName)
        `when`(messageCreateSubscription.from()).thenReturn(user)

        changeVersionService.processUpdates(createUpdate)

        verify(telegramBot).execute(captor.capture())
        assertEquals(CHANGE_VERSION_SUCCESS, captor.value.entities().parameters["text"])
        val subscriber = subscribersDao.getSubscriber(user.username())
        assertThat(subscriber?.chatId).isNotNull
    }

    @Test
    @Sql("/sql/subscriber_v2.sql")
    fun testVersionAlreadyChanged() {
        val messageCreateSubscription = mock(Message::class.java)
        val createUpdate = mock(Update::class.java)
        `when`(createUpdate.message()).thenReturn(messageCreateSubscription)
        `when`(messageCreateSubscription.text()).thenReturn(AdminCommand.CHANGE_VERSION_V2.commandName)
        `when`(messageCreateSubscription.from()).thenReturn(user)

        changeVersionService.processUpdates(createUpdate)

        verify(telegramBot).execute(captor.capture())
        assertEquals(ALREADY_V2_MESSAGE, captor.value.entities().parameters["text"])
    }
}