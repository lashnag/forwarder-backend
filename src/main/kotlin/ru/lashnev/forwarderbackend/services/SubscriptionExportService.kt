package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.dao.SubscriptionDao
import ru.lashnev.forwarderbackend.models.Subscription

@Service
class SubscriptionExportService(private val subscriptionDao: SubscriptionDao) {
    fun getAllSubscriptions(): Set<Subscription> {
        return subscriptionDao.getAll()
    }
}