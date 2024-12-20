package ru.lashnev.forwarderbackend.services

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.MessageFetcherProperties
import ru.lashnev.forwarderbackend.messagefetcher.dto.MessageFetcherResponse
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

    @Scheduled(fixedRate = 60)
    fun processMessages() {
        val subscribers = subscribersDao.getSubscribers()
        groupsDao.getGroups().forEach { group ->
            try {
                val response = restTemplate.getForEntity(
                    "${messageFetcherProperties.url}?subscription=${group.name}&last_message_id=${group.lastMessageId}",
                    MessageFetcherResponse::class.java
                ).body
                checkNotNull(response) { "Response body is null" }

                val subscriptions = subscriptionDao.getAll().filter { it.group.name == group.name }
                response.messages.forEach { message ->
                    subscriptions.forEach { subscription ->
                        val keywords = subscription.keywords.map { it.value }.toSet()
                        val subscriber = subscribers.find {
                            it.username == subscription.subscriber.username
                        } ?: throw  IllegalStateException("Cant find subscriber")
                        if (subscriber.chatId != null && messageCheckerService.containKeyword(message, keywords)) {
                            sendTextUtilService.sendText(subscriber.chatId, message)
                        }
                    }
                }
                groupsDao.setLastGroupMessage(group.name, response.lastMessageId)
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