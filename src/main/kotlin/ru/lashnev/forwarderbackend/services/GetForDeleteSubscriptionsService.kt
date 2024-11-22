package ru.lashnev.forwarderbackend.services

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.Keyboard
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.dao.SubscriptionDao
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.models.toCommand
import ru.lashnev.forwarderbackend.utils.logger
import com.github.lashnag.telegrambotstarter.UpdatesService

@Service
class GetForDeleteSubscriptionsService(
    private val bot: TelegramBot,
    private val subscriptionDao: SubscriptionDao,
) : UpdatesService {

    override fun processUpdates(update: Update) {
        if (update.callbackQuery() != null || update.message() != null) {
            onUpdateReceived(update)
        }
    }

    private fun onUpdateReceived(update: Update) {
        if (update.callbackQuery() != null) {
            val buttonCallBack: CallbackQuery = update.callbackQuery()
            if (buttonCallBack.data() == deleteSubscriber.callbackData) {
                deleteSubscriberButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data().contains(deleteKeyword.callbackData!!)) {
                deleteKeywordButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data().contains(deleteSubscription.callbackData!!)) {
                deleteSubscriptionButtonClicked(buttonCallBack)
            }
            return
        }

        val msg = update.message()
        val telegramUser = update.message().from()
        if (msg.text().toCommand() == AdminCommand.FETCH_SUBSCRIPTIONS) {
            val subscriptions = subscriptionDao.getSubscriptions(telegramUser.username())
            val buttons = InlineKeyboardMarkup()
            subscriptions.forEach { subscription ->
                buttons.addRow(
                    InlineKeyboardButton("$DELETE_SUBSCRIPTION_BUTTON_NAME${subscription.subscription}")
                        .callbackData("${deleteSubscription.callbackData}${subscription.subscription}")
                )
                subscription.keywords.forEach { keyword ->
                    buttons.addRow(
                        InlineKeyboardButton("$DELETE_KEYWORD_BUTTON_NAME $keyword")
                            .callbackData("${deleteSubscription.callbackData}${subscription.subscription}${deleteKeyword.callbackData}${keyword}")
                    )
                }
            }
            buttons.addRow(deleteSubscriber)
            sendText(telegramUser.id(), YOUR_SUBSCRIPTIONS, buttons)
            return
        }
    }

    private fun deleteSubscriberButtonClicked(callbackQuery: CallbackQuery) {
        subscriptionDao.deleteSubscriber(callbackQuery.from().username())
        sendText(callbackQuery.from().id(), DELETED)
    }

    private fun deleteSubscriptionButtonClicked(callbackQuery: CallbackQuery) {
        val subscription = callbackQuery.data().replace(deleteSubscription.callbackData!!, "")
        subscriptionDao.deleteSubscription(callbackQuery.from().username(), subscription)
        sendText(callbackQuery.from().id(), "$DELETED $subscription")
    }

    private fun deleteKeywordButtonClicked(callbackQuery: CallbackQuery) {
        val subscription = callbackQuery.data().substringAfter(deleteSubscription.callbackData!!).substringBefore(deleteKeyword.callbackData!!)
        val keyword = callbackQuery.data().substringAfter(deleteKeyword.callbackData!!)
        subscriptionDao.deleteKeyword(callbackQuery.from().username(), subscription, keyword)
        sendText(callbackQuery.from().id(), "$DELETED $keyword")
    }

    private fun sendText(who: Long, what: String? = null, replyMarkup: Keyboard? = null) {
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

    companion object {
        private val logger = logger()

        val deleteSubscriber: InlineKeyboardButton = InlineKeyboardButton("Удалить все подписки").callbackData("delete-all")
        val deleteSubscription: InlineKeyboardButton = InlineKeyboardButton("Удалить подписку").callbackData("ds-")
        val deleteKeyword: InlineKeyboardButton = InlineKeyboardButton("Удалить слово").callbackData("-dk-")

        const val DELETED = "Удалено"
        const val DELETE_SUBSCRIPTION_BUTTON_NAME = "Удалить группу: @"
        const val DELETE_KEYWORD_BUTTON_NAME = "Удалить слово: "
        const val YOUR_SUBSCRIPTIONS = "Ваши подписки"
    }
}