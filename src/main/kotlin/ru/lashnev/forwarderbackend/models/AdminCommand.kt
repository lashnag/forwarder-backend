package ru.lashnev.forwarderbackend.models

enum class AdminCommand(val commandName: String) {
    CREATE_SUBSCRIPTION("/create_subscription"),
    FETCH_SUBSCRIPTIONS("/fetch_subscriptions"),
    UNKNOWN_COMMAND("unknown");
}

fun String.toCommand(): AdminCommand {
    for (value in AdminCommand.entries) {
        if (value.commandName == this) {
            return value
        }
    }

    return AdminCommand.UNKNOWN_COMMAND
}
