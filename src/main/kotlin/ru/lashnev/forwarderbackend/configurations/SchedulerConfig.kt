package ru.lashnev.forwarderbackend.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
class SchedulerConfig {

    @Value("\${scheduler.pool-size}")
    private lateinit var poolSize: String

    @Bean
    fun taskScheduler(): ThreadPoolTaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.poolSize = poolSize.toInt()
        return scheduler
    }
}