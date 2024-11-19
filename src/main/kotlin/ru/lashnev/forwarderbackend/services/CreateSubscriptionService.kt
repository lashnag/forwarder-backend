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

@Service
class CreateSubscriptionService(
    private val bot: TelegramBot,
    private val subscriptionDao: SubscriptionDao,
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
            sendText(telegramUser.id(), ENTER_GROUP_NAME)
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

    fun sendText(who: Long, what: String?, replyMarkup: Keyboard? = null) {
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
            subscriptionDao.addSubscription(subscription)
            sendText(callbackQuery.from().id(), SUBSCRIPTION_SUCCESS)
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
            sendText(msg.from().id(), ENTER_KEYWORD)
            userState.subscription = msg.text()
            userState.stage = ProcessStage.ENTER_KEYWORD
        }
    }

    private fun keywordEntered(msg: Message, userState: State) {
        handleError(msg.from().id()) {
            sendText(
                msg.from().id(),
                userState.toString(),
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

        const val ENTER_GROUP_NAME = "Введите публичный логин группы"
        const val ENTER_KEYWORD = "Введите ключевые слова"
        const val SUBSCRIPTION_SUCCESS = "Вы подписались"
        const val SUBSCRIPTION_CANCELED = "Отменено"
    }
}