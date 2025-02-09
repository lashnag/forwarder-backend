package ru.lashnev.forwarderbackend.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageFetcherResponse(
    val messages: LinkedHashMap<Long, Message>
)

data class Message(
    val text: String,
    @JsonProperty("image_text_ru")
    val imageTextRu: String? = null,
    @JsonProperty("image_text_en")
    val imageTextEn: String? = null,
)
