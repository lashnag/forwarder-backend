package ru.lashnev.forwarderbackend

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.services.GetForDeleteSubscriptionsService
import ru.lashnev.forwarderbackend.services.SubscriptionExportService
import kotlin.test.assertEquals

class GetForDeleteSubscriptionsTest : BaseIT() {

    @Autowired
    private lateinit var getForDeleteSubscriptionsService: GetForDeleteSubscriptionsService

    @MockBean
    private lateinit var telegramBot: TelegramBot

    @MockBean
    private lateinit var subscriptionExportService: SubscriptionExportService

    private val user = mock(User::class.java)
    private val captor: ArgumentCaptor<SendMessage> = ArgumentCaptor.forClass(SendMessage::class.java)

    @BeforeEach
    fun setUp() {
        `when`(user.id()).thenReturn(1)
        `when`(user.username()).thenReturn("lashnag")
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun deleteAllSubscriptions() {
        val messageGetSubscriptions = mock(Message::class.java)
        val createUpdate = mock(Update::class.java)
        `when`(createUpdate.message()).thenReturn(messageGetSubscriptions)
        `when`(messageGetSubscriptions.text()).thenReturn(AdminCommand.FETCH_SUBSCRIPTIONS.commandName)
        `when`(messageGetSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(createUpdate)
        verify(telegramBot).execute(captor.capture())
        val buttons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(6, buttons.size)

        val callbackQueryDeleteAllSubscriptions = mock(CallbackQuery::class.java)
        val deleteAllSubscriptionsUpdate = mock(Update::class.java)
        `when`(deleteAllSubscriptionsUpdate.callbackQuery()).thenReturn(callbackQueryDeleteAllSubscriptions)
        val buttonDeleteAll = buttons[5][0]
        `when`(callbackQueryDeleteAllSubscriptions.data()).thenReturn(buttonDeleteAll.callbackData)
        `when`(callbackQueryDeleteAllSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(deleteAllSubscriptionsUpdate)

        val savedSubscriptions = subscriptionDao.getSubscriptions("lashnag")
        assertEquals(0, savedSubscriptions.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(GetForDeleteSubscriptionsService.DELETED, captor.value.entities().parameters["text"])
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun deleteSamokatusSubscription() {
        val messageGetSubscriptions = mock(Message::class.java)
        val createUpdate = mock(Update::class.java)
        `when`(createUpdate.message()).thenReturn(messageGetSubscriptions)
        `when`(messageGetSubscriptions.text()).thenReturn(AdminCommand.FETCH_SUBSCRIPTIONS.commandName)
        `when`(messageGetSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(createUpdate)
        verify(telegramBot).execute(captor.capture())
        val buttons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(6, buttons.size)

        val callbackQueryDeleteAllSubscriptions = mock(CallbackQuery::class.java)
        val deleteAllSubscriptionsUpdate = mock(Update::class.java)
        `when`(deleteAllSubscriptionsUpdate.callbackQuery()).thenReturn(callbackQueryDeleteAllSubscriptions)
        val deleteSamokatusSubscriptionButton = buttons[0][0]
        `when`(callbackQueryDeleteAllSubscriptions.data()).thenReturn(deleteSamokatusSubscriptionButton.callbackData)
        `when`(callbackQueryDeleteAllSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(deleteAllSubscriptionsUpdate)

        val savedSubscriptions = subscriptionDao.getSubscriptions("lashnag")
        assertEquals(1, savedSubscriptions.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(GetForDeleteSubscriptionsService.DELETED, captor.value.entities().parameters["text"])
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun deleteSamokatusKazanKeyword() {
        val messageGetSubscriptions = mock(Message::class.java)
        val createUpdate = mock(Update::class.java)
        `when`(createUpdate.message()).thenReturn(messageGetSubscriptions)
        `when`(messageGetSubscriptions.text()).thenReturn(AdminCommand.FETCH_SUBSCRIPTIONS.commandName)
        `when`(messageGetSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(createUpdate)
        verify(telegramBot).execute(captor.capture())
        val buttons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(6, buttons.size)

        val callbackQueryDeleteSamokatusKazanKeyword = mock(CallbackQuery::class.java)
        val deleteSamokatusKazanKeywordUpdate = mock(Update::class.java)
        `when`(deleteSamokatusKazanKeywordUpdate.callbackQuery()).thenReturn(callbackQueryDeleteSamokatusKazanKeyword)
        val deleteSamokatusKazanKeywordButton = buttons[1][0]
        `when`(callbackQueryDeleteSamokatusKazanKeyword.data()).thenReturn(deleteSamokatusKazanKeywordButton.callbackData)
        `when`(callbackQueryDeleteSamokatusKazanKeyword.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(deleteSamokatusKazanKeywordUpdate)

        val savedSubscriptions = subscriptionDao.getSubscriptions("lashnag")
        assertEquals(2, savedSubscriptions.size)
        assertEquals(1, savedSubscriptions.first().keywords.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(GetForDeleteSubscriptionsService.DELETED, captor.value.entities().parameters["text"])
    }
}