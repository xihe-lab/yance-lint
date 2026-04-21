package com.xihe_lab.yance.idea.p3c.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*
import com.xihe_lab.yance.idea.p3c.inspection.P3cProblemHelper.register

/**
 * 避免捕获 NullPointerException
 */
class AvoidCatchNullPointerExceptionInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitTryStatement(statement: PsiTryStatement) {
                super.visitTryStatement(statement)
                for (section in statement.catchSections) {
                    val catchType = section.catchType as? PsiClassType ?: continue
                    val className = catchType.resolve()?.qualifiedName ?: continue

                    if (className == "java.lang.NullPointerException") {
                        register(
                            holder,
                            section.firstChild,
                            "不要捕获 NullPointerException，应通过前置判空避免 NPE"
                        )
                    }
                }
            }
        }
    }
}