package ru.lashnev.forwarderbackend.services.bot

import com.github.lashnag.telegrambotstarter.UpdatesService
import com.pengrad.telegrambot.model.Update
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.models.toCommand
import ru.lashnev.forwarderbackend.utils.MDCType
import ru.lashnev.forwarderbackend.utils.SendTextUtilService
import ru.lashnev.forwarderbackend.utils.withMDC

@Service
class StartService(
    private val sendTextUtilService: SendTextUtilService,
) : UpdatesService {
    override fun processUpdates(update: Update) {
        if (update.message() != null) {
            val telegramUserName = update.message().from().username()
            withMDC {
                withMDC(MDCType.USER, telegramUserName) { onUpdateReceived(update) }
            }
        }
    }

    private fun onUpdateReceived(update: Update) {
        val msg = update.message()
        val telegramUser = update.message().from()
        if (msg.text().toCommand() == AdminCommand.START) {
            sendTextUtilService.sendText(telegramUser.id(), WELCOME_MESSAGE)
            return
        }
    }

    companion object {
        val WELCOME_MESSAGE =
            "Добро пожаловать в бот для поиска сообщений в группах. Воспользуйтесь командой " +
                "${AdminCommand.CREATE_SUBSCRIPTION.commandName} для создания подписки и обязательно рекомендуйте его друзьям"
    }
}
