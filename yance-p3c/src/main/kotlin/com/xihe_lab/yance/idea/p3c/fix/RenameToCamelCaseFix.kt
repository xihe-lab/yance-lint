package com.xihe_lab.yance.idea.p3c.fix

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.refactoring.rename.RenameProcessor

/**
 * 重命名为 CamelCase QuickFix
 * 修复 P3C 命名违规
 */
class RenameToCamelCaseFix(
    private val targetElement: PsiElement,
    private val suggestedName: String,
    private val caseType: CaseType
) : LocalQuickFix, HighPriorityAction {

    enum class CaseType {
        UPPER_CAMEL,  // 类名
        LOWER_CAMEL,  // 方法名/变量名
        CONSTANT      // 常量名
    }

    override fun getFamilyName(): String = "P3C Naming Fix"

    override fun getName(): String = "Rename to ${formatName(suggestedName, caseType)}"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: targetElement
        val newName = formatName(element.name ?: suggestedName, caseType)

        RenameProcessor(project, element, newName, false, false).run()
    }

    private fun formatName(name: String, type: CaseType): String {
        return when (type) {
            CaseType.UPPER_CAMEL -> name.replaceFirstChar { it.uppercase() }
                .replace("_", "")
                .split("_")
                .joinToString("") { it.replaceFirstChar { it.uppercase() } }

            CaseType.LOWER_CAMEL -> name.replaceFirstChar { it.lowercase() }
                .split("_")
                .let { parts ->
                    parts.first().lowercase() + parts.drop(1).joinToString("") {
                        it.replaceFirstChar { it.uppercase() }
                    }
                }

            CaseType.CONSTANT -> name.uppercase()
                .replace(" ", "_")
                .replace("-", "_")
        }
    }

    private val PsiElement.name: String?
        get() = when (this) {
            is PsiClass -> this.name
            is PsiMethod -> this.name
            else -> null
        }
}