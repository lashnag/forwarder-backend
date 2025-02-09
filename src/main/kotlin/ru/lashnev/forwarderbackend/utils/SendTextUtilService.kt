package ru.lashnev.forwarderbackend.utils

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.exceptions.UserBlockedException

@Service
class SendTextUtilService(private val bot: TelegramBot) {
    fun sendTextWithImage(who: Long, what: String, photo: String) {
        logger.info("Send photo: what $what, who $who")
        val messages = what.chunked(MAX_MESSAGE_LENGTH)
        for (message in messages) {
            sendMessage(who, message, null, photo)
        }
    }

    fun sendText(who: Long, what: String?, replyMarkup: Keyboard? = null) {
        logger.info("Send text: what $what, who $who")
        val messages = what?.chunked(MAX_MESSAGE_LENGTH) ?: listOf(null)
        for (message in messages) {
            sendMessage(who, message, replyMarkup, null)
        }
    }

    private fun sendMessage(who: Long, message: String?, replyMarkup: Keyboard?, photo: String?) {
        val messageBuilder = if (photo != null) {
            SendPhoto(who, photo).also { it.caption(message) }
        } else {
            SendMessage(who, message)
        }
        if (replyMarkup != null) {
            messageBuilder.replyMarkup(replyMarkup)
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