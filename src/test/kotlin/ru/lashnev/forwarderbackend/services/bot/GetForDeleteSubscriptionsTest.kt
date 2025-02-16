package ru.lashnev.forwarderbackend.services.bot

import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.junit.jupiter.api.Test
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.models.AdminCommand
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetForDeleteSubscriptionsTest : BaseIT() {
    @Autowired
    private lateinit var getForDeleteSubscriptionsService: GetForDeleteSubscriptionsService

    @Test
    @Sql("/sql/subscriptions.sql")
    fun deleteAllSubscriptions() {
        val subscriptionButtons = getSubscriptions()
        clickButton(subscriptionButtons, 5)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(0, savedSubscriptions.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(GetForDeleteSubscriptionsService.DELETED, captor.value.entities().parameters["text"])
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun deleteSamokatusSubscription() {
        val subscriptionButtons = getSubscriptions()
        clickButton(subscriptionButtons, 0)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(1, savedSubscriptions.size)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertTrue(
            captor.value
                .entities()
                .parameters["text"]
                .toString()
                .contains(GetForDeleteSubscriptionsService.DELETED),
        )
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun deleteSamokatusKazanKeyword() {
        val subscriptionButtons = getSubscriptions()
        clickButton(subscriptionButtons, 1)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(2, savedSubscriptions.size)
        assertEquals(
            1,
            savedSubscriptions
                .first()
                .search.properties.keywords.size,
        )
        verify(telegramBot, times(2)).execute(captor.capture())
        assertTrue(
            captor.value
                .entities()
                .parameters["text"]
                .toString()
                .contains(GetForDeleteSubscriptionsService.DELETED_SUBSCRIPTION),
        )
    }

    private fun getSubscriptions(): Array<out Array<InlineKeyboardButton>> {
        val messageGetSubscriptions = mock<Message>()
        val createUpdate = mock<Update>()
        whenever(createUpdate.message()).thenReturn(messageGetSubscriptions)
        whenever(messageGetSubscriptions.text()).thenReturn(AdminCommand.FETCH_SUBSCRIPTIONS.commandName)
        whenever(messageGetSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(createUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        val buttons = (lastCall.parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(6, buttons.size)
        return buttons
    }

    private fun clickButton(
        subscriptionButtons: Array<out Array<InlineKeyboardButton>>,
        buttonNumber: Int,
    ) {
        val callbackQueryDeleteAllSubscriptions = mock<CallbackQuery>()
        val deleteAllSubscriptionsUpdate = mock<Update>()
        whenever(deleteAllSubscriptionsUpdate.callbackQuery()).thenReturn(callbackQueryDeleteAllSubscriptions)
        val buttonDeleteAll = subscriptionButtons[buttonNumber][0]
        whenever(callbackQueryDeleteAllSubscriptions.data()).thenReturn(buttonDeleteAll.callbackData)
        whenever(callbackQueryDeleteAllSubscriptions.from()).thenReturn(user)

        getForDeleteSubscriptionsService.processUpdates(deleteAllSubscriptionsUpdate)
    }
}
