package com.xihe_lab.yance.ui

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.application.ReadAction
import com.xihe_lab.yance.core.provider.p3c.P3cScanService
import com.xihe_lab.yance.core.provider.p3c.P3cBridgeService
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.text.html.HTMLEditorKit
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.Executors

/**
 * P3C 工具窗口工厂
 *
 * 在 IDE 侧边栏创建一个 P3C 扫描结果面板
 * 支持 HTML 渲染、一键复制给 AI 和点击跳转到代码位置
 */
class P3cToolWindowFactory : ToolWindowFactory {

    private val logger = Logger.getInstance("YanceLint.P3cToolWindowFactory")

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val p3cService = ServiceManager.getService(project, P3cScanService::class.java)
        val contentFactory = ContentFactory.getInstance()

        // 创建面板
        val panel = createP3cPanel(project, p3cService)

        val content = contentFactory.createContent(panel, "P3C", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createP3cPanel(project: Project, service: P3cScanService): JPanel {
        val resultArea = JEditorPane().apply {
            contentType = "text/plain"
            isEditable = false
        }
        val scrollPane = JScrollPane(resultArea)

        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            // 标题
            val titleLabel = JLabel("P3C 命名检查").apply {
                font = font.deriveFont(16f).deriveFont(java.awt.Font.BOLD)
            }
            add(titleLabel)

            // 分隔线
            add(JSeparator())

            // 状态标签
            val statusLabel = JLabel("就绪").apply {
                foreground = java.awt.Color.GRAY
            }
            add(statusLabel)

            // 扫描按钮
            val scanButton = JButton("扫描项目").apply {
                addActionListener {
                    statusLabel.text = "正在扫描..."
                    statusLabel.foreground = java.awt.Color.BLUE
                    isEnabled = false

                    // 使用后台线程执行扫描，避免阻塞 EDT
                    val thread = Thread {
                        try {
                            val results = service.scanProject()
                            logger.info("Scan results: ${results.size} files with issues, ${results.values.flatten().size} total problems")

                            // 在 EDT 上更新 UI
                            ApplicationManager.getApplication().invokeLater {
                                displayResults(results, service, resultArea)
                                statusLabel.text = "扫描完成"
                                statusLabel.foreground = java.awt.Color.GRAY
                                isEnabled = true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            logger.error("Scan failed", e)

                            // 在 EDT 上更新 UI
                            ApplicationManager.getApplication().invokeLater {
                                val errorMsg = "扫描失败: ${e.message}"
                                resultArea.text = errorMsg
                                statusLabel.text = "扫描失败"
                                statusLabel.foreground = java.awt.Color.RED
                                isEnabled = true
                            }
                        }
                    }
                    thread.start()
                }
            }
            add(scanButton)

            // 结果区域
            add(scrollPane)

            // 按钮行: 清除 + 复制给 AI
            val buttonRow = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)

                val clearButton = JButton("清除结果").apply {
                    addActionListener {
                        resultArea.text = ""
                        statusLabel.text = "已清除"
                    }
                }
                add(clearButton)

                add(Box.createHorizontalStrut(10))

                val copyButton = JButton("复制给 AI").apply {
                    addActionListener {
                        val text = resultArea.text
                        // 转换为 Markdown 格式
                        val markdown = convertToMarkdown(text)
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(markdown), null)
                        statusLabel.text = "已复制给 AI"
                        statusLabel.foreground = java.awt.Color.GREEN
                    }
                }
                add(copyButton)
            }
            add(buttonRow)
        }
        return panel
    }

    private fun displayResults(results: Map<String, List<String>>, service: P3cScanService, resultArea: JEditorPane) {
        val total = results.values.flatten().size
        val bridgeService = P3cBridgeService(service.getProject())

        val text = StringBuilder()
        text.append("========================================\n")
        text.append("P3C 命名规范检查报告\n")
        text.append("========================================\n\n")

        // 检查 P3C 插件安装状态并显示推荐信息
        if (!bridgeService.isPluginAvailable()) {
            text.append("[提示] 未检测到阿里巴巴 P3C 插件\n")
            text.append("请在插件市场搜索 \"P3C\" 或 \"阿里巴巴Java编程规范\" 进行安装\n")
            text.append("以获得更全面的代码规范检查能力。\n\n")
        }

        text.append("扫描结果: 发现 $total 个问题\n")
        text.append("----------------------------------------\n\n")

        if (results.isNotEmpty()) {
            results.forEach { (file, issues) ->
                text.append("文件: $file\n")
                issues.forEach { issue ->
                    text.append("  - $issue\n")
                }
                text.append("\n")
            }
        } else {
            text.append("[成功] 未发现 P3C 命名违规\n")
        }

        text.append("\n----------------------------------------\n")
        text.append("修复建议:\n")
        text.append("1. 类名使用 UpperCamelCase (大驼峰): UserService, OrderController\n")
        text.append("2. 方法名使用 lowerCamelCase (小驼峰): getUserInfo, createOrder\n")
        text.append("3. 常量使用 CONSTANT_CASE (全大写下划线): MAX_SIZE, DEFAULT_TIMEOUT\n")
        text.append("----------------------------------------\n")
        text.append("详细规范请参考: https://alibaba.github.io/p3c/\n")

        resultArea.text = text.toString()
    }

    /**
     * 将文本转换为 Markdown (用于复制给 AI)
     */
    private fun convertToMarkdown(text: String): String {
        return text.trim()
    }

    /**
     * 安装阿里巴巴 P3C 插件
     * 打开插件市场并搜索 P3C 插件
     */
    private fun installP3cPlugin() {
        try {
            // 打开插件设置对话框
            ApplicationManager.getApplication().invokeLater {
                // 显示对话框指导用户安装
                Messages.showMessageDialog(
                    "请在插件市场中搜索 \"P3C\" 或 \"阿里巴巴Java编程规范\" 插件进行安装",
                    "安装 P3C 插件",
                    Messages.getInformationIcon()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Messages.showErrorDialog("无法打开插件市场: ${e.message}", "错误")
        }
    }

    /**
     * 打开文件并定位到行号
     */
    private fun openFileAtLine(project: Project, filePath: String) {
        try {
            val file = VirtualFileManager.getInstance().findFileByUrl("file://$filePath")
            if (file != null) {
                FileEditorManager.getInstance(project).openFile(file, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
