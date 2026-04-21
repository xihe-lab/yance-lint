package com.xihe_lab.yance.idea.lint.review

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.VirtualFile
import com.xihe_lab.yance.idea.lint.ui.ViolationItem
import com.xihe_lab.yance.idea.lint.ui.ViolationListCellRenderer
import com.xihe_lab.yance.service.ViolationCache
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * 代码检视面板
 * 展示变更文件的违规汇总
 */
object ReviewDialog {

    fun show(project: Project, files: List<VirtualFile>) {
        val panel = buildPanel(project, files)

        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, null)
            .setTitle("衍策 — 代码检视报告")
            .setResizable(true)
            .setMovable(true)
            .setMinSize(Dimension(600, 400))
            .setRequestFocus(true)
            .createPopup()

        popup.showInFocusCenter()
    }

    private fun buildPanel(project: Project, files: List<VirtualFile>): JComponent {
        val mainPanel = JPanel(BorderLayout(8, 8))
        mainPanel.border = BorderFactory.createEmptyBorder(8, 12, 8, 12)

        // Summary
        val summaryLabel = JLabel("正在分析 ${files.size} 个文件...")
        summaryLabel.font = summaryLabel.font.deriveFont(Font.BOLD, 13f)
        mainPanel.add(summaryLabel, BorderLayout.NORTH)

        // Violation list
        val model = DefaultListModel<ViolationItem>()
        val list = JList(model).apply {
            cellRenderer = ViolationListCellRenderer()
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        val item = selectedValue ?: return
                        navigateToViolation(project, item)
                    }
                }
            })
        }
        mainPanel.add(JScrollPane(list), BorderLayout.CENTER)

        // Stats bar
        val statsLabel = JLabel(" ")
        statsLabel.foreground = Color.GRAY
        mainPanel.add(statsLabel, BorderLayout.SOUTH)

        // Analyze files in background
        Thread {
            val allViolations = mutableListOf<ViolationItem>()
            val cache = ViolationCache.getInstance(project)
            var errors = 0
            var warnings = 0

            for (file in files) {
                val violations = cache.get(file.path, 0).orEmpty()
                for (v in violations) {
                    val item = ViolationItem(
                        message = v.message,
                        severity = when (v.severity) {
                            ViolationCache.Severity.ERROR -> ViolationItem.Severity.ERROR
                            ViolationCache.Severity.WARNING -> ViolationItem.Severity.WARNING
                            else -> ViolationItem.Severity.INFO
                        },
                        tool = v.tool,
                        filePath = v.filePath,
                        line = v.line,
                        column = v.column,
                        ruleId = v.ruleId
                    )
                    allViolations.add(item)
                    when (item.severity) {
                        ViolationItem.Severity.ERROR -> errors++
                        ViolationItem.Severity.WARNING -> warnings++
                        else -> {}
                    }
                }
            }

            // Sort by severity then file
            allViolations.sortWith(
                compareByDescending<ViolationItem> {
                    when (it.severity) {
                        ViolationItem.Severity.ERROR -> 2
                        ViolationItem.Severity.WARNING -> 1
                        else -> 0
                    }
                }.thenBy { it.filePath }.thenBy { it.line }
            )

            ApplicationManager.getApplication().invokeLater {
                model.clear()
                for (item in allViolations) model.addElement(item)

                val fileCount = allViolations.map { it.filePath }.distinct().size
                summaryLabel.text = "检视完成：${files.size} 个文件，${fileCount} 个存在违规"
                statsLabel.text = "共 ${allViolations.size} 个违规（$errors 错误 / $warnings 警告）"
            }
        }.start()

        return mainPanel
    }

    private fun navigateToViolation(project: Project, item: ViolationItem) {
        val file = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(item.filePath) ?: return
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(project).openFile(file, true)
            val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@invokeLater
            val document = editor.document
            val line = (item.line - 1).coerceIn(0, document.lineCount - 1)
            editor.caretModel.moveToOffset(document.getLineStartOffset(line))
        }
    }
}