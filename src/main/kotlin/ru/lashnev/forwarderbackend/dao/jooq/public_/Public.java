/*
 * This file is generated by jOOQ.
 */
package ru.lashnev.forwarderbackend.dao.jooq.public_;


import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import ru.lashnev.forwarderbackend.dao.jooq.DefaultCatalog;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Databasechangelog;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Databasechangeloglock;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Groups;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Keywords;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Subscribers;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.SubscriptionsOld;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.databasechangelog</code>.
     */
    public final Databasechangelog DATABASECHANGELOG = Databasechangelog.DATABASECHANGELOG;

    /**
     * The table <code>public.databasechangeloglock</code>.
     */
    public final Databasechangeloglock DATABASECHANGELOGLOCK = Databasechangeloglock.DATABASECHANGELOGLOCK;

    /**
     * The table <code>public.groups</code>.
     */
    public final Groups GROUPS = Groups.GROUPS;

    /**
     * The table <code>public.keywords</code>.
     */
    public final Keywords KEYWORDS = Keywords.KEYWORDS;

    /**
     * The table <code>public.subscribers</code>.
     */
    public final Subscribers SUBSCRIBERS = Subscribers.SUBSCRIBERS;

    /**
     * The table <code>public.subscriptions_old</code>.
     */
    public final SubscriptionsOld SUBSCRIPTIONS_OLD = SubscriptionsOld.SUBSCRIPTIONS_OLD;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        return Arrays.asList(
            Sequences.SUBSCRIPTIONS_SUBSCRIPTION_ID_SEQ
        );
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            Databasechangelog.DATABASECHANGELOG,
            Databasechangeloglock.DATABASECHANGELOGLOCK,
            Groups.GROUPS,
            Keywords.KEYWORDS,
            Subscribers.SUBSCRIBERS,
            SubscriptionsOld.SUBSCRIPTIONS_OLD
        );
    }
}
