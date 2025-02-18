package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.dao.SubscribersDao
import ru.lashnev.forwarderbackend.dto.SubscriptionRawDto

@Service
class SubscriptionsV1Service(
    private val subscriptionExportService: SubscriptionExportService,
    private val subscribersDao: SubscribersDao,
) {
    fun getAllSubscriptions(): Set<SubscriptionRawDto> {
        val allSubscribers = subscribersDao.getSubscribers()
        val allSubscriptions = subscriptionExportService.getAllSubscriptions()
        return allSubscriptions
            .mapNotNull { subscription ->
                val subscriber = checkNotNull(allSubscribers.find { it.username == subscription.subscriber.username })
                if (subscriber.chatId == null) {
                    SubscriptionRawDto(
                        subscription.subscriber.username,
                        subscription.group.name,
                        subscription.search.properties.keywords
                            .joinToString(separator = " "),
                    )
                } else {
                    null
                }
            }.toSet()
    }
}
