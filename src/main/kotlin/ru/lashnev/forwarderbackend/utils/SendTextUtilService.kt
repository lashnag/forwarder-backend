package ru.lashnev.forwarderbackend.utils

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Service

@Service
class SendTextUtilService(private val bot: TelegramBot) {
    fun sendText(who: Long, what: String?, replyMarkup: Keyboard? = null, useMarkdown: Boolean = false) {
        logger.info("Send: what $what, who $who")
        val smBuilder = SendMessage(who, what)
        if (replyMarkup != null) {
            smBuilder.replyMarkup(replyMarkup)
        }
        if (useMarkdown) {
            smBuilder.parseMode(ParseMode.Markdown)
        }

        try {
            val response = bot.execute(smBuilder)
            if (response.errorCode() != 0) {
                logger.warn("Cant send message to $who, error code: ${response.errorCode()}, error message: ${response.description()}")
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val logger = logger()
    }
}