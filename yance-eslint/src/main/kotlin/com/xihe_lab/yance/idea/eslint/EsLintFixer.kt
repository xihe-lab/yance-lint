package com.xihe_lab.yance.idea.eslint

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.xihe_lab.yance.engine.ExternalToolLocator
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * ESLint 修复器
 * 调用 eslint --fix 修复文件
 */
class EsLintFixer(private val project: Project) {

    private val logger = Logger.getInstance("YanceLint.EsLintFixer")

    /**
     * 修复指定文件
     * @param filePath 文件绝对路径
     * @return 修复是否成功
     */
    fun fixFile(filePath: String): Boolean {
        val locator = project.getService(ExternalToolLocator::class.java)
        val eslintPath = locator.locate("eslint") ?: run {
            logger.warn("ESLint not found")
            return false
        }

        try {
            val command = listOf(eslintPath, "--fix", filePath)
            logger.info("Running ESLint fix on $filePath")

            val pb = ProcessBuilder(command)
                .directory(File(project.basePath))
                .redirectErrorStream(true)

            // Ensure node is on PATH (IDE process may lack nvm/fnm paths)
            locator.locateNode()?.let { nodePath ->
                File(nodePath).parentFile?.absolutePath?.let { nodeDir ->
                    val env = pb.environment()
                    val currentPath = env["PATH"] ?: System.getenv("PATH") ?: ""
                    if (!currentPath.split(File.pathSeparator).contains(nodeDir)) {
                        env["PATH"] = "$nodeDir${File.pathSeparator}$currentPath"
                    }
                }
            }

            val process = pb.start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exited = process.waitFor(30, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                logger.warn("ESLint fix process killed after timeout")
                return false
            }

            val exitCode = process.exitValue()
            logger.info("ESLint fix exit code: $exitCode, output: ${output.take(200)}")

            // exit code 0 = no errors, exit code 1 = errors but fixed
            return exitCode == 0 || exitCode == 1
        } catch (e: Exception) {
            logger.warn("ESLint fix execution error", e)
            return false
        }
    }
}