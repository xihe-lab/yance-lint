package com.xihe_lab.yance.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiManager
import com.xihe_lab.yance.core.engine.InspectionContext
import com.xihe_lab.yance.core.provider.p3c.P3cInspection
import com.xihe_lab.yance.core.provider.p3c.P3cReportGenerator

/**
 * P3C 扫描 Action
 *
 * 用户手动触发后，扫描当前项目中的 Java 文件，检查 P3C 命名规范问题，
 * 生成报告并输出到控制台和消息工具窗口。
 */
class P3cScanAction : AnAction() {

    private val logger = Logger.getInstance(P3cScanAction::class.java)

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getProject() ?: return

        // 获取当前打开的文件
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: run {
            logger.warn("No virtual file found, will scan all project files")
            scanAllProjects(project)
            return
        }

        logger.info("Scanning file: ${virtualFile.path}")

        // 收集所有问题
        val allProblems = mutableMapOf<String, List<String>>()
        val inspection = P3cInspection()

        // 扫描当前文件
        val psiManager = PsiManager.getInstance(project)
        val problems = scanFile(project, virtualFile, inspection, psiManager)
        if (problems.isNotEmpty()) {
            allProblems[virtualFile.path] = problems
        }

        // 如果没有当前文件，扫描整个项目
        if (allProblems.isEmpty()) {
            logger.info("No problems in current file, scanning all project files...")
            scanAllProjects(project, allProblems, inspection, psiManager)
        }

        // 生成报告
        val firstFilePath = allProblems.keys.firstOrNull() ?: virtualFile.path
        val firstFile = psiManager.findFile(virtualFile)?.virtualFile ?: return
        val context = InspectionContext(
            file = psiManager.findFile(firstFile) ?: return,
            project = project,
            scanScope = if (allProblems.size > 1) "project" else "current file"
        )
        val reportGenerator = P3cReportGenerator()
        val report = reportGenerator.generateReport(context, allProblems)

        // 输出到控制台
        printReportToConsole(report)

        // 显示结果
        showResults(project, allProblems)
    }

    private fun scanAllProjects(project: Project, allProblems: MutableMap<String, List<String>> = mutableMapOf(), inspection: P3cInspection = P3cInspection(), psiManager: PsiManager = PsiManager.getInstance(project)) {
        val virtualFiles = getAllJavaFiles(project)
        logger.info("Found ${virtualFiles.size} Java files to scan")

        virtualFiles.forEach { virtualFile ->
            val problems = scanFile(project, virtualFile, inspection, psiManager)
            if (problems.isNotEmpty()) {
                allProblems[virtualFile.path] = problems
            }
        }
    }

    private fun scanAllProjects(project: Project) {
        val virtualFiles = getAllJavaFiles(project)
        logger.info("Found ${virtualFiles.size} Java files to scan")

        val allProblems = mutableMapOf<String, List<String>>()
        val inspection = P3cInspection()
        val psiManager = PsiManager.getInstance(project)

        virtualFiles.forEach { virtualFile ->
            val problems = scanFile(project, virtualFile, inspection, psiManager)
            if (problems.isNotEmpty()) {
                allProblems[virtualFile.path] = problems
            }
        }

        // 生成报告
        val firstFilePath = allProblems.keys.firstOrNull() ?: return
        val file = virtualFiles.firstOrNull() ?: return
        val firstPsiFile = psiManager.findFile(file) ?: return
        val context = InspectionContext(
            file = firstPsiFile,
            project = project,
            scanScope = "project"
        )
        val reportGenerator = P3cReportGenerator()
        val report = reportGenerator.generateReport(context, allProblems)

        // 输出到控制台
        printReportToConsole(report)

        // 显示结果
        showResults(project, allProblems)
    }

    private fun getAllJavaFiles(project: Project): List<VirtualFile> {
        val result = mutableListOf<VirtualFile>()
        val baseDir = project.baseDir ?: return result
        logger.info("Base dir: ${baseDir.path}")
        collectFilesRecursively(baseDir, result)
        logger.info("Total Java files found: ${result.size}")
        return result
    }

    private fun collectFilesRecursively(file: VirtualFile, collection: MutableList<VirtualFile>) {
        if (file.isDirectory) {
            file.children.forEach { child ->
                collectFilesRecursively(child, collection)
            }
        } else if (file.extension == "java") {
            collection.add(file)
            logger.debug("  Found Java file: ${file.path}")
        }
    }

    private fun scanFile(
        project: Project,
        virtualFile: VirtualFile,
        inspection: P3cInspection,
        psiManager: PsiManager
    ): List<String> {
        val psiFile = psiManager.findFile(virtualFile) ?: return emptyList()
        logger.info("Scanning file: ${virtualFile.name}")

        if (psiFile !is com.intellij.psi.PsiJavaFile) {
            logger.debug("Not a Java file: ${virtualFile.name}")
            return emptyList()
        }

        val problems = mutableListOf<String>()

        // 直接遍历类、方法、字段
        psiFile.classes.forEach { psiClass ->
            logger.debug("  Found class: ${psiClass.name}")

            // 检查类名
            val classProblem = inspection.checkClassNamePublic(psiClass)
            if (classProblem != null) {
                problems.add(classProblem)
            }

            // 遍历方法
            psiClass.methods.forEach { method ->
                logger.debug("    Found method: ${method.name}")
                val methodProblem = inspection.checkMethodNamePublic(method)
                if (methodProblem != null) {
                    problems.add(methodProblem)
                }
            }

            // 遍历字段
            psiClass.fields.forEach { field ->
                logger.debug("    Found field: ${field.name}")
                val fieldProblem = inspection.checkConstantNamePublic(field)
                if (fieldProblem != null) {
                    problems.add(fieldProblem)
                }
            }
        }

        logger.info("  Found ${problems.size} problems in ${virtualFile.name}")
        return problems
    }

    private fun printReportToConsole(report: String) {
        println("=== P3C Inspection Report ===")
        println(report)
        println("=== End of Report ===")
    }

    private fun showResults(
        project: Project,
        problems: Map<String, List<String>>
    ) {
        val total = problems.values.flatten().size

        if (total == 0) {
            com.intellij.openapi.ui.Messages.showInfoMessage(
                project,
                "未发现 P3C 命名违规",
                "P3C 扫描结果"
            )
        } else {
            com.intellij.openapi.ui.Messages.showWarningDialog(
                project,
                "发现 $total 个 P3C 命名违规\n\n请查看控制台输出完整报告",
                "P3C 扫描完成"
            )
        }
    }
}
