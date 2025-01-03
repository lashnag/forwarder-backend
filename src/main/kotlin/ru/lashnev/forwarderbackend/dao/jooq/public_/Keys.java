/*
 * This file is generated by jOOQ.
 */
package ru.lashnev.forwarderbackend.dao.jooq.public_;


import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Databasechangeloglock;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Groups;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Keywords;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Subscribers;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.SubscriptionsOld;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.records.DatabasechangeloglockRecord;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.records.GroupsRecord;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.records.KeywordsRecord;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.records.SubscribersRecord;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.records.SubscriptionsOldRecord;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<DatabasechangeloglockRecord> DATABASECHANGELOGLOCK_PKEY = Internal.createUniqueKey(Databasechangeloglock.DATABASECHANGELOGLOCK, DSL.name("databasechangeloglock_pkey"), new TableField[] { Databasechangeloglock.DATABASECHANGELOGLOCK.ID }, true);
    public static final UniqueKey<GroupsRecord> GROUPS_GROUPNAME_KEY = Internal.createUniqueKey(Groups.GROUPS, DSL.name("groups_groupname_key"), new TableField[] { Groups.GROUPS.GROUPNAME }, true);
    public static final UniqueKey<GroupsRecord> GROUPS_PKEY = Internal.createUniqueKey(Groups.GROUPS, DSL.name("groups_pkey"), new TableField[] { Groups.GROUPS.GROUP_ID }, true);
    public static final UniqueKey<KeywordsRecord> KEYWORDS_PKEY = Internal.createUniqueKey(Keywords.KEYWORDS, DSL.name("keywords_pkey"), new TableField[] { Keywords.KEYWORDS.KEYWORD_ID }, true);
    public static final UniqueKey<KeywordsRecord> KEYWORDS_SUBSCRIBER_ID_GROUP_ID_KEYWORD_KEY = Internal.createUniqueKey(Keywords.KEYWORDS, DSL.name("keywords_subscriber_id_group_id_keyword_key"), new TableField[] { Keywords.KEYWORDS.SUBSCRIBER_ID, Keywords.KEYWORDS.GROUP_ID, Keywords.KEYWORDS.KEYWORD }, true);
    public static final UniqueKey<SubscribersRecord> SUBSCRIBERS_PKEY = Internal.createUniqueKey(Subscribers.SUBSCRIBERS, DSL.name("subscribers_pkey"), new TableField[] { Subscribers.SUBSCRIBERS.SUBSCRIBER_ID }, true);
    public static final UniqueKey<SubscribersRecord> SUBSCRIBERS_USERNAME_KEY = Internal.createUniqueKey(Subscribers.SUBSCRIBERS, DSL.name("subscribers_username_key"), new TableField[] { Subscribers.SUBSCRIBERS.USERNAME }, true);
    public static final UniqueKey<SubscriptionsOldRecord> SUBSCRIPTIONS_PKEY = Internal.createUniqueKey(SubscriptionsOld.SUBSCRIPTIONS_OLD, DSL.name("subscriptions_pkey"), new TableField[] { SubscriptionsOld.SUBSCRIPTIONS_OLD.SUBSCRIPTION_ID }, true);
    public static final UniqueKey<SubscriptionsOldRecord> SUBSCRIPTIONS_SUBSCRIBER_SUBSCRIPTION_KEYWORD_KEY = Internal.createUniqueKey(SubscriptionsOld.SUBSCRIPTIONS_OLD, DSL.name("subscriptions_subscriber_subscription_keyword_key"), new TableField[] { SubscriptionsOld.SUBSCRIPTIONS_OLD.SUBSCRIBER, SubscriptionsOld.SUBSCRIPTIONS_OLD.SUBSCRIPTION, SubscriptionsOld.SUBSCRIPTIONS_OLD.KEYWORD }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<KeywordsRecord, GroupsRecord> KEYWORDS__KEYWORDS_GROUP_ID_FKEY = Internal.createForeignKey(Keywords.KEYWORDS, DSL.name("keywords_group_id_fkey"), new TableField[] { Keywords.KEYWORDS.GROUP_ID }, Keys.GROUPS_PKEY, new TableField[] { Groups.GROUPS.GROUP_ID }, true);
    public static final ForeignKey<KeywordsRecord, SubscribersRecord> KEYWORDS__KEYWORDS_SUBSCRIBER_ID_FKEY = Internal.createForeignKey(Keywords.KEYWORDS, DSL.name("keywords_subscriber_id_fkey"), new TableField[] { Keywords.KEYWORDS.SUBSCRIBER_ID }, Keys.SUBSCRIBERS_PKEY, new TableField[] { Subscribers.SUBSCRIBERS.SUBSCRIBER_ID }, true);
}
