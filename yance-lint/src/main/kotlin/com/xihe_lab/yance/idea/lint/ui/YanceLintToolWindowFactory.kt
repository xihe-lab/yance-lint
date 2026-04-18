package com.xihe_lab.yance.idea.lint.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.*
import java.awt.datatransfer.StringSelection
import javax.swing.*

class YanceLintToolWindowFactory : ToolWindowFactory {

    private val logger = Logger.getInstance("YanceLint.YanceLintToolWindowFactory")

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val panel = createPanel(project)
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private data class ToolDescriptor(
        val name: String,
        val scannerClass: String,
        val resultClass: String,
        val fields: List<String>
    )

    private val toolDescriptors = listOf(
        ToolDescriptor("P3C",
            "com.xihe_lab.yance.idea.p3c.service.P3cScanService",
            "java.lang.String",
            listOf("message")),
        ToolDescriptor("ESLint",
            "com.xihe_lab.yance.idea.eslint.EsLintRunner",
            "com.xihe_lab.yance.idea.eslint.EsLintRunner\$EsLintMessage",
            listOf("severity", "line", "column", "message", "ruleId")),
        ToolDescriptor("Stylelint",
            "com.xihe_lab.yance.idea.stylelint.StylelintRunner",
            "com.xihe_lab.yance.idea.stylelint.StylelintRunner\$StylelintMessage",
            listOf("severity", "line", "column", "text", "rule")),
        ToolDescriptor("Checkstyle",
            "com.xihe_lab.yance.idea.checkstyle.CheckstyleRunner",
            "com.xihe_lab.yance.idea.checkstyle.CheckstyleRunner\$CheckstyleViolation",
            listOf("severity", "line", "column", "message", "source"))
    )

    private fun createPanel(project: Project): JPanel {
        val tabbedPane = JTabbedPane()
        val resultPanes = mutableMapOf<String, JEditorPane>()

        for (tool in toolDescriptors) {
            val available = try {
                Class.forName(tool.scannerClass)
                true
            } catch (_: ClassNotFoundException) {
                false
            }

            val pane = JEditorPane().apply {
                contentType = "text/plain"
                isEditable = false
                text = if (available) "点击「全部扫描」开始检查" else "当前 IDE 不支持此工具"
            }
            resultPanes[tool.name] = pane
            tabbedPane.addTab(tool.name, JScrollPane(pane))
        }

        val statusLabel = JLabel("就绪").apply { foreground = Color.GRAY }
        val scanButton = JButton("全部扫描")
        val clearButton = JButton("清除")
        val copyButton = JButton("复制报告")

        scanButton.addActionListener {
            statusLabel.text = "正在扫描..."
            statusLabel.foreground = Color.BLUE
            scanButton.isEnabled = false

            Thread {
                var totalIssues = 0

                for (tool in toolDescriptors) {
                    try {
                        val scannerClazz = Class.forName(tool.scannerClass)
                        val instance = scannerClazz.getConstructor(Project::class.java).newInstance(project)

                        @Suppress("UNCHECKED_CAST")
                        val results: Map<String, List<Any>> = try {
                            val scanMethod = scannerClazz.getMethod("scanProject")
                            scanMethod.invoke(instance) as Map<String, List<Any>>
                        } catch (_: Exception) {
                            // P3C uses ServiceManager, try getService approach
                            try {
                                val serviceClazz = Class.forName("com.intellij.openapi.components.ServiceManager")
                                val getService = serviceClazz.getMethod("getService", Project::class.java, Class::class.java)
                                val service = getService.invoke(null, project, scannerClazz)
                                val scanMethod = scannerClazz.getMethod("scanProject")
                                scanMethod.invoke(service) as Map<String, List<Any>>
                            } catch (_: Exception) {
                                emptyMap()
                            }
                        }

                        val count = results.values.sumOf { it.size }
                        totalIssues += count

                        val report = formatReport(tool.name, results, tool.fields)
                        ApplicationManager.getApplication().invokeLater {
                            resultPanes[tool.name]?.text = report
                        }
                    } catch (e: Exception) {
                        logger.warn("${tool.name} scan failed", e)
                        ApplicationManager.getApplication().invokeLater {
                            resultPanes[tool.name]?.text = "扫描失败: ${e.message}"
                        }
                    }
                }

                val finalTotal = totalIssues
                ApplicationManager.getApplication().invokeLater {
                    statusLabel.text = if (finalTotal == 0) "扫描完成：未发现问题" else "扫描完成：发现 $finalTotal 个问题"
                    statusLabel.foreground = if (finalTotal == 0) Color(0, 153, 0) else Color.RED
                    scanButton.isEnabled = true
                }
            }.start()
        }

        clearButton.addActionListener {
            for (pane in resultPanes.values) pane.text = ""
            statusLabel.text = "已清除"
            statusLabel.foreground = Color.GRAY
        }

        copyButton.addActionListener {
            val idx = tabbedPane.selectedIndex
            val title = tabbedPane.getTitleAt(idx)
            val text = resultPanes[title]?.text?.trim() ?: ""
            if (text.isNotEmpty()) {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
                statusLabel.text = "已复制 $title 报告"
                statusLabel.foreground = Color(0, 153, 0)
            }
        }

        val toolBar = JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT)
            add(scanButton)
            add(clearButton)
            add(copyButton)
            add(Box.createHorizontalStrut(20))
            add(statusLabel)
        }

        return JPanel().apply {
            layout = BorderLayout()
            add(toolBar, BorderLayout.NORTH)
            add(tabbedPane, BorderLayout.CENTER)
        }
    }

    private fun formatReport(toolName: String, results: Map<String, List<Any>>, fields: List<String>): String {
        val sb = StringBuilder()
        val total = results.values.sumOf { it.size }
        sb.appendLine("$toolName 检查报告")
        sb.appendLine("=".repeat(40))
        sb.appendLine("扫描结果: 发现 $total 个问题（涉及 ${results.size} 个文件）")
        sb.appendLine("-".repeat(40))
        if (results.isEmpty()) {
            sb.appendLine("\n[通过] 未发现 $toolName 问题")
        } else {
            results.forEach { (file, items) ->
                sb.appendLine("\n文件: $file")
                for (item in items) {
                    val line = formatItem(item, fields)
                    sb.appendLine("  $line")
                }
            }
        }
        return sb.toString()
    }

    private fun formatItem(item: Any, fields: List<String>): String {
        val clazz = item.javaClass
        val parts = mutableListOf<String>()
        for (field in fields) {
            try {
                val prop = clazz.getMethod("get${field.replaceFirstChar { it.uppercase() }}")
                val value = prop.invoke(item) ?: continue
                parts.add("$field=$value")
            } catch (_: Exception) {
                // try direct field access for data class
                try {
                    val f = clazz.getDeclaredField(field)
                    f.isAccessible = true
                    val value = f.get(item) ?: continue
                    parts.add("$field=$value")
                } catch (_: Exception) {}
            }
        }
        return parts.joinToString(", ")
    }
}
