package ru.lashnev.forwarderbackend.models

enum class AdminCommand(
    val commandName: String,
) {
    START("/start"),
    CHANGELOG("/changelog"),
    CREATE_SUBSCRIPTION("/create_subscription"),
    CHANGE_VERSION_V2("/change_version_v2"),
    FETCH_SUBSCRIPTIONS("/fetch_subscriptions"),
    UNKNOWN_COMMAND("unknown"),
}

fun String.toCommand(): AdminCommand {
    for (value in AdminCommand.entries) {
        if (value.commandName == this) {
            return value
        }
    }

    return AdminCommand.UNKNOWN_COMMAND
}
