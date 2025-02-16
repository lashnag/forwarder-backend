package ru.lashnev.forwarderbackend.configurations

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheConfig {
    @Value("\${cache.max-size}")
    private lateinit var maxSize: String

    @Bean
    fun cacheManager(): CacheManager {
        val caffeineBuilder = Caffeine.newBuilder().maximumSize(maxSize.toLong())
        return CaffeineCacheManager("lemmatizationCache").apply {
            setCaffeine(caffeineBuilder)
        }
    }
}
