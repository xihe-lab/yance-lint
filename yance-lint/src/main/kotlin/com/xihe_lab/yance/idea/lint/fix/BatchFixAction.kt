package com.xihe_lab.yance.idea.lint.fix

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.xihe_lab.yance.idea.eslint.EsLintFixer
import com.xihe_lab.yance.idea.stylelint.StylelintFixer
import com.xihe_lab.yance.service.ViolationCache

/**
 * R13: 批量自动修复 Action
 * Tools → YanceLint → Batch Fix
 * 对选中文件/目录执行批量自动修复
 */
class BatchFixAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        e.presentation.isEnabledAndVisible = project != null && !files.isNullOrEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return
        if (files.isEmpty()) return

        val allFiles = expandFiles(files.toList())
        val fixableFiles = allFiles.filter { isFixable(it) }

        if (fixableFiles.isEmpty()) {
            notify(project, 0, 0, "没有可修复的文件")
            return
        }

        Thread {
            var fixed = 0
            var failed = 0
            val cache = ViolationCache.getInstance(project)

            for (file in fixableFiles) {
                val success = fixFile(project, file)
                if (success) {
                    fixed++
                    cache.invalidate(file.path)
                } else {
                    failed++
                }
            }

            ApplicationManager.getApplication().invokeLater {
                // Refresh files
                VirtualFileManager.getInstance().syncRefresh()
                notify(project, fixed, failed, null)
            }
        }.start()
    }

    private fun expandFiles(files: List<VirtualFile>): List<VirtualFile> {
        val result = mutableListOf<VirtualFile>()
        for (file in files) {
            if (file.isDirectory) {
                for (child in file.children) {
                    result.addAll(expandFiles(listOf(child)))
                }
            } else {
                result.add(file)
            }
        }
        return result
    }

    private fun isFixable(file: VirtualFile): Boolean {
        val ext = file.extension?.lowercase() ?: return false
        return ext in setOf("js", "jsx", "ts", "tsx", "vue", "css", "scss", "less", "sass")
    }

    private fun fixFile(project: Project, file: VirtualFile): Boolean {
        val ext = file.extension?.lowercase() ?: return false
        return when (ext) {
            "js", "jsx", "ts", "tsx", "vue" -> {
                try {
                    val fixer = EsLintFixer(project)
                    fixer.fixFile(file.path)
                } catch (_: Throwable) {
                    false
                }
            }
            "css", "scss", "less", "sass" -> {
                try {
                    val fixer = StylelintFixer(project)
                    fixer.fixFile(file.path)
                } catch (_: Throwable) {
                    false
                }
            }
            else -> false
        }
    }

    private fun notify(project: Project, fixed: Int, failed: Int, message: String?) {
        val group = NotificationGroupManager.getInstance()
            .getNotificationGroup("YanceLint Notifications")

        if (message != null) {
            group.createNotification("YanceLint 批量修复", message, NotificationType.INFORMATION)
                .notify(project)
        } else {
            val text = buildString {
                append("修复完成：$fixed 个文件已修复")
                if (failed > 0) append("，$failed 个文件修复失败")
            }
            val type = if (failed > 0) NotificationType.WARNING else NotificationType.INFORMATION
            group.createNotification("YanceLint 批量修复", text, type)
                .notify(project)
        }
    }
}