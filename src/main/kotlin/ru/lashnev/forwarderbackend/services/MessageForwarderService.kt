package ru.lashnev.forwarderbackend.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.MessageFetcherProperties
import ru.lashnev.forwarderbackend.dao.GroupsDao
import ru.lashnev.forwarderbackend.dao.SubscriptionDao
import ru.lashnev.forwarderbackend.dto.MessageFetcherResponse
import ru.lashnev.forwarderbackend.exceptions.UserBlockedException
import ru.lashnev.forwarderbackend.models.Group
import ru.lashnev.forwarderbackend.models.Subscription
import ru.lashnev.forwarderbackend.utils.SendTextUtilService
import ru.lashnev.forwarderbackend.utils.logger

@Service
class MessageForwarderService(
    private val restTemplate: RestTemplate,
    private val groupsDao: GroupsDao,
    private val subscriptionDao: SubscriptionDao,
    private val messageCheckerService: MessageCheckerService,
    private val messageFetcherProperties: MessageFetcherProperties,
    private val sendTextUtilService: SendTextUtilService,
) {

    @Value("\${scheduler.antispam-delay}")
    private lateinit var antispamDelay: String

    @Scheduled(fixedDelay = 5_000, initialDelay = 30_000)
    fun processMessages() {
        logger.info("Start forwarder scheduler")
        val groups = try {
            groupsDao.getValidGroups()
        } catch (e: Exception) {
            logger.error("Cant get group: ", e)
            return
        }
        groups.forEach { group ->
            try {
                logger.info("Processing group ${group.name}")
                val response = restTemplate.getForEntity(
                    "${messageFetcherProperties.url}?subscription=${group.name}&last_message_id=${group.lastMessageId}",
                    MessageFetcherResponse::class.java
                ).body

                checkNotNull(response) { "Response body is null" }

                sendMessagesByGroup(group, response)
                response.messages.keys.maxOrNull()?.let { lastMessageId ->
                    groupsDao.setLastGroupMessage(group.name, lastMessageId)
                }
                Thread.sleep(antispamDelay.toLong())
            } catch (e: HttpClientErrorException) {
                logger.error("Invalid group ${group.name}", e)
                groupsDao.setGroupInvalid(group.name)
            } catch (e: Exception) {
                logger.error(e.message, e)
            }
        }
    }

    private fun sendMessagesByGroup(group: Group, response: MessageFetcherResponse) {
        val subscriptions = subscriptionDao.getAll().filter { it.group.name == group.name }.filterNot { it.subscriber.chatId == null }
        response.messages.forEach { message ->
            val usersGotThisMessage = mutableSetOf<Long>()
            subscriptions.forEach { subscription ->
                checkNotNull(subscription.subscriber.chatId)
                logger.info("Check subscriber ${subscription.subscriber.username} ${subscription.subscriber.chatId} on group ${subscription.group.name} with search ${subscription.search.properties}")
                if (usersGotThisMessage.contains(subscription.subscriber.chatId)) {
                    logger.info("Subscriber ${subscription.subscriber.username} already got message.")
                } else {
                    if (messageCheckerService.doesMessageFit(message.value, subscription.search.properties)) {
                        usersGotThisMessage.add(subscription.subscriber.chatId)
                        try {
                            sendMessage(group, message, subscription)
                        } catch (e: UserBlockedException) {
                            subscriptionDao.deleteSubscriber(subscription.subscriber.username)
                        } catch (e: Exception) {
                            logger.error(e.message, e)
                        }
                    }
                }
            }
        }
    }

    private fun sendMessage(group: Group, message: Map.Entry<Long, String>, subscription: Subscription) {
        checkNotNull(subscription.subscriber.chatId)
        val messageLink = "https://t.me/${group.name}/${message.key}"
        val messageWithAdditionalData = message.value +
                "\n\n[Перейти к сообщению в группе ${group.name}]($messageLink)" +
                "\nПоиск по: ${subscription.search.properties}"
        sendTextUtilService.sendText(
            who = subscription.subscriber.chatId,
            what = messageWithAdditionalData,
        )
    }

    companion object {
        private val logger = logger()
    }
}