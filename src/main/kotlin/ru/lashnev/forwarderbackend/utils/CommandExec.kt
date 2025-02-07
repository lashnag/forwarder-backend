package ru.lashnev.forwarderbackend.utils

import java.io.BufferedReader
import java.io.InputStreamReader

fun runCommandAndWaitResult(command: Array<String>): String {
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
        throw Exception("Error command '$command' code $exitCode: ${errorResult.toString().trim()}")
    }

    return result.toString().trim()
}