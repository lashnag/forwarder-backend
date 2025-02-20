package ru.lashnev.forwarderbackend.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import ru.lashnev.forwarderbackend.utils.MDCType.TRACE_ID
import java.util.*

inline fun <reified T> T.logger(): Logger {
    if (T::class.isCompanion) {
        return LoggerFactory.getLogger(T::class.java.enclosingClass)
    }
    return LoggerFactory.getLogger(T::class.java)
}

fun <T> withMDC(action: () -> T): T {
    val traceId = UUID.randomUUID().toString()
    MDC.put(TRACE_ID.value, traceId)
    return try {
        action()
    } finally {
        MDC.remove(TRACE_ID.value)
    }
}

fun <T> withMDC(mdcType: MDCType, mdcValue: String, action: () -> T): T {
    MDC.put(mdcType.value, mdcValue)
    return try {
        action()
    } finally {
        MDC.remove(mdcType.value)
    }
}

enum class MDCType(val value: String) {
    TRACE_ID("traceId"), GROUP("groupName"), USER("userName"), MESSAGE_ID("messageId");
}
