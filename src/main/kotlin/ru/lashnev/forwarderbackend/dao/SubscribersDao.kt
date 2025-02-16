package ru.lashnev.forwarderbackend.dao

import ru.lashnev.forwarderbackend.models.Subscriber

interface SubscribersDao {
    fun getSubscribers(): Set<Subscriber>

    fun getSubscriber(name: String): Subscriber?

    fun setSubscriberChatId(
        subscriber: String,
        chatId: Long,
    )
}
