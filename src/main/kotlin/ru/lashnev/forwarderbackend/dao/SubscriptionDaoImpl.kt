package ru.lashnev.forwarderbackend.dao

import jakarta.transaction.Transactional
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Result
import ru.lashnev.forwarderbackend.dao.jooq.tables.Groups.*
import ru.lashnev.forwarderbackend.dao.jooq.tables.Keywords.*
import ru.lashnev.forwarderbackend.dao.jooq.tables.Subscribers.*
import org.springframework.stereotype.Repository
import ru.lashnev.forwarderbackend.models.Group
import ru.lashnev.forwarderbackend.models.Keyword
import ru.lashnev.forwarderbackend.models.Subscriber
import ru.lashnev.forwarderbackend.models.Subscription

@Repository
class SubscriptionDaoImpl(private val dsl: DSLContext) : SubscriptionDao {

    @Transactional
    override fun addSubscription(subscription: Subscription) {
        val subscriberId = dsl.insertInto(SUBSCRIBERS)
            .set(SUBSCRIBERS.USERNAME, subscription.subscriber.username)
            .onConflict(SUBSCRIBERS.USERNAME)
            .doUpdate()
            .set(SUBSCRIBERS.CHATID, subscription.subscriber.chatId.toString())
            .returning(SUBSCRIBERS.SUBSCRIBER_ID)
            .fetchOne()!!
            .getValue(SUBSCRIBERS.SUBSCRIBER_ID)

        val groupId = dsl.insertInto(GROUPS)
            .set(GROUPS.GROUPNAME, subscription.group.name)
            .onConflict(GROUPS.GROUPNAME)
            .doUpdate()
            .set(GROUPS.GROUPNAME, subscription.group.name)
            .returning()
            .fetchOne()!!
            .getValue(GROUPS.GROUP_ID)

        subscription.keywords.forEach { keyword ->
            dsl.insertInto(KEYWORDS)
                .set(KEYWORDS.KEYWORD, keyword.value)
                .set(KEYWORDS.GROUP_ID, groupId)
                .set(KEYWORDS.SUBSCRIBER_ID, subscriberId)
                .execute()
        }
    }

    override fun getSubscriptionsBySubscriber(subscriber: String): Set<Subscription> {
        val subscriptions = dsl.select()
            .from(SUBSCRIBERS)
            .join(KEYWORDS).on(SUBSCRIBERS.SUBSCRIBER_ID.eq(KEYWORDS.SUBSCRIBER_ID))
            .join(GROUPS).on(GROUPS.GROUP_ID.eq(KEYWORDS.GROUP_ID))
            .where(SUBSCRIBERS.USERNAME.eq(subscriber))
            .fetch()

        return subscriptions.toSubscriptions()
    }

    @Transactional
    override fun deleteSubscriber(subscriber: String) {
        val subscriberId = dsl.select()
            .from(SUBSCRIBERS)
            .where(SUBSCRIBERS.USERNAME.eq(subscriber))
            .fetchOne()!!
            .getValue(SUBSCRIBERS.SUBSCRIBER_ID)

        dsl.delete(KEYWORDS)
            .where(KEYWORDS.SUBSCRIBER_ID.eq(subscriberId))
            .execute()

        dsl.deleteFrom(SUBSCRIBERS)
            .where(SUBSCRIBERS.SUBSCRIBER_ID.eq(subscriberId))
    }

    @Transactional
    override fun deleteSubscription(subscriber: String, group: String) {
        val subscriberId = dsl.select()
            .from(SUBSCRIBERS)
            .where(SUBSCRIBERS.USERNAME.eq(subscriber))
            .fetchOne()!!
            .getValue(SUBSCRIBERS.SUBSCRIBER_ID)

        val groupId = dsl.select()
            .from(GROUPS)
            .where(GROUPS.GROUPNAME.eq(group))
            .fetchOne()!!
            .getValue(GROUPS.GROUP_ID)

        dsl.delete(KEYWORDS)
            .where(KEYWORDS.SUBSCRIBER_ID.eq(subscriberId))
            .and(KEYWORDS.GROUP_ID.eq(groupId))
            .execute()
    }

    @Transactional
    override fun deleteKeyword(subscriber: String, keywordId: Int) {
        val subscriberId = dsl.select()
            .from(SUBSCRIBERS)
            .where(SUBSCRIBERS.USERNAME.eq(subscriber))
            .fetchOne()!!
            .getValue(SUBSCRIBERS.SUBSCRIBER_ID)

        dsl.delete(KEYWORDS)
            .where(KEYWORDS.SUBSCRIBER_ID.eq(subscriberId))
            .and(KEYWORDS.KEYWORD_ID.eq(keywordId))
            .execute()
    }

    override fun deleteAll() {
        dsl.delete(KEYWORDS).execute()
        dsl.delete(GROUPS).execute()
        dsl.delete(SUBSCRIBERS).execute()
    }

    override fun getAll(): Set<Subscription> {
        val subscriptions = dsl.select()
            .from(SUBSCRIBERS)
            .join(KEYWORDS).on(SUBSCRIBERS.SUBSCRIBER_ID.eq(KEYWORDS.SUBSCRIBER_ID))
            .join(GROUPS).on(GROUPS.GROUP_ID.eq(KEYWORDS.GROUP_ID))
            .fetch()

        return subscriptions.toSubscriptions()
    }

    private fun Result<Record>.toSubscriptions(): Set<Subscription> {
        return this.groupBy { it.get(SUBSCRIBERS.USERNAME) }
            .flatMap { (subscriber, group) ->
                group.groupBy { it.get(GROUPS.GROUPNAME) }.map { (groupName, groupRecords) ->
                    val chatId = groupRecords.firstOrNull()?.get(SUBSCRIBERS.CHATID)
                    val lastMessageId = groupRecords.firstOrNull()?.get(GROUPS.LASTMESSAGEID)

                    val keywords = groupRecords.map {
                        Keyword(
                            it.get(KEYWORDS.KEYWORD_ID),
                            it.get(KEYWORDS.KEYWORD)
                        )
                    }.toSet()

                    Subscription(
                        Subscriber(subscriber, chatId?.toLong()),
                        Group(groupName, lastMessageId ?: 0),
                        keywords
                    )
                }
            }.toSet()
    }

}