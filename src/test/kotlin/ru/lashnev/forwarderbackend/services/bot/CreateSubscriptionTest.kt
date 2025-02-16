package ru.lashnev.forwarderbackend.services.bot

import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.models.AdminCommand
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CreateSubscriptionTest : BaseIT() {

    @Autowired
    private lateinit var createSubscriptionService: CreateSubscriptionService

    @MockBean
    private lateinit var restTemplate: RestTemplate

    @Test
    fun testCreateSubscriptionSearchByTwoKeywords() {
        enterCreateSubscription()
        val searchTypeButtons = enterSubscriptionName(VALID_GROUP_NAME)
        chooseSearchByKeyword(searchTypeButtons)
        val afterEnterKeyword1Buttons = enterKeyword(KEYWORD_1)
        enterMoreSearchCondition(afterEnterKeyword1Buttons)
        chooseSearchByKeyword(searchTypeButtons)
        val afterEnterKeyword2Buttons = enterKeyword(KEYWORD_2)
        enterSubscribeButton(afterEnterKeyword2Buttons, containResponse = CreateSubscriptionService.SUBSCRIPTION_SUCCESS)

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
        enterCreateSubscription()
        val searchTypeButtons = enterSubscriptionName(VALID_GROUP_NAME)
        chooseSearchByKeyword(searchTypeButtons)
        val afterEnterKeywordButtons = enterKeyword(KEYWORD_1)
        enterMoreSearchCondition(afterEnterKeywordButtons)
        chooseSearchByMaxAmount(searchTypeButtons)
        val afterEnterMaxAmountButtons = enterMaxAmount("10000")
        enterSubscribeButton(afterEnterMaxAmountButtons, containResponse = CreateSubscriptionService.SUBSCRIPTION_SUCCESS)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(1, savedSubscriptions.size)
        assertEquals(1, savedSubscriptions.first().search.properties.keywords.size)
        assertEquals(10000, savedSubscriptions.first().search.properties.maxMoney)
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun testCreateAlreadyExistedSubscription() {
        enterCreateSubscription()
        val searchTypeButtons = enterSubscriptionName(VALID_GROUP_NAME)
        chooseSearchByKeyword(searchTypeButtons)
        val afterEnterKeywordButtons = enterKeyword(KEYWORD_1)
        enterSubscribeButton(afterEnterKeywordButtons, containResponse = CreateSubscriptionService.ALREADY_EXISTED_SUBSCRIPTION)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(3, savedSubscriptions.size)
    }

    @Test
    fun testCreateSubscriptionWrongGroup() {
        enterCreateSubscription()
        val searchTypeButtons = enterSubscriptionName(VALID_GROUP_NAME)
        chooseSearchByKeyword(searchTypeButtons)
        val afterEnterKeywordButtons = enterKeyword(KEYWORD_1)
        whenever(
            restTemplate.getForEntity(
                any<String>(),
                eq(Void::class.java),
                any<Map<String, String>>()
            )
        ).thenThrow(RuntimeException("Group is invalid"))
        enterSubscribeButton(afterEnterKeywordButtons, containResponse = CreateSubscriptionService.GROUP_INVALID)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(0, savedSubscriptions.size)
    }

    @Test
    @Sql("/sql/invalid_group.sql")
    fun testCreateSubscriptionAlreadyExistedWrongGroup() {
        enterCreateSubscription()
        val searchTypeButtons = enterSubscriptionName(INVALID_GROUP_NAME)
        chooseSearchByKeyword(searchTypeButtons)
        val afterEnterKeywordButtons = enterKeyword(KEYWORD_1)
        enterSubscribeButton(afterEnterKeywordButtons, containResponse = CreateSubscriptionService.GROUP_INVALID)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(0, savedSubscriptions.size)
    }

    @Test
    fun testCreateSubscriptionCanceled() {
        enterCreateSubscription()
        val searchTypeButtons = enterSubscriptionName(VALID_GROUP_NAME)
        chooseSearchByKeyword(searchTypeButtons)
        val afterEnterKeywordButtons = enterKeyword(KEYWORD_1)
        enterCancelSubscription(afterEnterKeywordButtons)

        val savedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(testUsername)
        assertEquals(0, savedSubscriptions.size)
    }

    @Test
    fun testCreateSubscriptionWithWrongNameNeedToRetry() {
        enterCreateSubscription()
        enterSubscriptionWrongFormatName("invalid_group_format")
    }

    @Test
    fun testCreateSubscriptionSearchMaxAmountWrongFormat() {
        enterCreateSubscription()
        val searchTypeButtons = enterSubscriptionName(VALID_GROUP_NAME)
        chooseSearchByMaxAmount(searchTypeButtons)
        enterMaxAmountWrongFormat("not_digit")
    }

    private fun enterCreateSubscription() {
        val createSubscriptionMessage = mock<Message>()
        val createSubscriptionUpdate = mock<Update>()
        whenever(createSubscriptionUpdate.message()).thenReturn(createSubscriptionMessage)
        whenever(createSubscriptionMessage.text()).thenReturn(AdminCommand.CREATE_SUBSCRIPTION.commandName)
        whenever(createSubscriptionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createSubscriptionUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertEquals(CreateSubscriptionService.ENTER_GROUP_NAME, lastCall.parameters["text"])
    }

    private fun enterSubscriptionName(group: String): Array<out Array<InlineKeyboardButton>> {
        val enterSubscriptionMessage = mock<Message>()
        val enterSubscriptionUpdate = mock<Update>()
        whenever(enterSubscriptionUpdate.message()).thenReturn(enterSubscriptionMessage)
        whenever(enterSubscriptionMessage.text()).thenReturn(group)
        whenever(enterSubscriptionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterSubscriptionUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertEquals(CreateSubscriptionService.CHOOSE_SEARCH_PARAM, lastCall.parameters["text"])
        val searchTypeButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, searchTypeButtons.size)
        return searchTypeButtons
    }

    private fun enterSubscriptionWrongFormatName(groupName: String) {
        val messageCreateSubscriptionEnterSubscription = mock<Message>()
        val createUpdateEnterSubscription = mock<Update>()
        whenever(createUpdateEnterSubscription.message()).thenReturn(messageCreateSubscriptionEnterSubscription)
        whenever(messageCreateSubscriptionEnterSubscription.text()).thenReturn(groupName)
        whenever(messageCreateSubscriptionEnterSubscription.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateEnterSubscription)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertEquals(lastCall.parameters["text"], CreateSubscriptionService.ERROR_GROUP_FORMAT)
    }

    private fun chooseSearchByKeyword(searchTypeButtons: Array<out Array<InlineKeyboardButton>>) {
        val firstChooseSearchTypeKeywordCallbackQuery = mock<CallbackQuery>()
        val firstChooseSearchTypeKeywordUpdate = mock<Update>()
        whenever(firstChooseSearchTypeKeywordUpdate.callbackQuery()).thenReturn(
            firstChooseSearchTypeKeywordCallbackQuery
        )
        val firstChooseSearchTypeKeywordButton = searchTypeButtons.first().first()
        whenever(firstChooseSearchTypeKeywordCallbackQuery.data()).thenReturn(firstChooseSearchTypeKeywordButton.callbackData)
        whenever(firstChooseSearchTypeKeywordCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(firstChooseSearchTypeKeywordUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertEquals(CreateSubscriptionService.ENTER_KEYWORD, lastCall.parameters["text"])
    }

    private fun enterKeyword(keyword: String): Array<out Array<InlineKeyboardButton>> {
        val enterKeywordMessage = mock<Message>()
        val enterKeywordUpdate = mock<Update>()
        whenever(enterKeywordUpdate.message()).thenReturn(enterKeywordMessage)
        whenever(enterKeywordMessage.text()).thenReturn(keyword)
        whenever(enterKeywordMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterKeywordUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertEquals(CreateSubscriptionService.CHOOSE_ACTION, lastCall.parameters["text"])
        val afterKeywordButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, afterKeywordButtons.size)
        return afterKeywordButtons
    }

    private fun enterMoreSearchCondition(afterEnterKeywordButtons: Array<out Array<InlineKeyboardButton>>) {
        val moreSearchConditionMessage = mock<CallbackQuery>()
        val moreSearchConditionUpdate = mock<Update>()
        whenever(moreSearchConditionUpdate.callbackQuery()).thenReturn(moreSearchConditionMessage)
        val moreSearchConditionButton = afterEnterKeywordButtons.first().first()
        whenever(moreSearchConditionMessage.data()).thenReturn(moreSearchConditionButton.callbackData)
        whenever(moreSearchConditionMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(moreSearchConditionUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertTrue((lastCall.parameters["text"] as String).contains(CreateSubscriptionService.CHOOSE_SEARCH_PARAM))
    }

    private fun enterSubscribeButton(afterEnterKeyword2Buttons: Array<out Array<InlineKeyboardButton>>, containResponse: String) {
        val saveCallbackQuery = mock<CallbackQuery>()
        val saveUpdate = mock<Update>()
        whenever(saveUpdate.callbackQuery()).thenReturn(saveCallbackQuery)
        val subscribeButton = afterEnterKeyword2Buttons[1][0]
        whenever(saveCallbackQuery.data()).thenReturn(subscribeButton.callbackData)
        whenever(saveCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(saveUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertTrue((lastCall.parameters["text"] as String).contains(containResponse))
    }

    private fun chooseSearchByMaxAmount(searchTypeButtons: Array<out Array<InlineKeyboardButton>>) {
        val chooseSearchTypeMaxAmountCallbackQuery = mock<CallbackQuery>()
        val chooseSearchTypeMaxAmountUpdate = mock<Update>()
        whenever(chooseSearchTypeMaxAmountUpdate.callbackQuery()).thenReturn(chooseSearchTypeMaxAmountCallbackQuery)
        val chooseSearchTypeMaxAmountButton = searchTypeButtons[1].first()
        whenever(chooseSearchTypeMaxAmountCallbackQuery.data()).thenReturn(chooseSearchTypeMaxAmountButton.callbackData)
        whenever(chooseSearchTypeMaxAmountCallbackQuery.from()).thenReturn(user)

        createSubscriptionService.processUpdates(chooseSearchTypeMaxAmountUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertEquals(CreateSubscriptionService.ENTER_MAX_MONEY, lastCall.parameters["text"])
    }

    private fun enterMaxAmount(maxAmount: String): Array<out Array<InlineKeyboardButton>> {
        val enterMaxAmountMessage = mock<Message>()
        val enterMaxAmountUpdate = mock<Update>()
        whenever(enterMaxAmountUpdate.message()).thenReturn(enterMaxAmountMessage)
        whenever(enterMaxAmountMessage.text()).thenReturn(maxAmount)
        whenever(enterMaxAmountMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterMaxAmountUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertEquals(CreateSubscriptionService.CHOOSE_ACTION, lastCall.parameters["text"])
        val afterEnterMaxAmountButtons = (captor.value.entities().parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        assertEquals(3, afterEnterMaxAmountButtons.size)
        return afterEnterMaxAmountButtons
    }

    private fun enterMaxAmountWrongFormat(maxAmount: String) {
        val enterMaxAmountMessage = mock<Message>()
        val enterMaxAmountUpdate = mock<Update>()
        whenever(enterMaxAmountUpdate.message()).thenReturn(enterMaxAmountMessage)
        whenever(enterMaxAmountMessage.text()).thenReturn(maxAmount)
        whenever(enterMaxAmountMessage.from()).thenReturn(user)

        createSubscriptionService.processUpdates(enterMaxAmountUpdate)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertEquals(CreateSubscriptionService.ERROR_TEXT, lastCall.parameters["text"])
    }

    private fun enterCancelSubscription(afterEnterKeywordButtons: Array<out Array<InlineKeyboardButton>>) {
        val callbackQuerySave = mock<CallbackQuery>()
        val createUpdateSave = mock<Update>()
        whenever(createUpdateSave.callbackQuery()).thenReturn(callbackQuerySave)
        val buttonSubscribe = afterEnterKeywordButtons[2][0]
        whenever(callbackQuerySave.data()).thenReturn(buttonSubscribe.callbackData)
        whenever(callbackQuerySave.from()).thenReturn(user)

        createSubscriptionService.processUpdates(createUpdateSave)
        verify(telegramBot, atLeast(1)).execute(captor.capture())
        val lastCall = captor.allValues[captor.allValues.size - 1]
        assertEquals(lastCall.parameters["text"], CreateSubscriptionService.SUBSCRIPTION_CANCELED)
    }

    companion object {
        const val VALID_GROUP_NAME = "https://t.me/samokatus"
        const val INVALID_GROUP_NAME = "https://t.me/invalid_group"
        const val KEYWORD_1 = "казань"
        const val KEYWORD_2 = "новосибирск"
    }
}