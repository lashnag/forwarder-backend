package ru.lashnev.forwarderbackend.dao

import org.jooq.DSLContext
import ru.lashnev.forwarderbackend.dao.jooq.public_.tables.Groups.GROUPS
import org.springframework.stereotype.Repository
import ru.lashnev.forwarderbackend.models.Group

@Repository
class GroupsDaoImpl(private val dsl: DSLContext) : GroupsDao {
    override fun getValidGroups(): Set<Group> {
        return dsl.select()
            .from(GROUPS)
            .where(GROUPS.INVALID.eq(false))
            .fetch()
            .map { Group(
                it.get(GROUPS.GROUPNAME),
                it.get(GROUPS.LASTMESSAGEID).toLong(),
                it.get(GROUPS.INVALID)
            ) }
            .toSet()
    }

    override fun setGroupInvalid(group: String) {
        dsl.update(GROUPS)
            .set(GROUPS.INVALID, true)
            .where(GROUPS.GROUPNAME.eq(group))
            .execute()
    }

    override fun setLastGroupMessage(group: String, messageId: Long) {
        dsl.update(GROUPS)
            .set(GROUPS.LASTMESSAGEID, messageId)
            .where(GROUPS.GROUPNAME.eq(group))
            .execute()
    }
}