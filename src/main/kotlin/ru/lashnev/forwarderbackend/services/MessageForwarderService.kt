package ru.lashnev.forwarderbackend.services

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.MessageFetcherProperties
import ru.lashnev.forwarderbackend.dto.MessageFetcherResponse
import ru.lashnev.forwarderbackend.dao.GroupsDao
import ru.lashnev.forwarderbackend.dao.SubscriptionDao
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

    @Scheduled(fixedRate = 60_000, initialDelay = 10_000)
    fun processMessages() {
        logger.info("Start forwarder scheduler")
        groupsDao.getValidGroups().forEach { group ->
            try {
                logger.info("Processing group ${group.name}")
                val response = restTemplate.getForEntity(
                    "${messageFetcherProperties.url}?subscription=${group.name}&last_message_id=${group.lastMessageId}",
                    MessageFetcherResponse::class.java
                ).body

                checkNotNull(response) { "Response body is null" }

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
                                val messageLink = "https://t.me/${group.name}/${message.key}"
                                val messageWithAdditionalData = message.value +
                                        "\n\n[Перейти к сообщению в группе ${group.name}]($messageLink)" +
                                        "\nПоиск по: ${subscription.search.properties}"
                                sendTextUtilService.sendText(
                                    who = subscription.subscriber.chatId,
                                    what = messageWithAdditionalData,
                                )
                            }
                        }
                    }
                }
                response.messages.keys.maxOrNull()?.let { lastMessageId ->
                    groupsDao.setLastGroupMessage(group.name, lastMessageId)
                }
            } catch (e: HttpClientErrorException) {
                logger.error("Invalid group ${group.name}", e)
                groupsDao.setGroupInvalid(group.name)
            } catch (e: RestClientException) {
                logger.warn(e.message, e)
            } catch (e: Exception) {
                logger.error(e.message, e)
            }
        }
    }

    companion object {
        private val logger = logger()
    }
}