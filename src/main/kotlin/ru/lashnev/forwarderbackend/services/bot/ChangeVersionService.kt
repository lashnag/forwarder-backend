package ru.lashnev.forwarderbackend.services.bot

import com.github.lashnag.telegrambotstarter.UpdatesService
import com.pengrad.telegrambot.model.Update
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.dao.SubscribersDao
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.models.toCommand
import ru.lashnev.forwarderbackend.utils.SendTextUtilService

@Service
class ChangeVersionService(
    private val subscribersDao: SubscribersDao,
    private val sendTextUtilService: SendTextUtilService,
) : UpdatesService {
    override fun processUpdates(update: Update) {
        if (update.message() != null) {
            onUpdateReceived(update)
        }
    }

    private fun onUpdateReceived(update: Update) {
        val msg = update.message()
        val telegramUser = update.message().from()
        if (msg.text().toCommand() == AdminCommand.CHANGE_VERSION_V2) {
            val subscriber = checkNotNull(subscribersDao.getSubscriber(telegramUser.username()))
            val responseMessage = if (subscriber.chatId == null) {
                subscribersDao.setSubscriberChatId(telegramUser.username(), telegramUser.id())
                CHANGE_VERSION_SUCCESS
            } else {
                ALREADY_V2_MESSAGE
            }
            sendTextUtilService.sendText(telegramUser.id(), responseMessage)
            return
        }
    }

    companion object {
        const val ALREADY_V2_MESSAGE = "Вы уже используете 2 версию"
        const val CHANGE_VERSION_SUCCESS = "Вы перешли на версию 2. Теперь сообщения будут приходить в боте"
    }
}