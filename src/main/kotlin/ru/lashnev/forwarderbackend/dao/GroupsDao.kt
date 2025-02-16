package ru.lashnev.forwarderbackend.dao

import ru.lashnev.forwarderbackend.models.Group

interface GroupsDao {
    fun getByName(name: String): Group?

    fun getValidGroups(): Set<Group>

    fun setGroupInvalid(group: String)

    fun setLastGroupMessage(
        group: String,
        messageId: Long,
    )
}
