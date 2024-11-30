package ru.lashnev.forwarderbackend

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.services.GetForDeleteSubscriptionsService
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetForDeleteSubscriptionsTest : BaseIT() {

    @Autowired
    private lateinit var getForDeleteSubscriptionsService: GetForDeleteSubscriptionsService

    @MockBean
    private lateinit var telegramBot: TelegramBot

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

        val savedSubscriptions = subscriptionDao.getSubscriptions(testUsername)
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

        val savedSubscriptions = subscriptionDao.getSubscriptions(testUsername)
        assertEquals(1, savedSubscriptions.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertTrue(captor.value.entities().parameters["text"].toString().contains(GetForDeleteSubscriptionsService.DELETED))
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

        val savedSubscriptions = subscriptionDao.getSubscriptions(testUsername)
        assertEquals(2, savedSubscriptions.size)
        assertEquals(1, savedSubscriptions.first().keywords.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertTrue(captor.value.entities().parameters["text"].toString().contains(GetForDeleteSubscriptionsService.DELETED))
    }
}