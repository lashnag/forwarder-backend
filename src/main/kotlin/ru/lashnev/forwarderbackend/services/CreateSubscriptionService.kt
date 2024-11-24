package ru.lashnev.forwarderbackend.services

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.Keyboard
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.dao.SubscriptionDao
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.models.Subscription
import ru.lashnev.forwarderbackend.models.toCommand
import ru.lashnev.forwarderbackend.utils.logger
import com.github.lashnag.telegrambotstarter.UpdatesService
import org.springframework.beans.factory.annotation.Value

@Service
class CreateSubscriptionService(
    private val bot: TelegramBot,
    private val subscriptionDao: SubscriptionDao,
    @Value("\${telegram-forwarder-user}") private val telegramForwarderUser: String
) : UpdatesService {

    val userContext: MutableMap<Long, State> = mutableMapOf()

    enum class ProcessStage {
        ENTER_SUBSCRIPTION,
        ENTER_KEYWORD,
        CONFIRMATION
    }

    data class State(
        var stage: ProcessStage,
        var subscriber: String,
        var subscription: String? = null,
        var keywords: MutableSet<String> = mutableSetOf(),
    )

    override fun processUpdates(update: Update) {
        if (update.callbackQuery() != null || update.message() != null) {
            onUpdateReceived(update)
        }
    }

    private fun onUpdateReceived(update: Update) {
        if (update.callbackQuery() != null) {
            val buttonCallBack: CallbackQuery = update.callbackQuery()
            if (buttonCallBack.data() == moreButton.callbackData) {
                moreButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data() == subscribeButton.callbackData) {
                subscribeButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data() == cancelButton.callbackData) {
                cancelButtonClicked(buttonCallBack)
            }
            return
        }

        val msg = update.message()
        val telegramUser = update.message().from()
        if (msg.text().toCommand() == AdminCommand.CREATE_SUBSCRIPTION) {
            userContext[telegramUser.id()] = State(ProcessStage.ENTER_SUBSCRIPTION, msg.from().username())
            sendText(telegramUser.id(), ENTER_GROUP_NAME, replyMarkup = InlineKeyboardMarkup().addRow(cancelButton))
            return
        }

        val userState = userContext[telegramUser.id()]
        if (userState != null) {
            when (userState.stage) {
                ProcessStage.ENTER_SUBSCRIPTION -> subscriptionEntered(msg, userState)
                ProcessStage.ENTER_KEYWORD -> keywordEntered(msg, userState)
                ProcessStage.CONFIRMATION -> TODO()
            }
        }
    }

    private fun sendText(who: Long, what: String?, replyMarkup: Keyboard? = null) {
        logger.info("Send: what $what, who $who")
        val smBuilder = com.pengrad.telegrambot.request.SendMessage(who, what)
        if (replyMarkup != null) {
            smBuilder.replyMarkup(replyMarkup)
        }

        try {
            bot.execute(smBuilder)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun moreButtonClicked(callbackQuery: CallbackQuery) {
        handleError(callbackQuery.from().id()) {
            sendText(callbackQuery.from().id(), ENTER_KEYWORD)
            userContext[callbackQuery.from().id()]?.stage = ProcessStage.ENTER_KEYWORD
        }
    }

    private fun subscribeButtonClicked(callbackQuery: CallbackQuery) {
        handleError(callbackQuery.from().id()) {
            val state = userContext[callbackQuery.from().id()]
            checkNotNull(state)
            val subscription = Subscription(state.subscriber, state.subscription!!, state.keywords)
            val existedSubscriptions = subscriptionDao.getSubscriptions(state.subscriber)
            val intersectedSubscriptions = existedSubscriptions.filter {
                it.subscriber == state.subscriber && it.subscription == state.subscription!! && it.keywords.intersect(state.keywords).isNotEmpty()
            }

            if (intersectedSubscriptions.isEmpty()) {
                subscriptionDao.addSubscription(subscription)
                sendText(callbackQuery.from().id(), "$SUBSCRIPTION_SUCCESS. Добавьте в контакты @$telegramForwarderUser")
            } else {
                sendText(callbackQuery.from().id(), "$ALREADY_EXISTED_SUBSCRIPTION ${intersectedSubscriptions.first().subscription}")
            }
            userContext.remove(callbackQuery.from().id())
        }
    }


    private fun cancelButtonClicked(callbackQuery: CallbackQuery) {
        handleError(callbackQuery.from().id()) {
            sendText(callbackQuery.from().id(), SUBSCRIPTION_CANCELED)
            userContext.remove(callbackQuery.from().id())
        }
    }

    private fun subscriptionEntered(msg: Message, userState: State) {
        handleError(msg.from().id()) {
            sendText(msg.from().id(), ENTER_KEYWORD, replyMarkup = InlineKeyboardMarkup().addRow(cancelButton))
            userState.subscription = msg.text().replace("@", "")
            userState.stage = ProcessStage.ENTER_KEYWORD
        }
    }

    private fun keywordEntered(msg: Message, userState: State) {
        handleError(msg.from().id()) {
            sendText(
                msg.from().id(),
                CHOOSE_ACTION,
                replyMarkup = InlineKeyboardMarkup()
                    .addRow(moreButton)
                    .addRow(subscribeButton)
                    .addRow(cancelButton)
            )
            userState.keywords.add(msg.text())
            userState.stage = ProcessStage.CONFIRMATION
        }
    }

    private fun handleError(chatId: Long, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            logger.error("Exception in AdminBot ${e.message}", e)
            sendText(chatId, "Произошла ошибка попробуйте еще раз")
        }
    }

    companion object {
        private val logger = logger()

        val moreButton: InlineKeyboardButton = InlineKeyboardButton("Еще слова").callbackData("more")
        val subscribeButton: InlineKeyboardButton = InlineKeyboardButton("Подписаться").callbackData("subscribe")
        val cancelButton: InlineKeyboardButton = InlineKeyboardButton("Отмена").callbackData("cancel")

        const val ENTER_GROUP_NAME = "Введите публичный логин группы с @"
        const val ENTER_KEYWORD = "Введите ключевые слова"
        const val SUBSCRIPTION_SUCCESS = "Вы подписались"
        const val CHOOSE_ACTION = "Нажмите для продолжения"
        const val SUBSCRIPTION_CANCELED = "Отменено"
        const val ALREADY_EXISTED_SUBSCRIPTION = "Такая подписка уже есть в"
    }
}