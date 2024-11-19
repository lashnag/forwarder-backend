package ru.lashnev.forwarderbackend

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.services.CreateSubscriptionService
import kotlin.test.assertEquals

class CreateSubscriptionTest : BaseIT() {

    @Autowired
    private lateinit var createSubscriptionService: CreateSubscriptionService

    @MockBean
    private lateinit var telegramBot: TelegramBot

    @Test
    fun testCreateSubscription() {
        val captor: ArgumentCaptor<SendMessage> = ArgumentCaptor.forClass(SendMessage::class.java)

        // request to create subscription
        val user = mock(User::class.java)
        val messageCreateSubscription = mock(Message::class.java)
        val createUpdate = mock(Update::class.java)
        `when`(createUpdate.message()).thenReturn(messageCreateSubscription)
        `when`(messageCreateSubscription.text()).thenReturn(AdminCommand.CREATE_SUBSCRIPTION.commandName)
        `when`(messageCreateSubscription.from()).thenReturn(user)
        `when`(user.id()).thenReturn(1)
        `when`(user.username()).thenReturn("lashnag")

        createSubscriptionService.processUpdates(createUpdate)
        verify(telegramBot).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_GROUP_NAME, captor.value.entities().parameters["text"])

        // enter subscription group
        val messageCreateSubscriptionEnterSubscription = mock(Message::class.java)
        val createUpdateEnterSubscription = mock(Update::class.java)
        `when`(createUpdateEnterSubscription.message()).thenReturn(messageCreateSubscriptionEnterSubscription)
        `when`(messageCreateSubscriptionEnterSubscription.text()).thenReturn("samokatus")
        `when`(messageCreateSubscriptionEnterSubscription.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateEnterSubscription)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_KEYWORD, captor.value.entities().parameters["text"])

        // enter keyword 1
        val messageCreateSubscriptionEnterKeyword1 = mock(Message::class.java)
        val createUpdateEnterKeyword1 = mock(Update::class.java)
        `when`(createUpdateEnterKeyword1.message()).thenReturn(messageCreateSubscriptionEnterKeyword1)
        `when`(messageCreateSubscriptionEnterKeyword1.text()).thenReturn("казань")
        `when`(messageCreateSubscriptionEnterKeyword1.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateEnterKeyword1)
        verify(telegramBot, times(3)).execute(captor.capture())
        val buttons1 = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, buttons1.size)

        // enter one more keyword
        val callbackQueryMoreKeywords = mock(CallbackQuery::class.java)
        val createUpdateMoreKeywords = mock(Update::class.java)
        `when`(createUpdateMoreKeywords.callbackQuery()).thenReturn(callbackQueryMoreKeywords)
        val buttonMore = buttons1.first()[0]
        `when`(callbackQueryMoreKeywords.data()).thenReturn(buttonMore.callbackData)
        `when`(callbackQueryMoreKeywords.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateMoreKeywords)
        verify(telegramBot, times(4)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_KEYWORD, captor.value.entities().parameters["text"])

        // enter keyword 2
        val messageCreateSubscriptionEnterKeyword2 = mock(Message::class.java)
        val createUpdateEnterKeyword2 = mock(Update::class.java)
        `when`(createUpdateEnterKeyword2.message()).thenReturn(messageCreateSubscriptionEnterKeyword2)
        `when`(messageCreateSubscriptionEnterKeyword2.text()).thenReturn("новосибирск")
        `when`(messageCreateSubscriptionEnterKeyword2.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateEnterKeyword2)
        verify(telegramBot, times(5)).execute(captor.capture())
        val buttons2 = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, buttons2.size)

        // save subscription entered
        val callbackQuerySave = mock(CallbackQuery::class.java)
        val createUpdateSave = mock(Update::class.java)
        `when`(createUpdateSave.callbackQuery()).thenReturn(callbackQuerySave)
        val buttonSubscribe = buttons2[1][0]
        `when`(callbackQuerySave.data()).thenReturn(buttonSubscribe.callbackData)
        `when`(callbackQuerySave.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateSave)
        verify(telegramBot, times(6)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.SUBSCRIPTION_SUCCESS, captor.value.entities().parameters["text"])

        val savedSubscriptions = subscriptionDao.getSubscriptions("lashnag")
        assertEquals(1, savedSubscriptions.size)
        assertEquals(2, savedSubscriptions.first().keywords.size)
    }
}