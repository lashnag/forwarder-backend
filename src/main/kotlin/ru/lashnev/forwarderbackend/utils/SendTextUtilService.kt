package ru.lashnev.forwarderbackend.utils

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.exceptions.UserBlockedException

@Service
class SendTextUtilService(private val bot: TelegramBot) {
    fun sendText(who: Long, what: String?, replyMarkup: Keyboard? = null, markdown: Boolean = false) {
        logger.info("Send text: what $what, who $who")
        val messages = what?.chunked(MAX_MESSAGE_LENGTH) ?: listOf(null)
        for (message in messages) {
            sendMessageChunk(who, message, replyMarkup, markdown)
        }
    }

    private fun sendMessageChunk(who: Long, message: String?, replyMarkup: Keyboard?, markdown: Boolean) {
        val messageBuilder = SendMessage(who, message)
        if (replyMarkup != null) {
            messageBuilder.replyMarkup(replyMarkup)
        }
        if (markdown) {
            messageBuilder.parseMode(ParseMode.Markdown)
        }

        val response = bot.execute(messageBuilder)
        if (response.errorCode() != 0) {
            logger.error("Cant send message to $who, error code: ${response.errorCode()}, error message: ${response.description()}")
            if (response.errorCode() == 403) {
                throw UserBlockedException()
            }
        }
    }

    companion object {
        private val logger = logger()
        private const val MAX_MESSAGE_LENGTH = 4000
    }
}