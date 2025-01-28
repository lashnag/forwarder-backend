package ru.lashnev.forwarderbackend.services

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.MessageFetcherProperties
import ru.lashnev.forwarderbackend.dto.MessageFetcherResponse
import ru.lashnev.forwarderbackend.dao.GroupsDao
import ru.lashnev.forwarderbackend.dao.SubscribersDao
import ru.lashnev.forwarderbackend.dao.SubscriptionDao
import ru.lashnev.forwarderbackend.utils.SendTextUtilService
import ru.lashnev.forwarderbackend.utils.logger

@Service
class MessageForwarderService(
    private val restTemplate: RestTemplate,
    private val groupsDao: GroupsDao,
    private val subscribersDao: SubscribersDao,
    private val subscriptionDao: SubscriptionDao,
    private val messageCheckerService: MessageCheckerService,
    private val messageFetcherProperties: MessageFetcherProperties,
    private val sendTextUtilService: SendTextUtilService,
) {

    @Scheduled(fixedRate = 60_000, initialDelay = 10_000)
    fun processMessages() {
        val subscribers = subscribersDao.getSubscribers()
        groupsDao.getValidGroups().forEach { group ->
            try {
                logger.info("Processing group ${group.name}")
                val response = restTemplate.getForEntity(
                    "${messageFetcherProperties.url}?subscription=${group.name}&last_message_id=${group.lastMessageId}",
                    MessageFetcherResponse::class.java
                ).body

                logger.info("Send request ${messageFetcherProperties.url}?subscription=${group.name}&last_message_id=${group.lastMessageId}")
                logger.info("Got response: $response")

                checkNotNull(response) { "Response body is null" }

                val subscriptions = subscriptionDao.getAll().filter { it.group.name == group.name }
                response.messages.forEach { message ->
                    subscriptions.forEach { subscription ->
                        val subscriber = subscribers.find {
                            it.username == subscription.subscriber.username
                        } ?: throw  IllegalStateException("Cant find subscriber")
                        logger.info("Check subscriber ${subscriber.username} with search ${subscription.search.properties}")
                        if (subscriber.chatId != null && messageCheckerService.doesMessageFit(message.value, subscription.search.properties)) {
                            val messageLink = "https://t.me/${group.name}/${message.key}"
                            val sendMessage = "${message.value} \n\n Сообщение переслано из группы: @${group.name} \n [Перейти к сообщению]($messageLink)"
                            sendTextUtilService.sendText(subscriber.chatId, sendMessage)
                        }
                    }
                }
                response.messages.keys.lastOrNull()?.let { lastMessageId ->
                    groupsDao.setLastGroupMessage(group.name, lastMessageId)
                }
            } catch (e: HttpClientErrorException) {
                logger.warn("Invalid group ${group.name}")
                groupsDao.setGroupInvalid(group.name)
            } catch (e: RestClientException) {
                logger.error(e.message, e)
            }
        }
    }

    companion object {
        private val logger = logger()
    }
}