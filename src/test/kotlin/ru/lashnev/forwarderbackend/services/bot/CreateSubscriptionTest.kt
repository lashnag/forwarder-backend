package ru.lashnev.forwarderbackend.services.bot

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
import org.springframework.test.context.jdbc.Sql
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.models.AdminCommand
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CreateSubscriptionTest : BaseIT() {

    @Autowired
    private lateinit var createSubscriptionService: CreateSubscriptionService

    @Test
    fun testCreateSubscriptionSearchByTwoKeywords() {
        // request to create subscription
        val createSubscriptionMessage = mock<Message>()
        val createSubscriptionUpdate = mock<Update>()
        whenever(createSubscriptionUpdate.message()).thenReturn(createSubscriptionMessage)
        whenever(createSubscriptionMessage.text()).thenReturn(AdminCommand.CREATE_SUBSCRIPTION.commandName)
        whenever(createSubscriptionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createSubscriptionUpdate)
        verify(telegramBot).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_GROUP_NAME, captor.value.entities().parameters["text"])

        // enter subscription
        val enterSubscriptionMessage = mock<Message>()
        val enterSubscriptionUpdate = mock<Update>()
        whenever(enterSubscriptionUpdate.message()).thenReturn(enterSubscriptionMessage)
        whenever(enterSubscriptionMessage.text()).thenReturn(VALID_GROUP_NAME)
        whenever(enterSubscriptionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterSubscriptionUpdate)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.CHOOSE_SEARCH_PARAM, captor.value.entities().parameters["text"])
        val searchTypeButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, searchTypeButtons.size)

        // choose search by keyword first
        val firstChooseSearchTypeKeywordCallbackQuery = mock<CallbackQuery>()
        val firstChooseSearchTypeKeywordUpdate = mock<Update>()
        whenever(firstChooseSearchTypeKeywordUpdate.callbackQuery()).thenReturn(firstChooseSearchTypeKeywordCallbackQuery)
        val firstChooseSearchTypeKeywordButton = searchTypeButtons.first().first()
        whenever(firstChooseSearchTypeKeywordCallbackQuery.data()).thenReturn(firstChooseSearchTypeKeywordButton.callbackData)
        whenever(firstChooseSearchTypeKeywordCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(firstChooseSearchTypeKeywordUpdate)
        verify(telegramBot, times(3)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_KEYWORD, captor.value.entities().parameters["text"])

        // enter first keyword
        val enterFirstKeywordMessage = mock<Message>()
        val enterFirstKeywordUpdate = mock<Update>()
        whenever(enterFirstKeywordUpdate.message()).thenReturn(enterFirstKeywordMessage)
        whenever(enterFirstKeywordMessage.text()).thenReturn("казань")
        whenever(enterFirstKeywordMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterFirstKeywordUpdate)
        verify(telegramBot, times(4)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.CHOOSE_ACTION, captor.value.entities().parameters["text"])
        val afterFirstKeywordButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, afterFirstKeywordButtons.size)

        // more search condition
        val moreSearchConditionMessage = mock<CallbackQuery>()
        val moreSearchConditionUpdate = mock<Update>()
        whenever(moreSearchConditionUpdate.callbackQuery()).thenReturn(moreSearchConditionMessage)
        val moreSearchConditionButton = afterFirstKeywordButtons.first().first()
        whenever(moreSearchConditionMessage.data()).thenReturn(moreSearchConditionButton.callbackData)
        whenever(moreSearchConditionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(moreSearchConditionUpdate)
        verify(telegramBot, times(5)).execute(captor.capture())
        assertTrue((captor.value.entities().parameters["text"] as String).contains(CreateSubscriptionService.CHOOSE_SEARCH_PARAM))

        // choose search by keyword second
        val secondChooseSearchTypeKeywordCallbackQuery = mock<CallbackQuery>()
        val secondChooseSearchTypeKeywordUpdate = mock<Update>()
        whenever(secondChooseSearchTypeKeywordUpdate.callbackQuery()).thenReturn(secondChooseSearchTypeKeywordCallbackQuery)
        val secondChooseSearchTypeKeywordButton = searchTypeButtons.first().first()
        whenever(secondChooseSearchTypeKeywordCallbackQuery.data()).thenReturn(secondChooseSearchTypeKeywordButton.callbackData)
        whenever(secondChooseSearchTypeKeywordCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(secondChooseSearchTypeKeywordUpdate)
        verify(telegramBot, times(6)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_KEYWORD, captor.value.entities().parameters["text"])

        // enter second keyword
        val enterSecondKeywordMessage = mock<Message>()
        val enterSecondKeywordUpdate = mock<Update>()
        whenever(enterSecondKeywordUpdate.message()).thenReturn(enterSecondKeywordMessage)
        whenever(enterSecondKeywordMessage.text()).thenReturn("новосибирск")
        whenever(enterSecondKeywordMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterSecondKeywordUpdate)
        verify(telegramBot, times(7)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.CHOOSE_ACTION, captor.value.entities().parameters["text"])
        val afterSecondKeywordButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, afterSecondKeywordButtons.size)

        // save subscription entered
        val saveCallbackQuery = mock<CallbackQuery>()
        val saveUpdate = mock<Update>()
        whenever(saveUpdate.callbackQuery()).thenReturn(saveCallbackQuery)
        val subscribeButton = afterSecondKeywordButtons[1][0]
        whenever(saveCallbackQuery.data()).thenReturn(subscribeButton.callbackData)
        whenever(saveCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(saveUpdate)
        verify(telegramBot, times(8)).execute(captor.capture())
        assertTrue((captor.value.entities().parameters["text"] as String).contains(CreateSubscriptionService.SUBSCRIPTION_SUCCESS))

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(1, savedSubscriptions.size)
        assertEquals(2, savedSubscriptions.first().search.properties.keywords.size)
        val savedSubscribers = subscribersDao.getSubscribers()
        assertEquals(1, savedSubscribers.size)
        assertEquals(testUsername, savedSubscribers.first().username)
        assertNotNull(savedSubscribers.first().chatId)
        val savedGroups = groupsDao.getValidGroups()
        assertEquals(1, savedGroups.size)
        assertTrue(VALID_GROUP_NAME.contains(savedGroups.first().name))
    }

    @Test
    fun testCreateSubscriptionSearchKeywordAndMaxAmount() {
        // request to create subscription
        val createSubscriptionMessage = mock<Message>()
        val createSubscriptionUpdate = mock<Update>()
        whenever(createSubscriptionUpdate.message()).thenReturn(createSubscriptionMessage)
        whenever(createSubscriptionMessage.text()).thenReturn(AdminCommand.CREATE_SUBSCRIPTION.commandName)
        whenever(createSubscriptionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createSubscriptionUpdate)
        verify(telegramBot).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_GROUP_NAME, captor.value.entities().parameters["text"])

        // enter subscription
        val enterSubscriptionMessage = mock<Message>()
        val enterSubscriptionUpdate = mock<Update>()
        whenever(enterSubscriptionUpdate.message()).thenReturn(enterSubscriptionMessage)
        whenever(enterSubscriptionMessage.text()).thenReturn(VALID_GROUP_NAME)
        whenever(enterSubscriptionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterSubscriptionUpdate)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.CHOOSE_SEARCH_PARAM, captor.value.entities().parameters["text"])
        val searchTypeButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, searchTypeButtons.size)

        // choose search by keyword
        val chooseSearchTypeKeywordCallbackQuery = mock<CallbackQuery>()
        val chooseSearchTypeKeywordUpdate = mock<Update>()
        whenever(chooseSearchTypeKeywordUpdate.callbackQuery()).thenReturn(chooseSearchTypeKeywordCallbackQuery)
        val chooseSearchTypeKeywordButton = searchTypeButtons.first().first()
        whenever(chooseSearchTypeKeywordCallbackQuery.data()).thenReturn(chooseSearchTypeKeywordButton.callbackData)
        whenever(chooseSearchTypeKeywordCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(chooseSearchTypeKeywordUpdate)
        verify(telegramBot, times(3)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_KEYWORD, captor.value.entities().parameters["text"])

        // enter keyword
        val enterKeywordMessage = mock<Message>()
        val enterKeywordUpdate = mock<Update>()
        whenever(enterKeywordUpdate.message()).thenReturn(enterKeywordMessage)
        whenever(enterKeywordMessage.text()).thenReturn("казань")
        whenever(enterKeywordMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterKeywordUpdate)
        verify(telegramBot, times(4)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.CHOOSE_ACTION, captor.value.entities().parameters["text"])
        val afterFirstKeywordButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, afterFirstKeywordButtons.size)

        // more search condition
        val moreSearchConditionMessage = mock<CallbackQuery>()
        val moreSearchConditionUpdate = mock<Update>()
        whenever(moreSearchConditionUpdate.callbackQuery()).thenReturn(moreSearchConditionMessage)
        val moreSearchConditionButton = afterFirstKeywordButtons.first().first()
        whenever(moreSearchConditionMessage.data()).thenReturn(moreSearchConditionButton.callbackData)
        whenever(moreSearchConditionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(moreSearchConditionUpdate)
        verify(telegramBot, times(5)).execute(captor.capture())
        assertTrue((captor.value.entities().parameters["text"] as String).contains(CreateSubscriptionService.CHOOSE_SEARCH_PARAM))

        // choose search by max amount
        val chooseSearchTypeMaxAmountCallbackQuery = mock<CallbackQuery>()
        val chooseSearchTypeMaxAmountUpdate = mock<Update>()
        whenever(chooseSearchTypeMaxAmountUpdate.callbackQuery()).thenReturn(chooseSearchTypeMaxAmountCallbackQuery)
        val chooseSearchTypeMaxAmountButton = searchTypeButtons[1].first()
        whenever(chooseSearchTypeMaxAmountCallbackQuery.data()).thenReturn(chooseSearchTypeMaxAmountButton.callbackData)
        whenever(chooseSearchTypeMaxAmountCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(chooseSearchTypeMaxAmountUpdate)
        verify(telegramBot, times(6)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_MAX_MONEY, captor.value.entities().parameters["text"])

        // enter max amount
        val enterMaxAmountMessage = mock<Message>()
        val enterMaxAmountUpdate = mock<Update>()
        whenever(enterMaxAmountUpdate.message()).thenReturn(enterMaxAmountMessage)
        whenever(enterMaxAmountMessage.text()).thenReturn("10000")
        whenever(enterMaxAmountMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterMaxAmountUpdate)
        verify(telegramBot, times(7)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.CHOOSE_ACTION, captor.value.entities().parameters["text"])
        val afterSecondKeywordButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, afterSecondKeywordButtons.size)

        // save subscription entered
        val saveCallbackQuery = mock<CallbackQuery>()
        val saveUpdate = mock<Update>()
        whenever(saveUpdate.callbackQuery()).thenReturn(saveCallbackQuery)
        val subscribeButton = afterSecondKeywordButtons[1][0]
        whenever(saveCallbackQuery.data()).thenReturn(subscribeButton.callbackData)
        whenever(saveCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(saveUpdate)
        verify(telegramBot, times(8)).execute(captor.capture())
        assertTrue((captor.value.entities().parameters["text"] as String).contains(CreateSubscriptionService.SUBSCRIPTION_SUCCESS))

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(1, savedSubscriptions.size)
        assertEquals(1, savedSubscriptions.first().search.properties.keywords.size)
        assertEquals(10000, savedSubscriptions.first().search.properties.maxMoney)
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun testCreateAlreadyExistedSubscription() {
        // request to create subscription
        val createSubscriptionMessage = mock<Message>()
        val createSubscriptionUpdate = mock<Update>()
        whenever(createSubscriptionUpdate.message()).thenReturn(createSubscriptionMessage)
        whenever(createSubscriptionMessage.text()).thenReturn(AdminCommand.CREATE_SUBSCRIPTION.commandName)
        whenever(createSubscriptionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createSubscriptionUpdate)

        // enter existed subscription
        val messageCreateSubscriptionEnterExistedSubscription = mock<Message>()
        val createUpdateEnterExistedSubscription = mock<Update>()
        whenever(createUpdateEnterExistedSubscription.message()).thenReturn(messageCreateSubscriptionEnterExistedSubscription)
        whenever(messageCreateSubscriptionEnterExistedSubscription.text()).thenReturn(VALID_GROUP_NAME)
        whenever(messageCreateSubscriptionEnterExistedSubscription.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateEnterExistedSubscription)
        verify(telegramBot, times(2)).execute(captor.capture())
        val searchTypeButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, searchTypeButtons.size)

        // choose search by keyword second
        val chooseSearchTypeKeywordCallbackQuery = mock<CallbackQuery>()
        val chooseSearchTypeKeywordUpdate = mock<Update>()
        whenever(chooseSearchTypeKeywordUpdate.callbackQuery()).thenReturn(chooseSearchTypeKeywordCallbackQuery)
        val secondChooseSearchTypeKeywordButton = searchTypeButtons.first().first()
        whenever(chooseSearchTypeKeywordCallbackQuery.data()).thenReturn(secondChooseSearchTypeKeywordButton.callbackData)
        whenever(chooseSearchTypeKeywordCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(chooseSearchTypeKeywordUpdate)
        verify(telegramBot, times(3)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_KEYWORD, captor.value.entities().parameters["text"])

        // enter existed keyword
        val messageCreateSubscriptionEnterExistedKeyword = mock<Message>()
        val createUpdateEnterExistedKeyword = mock<Update>()
        whenever(createUpdateEnterExistedKeyword.message()).thenReturn(messageCreateSubscriptionEnterExistedKeyword)
        whenever(messageCreateSubscriptionEnterExistedKeyword.text()).thenReturn("казань")
        whenever(messageCreateSubscriptionEnterExistedKeyword.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateEnterExistedKeyword)
        verify(telegramBot, times(4)).execute(captor.capture())
        val buttons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()

        // save subscription entered
        val callbackQuerySave = mock<CallbackQuery>()
        val createUpdateSave = mock<Update>()
        whenever(createUpdateSave.callbackQuery()).thenReturn(callbackQuerySave)
        val buttonSubscribe = buttons[1][0]
        whenever(callbackQuerySave.data()).thenReturn(buttonSubscribe.callbackData)
        whenever(callbackQuerySave.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateSave)
        verify(telegramBot, times(5)).execute(captor.capture())
        assertTrue((captor.value.entities().parameters["text"] as String).contains(CreateSubscriptionService.ALREADY_EXISTED_SUBSCRIPTION))

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(3, savedSubscriptions.size)
    }

    @Test
    fun testCreateSubscriptionCanceled() {
        // request to create subscription
        val messageCreateSubscription = mock<Message>()
        val createUpdate = mock<Update>()
        whenever(createUpdate.message()).thenReturn(messageCreateSubscription)
        whenever(messageCreateSubscription.text()).thenReturn(AdminCommand.CREATE_SUBSCRIPTION.commandName)
        whenever(messageCreateSubscription.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdate)

        // enter subscription name
        val messageCreateSubscriptionEnterSubscription = mock<Message>()
        val createUpdateEnterSubscription = mock<Update>()
        whenever(createUpdateEnterSubscription.message()).thenReturn(messageCreateSubscriptionEnterSubscription)
        whenever(messageCreateSubscriptionEnterSubscription.text()).thenReturn(VALID_GROUP_NAME)
        whenever(messageCreateSubscriptionEnterSubscription.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateEnterSubscription)
        verify(telegramBot, times(2)).execute(captor.capture())
        val searchTypeButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, searchTypeButtons.size)

        // choose search by keyword second
        val chooseSearchTypeKeywordCallbackQuery = mock<CallbackQuery>()
        val chooseSearchTypeKeywordUpdate = mock<Update>()
        whenever(chooseSearchTypeKeywordUpdate.callbackQuery()).thenReturn(chooseSearchTypeKeywordCallbackQuery)
        val secondChooseSearchTypeKeywordButton = searchTypeButtons.first().first()
        whenever(chooseSearchTypeKeywordCallbackQuery.data()).thenReturn(secondChooseSearchTypeKeywordButton.callbackData)
        whenever(chooseSearchTypeKeywordCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(chooseSearchTypeKeywordUpdate)
        verify(telegramBot, times(3)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_KEYWORD, captor.value.entities().parameters["text"])

        // enter keyword
        val messageCreateSubscriptionEnterKeyword = mock<Message>()
        val createUpdateEnterKeyword = mock<Update>()
        whenever(createUpdateEnterKeyword.message()).thenReturn(messageCreateSubscriptionEnterKeyword)
        whenever(messageCreateSubscriptionEnterKeyword.text()).thenReturn("казань")
        whenever(messageCreateSubscriptionEnterKeyword.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateEnterKeyword)
        verify(telegramBot, times(4)).execute(captor.capture())
        val buttons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()

        // cancel subscription entered
        val callbackQuerySave = mock<CallbackQuery>()
        val createUpdateSave = mock<Update>()
        whenever(createUpdateSave.callbackQuery()).thenReturn(callbackQuerySave)
        val buttonSubscribe = buttons[2][0]
        whenever(callbackQuerySave.data()).thenReturn(buttonSubscribe.callbackData)
        whenever(callbackQuerySave.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateSave)
        verify(telegramBot, times(5)).execute(captor.capture())
        assertEquals(captor.value.entities().parameters["text"], CreateSubscriptionService.SUBSCRIPTION_CANCELED)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(0, savedSubscriptions.size)
    }

    @Test
    fun testCreateSubscriptionWithWrongNameNeedToRetry() {
        // request to create subscription
        val messageCreateSubscription = mock<Message>()
        val createUpdate = mock<Update>()
        whenever(createUpdate.message()).thenReturn(messageCreateSubscription)
        whenever(messageCreateSubscription.text()).thenReturn(AdminCommand.CREATE_SUBSCRIPTION.commandName)
        whenever(messageCreateSubscription.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdate)

        // enter subscription name
        val messageCreateSubscriptionEnterSubscription = mock<Message>()
        val createUpdateEnterSubscription = mock<Update>()
        whenever(createUpdateEnterSubscription.message()).thenReturn(messageCreateSubscriptionEnterSubscription)
        whenever(messageCreateSubscriptionEnterSubscription.text()).thenReturn("invalid_group_name")
        whenever(messageCreateSubscriptionEnterSubscription.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateEnterSubscription)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(captor.value.entities().parameters["text"], CreateSubscriptionService.ERROR_GROUP_FORMAT)
    }

    @Test
    fun testCreateSubscriptionSearchMaxAmountWrongFormat() {
        // request to create subscription
        val createSubscriptionMessage = mock<Message>()
        val createSubscriptionUpdate = mock<Update>()
        whenever(createSubscriptionUpdate.message()).thenReturn(createSubscriptionMessage)
        whenever(createSubscriptionMessage.text()).thenReturn(AdminCommand.CREATE_SUBSCRIPTION.commandName)
        whenever(createSubscriptionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createSubscriptionUpdate)
        verify(telegramBot).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_GROUP_NAME, captor.value.entities().parameters["text"])

        // enter subscription
        val enterSubscriptionMessage = mock<Message>()
        val enterSubscriptionUpdate = mock<Update>()
        whenever(enterSubscriptionUpdate.message()).thenReturn(enterSubscriptionMessage)
        whenever(enterSubscriptionMessage.text()).thenReturn(VALID_GROUP_NAME)
        whenever(enterSubscriptionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterSubscriptionUpdate)
        verify(telegramBot, times(2)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.CHOOSE_SEARCH_PARAM, captor.value.entities().parameters["text"])
        val searchTypeButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, searchTypeButtons.size)

        // choose search by max amount
        val chooseSearchTypeMaxAmountCallbackQuery = mock<CallbackQuery>()
        val chooseSearchTypeMaxAmountUpdate = mock<Update>()
        whenever(chooseSearchTypeMaxAmountUpdate.callbackQuery()).thenReturn(chooseSearchTypeMaxAmountCallbackQuery)
        val chooseSearchTypeMaxAmountButton = searchTypeButtons[1].first()
        whenever(chooseSearchTypeMaxAmountCallbackQuery.data()).thenReturn(chooseSearchTypeMaxAmountButton.callbackData)
        whenever(chooseSearchTypeMaxAmountCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(chooseSearchTypeMaxAmountUpdate)
        verify(telegramBot, times(3)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ENTER_MAX_MONEY, captor.value.entities().parameters["text"])

        // enter max amount
        val enterMaxAmountMessage = mock<Message>()
        val enterMaxAmountUpdate = mock<Update>()
        whenever(enterMaxAmountUpdate.message()).thenReturn(enterMaxAmountMessage)
        whenever(enterMaxAmountMessage.text()).thenReturn("NOT_DIGIT")
        whenever(enterMaxAmountMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterMaxAmountUpdate)
        verify(telegramBot, times(4)).execute(captor.capture())
        assertEquals(CreateSubscriptionService.ERROR_TEXT, captor.value.entities().parameters["text"])
    }

    companion object {
        const val VALID_GROUP_NAME = "https://t.me/samokatus"
    }
}