package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.configurations.ApiSenderProperties
import ru.lashnev.forwarderbackend.dto.ApiResponseDto
import ru.lashnev.forwarderbackend.dto.SubscriptionCreateDto
import ru.lashnev.forwarderbackend.models.Subscription

@Service
class SubscriptionExportService(private val restTemplate: RestTemplate, private val apiSenderProperties: ApiSenderProperties) {
    fun addSubscription(subscription: Subscription) {
        subscription.keywords.forEach { keyword ->
            val subscriptionDto = SubscriptionCreateDto(
                subscription.subscription,
                keyword
            )
            restTemplate.postForEntity(
                apiSenderProperties.uri + subscription.subscriber,
                subscriptionDto,
                ApiResponseDto::class.java
            )
        }
    }

    fun deleteSubscriber(subscriber: String) {
        restTemplate.delete(apiSenderProperties.uri + subscriber)
    }

    fun deleteSubscription(subscriber: String, subscription: String) {
        restTemplate.delete(apiSenderProperties.uri + subscriber + "/" + subscription)
    }

    fun deleteKeyword(subscriber: String, subscription: String, keyword: String) {
        restTemplate.delete(apiSenderProperties.uri + subscriber + "/" + subscription + "/" + keyword)
    }
}