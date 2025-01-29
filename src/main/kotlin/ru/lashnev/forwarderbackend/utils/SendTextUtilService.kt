package ru.lashnev.forwarderbackend.utils

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.model.request.ParseMode
import org.springframework.stereotype.Service

@Service
class SendTextUtilService(private val bot: TelegramBot) {
    fun sendText(who: Long, what: String?, replyMarkup: Keyboard? = null, useMarkdown: Boolean = false) {
        logger.info("Send: what $what, who $who")
        val smBuilder = com.pengrad.telegrambot.request.SendMessage(who, what)
        if (replyMarkup != null) {
            smBuilder.replyMarkup(replyMarkup)
        }
        if (useMarkdown) {
            smBuilder.parseMode(ParseMode.Markdown)
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