package ru.lashnev.forwarderbackend.models

import com.fasterxml.jackson.annotation.JsonInclude

data class Search(val searchId: Int? = null, val properties: Properties)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Properties(
    val keywords: MutableList<String> = mutableListOf(),
    var maxMoney: Long? = null,
) {
    override fun toString(): String {
        val result = mutableListOf<String>()

        if (keywords.isNotEmpty()) result.add("Слова = $keywords")
        if (maxMoney != null) result.add("Сумма = $maxMoney")

        return result.joinToString(", ")
    }
}