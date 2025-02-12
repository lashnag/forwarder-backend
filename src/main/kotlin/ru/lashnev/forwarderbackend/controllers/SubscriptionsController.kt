package ru.lashnev.forwarderbackend.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.lashnev.forwarderbackend.dao.SubscribersDao
import ru.lashnev.forwarderbackend.dto.SubscriptionRawDto
import ru.lashnev.forwarderbackend.services.SubscriptionExportService

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionsController(
    private val subscriptionExportService: SubscriptionExportService,
    private val subscribersDao: SubscribersDao,
) {

    @GetMapping
    fun getAllSubscriptions(): Set<SubscriptionRawDto> {
        val allSubscribers = subscribersDao.getSubscribers()
        val allSubscriptions = subscriptionExportService.getAllSubscriptions()
        return allSubscriptions.mapNotNull { subscription ->
            val subscriber = checkNotNull(allSubscribers.find { it.username == subscription.subscriber.username })
            if (subscriber.chatId == null) {
                SubscriptionRawDto(
                    subscription.subscriber.username,
                    subscription.group.name,
                    subscription.search.properties.keywords.joinToString(separator = " ")
                )
            } else {
                null
            }
        }.toSet()
    }
}