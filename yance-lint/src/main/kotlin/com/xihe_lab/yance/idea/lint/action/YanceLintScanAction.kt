package com.xihe_lab.yance.idea.lint.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

class YanceLintScanAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        Thread {
            var total = 0
            val counts = mutableMapOf<String, Int>()

            val scannerClasses = listOf(
                "P3C" to "com.xihe_lab.yance.idea.p3c.service.P3cScanService",
                "ESLint" to "com.xihe_lab.yance.idea.eslint.EsLintRunner",
                "Stylelint" to "com.xihe_lab.yance.idea.stylelint.StylelintRunner",
                "Checkstyle" to "com.xihe_lab.yance.idea.checkstyle.CheckstyleRunner"
            )

            for ((name, className) in scannerClasses) {
                try {
                    val clazz = Class.forName(className, false, javaClass.classLoader)
                    val instance = clazz.getConstructor(Project::class.java).newInstance(project)
                    val method = clazz.getMethod("scanProject")
                    @Suppress("UNCHECKED_CAST")
                    val results = method.invoke(instance) as? Map<String, List<*>> ?: emptyMap()
                    val count = results.values.sumOf { it.size }
                    total += count
                    counts[name] = count
                } catch (_: Throwable) {
                    counts[name] = 0
                }
            }

            val finalTotal = total
            ApplicationManager.getApplication().invokeLater {
                if (finalTotal == 0) {
                    com.intellij.openapi.ui.Messages.showInfoMessage(project, "未发现规约违规", "YanceLint 扫描结果")
                } else {
                    val details = counts.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }
                    com.intellij.openapi.ui.Messages.showWarningDialog(
                        project,
                        "发现 $finalTotal 个规约违规\n$details\n\n请在 YanceLint 工具窗口查看详情",
                        "YanceLint 扫描完成"
                    )
                }
            }
        }.start()
    }
}
