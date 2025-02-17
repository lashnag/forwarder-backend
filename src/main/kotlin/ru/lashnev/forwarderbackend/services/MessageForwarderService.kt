package ru.lashnev.forwarderbackend.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.ApiProperties
import ru.lashnev.forwarderbackend.dao.GroupsDao
import ru.lashnev.forwarderbackend.dao.SubscriptionDao
import ru.lashnev.forwarderbackend.dto.MessageFetcherResponse
import ru.lashnev.forwarderbackend.exceptions.UserBlockedException
import ru.lashnev.forwarderbackend.models.Group
import ru.lashnev.forwarderbackend.models.Subscription
import ru.lashnev.forwarderbackend.utils.SendTextUtilService
import ru.lashnev.forwarderbackend.utils.TextUtils
import ru.lashnev.forwarderbackend.utils.logger

@Service
class MessageForwarderService(
    private val restTemplate: RestTemplate,
    private val groupsDao: GroupsDao,
    private val subscriptionDao: SubscriptionDao,
    private val messageCheckerService: MessageCheckerService,
    private val apiProperties: ApiProperties,
    private val sendTextUtilService: SendTextUtilService,
    private val ocrService: OcrService,
    private val textUtils: TextUtils,
) {
    @Value("\${scheduler.antispam-delay}")
    private lateinit var antispamDelay: String

    @Scheduled(fixedDelay = 5_000, initialDelay = 100_000)
    fun processMessages() {
        logger.info("Start forwarder scheduler")
        val groups =
            try {
                groupsDao.getValidGroups()
            } catch (e: Exception) {
                logger.error("Cant get group: ", e)
                return
            }
        groups.forEach { group ->
            try {
                logger.info("Processing group ${group.name}")
                val messagesResponse = getMessagesResponse(group)
                sendMessagesByGroup(group, messagesResponse)
                Thread.sleep(antispamDelay.toLong())
            } catch (e: HttpClientErrorException) {
                logger.error("Blocked group ${group.name}", e)
                groupsDao.setGroupInvalid(group.name)
            } catch (e: Exception) {
                logger.error(e.message, e)
            }
        }
    }

    private fun getMessagesResponse(group: Group) =
        checkNotNull(
            restTemplate
                .getForEntity(
                    "${apiProperties.getMessageUrl}?subscription={subscription}&last_message_id={lastMessageId}",
                    MessageFetcherResponse::class.java,
                    mapOf(
                        "subscription" to group.name,
                        "lastMessageId" to group.lastMessageId,
                    ),
                ).body,
        )

    private fun sendMessagesByGroup(
        group: Group,
        messagesResponse: MessageFetcherResponse,
    ) {
        val subscriptions = subscriptionDao.getAll().filter { it.group.name == group.name }.filterNot { it.subscriber.chatId == null }
        messagesResponse.messages.forEach { message ->
            val imageText =
                message.value.image?.let {
                    ocrService.convertToText(it)
                }
            val clearMessage = message.value.text?.let { textUtils.removeMarkdown(it) }
            val clearMessageWithImageText = ((clearMessage ?: "").plus(" ").plus(imageText ?: "")).trim()
            logger.info("Check message $clearMessageWithImageText")
            val usersGotThisMessage = mutableSetOf<Long>()
            subscriptions.forEach { subscription ->
                val chatId = checkNotNull(subscription.subscriber.chatId)
                if (usersGotThisMessage.contains(chatId)) {
                    logger.info("Subscriber ${subscription.subscriber.username} already got message.")
                } else {
                    if (sendIfMessageFit(
                            subscription,
                            clearMessageWithImageText,
                            message.key to (clearMessage ?: "Найден в изображении"),
                        )
                    ) {
                        usersGotThisMessage.add(chatId)
                    }
                }
            }
        }
        messagesResponse.messages.keys.maxOrNull()?.let { lastMessageId ->
            groupsDao.setLastGroupMessage(group.name, lastMessageId)
        }
    }

    private fun sendIfMessageFit(
        subscription: Subscription,
        clearMessageWithImageText: String,
        messageToSend: Pair<Long, String>,
    ): Boolean {
        val chatId = checkNotNull(subscription.subscriber.chatId)
        logger.info(
            "Check subscriber ${subscription.subscriber.username} $chatId on group ${subscription.group.name} with search ${subscription.search.properties}",
        )
        try {
            if (messageCheckerService.doesMessageFit(clearMessageWithImageText, subscription.search.properties)) {
                sendMessage(subscription.group, messageToSend, subscription)
            }
            return true
        } catch (e: UserBlockedException) {
            subscriptionDao.deleteSubscriber(subscription.subscriber.username)
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
        return false
    }

    private fun sendMessage(
        group: Group,
        message: Pair<Long, String>,
        subscription: Subscription,
    ) {
        val chatId = checkNotNull(subscription.subscriber.chatId)
        val messageLink =
            if (group.isPrivate()) {
                "https://t.me/${group.name}"
            } else {
                "https://t.me/${group.name}/${message.first}"
            }
        val messageWithAdditionalData =
            message.second +
                "\n\nПерейти к сообщению в группе ${group.name} -> $messageLink" +
                "\nПоиск по: ${subscription.search.properties}"
        sendTextUtilService.sendText(
            who = chatId,
            what = messageWithAdditionalData,
        )
    }

    companion object {
        private val logger = logger()
    }
}
