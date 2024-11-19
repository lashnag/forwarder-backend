package ru.lashnev.forwarderbackend.dao

import ru.lashnev.forwarderbackend.models.Subscription

interface SubscriptionDao {
    fun addSubscription(subscription: Subscription)
    fun getSubscriptions(subscriber: String): Set<Subscription>
    fun deleteSubscriber(subscriber: String)
    fun deleteSubscription(subscriber: String, subscription: String)
    fun deleteKeyword(subscriber: String, subscription: String, keyword: String)
    fun deleteAll()
}