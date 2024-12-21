/*
 * This file is generated by jOOQ.
 */
package ru.lashnev.forwarderbackend.dao.jooq.public_.tables;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import ru.lashnev.forwarderbackend.dao.jooq.public_.Keys;
import ru.lashnev.forwarderbackend.dao.jooq.public_.Public;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Groups.GroupsPath;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Subscribers.SubscribersPath;
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.records.KeywordsRecord;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Keywords extends TableImpl<KeywordsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.keywords</code>
     */
    public static final Keywords KEYWORDS = new Keywords();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<KeywordsRecord> getRecordType() {
        return KeywordsRecord.class;
    }

    /**
     * The column <code>public.keywords.keyword_id</code>.
     */
    public final TableField<KeywordsRecord, Integer> KEYWORD_ID = createField(DSL.name("keyword_id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.keywords.keyword</code>.
     */
    public final TableField<KeywordsRecord, String> KEYWORD = createField(DSL.name("keyword"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.keywords.subscriber_id</code>.
     */
    public final TableField<KeywordsRecord, Integer> SUBSCRIBER_ID = createField(DSL.name("subscriber_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.keywords.group_id</code>.
     */
    public final TableField<KeywordsRecord, Integer> GROUP_ID = createField(DSL.name("group_id"), SQLDataType.INTEGER.nullable(false), this, "");

    private Keywords(Name alias, Table<KeywordsRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Keywords(Name alias, Table<KeywordsRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.keywords</code> table reference
     */
    public Keywords(String alias) {
        this(DSL.name(alias), KEYWORDS);
    }

    /**
     * Create an aliased <code>public.keywords</code> table reference
     */
    public Keywords(Name alias) {
        this(alias, KEYWORDS);
    }

    /**
     * Create a <code>public.keywords</code> table reference
     */
    public Keywords() {
        this(DSL.name("keywords"), null);
    }

    public <O extends Record> Keywords(Table<O> path, ForeignKey<O, KeywordsRecord> childPath, InverseForeignKey<O, KeywordsRecord> parentPath) {
        super(path, childPath, parentPath, KEYWORDS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class KeywordsPath extends Keywords implements Path<KeywordsRecord> {
        public <O extends Record> KeywordsPath(Table<O> path, ForeignKey<O, KeywordsRecord> childPath, InverseForeignKey<O, KeywordsRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private KeywordsPath(Name alias, Table<KeywordsRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public KeywordsPath as(String alias) {
            return new KeywordsPath(DSL.name(alias), this);
        }

        @Override
        public KeywordsPath as(Name alias) {
            return new KeywordsPath(alias, this);
        }

        @Override
        public KeywordsPath as(Table<?> alias) {
            return new KeywordsPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<KeywordsRecord, Integer> getIdentity() {
        return (Identity<KeywordsRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<KeywordsRecord> getPrimaryKey() {
        return Keys.KEYWORDS_PKEY;
    }

    @Override
    public List<UniqueKey<KeywordsRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.KEYWORDS_SUBSCRIBER_ID_GROUP_ID_KEYWORD_KEY);
    }

    @Override
    public List<ForeignKey<KeywordsRecord, ?>> getReferences() {
        return Arrays.asList(Keys.KEYWORDS__KEYWORDS_SUBSCRIBER_ID_FKEY, Keys.KEYWORDS__KEYWORDS_GROUP_ID_FKEY);
    }

    private transient SubscribersPath _subscribers;

    /**
     * Get the implicit join path to the <code>public.subscribers</code> table.
     */
    public SubscribersPath subscribers() {
        if (_subscribers == null)
            _subscribers = new SubscribersPath(this, Keys.KEYWORDS__KEYWORDS_SUBSCRIBER_ID_FKEY, null);

        return _subscribers;
    }

    private transient GroupsPath _groups;

    /**
     * Get the implicit join path to the <code>public.groups</code> table.
     */
    public GroupsPath groups() {
        if (_groups == null)
            _groups = new GroupsPath(this, Keys.KEYWORDS__KEYWORDS_GROUP_ID_FKEY, null);

        return _groups;
    }

    @Override
    public Keywords as(String alias) {
        return new Keywords(DSL.name(alias), this);
    }

    @Override
    public Keywords as(Name alias) {
        return new Keywords(alias, this);
    }

    @Override
    public Keywords as(Table<?> alias) {
        return new Keywords(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Keywords rename(String name) {
        return new Keywords(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Keywords rename(Name name) {
        return new Keywords(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Keywords rename(Table<?> name) {
        return new Keywords(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Keywords where(Condition condition) {
        return new Keywords(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Keywords where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Keywords where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Keywords where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Keywords where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Keywords where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Keywords where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Keywords where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Keywords whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Keywords whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}