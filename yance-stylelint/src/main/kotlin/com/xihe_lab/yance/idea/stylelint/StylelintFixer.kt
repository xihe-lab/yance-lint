package com.xihe_lab.yance.idea.stylelint

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.xihe_lab.yance.engine.ExternalToolLocator
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Stylelint 修复器
 * 调用 stylelint --fix 修复文件
 */
class StylelintFixer(private val project: Project) {

    private val logger = Logger.getInstance("YanceLint.StylelintFixer")

    /**
     * 修复指定文件
     * @param filePath 文件绝对路径
     * @return 修复是否成功
     */
    fun fixFile(filePath: String): Boolean {
        val locator = project.getService(ExternalToolLocator::class.java)
        val stylelintPath = locator.locate("stylelint") ?: run {
            logger.warn("Stylelint not found")
            return false
        }

        try {
            val command = listOf(stylelintPath, "--fix", filePath)
            logger.info("Running Stylelint fix on $filePath")

            val process = ProcessBuilder(command)
                .directory(File(project.basePath))
                .redirectErrorStream(true)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exited = process.waitFor(30, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                logger.warn("Stylelint fix process killed after timeout")
                return false
            }

            val exitCode = process.exitValue()
            logger.info("Stylelint fix exit code: $exitCode, output: ${output.take(200)}")

            // exit code 0 = no errors, exit code 1 = errors but fixed
            return exitCode == 0 || exitCode == 1
        } catch (e: Exception) {
            logger.warn("Stylelint fix execution error", e)
            return false
        }
    }
}