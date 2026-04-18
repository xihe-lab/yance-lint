package com.xihe_lab.yance.idea.eslint

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.xihe_lab.yance.engine.ExternalToolLocator
import com.xihe_lab.yance.model.LanguageType
import com.xihe_lab.yance.model.RuleCategory
import com.xihe_lab.yance.model.RuleSeverity
import com.xihe_lab.yance.model.YanceRule
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.*

class EsLintRunner(private val project: Project) {

    private val logger = Logger.getInstance("YanceLint.EsLintRunner")

    data class EsLintMessage(
        val ruleId: String?,
        val severity: Int,
        val message: String,
        val line: Int,
        val column: Int
    )

    fun run(filePath: String): List<EsLintMessage> {
        val locator = project.getService(ExternalToolLocator::class.java)
        val eslintPath = locator.locate("eslint") ?: run {
            logger.info("ESLint not found in project or global PATH")
            return emptyList()
        }

        return executeEsLint(eslintPath, filePath)
    }

    private fun executeEsLint(eslintPath: String, filePath: String): List<EsLintMessage> {
        try {
            val command = mutableListOf(eslintPath, "--format", "json", "--no-color", filePath)

            logger.info("Running ESLint: ${command.joinToString(" ")}")

            val process = ProcessBuilder(command)
                .directory(java.io.File(project.basePath))
                .redirectErrorStream(true)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exited = process.waitFor(30, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                logger.warn("ESLint process killed after timeout")
            }

            val exitCode = process.exitValue()
            logger.info("ESLint exit code: $exitCode, output length: ${output.length}")

            // exit code 0 = no issues, 1 = issues found, 2 = fatal error
            if (exitCode == 2) {
                logger.warn("ESLint fatal error: ${output.take(500)}")
                return emptyList()
            }

            return parseOutput(output)
        } catch (e: Exception) {
            logger.warn("ESLint execution error", e)
            return emptyList()
        }
    }

    fun parseOutput(output: String): List<EsLintMessage> {
        val results = mutableListOf<EsLintMessage>()
        try {
            val trimmed = output.trim()
            if (!trimmed.startsWith("[")) {
                logger.warn("ESLint output does not start with '[': ${trimmed.take(200)}")
                return emptyList()
            }

            val jsonArray = JsonParser.parseString(trimmed).asJsonArray
            for (fileElement in jsonArray) {
                val fileObj = fileElement.asJsonObject
                val messages = fileObj.getAsJsonArray("messages") ?: continue
                for (msgElement in messages) {
                    val msgObj = msgElement.asJsonObject
                    results.add(EsLintMessage(
                        ruleId = msgObj.get("ruleId")?.takeIf { !it.isJsonNull }?.asString,
                        severity = msgObj.get("severity")?.takeIf { !it.isJsonNull }?.asInt ?: 1,
                        message = msgObj.get("message")?.takeIf { !it.isJsonNull }?.asString ?: "",
                        line = msgObj.get("line")?.takeIf { !it.isJsonNull }?.asInt ?: 1,
                        column = msgObj.get("column")?.takeIf { !it.isJsonNull }?.asInt ?: 1
                    ))
                }
            }
            logger.info("ESLint parsed ${results.size} messages")
        } catch (e: Exception) {
            logger.warn("Failed to parse ESLint output: ${output.take(200)}", e)
        }
        return results
    }

    companion object {
        fun mapSeverity(eslintSeverity: Int): RuleSeverity = when (eslintSeverity) {
            2 -> RuleSeverity.ERROR
            else -> RuleSeverity.WARNING
        }

        fun toRule(ruleId: String): YanceRule = YanceRule(
            id = "eslint-$ruleId",
            name = ruleId,
            description = "ESLint rule: $ruleId",
            severity = RuleSeverity.WARNING,
            language = LanguageType.JAVASCRIPT,
            category = RuleCategory.STYLE,
            source = "eslint",
            enabled = true,
            tags = listOf("eslint")
        )
    }
}
