package ru.lashnev.forwarderbackend.services

import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader

@Service
class PythonLemmatizerService : LemmatizerService {
    init {
        val pipInstallCommand = arrayOf("pip", "install", "-r", "requirements.txt")
        runCommand(pipInstallCommand)
    }

    override fun normalize(sentence: String): String {
        val pythonCommand = arrayOf("python", "src/main/python/lemmatizer.py", sentence)
        return runCommand(pythonCommand)
    }

    private fun runCommand(command: Array<String>): String {
        val process = Runtime.getRuntime().exec(command)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))
        val result = StringBuilder()
        val errorResult = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            result.append(line)
        }

        while (errorReader.readLine().also { line = it } != null) {
            errorResult.append(line)
        }

        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw Exception("Error python command '$command' code $exitCode: ${errorResult.toString().trim()}")
        }

        return result.toString().trim()
    }
}