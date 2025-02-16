package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import org.tartarus.snowball.ext.EnglishStemmer
import org.tartarus.snowball.ext.RussianStemmer

@Service
class SnowballLemmatizerService : LemmatizerService {
    override fun normalize(word: String): String = word.stem()

    private fun String.stem(): String =
        if (isRussian(this)) {
            russianStemmer.current = this
            russianStemmer.stem()
            russianStemmer.current
        } else {
            englishStemmer.current = this
            englishStemmer.stem()
            englishStemmer.current
        }

    private fun isRussian(message: String): Boolean {
        val regex = Regex("[а-яА-ЯёЁ]+")
        return regex.containsMatchIn(message)
    }

    companion object {
        val russianStemmer = RussianStemmer()
        val englishStemmer = EnglishStemmer()
    }
}
