package ru.lashnev.forwarderbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableAsync
class TelegramForwarderApplication

fun main(args: Array<String>) {
    runApplication<TelegramForwarderApplication>(*args)
}
