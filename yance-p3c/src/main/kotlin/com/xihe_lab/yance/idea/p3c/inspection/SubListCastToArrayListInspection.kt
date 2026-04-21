package com.xihe_lab.yance.idea.p3c.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*
import com.xihe_lab.yance.idea.p3c.inspection.P3cProblemHelper.register

/**
 * ArrayList 的 subList 结果不可强转成 ArrayList
 */
class SubListCastToArrayListInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitTypeCastExpression(expression: PsiTypeCastExpression) {
                super.visitTypeCastExpression(expression)
                val castTypeText = expression.castType?.text ?: return
                if (castTypeText != "ArrayList") return

                val operand = expression.operand as? PsiMethodCallExpression ?: return
                val methodName = operand.methodExpression.referenceName ?: return
                if (methodName != "subList") return

                register(holder, expression, "subList() 返回值不能强转成 ArrayList，否则会抛出 ClassCastException")
            }
        }
    }
}