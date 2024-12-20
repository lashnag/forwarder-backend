package ru.lashnev.forwarderbackend.services.bot

import com.github.lashnag.telegrambotstarter.UpdatesService
import com.pengrad.telegrambot.model.Update
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.models.AdminCommand
import ru.lashnev.forwarderbackend.models.toCommand
import ru.lashnev.forwarderbackend.utils.SendTextUtilService
import java.nio.charset.Charset

@Service
class ChangelogService(private val sendTextUtilService: SendTextUtilService, resourceLoader: ResourceLoader) : UpdatesService {

    private var changelog: Resource = resourceLoader.getResource("classpath:changelog.md")

    override fun processUpdates(update: Update) {
        if (update.message() != null) {
            onUpdateReceived(update)
        }
    }

    private fun onUpdateReceived(update: Update) {
        val msg = update.message()
        val telegramUser = update.message().from()
        if (msg.text().toCommand() == AdminCommand.CHANGELOG) {
            sendTextUtilService.sendText(telegramUser.id(), changelog.getContentAsString(Charset.defaultCharset()))
            return
        }
    }
}