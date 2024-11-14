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
import ru.lashnev.telegrambotstarter.UpdatesService

@Service
class GetForDeleteSubscriptionsService(
    private val bot: TelegramBot,
    private val subscriptionDao: SubscriptionDao,
    private val subscriptionExportService: SubscriptionExportService
) : UpdatesService {

    val deleteSubscriber: InlineKeyboardButton = InlineKeyboardButton("Удалить все подписки").callbackData("delete-all")
    val deleteSubscription: InlineKeyboardButton = InlineKeyboardButton("Удалить подписку").callbackData("delete-subscription")
    val deleteKeyword: InlineKeyboardButton = InlineKeyboardButton("Удалить слово").callbackData("delete-keyword")

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
            } else if (buttonCallBack.data().contains(deleteSubscription.callbackData!!)) {
                deleteSubscriptionButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data().contains(deleteKeyword.callbackData!!)) {
                deleteKeywordButtonClicked(buttonCallBack)
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
                    InlineKeyboardButton("Удалить " + subscription.subscription)
                        .callbackData("${deleteSubscription.callbackData}-${subscription.subscription}")
                )
                subscription.keywords.forEach { keyword ->
                    buttons.addRow(
                        InlineKeyboardButton("Удалить $keyword")
                            .callbackData("${deleteSubscription.callbackData}-${subscription.subscription}-${deleteKeyword.callbackData}-${keyword}")
                    )
                }
            }
            buttons.addRow(deleteSubscriber)
            sendText(telegramUser.id(), replyMarkup = buttons)
            return
        }
    }

    private fun deleteSubscriberButtonClicked(callbackQuery: CallbackQuery) {
        subscriptionDao.deleteSubscriber(callbackQuery.from().username())
        subscriptionExportService.deleteSubscriber(callbackQuery.from().username())
        sendText(callbackQuery.from().id(), "Удалено")
    }

    private fun deleteSubscriptionButtonClicked(callbackQuery: CallbackQuery) {
        val subscription = callbackQuery.data().substringAfter("-")
        subscriptionDao.deleteSubscription(callbackQuery.from().username(), subscription)
        subscriptionExportService.deleteSubscription(callbackQuery.from().username(), subscription)
        sendText(callbackQuery.from().id(), "Удалено")
    }

    private fun deleteKeywordButtonClicked(callbackQuery: CallbackQuery) {
        val words = callbackQuery.data().split("-")
        val subscription = words[1]
        val keyword = words[3]
        subscriptionDao.deleteKeyword(callbackQuery.from().username(), subscription, keyword)
        subscriptionExportService.deleteKeyword(callbackQuery.from().username(), subscription, keyword)
        sendText(callbackQuery.from().id(), "Удалено")
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
    }
}