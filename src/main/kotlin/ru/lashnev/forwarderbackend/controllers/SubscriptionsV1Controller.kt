package ru.lashnev.forwarderbackend.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.lashnev.forwarderbackend.dto.SubscriptionRawDto
import ru.lashnev.forwarderbackend.services.SubscriptionsV1Service

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionsV1Controller(
    private val subscriptionsV1Service: SubscriptionsV1Service,
) {
    @GetMapping
    fun getAllSubscriptions(): Set<SubscriptionRawDto> = subscriptionsV1Service.getAllSubscriptions()
}
