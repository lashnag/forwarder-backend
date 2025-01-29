package ru.lashnev.forwarderbackend.models

data class Search(val searchId: Int? = null, val properties: Properties)
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