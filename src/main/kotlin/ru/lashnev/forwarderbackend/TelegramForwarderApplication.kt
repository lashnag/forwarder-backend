package ru.lashnev.forwarderbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class TelegramForwarderApplication

fun main(args: Array<String>) {
	runApplication<TelegramForwarderApplication>(*args)
}
