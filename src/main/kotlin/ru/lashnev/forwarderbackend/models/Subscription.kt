package ru.lashnev.forwarderbackend.models

data class Subscription(val subscriber: Subscriber, val group: Group, val search: Search)
