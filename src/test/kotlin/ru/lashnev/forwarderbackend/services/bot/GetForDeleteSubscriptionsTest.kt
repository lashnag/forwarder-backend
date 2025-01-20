package ru.lashnev.forwarderbackend.services.bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.models.AdminCommand
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
        val messageGetSubscriptions = mock<Message>()
        val createUpdate = mock<Update>()
        whenever(createUpdate.message()).thenReturn(messageGetSubscriptions)
        whenever(messageGetSubscriptions.text()).thenReturn(AdminCommand.FETCH_SUBSCRIPTIONS.commandName)
        whenever(messageGetSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(createUpdate)
        verify(telegramBot).execute(captor.capture())
        val buttons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(6, buttons.size)

        val callbackQueryDeleteAllSubscriptions = mock<CallbackQuery>()
        val deleteAllSubscriptionsUpdate = mock<Update>()
        whenever(deleteAllSubscriptionsUpdate.callbackQuery()).thenReturn(callbackQueryDeleteAllSubscriptions)
        val buttonDeleteAll = buttons[5][0]
        whenever(callbackQueryDeleteAllSubscriptions.data()).thenReturn(buttonDeleteAll.callbackData)
        whenever(callbackQueryDeleteAllSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(deleteAllSubscriptionsUpdate)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(0, savedSubscriptions.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(GetForDeleteSubscriptionsService.DELETED, captor.value.entities().parameters["text"])
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun deleteSamokatusSubscription() {
        val messageGetSubscriptions = mock<Message>()
        val createUpdate = mock<Update>()
        whenever(createUpdate.message()).thenReturn(messageGetSubscriptions)
        whenever(messageGetSubscriptions.text()).thenReturn(AdminCommand.FETCH_SUBSCRIPTIONS.commandName)
        whenever(messageGetSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(createUpdate)
        verify(telegramBot).execute(captor.capture())
        val buttons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(6, buttons.size)

        val callbackQueryDeleteAllSubscriptions = mock<CallbackQuery>()
        val deleteAllSubscriptionsUpdate = mock<Update>()
        whenever(deleteAllSubscriptionsUpdate.callbackQuery()).thenReturn(callbackQueryDeleteAllSubscriptions)
        val deleteSamokatusSubscriptionButton = buttons[0][0]
        whenever(callbackQueryDeleteAllSubscriptions.data()).thenReturn(deleteSamokatusSubscriptionButton.callbackData)
        whenever(callbackQueryDeleteAllSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(deleteAllSubscriptionsUpdate)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(1, savedSubscriptions.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertTrue(captor.value.entities().parameters["text"].toString().contains(GetForDeleteSubscriptionsService.DELETED))
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun deleteSamokatusKazanKeyword() {
        val messageGetSubscriptions = mock<Message>()
        val createUpdate = mock<Update>()
        whenever(createUpdate.message()).thenReturn(messageGetSubscriptions)
        whenever(messageGetSubscriptions.text()).thenReturn(AdminCommand.FETCH_SUBSCRIPTIONS.commandName)
        whenever(messageGetSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(createUpdate)
        verify(telegramBot).execute(captor.capture())
        val buttons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(6, buttons.size)

        val callbackQueryDeleteSamokatusKazanKeyword = mock<CallbackQuery>()
        val deleteSamokatusKazanKeywordUpdate = mock<Update>()
        whenever(deleteSamokatusKazanKeywordUpdate.callbackQuery()).thenReturn(callbackQueryDeleteSamokatusKazanKeyword)
        val deleteSamokatusKazanKeywordButton = buttons[1][0]
        whenever(callbackQueryDeleteSamokatusKazanKeyword.data()).thenReturn(deleteSamokatusKazanKeywordButton.callbackData)
        whenever(callbackQueryDeleteSamokatusKazanKeyword.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(deleteSamokatusKazanKeywordUpdate)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(2, savedSubscriptions.size)
        assertEquals(1, savedSubscriptions.first().search.properties.keywords.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertTrue(captor.value.entities().parameters["text"].toString().contains(GetForDeleteSubscriptionsService.DELETED_SUBSCRIPTION))
    }
}