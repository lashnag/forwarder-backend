package ru.lashnev.forwarderbackend.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.lashnev.forwarderbackend.dto.SubscriptionRawDto
import ru.lashnev.forwarderbackend.services.SubscriptionExportService

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionsController(private val subscriptionExportService: SubscriptionExportService) {

    @GetMapping
    fun getAllSubscriptions(): Set<SubscriptionRawDto> {
        val allSubscriptions = subscriptionExportService.getAllSubscriptions()
        return allSubscriptions.flatMap { subscription ->
            subscription.keywords.map { keyword ->
                SubscriptionRawDto(subscription.subscriber, subscription.subscription, keyword)
            }
        }.toSet()
    }
}