package com.xihe_lab.yance.idea.p3c.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*
import com.xihe_lab.yance.idea.p3c.inspection.P3cProblemHelper.register

/**
 * 线程池不允许使用 Executors 去创建，应通过 ThreadPoolExecutor 创建
 * 避免 OOM 风险
 */
class AvoidManuallyCreateThreadPoolInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                val methodExpression = expression.methodExpression
                val qualifier = methodExpression.qualifierExpression as? PsiReferenceExpression ?: return
                val qualifierClass = qualifier.resolve() as? PsiClass ?: return

                if (qualifierClass.qualifiedName != "java.util.concurrent.Executors") return

                val methodName = methodExpression.referenceName ?: return
                val unsafeMethods = setOf(
                    "newFixedThreadPool", "newSingleThreadExecutor",
                    "newCachedThreadPool", "newScheduledThreadPool",
                    "newSingleThreadScheduledExecutor"
                )
                if (methodName in unsafeMethods) {
                    register(
                        holder,
                        expression,
                        "不要使用 Executors 创建线程池，应通过 ThreadPoolExecutor 方式创建，明确线程池参数"
                    )
                }
            }
        }
    }
}