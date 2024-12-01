package ru.lashnev.forwarderbackend.models

data class Subscription(val subscriber: String, val subscription: String, val keywords: Set<Keyword>)
