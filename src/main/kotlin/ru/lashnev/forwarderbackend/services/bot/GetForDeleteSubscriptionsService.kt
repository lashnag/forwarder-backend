package ru.lashnev.forwarderbackend.services.bot

import com.github.lashnag.telegrambotstarter.UpdatesService
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.dao.SubscriptionDao
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.models.toCommand
import ru.lashnev.forwarderbackend.utils.MDCType
import ru.lashnev.forwarderbackend.utils.SendTextUtilService
import ru.lashnev.forwarderbackend.utils.withMDC

@Service
class GetForDeleteSubscriptionsService(
    private val sendTextUtilService: SendTextUtilService,
    private val subscriptionDao: SubscriptionDao,
) : UpdatesService {
    override fun processUpdates(update: Update) {
        if (update.callbackQuery() != null || update.message() != null) {
            val telegramUserName = when {
                update.callbackQuery() != null -> update.callbackQuery().from().username()
                update.message() != null -> update.message().from().username()
                else -> throw IllegalStateException("User name must be provided")
            }
            withMDC {
                withMDC(MDCType.USER, telegramUserName) { onUpdateReceived(update) }
            }
        }
    }

    private fun onUpdateReceived(update: Update) {
        if (update.callbackQuery() != null) {
            val buttonCallBack: CallbackQuery = update.callbackQuery()
            if (buttonCallBack.data() == deleteSubscriber.callbackData) {
                deleteSubscriberButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data().contains(deleteSubscription.callbackData!!)) {
                deleteSubscriptionButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data().contains(deleteGroup.callbackData!!)) {
                deleteGroupButtonClicked(buttonCallBack)
            }
            return
        }

        val msg = update.message()
        val telegramUser = update.message().from()
        if (msg.text().toCommand() == AdminCommand.FETCH_SUBSCRIPTIONS) {
            val subscriptions = subscriptionDao.getSubscriptionsBySubscriber(telegramUser.username())
            val buttons = InlineKeyboardMarkup()
            subscriptions.groupBy { it.group }.forEach { (group, subscriptionsByGroup) ->
                buttons.addRow(
                    InlineKeyboardButton("$DELETE_GROUP_BUTTON_NAME${group.name}")
                        .callbackData("${deleteGroup.callbackData}${group.name}"),
                )
                subscriptionsByGroup.forEach { subscription ->
                    val deleteButtonName = "$DELETE_SUBSCRIPTION_BUTTON_NAME ${subscription.search.properties}"
                    if (subscription.group.invalid) {
                        deleteButtonName.plus(" (группа неактивна)")
                    }
                    buttons.addRow(
                        InlineKeyboardButton(deleteButtonName)
                            .callbackData(
                                "${deleteGroup.callbackData}${subscription.group.name}${deleteSubscription.callbackData}${subscription.search.searchId}",
                            ),
                    )
                }
            }
            buttons.addRow(deleteSubscriber)
            sendTextUtilService.sendText(telegramUser.id(), YOUR_SUBSCRIPTIONS, buttons)
            return
        }
    }

    private fun deleteSubscriberButtonClicked(callbackQuery: CallbackQuery) {
        subscriptionDao.deleteSubscriber(callbackQuery.from().username())
        sendTextUtilService.sendText(callbackQuery.from().id(), DELETED)
    }

    private fun deleteGroupButtonClicked(callbackQuery: CallbackQuery) {
        val subscription = callbackQuery.data().replace(deleteGroup.callbackData!!, "")
        subscriptionDao.deleteSubscription(callbackQuery.from().username(), subscription)
        sendTextUtilService.sendText(callbackQuery.from().id(), "$DELETED $subscription")
    }

    private fun deleteSubscriptionButtonClicked(callbackQuery: CallbackQuery) {
        val subscription =
            callbackQuery
                .data()
                .substringAfter(
                    deleteGroup.callbackData!!,
                ).substringBefore(deleteSubscription.callbackData!!)
        val searchId = callbackQuery.data().substringAfter(deleteSubscription.callbackData!!)
        subscriptionDao.deleteSubscription(callbackQuery.from().username(), searchId.toInt())
        sendTextUtilService.sendText(callbackQuery.from().id(), DELETED_SUBSCRIPTION + subscription)
    }

    companion object {
        val deleteSubscriber: InlineKeyboardButton = InlineKeyboardButton("Удалить все подписки").callbackData("delete-all")
        val deleteGroup: InlineKeyboardButton = InlineKeyboardButton("Удалить группу").callbackData("dg-")
        val deleteSubscription: InlineKeyboardButton = InlineKeyboardButton("Удалить подписку").callbackData("-ds-")

        const val DELETED = "Удалены все подписки"
        const val DELETED_SUBSCRIPTION = "Удалена подписка из группы @"
        const val DELETE_GROUP_BUTTON_NAME = "Удалить группу: @"
        const val DELETE_SUBSCRIPTION_BUTTON_NAME = "Удалить подписку: "
        const val YOUR_SUBSCRIPTIONS = "Ваши подписки"
    }
}
