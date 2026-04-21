package com.xihe_lab.yance.idea.p3c.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*
import com.xihe_lab.yance.idea.p3c.inspection.P3cProblemHelper.register

/**
 * 不要用 new Thread()，应使用线程池
 */
class ThreadPoolCreationInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitNewExpression(expression: PsiNewExpression) {
                super.visitNewExpression(expression)
                val className = expression.classReference?.qualifiedName ?: return
                if (className == "Thread") {
                    register(holder, expression, "不要显式创建线程，应使用线程池（ExecutorService / ThreadPoolExecutor）")
                }
            }
        }
    }
}