package com.xihe_lab.yance.idea.stylelint

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

class StylelintRunner(private val project: Project) {

    private val logger = Logger.getInstance("YanceLint.StylelintRunner")

    data class StylelintMessage(
        val rule: String,
        val severity: String,
        val text: String,
        val line: Int,
        val column: Int
    )

    fun run(filePath: String): List<StylelintMessage> {
        val locator = project.getService(ExternalToolLocator::class.java)
        val stylelintPath = locator.locate("stylelint") ?: run {
            logger.info("Stylelint not found in project or global PATH")
            return emptyList()
        }

        return executeStylelint(stylelintPath, filePath)
    }

    private fun executeStylelint(stylelintPath: String, filePath: String): List<StylelintMessage> {
        try {
            val command = mutableListOf(stylelintPath, "--formatter", "json", filePath)

            logger.info("Running Stylelint: ${command.joinToString(" ")}")

            val process = ProcessBuilder(command)
                .directory(java.io.File(project.basePath))
                .redirectErrorStream(true)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exited = process.waitFor(30, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                logger.warn("Stylelint process killed after timeout")
            }

            val exitCode = process.exitValue()
            logger.info("Stylelint exit code: $exitCode, output length: ${output.length}")

            // exit code 0 = no issues, 1 = issues found, 2 = fatal error
            if (exitCode == 2) {
                logger.warn("Stylelint fatal error: ${output.take(500)}")
                return emptyList()
            }

            return parseOutput(output)
        } catch (e: Exception) {
            logger.warn("Stylelint execution error", e)
            return emptyList()
        }
    }

    fun parseOutput(output: String): List<StylelintMessage> {
        val results = mutableListOf<StylelintMessage>()
        try {
            val trimmed = output.trim()
            if (!trimmed.startsWith("[")) {
                logger.warn("Stylelint output does not start with '[': ${trimmed.take(200)}")
                return emptyList()
            }

            val jsonArray = JsonParser.parseString(trimmed).asJsonArray
            for (fileElement in jsonArray) {
                val fileObj = fileElement.asJsonObject
                val warnings = fileObj.getAsJsonArray("warnings") ?: continue
                for (msgElement in warnings) {
                    val msgObj = msgElement.asJsonObject
                    results.add(StylelintMessage(
                        rule = msgObj.get("rule")?.takeIf { !it.isJsonNull }?.asString ?: "",
                        severity = msgObj.get("severity")?.takeIf { !it.isJsonNull }?.asString ?: "warning",
                        text = msgObj.get("text")?.takeIf { !it.isJsonNull }?.asString ?: "",
                        line = msgObj.get("line")?.takeIf { !it.isJsonNull }?.asInt ?: 1,
                        column = msgObj.get("column")?.takeIf { !it.isJsonNull }?.asInt ?: 1
                    ))
                }
            }
            logger.info("Stylelint parsed ${results.size} messages")
        } catch (e: Exception) {
            logger.warn("Failed to parse Stylelint output: ${output.take(200)}", e)
        }
        return results
    }

    companion object {
        fun mapSeverity(severity: String): RuleSeverity = when (severity) {
            "error" -> RuleSeverity.ERROR
            else -> RuleSeverity.WARNING
        }

        fun toRule(ruleId: String): YanceRule = YanceRule(
            id = "stylelint-$ruleId",
            name = ruleId,
            description = "Stylelint rule: $ruleId",
            severity = RuleSeverity.WARNING,
            language = LanguageType.CSS,
            category = RuleCategory.STYLE,
            source = "stylelint",
            enabled = true,
            tags = listOf("stylelint")
        )
    }
}
