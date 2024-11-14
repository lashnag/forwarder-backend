package ru.lashnev.forwarderbackend.dao

import org.jooq.DSLContext
import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.dao.generated.Subscriptions
import ru.lashnev.forwarderbackend.models.Subscription

@Service
class SubscriptionDaoImpl(private val dsl: DSLContext) : SubscriptionDao {
    override fun addSubscription(subscription: Subscription) {
        subscription.keywords.forEach { keyword ->
            dsl.insertInto(Subscriptions.SUBSCRIPTIONS)
                .set(Subscriptions.SUBSCRIPTIONS.SUBSCRIBER, subscription.subscriber)
                .set(Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION, subscription.subscription)
                .set(Subscriptions.SUBSCRIPTIONS.KEYWORD, keyword)
                .execute()
        }
    }

    override fun getSubscriptions(subscriber: String): Set<Subscription> {
        val subscriptions = dsl.select().from(Subscriptions.SUBSCRIPTIONS)
            .where(Subscriptions.SUBSCRIPTIONS.SUBSCRIBER.eq(subscriber))
            .fetch()

        val groupedSubscriptions = subscriptions.groupBy(
            { it[Subscriptions.SUBSCRIPTIONS.SUBSCRIBER] to it[Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION] },
            { it[Subscriptions.SUBSCRIPTIONS.KEYWORD] }
        )

        return groupedSubscriptions.map { (key, keywords) ->
            Subscription(
                subscriber = key.first,
                subscription = key.second,
                keywords = keywords.toSet()
            )
        }.toSet()
    }

    override fun deleteSubscriber(subscriber: String) {
        dsl.delete(Subscriptions.SUBSCRIPTIONS)
            .where(Subscriptions.SUBSCRIPTIONS.SUBSCRIBER.eq(subscriber))
            .execute()
    }

    override fun deleteSubscription(subscriber: String, subscription: String) {
        dsl.delete(Subscriptions.SUBSCRIPTIONS)
            .where(Subscriptions.SUBSCRIPTIONS.SUBSCRIBER.eq(subscriber))
            .and(Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION.eq(subscription))
            .execute()
    }

    override fun deleteKeyword(subscriber: String, subscription: String, keyword: String) {
        dsl.delete(Subscriptions.SUBSCRIPTIONS)
            .where(Subscriptions.SUBSCRIPTIONS.SUBSCRIBER.eq(subscriber))
            .and(Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION.eq(subscription))
            .and(Subscriptions.SUBSCRIPTIONS.KEYWORD.eq(keyword))
            .execute()
    }
}