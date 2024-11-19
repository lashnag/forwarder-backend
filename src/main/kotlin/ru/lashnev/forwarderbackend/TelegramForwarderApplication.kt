package ru.lashnev.forwarderbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TelegramForwarderApplication

fun main(args: Array<String>) {
	runApplication<TelegramForwarderApplication>(*args)
}
