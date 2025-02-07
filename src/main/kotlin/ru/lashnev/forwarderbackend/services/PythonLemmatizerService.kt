package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import ru.lashnev.forwarderbackend.utils.runCommandAndWaitResult

@Service
class PythonLemmatizerService : LemmatizerService {
    init {
        val pipInstallCommand = arrayOf("pip", "install", "-r", "requirements.txt")
        runCommandAndWaitResult(pipInstallCommand)
    }

    override fun normalize(sentence: String): String {
        val pythonCommand = arrayOf("python", "src/main/python/lemmatizer.py", sentence)
        return runCommandAndWaitResult(pythonCommand)
    }


}