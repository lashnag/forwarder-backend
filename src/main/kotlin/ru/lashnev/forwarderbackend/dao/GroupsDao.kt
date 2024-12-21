package ru.lashnev.forwarderbackend.dao

import ru.lashnev.forwarderbackend.models.Group

interface GroupsDao {
    fun getValidGroups(): Set<Group>
    fun setGroupInvalid(group: String)
    fun setLastGroupMessage(group: String, messageId: Long)
}