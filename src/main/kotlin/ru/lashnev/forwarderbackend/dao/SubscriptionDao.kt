package ru.lashnev.forwarderbackend.dao

import ru.lashnev.forwarderbackend.models.Subscription

interface SubscriptionDao {
    fun addSubscription(subscription: Subscription)
    fun getSubscriptionsBySubscriber(subscriber: String): Set<Subscription>
    fun deleteSubscriber(subscriber: String)
    fun deleteSubscription(subscriber: String, group: String)
    fun deleteKeyword(subscriber: String, keywordId: Int)

    fun deleteAll()
    fun getAll(): Set<Subscription>
}