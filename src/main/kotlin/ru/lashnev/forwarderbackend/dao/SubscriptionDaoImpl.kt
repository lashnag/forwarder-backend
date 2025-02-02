package ru.lashnev.forwarderbackend.dao

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Groups.*
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Searches.*
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Subscribers.*
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Subscriptions.*
import ru.lashnev.forwarderbackend.models.*


@Repository
class SubscriptionDaoImpl(private val dsl: DSLContext, private val objectMapper: ObjectMapper) : SubscriptionDao {

    @Transactional
    override fun addSubscription(subscription: Subscription) {
        val subscriberId = dsl.insertInto(SUBSCRIBERS)
            .set(SUBSCRIBERS.USERNAME, subscription.subscriber.username)
            .set(SUBSCRIBERS.CHATID, subscription.subscriber.chatId.toString())
            .onConflict(SUBSCRIBERS.USERNAME)
            .doUpdate()
            .set(SUBSCRIBERS.CHATID, subscription.subscriber.chatId.toString())
            .returning(SUBSCRIBERS.SUBSCRIBER_ID)
            .fetchOne()!!
            .getValue(SUBSCRIBERS.SUBSCRIBER_ID)

        val existingGroupId = dsl.select(GROUPS.GROUP_ID)
            .from(GROUPS)
            .where(GROUPS.GROUPNAME.eq(subscription.group.name))
            .fetchOne()
            ?.getValue(GROUPS.GROUP_ID)

        val groupId = existingGroupId ?: dsl.insertInto(GROUPS)
            .set(GROUPS.GROUPNAME, subscription.group.name)
            .returning()
            .fetchOne()!!
            .getValue(GROUPS.GROUP_ID)

        val existingSearchId = dsl.select(SEARCHES.SEARCH_ID)
            .from(SEARCHES)
            .where(SEARCHES.PROPERTIES.eq(objectMapper.writeValueAsString(subscription.search.properties)))
            .fetchOne()
            ?.getValue(SEARCHES.SEARCH_ID)

        val searchId = existingSearchId ?: dsl.insertInto(SEARCHES)
            .set(SEARCHES.PROPERTIES, objectMapper.writeValueAsString(subscription.search.properties))
            .returning()
            .fetchOne()!!
            .getValue(SEARCHES.SEARCH_ID)

        dsl.insertInto(SUBSCRIPTIONS)
            .set(SUBSCRIPTIONS.GROUP_ID, groupId)
            .set(SUBSCRIPTIONS.SUBSCRIBER_ID, subscriberId)
            .set(SUBSCRIPTIONS.SEARCH_ID, searchId)
            .execute()
    }

    override fun getSubscriptionsBySubscriber(subscriber: String): Set<Subscription> {
        val subscriptions = dsl.select()
            .from(SUBSCRIPTIONS)
            .join(SUBSCRIBERS).on(SUBSCRIBERS.SUBSCRIBER_ID.eq(SUBSCRIPTIONS.SUBSCRIBER_ID))
            .join(GROUPS).on(GROUPS.GROUP_ID.eq(SUBSCRIPTIONS.GROUP_ID))
            .join(SEARCHES).on(SEARCHES.SEARCH_ID.eq(SUBSCRIPTIONS.SEARCH_ID))
            .where(SUBSCRIBERS.USERNAME.eq(subscriber))
            .fetch()

        return subscriptions.map { it.toSubscription() }.toSet()
    }

    @Transactional
    override fun deleteSubscriber(subscriber: String) {
        val subscriberId = dsl.select()
            .from(SUBSCRIBERS)
            .where(SUBSCRIBERS.USERNAME.eq(subscriber))
            .fetchOne()!!
            .getValue(SUBSCRIBERS.SUBSCRIBER_ID)

        dsl.delete(SUBSCRIPTIONS)
            .where(SUBSCRIPTIONS.SUBSCRIBER_ID.eq(subscriberId))
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

        dsl.delete(SUBSCRIPTIONS)
            .where(SUBSCRIPTIONS.SUBSCRIBER_ID.eq(subscriberId))
            .and(SUBSCRIPTIONS.GROUP_ID.eq(groupId))
            .execute()
    }

    @Transactional
    override fun deleteSubscription(subscriber: String, searchId: Int) {
        val subscriberId = dsl.select()
            .from(SUBSCRIBERS)
            .where(SUBSCRIBERS.USERNAME.eq(subscriber))
            .fetchOne()!!
            .getValue(SUBSCRIBERS.SUBSCRIBER_ID)

        dsl.delete(SUBSCRIPTIONS)
            .where(SUBSCRIPTIONS.SUBSCRIBER_ID.eq(subscriberId))
            .and(SUBSCRIPTIONS.SEARCH_ID.eq(searchId))
            .execute()
    }

    override fun deleteAll() {
        dsl.delete(SUBSCRIPTIONS).execute()
        dsl.delete(SEARCHES).execute()
        dsl.delete(GROUPS).execute()
        dsl.delete(SUBSCRIBERS).execute()
    }

    override fun getAll(): Set<Subscription> {
        val subscriptions = dsl.select()
            .from(SUBSCRIPTIONS)
            .join(SUBSCRIBERS).on(SUBSCRIBERS.SUBSCRIBER_ID.eq(SUBSCRIPTIONS.SUBSCRIBER_ID))
            .join(GROUPS).on(GROUPS.GROUP_ID.eq(SUBSCRIPTIONS.GROUP_ID))
            .join(SEARCHES).on(SEARCHES.SEARCH_ID.eq(SUBSCRIPTIONS.SEARCH_ID))
            .fetch()

        return subscriptions.map { it.toSubscription() }.toSet()
    }

    private fun Record.toSubscription(): Subscription {
        val groupName = this.get(GROUPS.GROUPNAME)
        val lastMessageId = this.get(GROUPS.LASTMESSAGEID)
        val group = Group(groupName, lastMessageId ?: 0)

        val username = this.get(SUBSCRIBERS.USERNAME)
        val chatId = this.get(SUBSCRIBERS.CHATID)?.toLong()
        val subscriber = Subscriber(username, chatId)

        val searchId = this.get(SEARCHES.SEARCH_ID)
        val searchPropertiesString = this.get(SEARCHES.PROPERTIES)
        val searchProperties = objectMapper.readValue(searchPropertiesString, Properties::class.java)
        val search = Search(searchId, searchProperties)

        return Subscription(
            subscriber,
            group,
            search
        )
    }

}