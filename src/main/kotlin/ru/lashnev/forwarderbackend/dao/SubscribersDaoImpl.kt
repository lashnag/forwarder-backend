package ru.lashnev.forwarderbackend.dao

import org.jooq.DSLContext
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Subscribers.SUBSCRIBERS
import org.springframework.stereotype.Repository
import ru.lashnev.forwarderbackend.models.Subscriber

@Repository
class SubscribersDaoImpl(private val dsl: DSLContext) : SubscribersDao {
    override fun getSubscribers(): Set<Subscriber> {
        return dsl.select()
            .from(SUBSCRIBERS)
            .fetch()
            .map { Subscriber(it.get(SUBSCRIBERS.USERNAME), it.get(SUBSCRIBERS.CHATID)?.toLong()) }
            .toSet()
    }

    override fun getSubscriber(name: String): Subscriber? {
        return dsl.select()
            .from(SUBSCRIBERS)
            .where(SUBSCRIBERS.USERNAME.eq(name))
            .fetchOne()?.map { Subscriber(it.get(SUBSCRIBERS.USERNAME), it.get(SUBSCRIBERS.CHATID)?.toLong()) }
    }

    override fun setSubscriberChatId(subscriber: String, chatId: Long) {
        dsl.update(SUBSCRIBERS)
            .set(SUBSCRIBERS.CHATID, chatId.toString())
            .where(SUBSCRIBERS.USERNAME.eq(subscriber))
            .execute()
    }
}