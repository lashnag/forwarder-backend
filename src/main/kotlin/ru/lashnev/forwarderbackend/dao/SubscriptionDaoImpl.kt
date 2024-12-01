package ru.lashnev.forwarderbackend.dao

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import ru.lashnev.forwarderbackend.dao.generated.Subscriptions
import ru.lashnev.forwarderbackend.models.Keyword
import ru.lashnev.forwarderbackend.models.Subscription

@Repository
class SubscriptionDaoImpl(private val dsl: DSLContext) : SubscriptionDao {
    override fun addSubscription(subscription: Subscription) {
        subscription.keywords.forEach { keyword ->
            dsl.insertInto(Subscriptions.SUBSCRIPTIONS)
                .set(Subscriptions.SUBSCRIPTIONS.SUBSCRIBER, subscription.subscriber)
                .set(Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION, subscription.subscription)
                .set(Subscriptions.SUBSCRIPTIONS.KEYWORD, keyword.value)
                .execute()
        }
    }

    override fun getSubscriptions(subscriber: String): Set<Subscription> {
        val subscriptionRecords = dsl.select().from(Subscriptions.SUBSCRIPTIONS)
            .where(Subscriptions.SUBSCRIPTIONS.SUBSCRIBER.eq(subscriber))
            .fetch()

        val groupedSubscriptions = subscriptionRecords.groupBy(
            { it[Subscriptions.SUBSCRIPTIONS.SUBSCRIBER] to it[Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION] },
            { it[Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION_ID] to it[Subscriptions.SUBSCRIPTIONS.KEYWORD] }
        )

        return groupedSubscriptions.map { (key, keywords) ->
            Subscription(
                subscriber = key.first,
                subscription = key.second,
                keywords = keywords.map { Keyword(it.first, it.second) }.toSet()
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

    override fun deleteKeyword(subscriber: String, keywordSubscriptionId: Int) {
        dsl.delete(Subscriptions.SUBSCRIPTIONS)
            .where(Subscriptions.SUBSCRIPTIONS.SUBSCRIBER.eq(subscriber))
            .and(Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION_ID.eq(keywordSubscriptionId))
            .execute()
    }

    override fun deleteAll() {
        dsl.delete(Subscriptions.SUBSCRIPTIONS).execute()
    }

    override fun getAll(): Set<Subscription> {
        val subscriptionRecords = dsl.select().from(Subscriptions.SUBSCRIPTIONS).fetch()

        val groupedSubscriptions = subscriptionRecords.groupBy(
            { it[Subscriptions.SUBSCRIPTIONS.SUBSCRIBER] to it[Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION] },
            { it[Subscriptions.SUBSCRIPTIONS.SUBSCRIPTION_ID] to it[Subscriptions.SUBSCRIPTIONS.KEYWORD] }
        )

        return groupedSubscriptions.map { (key, keywords) ->
            Subscription(
                subscriber = key.first,
                subscription = key.second,
                keywords = keywords.map { Keyword(it.first, it.second) }.toSet()
            )
        }.toSet()
    }
}