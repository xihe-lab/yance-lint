package com.xihe_lab.yance.idea.lint.action

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

class YanceLintScanAction : AnAction() {

    companion object {
        private const val TOOL_WINDOW_ID = "YanceLint"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        activateToolWindow(project)

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
            notifyResult(project, finalTotal, counts)
        }.start()
    }

    private fun activateToolWindow(project: Project) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID)
        toolWindow?.activate(null)
    }

    private fun notifyResult(project: Project, total: Int, counts: Map<String, Int>) {
        val groupManager = NotificationGroupManager.getInstance()
        val group = groupManager.getNotificationGroup("YanceLint Notifications")

        val notification = if (total == 0) {
            group.createNotification(
                "YanceLint 扫描完成",
                "无规约违规",
                NotificationType.INFORMATION
            )
        } else {
            val details = counts.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }
            group.createNotification(
                "YanceLint 扫描完成",
                "发现 $total 个规约违规\n$details",
                NotificationType.WARNING
            )
        }
        notification.notify(project)
    }
}
