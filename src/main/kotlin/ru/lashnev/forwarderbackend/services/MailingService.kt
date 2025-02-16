package ru.lashnev.forwarderbackend.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.dao.SubscribersDao
import ru.lashnev.forwarderbackend.utils.SendTextUtilService
import ru.lashnev.forwarderbackend.utils.logger

@Service
class MailingService(private val subscribersDao: SubscribersDao, private val sendTextUtilService: SendTextUtilService) {

    @Value("\${scheduler.antispam-delay}")
    private lateinit var antispamDelay: String

    fun sendAll(text: String) {
        logger.info("Start mailing: $text")

        val allSubscribersWithChat = subscribersDao.getSubscribers().filter { it.chatId != null }
        allSubscribersWithChat.forEach { subscriber ->
            try {
                logger.info("Send to subscriber ${subscriber.username}")
                val chatId = checkNotNull(subscriber.chatId)
                sendTextUtilService.sendText(chatId, text)
                Thread.sleep(antispamDelay.toLong())
            } catch (e: Exception) {
                logger.error("Cant send mail to user", e)
            }
        }

        logger.info("End mailing")
    }

    companion object {
        private val logger = logger()
    }
}