package ru.lashnev.forwarderbackend.services.bot

import com.github.lashnag.telegrambotstarter.UpdatesService
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.ApiProperties
import ru.lashnev.forwarderbackend.dao.GroupsDao
import ru.lashnev.forwarderbackend.dao.SubscriptionDao
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.models.Group
import ru.lashnev.forwarderbackend.models.Properties
import ru.lashnev.forwarderbackend.models.Search
import ru.lashnev.forwarderbackend.models.Subscriber
import ru.lashnev.forwarderbackend.models.Subscription
import ru.lashnev.forwarderbackend.models.toCommand
import ru.lashnev.forwarderbackend.utils.SendTextUtilService
import ru.lashnev.forwarderbackend.utils.logger

@Service
class CreateSubscriptionService(
    private val subscriptionDao: SubscriptionDao,
    private val groupsDao: GroupsDao,
    private val sendTextUtilService: SendTextUtilService,
    private val restTemplate: RestTemplate,
    private val apiProperties: ApiProperties,
) : UpdatesService {

    val userContext: MutableMap<Long, State> = mutableMapOf()

    enum class ProcessStage {
        ENTER_GROUP,
        CHOOSE_SEARCH_TYPE,
        ENTER_SEARCH_KEYWORD,
        ENTER_SEARCH_MAX_MONEY,
        CONFIRMATION
    }

    data class State(
        var stage: ProcessStage,
        var subscriber: Subscriber,
        var group: Group? = null,
        var search: Search? = null,
    )

    override fun processUpdates(update: Update) {
        if (update.callbackQuery() != null || update.message() != null) {
            onUpdateReceived(update)
        }
    }

    private fun onUpdateReceived(update: Update) {
        if (update.callbackQuery() != null) {
            val buttonCallBack: CallbackQuery = update.callbackQuery()
            if (buttonCallBack.data() == keywordButton.callbackData) {
                enterKeyword(buttonCallBack)
            } else if (buttonCallBack.data() == maxMoneyButton.callbackData) {
                enterMaxMoney(buttonCallBack)
            } else if (buttonCallBack.data() == moreButton.callbackData) {
                moreButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data() == subscribeButton.callbackData) {
                subscribeButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data() == cancelButton.callbackData) {
                cancelButtonClicked(buttonCallBack)
            }
            return
        }

        val msg = update.message()
        val telegramUser = msg.from()
        if (msg.text().toCommand() == AdminCommand.CREATE_SUBSCRIPTION) {
            userContext[telegramUser.id()] = State(ProcessStage.ENTER_GROUP, Subscriber(telegramUser.username(), telegramUser.id()))
            sendTextUtilService.sendText(telegramUser.id(), ENTER_GROUP_NAME, replyMarkup = InlineKeyboardMarkup().addRow(cancelButton))
            return
        }

        val userState = userContext[telegramUser.id()] ?: return
        when (userState.stage) {
            ProcessStage.ENTER_GROUP -> groupEntered(msg)
            ProcessStage.ENTER_SEARCH_KEYWORD,
            ProcessStage.ENTER_SEARCH_MAX_MONEY -> searchPropertyEntered(msg)
            ProcessStage.CHOOSE_SEARCH_TYPE -> TODO()
            ProcessStage.CONFIRMATION -> TODO()
        }
    }

    private fun enterKeyword(callbackQuery: CallbackQuery) {
        handleError(callbackQuery.from().id()) {
            sendTextUtilService.sendText(callbackQuery.from().id(), ENTER_KEYWORD)
            userContext[callbackQuery.from().id()]?.stage = ProcessStage.ENTER_SEARCH_KEYWORD
        }
    }

    private fun enterMaxMoney(callbackQuery: CallbackQuery) {
        handleError(callbackQuery.from().id()) {
            sendTextUtilService.sendText(callbackQuery.from().id(), ENTER_MAX_MONEY)
            userContext[callbackQuery.from().id()]?.stage = ProcessStage.ENTER_SEARCH_MAX_MONEY
        }
    }

    private fun moreButtonClicked(callbackQuery: CallbackQuery) {
        handleError(callbackQuery.from().id()) {
            val userState = checkNotNull(userContext[callbackQuery.from().id()])

            val replyButtons = InlineKeyboardMarkup()
            replyButtons.addRow(keywordButton)
            if (userState.search?.properties?.maxMoney == null) replyButtons.addRow(maxMoneyButton)
            replyButtons.addRow(cancelButton)

            userState.stage = ProcessStage.CHOOSE_SEARCH_TYPE
            sendTextUtilService.sendText(
                callbackQuery.from().id(),
                CHOOSE_SEARCH_PARAM,
                replyMarkup = replyButtons
            )
        }
    }

    private fun subscribeButtonClicked(callbackQuery: CallbackQuery) {
        handleError(callbackQuery.from().id()) {
            val state = checkNotNull(userContext[callbackQuery.from().id()])
            val subscription = Subscription(state.subscriber, state.group!!, state.search!!)
            val existedSubscriptions = subscriptionDao.getSubscriptionsBySubscriber(state.subscriber.username)
            val intersectedSubscriptions = existedSubscriptions.filter {
                it.subscriber.username == state.subscriber.username
                    && it.group.name == state.group!!.name
                    && it.search.properties == state.search!!.properties
            }

            if (intersectedSubscriptions.isEmpty()) {
                if (isGroupValid(state.group!!.name)) {
                    subscriptionDao.addSubscription(subscription)
                    sendTextUtilService.sendText(callbackQuery.from().id(), SUBSCRIPTION_SUCCESS)
                } else {
                    sendTextUtilService.sendText(callbackQuery.from().id(), GROUP_INVALID)
                }
            } else {
                sendTextUtilService.sendText(callbackQuery.from().id(), "$ALREADY_EXISTED_SUBSCRIPTION ${intersectedSubscriptions.first().group}")
            }
            userContext.remove(callbackQuery.from().id())
        }
    }

    private fun isGroupValid(groupName: String): Boolean {
        val group = groupsDao.getByName(groupName)
        return if (group == null) {
            try {
                restTemplate.getForEntity(
                    "${apiProperties.joinGroupUrl}?subscription={subscription}",
                    Void::class.java,
                    mapOf("subscription" to groupName)
                )
                true
            } catch (e: Exception) {
                logger.warn("Cant join to the group $groupName")
                false
            }
        } else {
            !group.invalid
        }
    }

    private fun cancelButtonClicked(callbackQuery: CallbackQuery) {
        handleError(callbackQuery.from().id()) {
            sendTextUtilService.sendText(callbackQuery.from().id(), SUBSCRIPTION_CANCELED)
            userContext.remove(callbackQuery.from().id())
        }
    }

    private fun groupEntered(msg: Message) {
        handleError(msg.from().id()) {
            val userState = checkNotNull(userContext[msg.from().id()])

            if(msg.text().contains(DOMAIN_IN_TELEGRAM_LINK) || msg.text().contains(
                    DOMAIN_IN_TELEGRAM_LINK_WITHOUT_PROTOCOL
                ) || msg.text().contains(DOMAIN_WEB_TELEGRAM_LINK)) {
                userState.group = Group(
                    msg.text()
                        .replace(DOMAIN_IN_TELEGRAM_LINK, "")
                        .replace(DOMAIN_IN_TELEGRAM_LINK_WITHOUT_PROTOCOL, "")
                        .replace(DOMAIN_WEB_TELEGRAM_LINK, "")
                        .lowercaseIfNotInviteLink(),
                    0
                )
                val replyButtons = InlineKeyboardMarkup()
                replyButtons.addRow(keywordButton)
                if (userState.search?.properties?.maxMoney == null) replyButtons.addRow(maxMoneyButton)
                replyButtons.addRow(cancelButton)

                userState.stage = ProcessStage.CHOOSE_SEARCH_TYPE
                sendTextUtilService.sendText(
                    msg.from().id(),
                    CHOOSE_SEARCH_PARAM,
                    replyMarkup = replyButtons
                )
            } else {
                sendTextUtilService.sendText(
                    msg.from().id(),
                    ERROR_GROUP_FORMAT
                )
            }
        }
    }

    private fun searchPropertyEntered(msg: Message) {
        handleError(msg.from().id()) {
            val userState = checkNotNull(userContext[msg.from().id()])
            val properties = userState.search?.properties ?: Properties().also {
                userState.search = Search(null, it)
            }
            when(userState.stage) {
                ProcessStage.ENTER_SEARCH_KEYWORD -> properties.keywords.add(msg.text().lowercase())
                ProcessStage.ENTER_SEARCH_MAX_MONEY -> properties.maxMoney = msg.text().toLong()
                else -> throw IllegalStateException("State is ${userState.stage}")
            }
            userState.stage = ProcessStage.CONFIRMATION

            sendTextUtilService.sendText(
                msg.from().id(),
                CHOOSE_ACTION,
                replyMarkup = InlineKeyboardMarkup()
                    .addRow(moreButton)
                    .addRow(subscribeButton)
                    .addRow(cancelButton)
            )
        }
    }

    private fun handleError(chatId: Long, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            logger.error("Exception in AdminBot ${e.message}", e)
            sendTextUtilService.sendText(chatId, ERROR_TEXT)
        }
    }

    private fun String.lowercaseIfNotInviteLink() =
        if (!this.startsWith("+"))
            this.lowercase()
        else
            this

    companion object {
        private val logger = logger()

        val moreButton: InlineKeyboardButton = InlineKeyboardButton("Еще условие").callbackData("more")
        val subscribeButton: InlineKeyboardButton = InlineKeyboardButton("Подписаться").callbackData("subscribe")
        val cancelButton: InlineKeyboardButton = InlineKeyboardButton("Отмена").callbackData("cancel")

        val keywordButton: InlineKeyboardButton = InlineKeyboardButton("Ключевое слово").callbackData("keyword")
        const val ENTER_KEYWORD = "Введите ключевое слово для поиска (вводить можно в любой форме один раз и в любом регистре)"

        val maxMoneyButton: InlineKeyboardButton = InlineKeyboardButton("Максимальная сумма").callbackData("max-money")
        const val ENTER_MAX_MONEY = "Введите максимальную сумму поиск (целое число)"

        const val DOMAIN_IN_TELEGRAM_LINK = "https://t.me/"
        const val DOMAIN_IN_TELEGRAM_LINK_WITHOUT_PROTOCOL = "t.me/"
        const val DOMAIN_WEB_TELEGRAM_LINK = "https://telegram.me/s/"
        const val ERROR_GROUP_FORMAT = "Неправильный формат группы. Введите еще раз"
        const val ENTER_GROUP_NAME = "Введите ссылку на группу (${DOMAIN_IN_TELEGRAM_LINK}some_group_username) или инвайт ссылку"
        const val CHOOSE_SEARCH_PARAM = "Введите условия для поиска сообщений (несколько условий будут соединены через условие \"И\")"
        const val SUBSCRIPTION_SUCCESS = "Вы подписались"
        const val CHOOSE_ACTION = "Нажмите для продолжения"
        const val SUBSCRIPTION_CANCELED = "Отменено"
        const val ALREADY_EXISTED_SUBSCRIPTION = "Ошибка. Такая подписка уже есть в"
        const val GROUP_INVALID = "Ошибка при добавлении группы. Проверьте ссылку или попробуйте позднее"
        const val ERROR_TEXT = "Произошла ошибка попробуйте еще раз"
    }
}