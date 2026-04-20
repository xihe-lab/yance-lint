package com.xihe_lab.yance.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * QuickFix 注册表
 * 管理可用的 QuickFix，按 tool + ruleId 查找
 */
@Service(Service.Level.PROJECT)
class QuickFixRegistry(private val project: Project) {

    data class QuickFixInfo(
        val description: String,
        val fixType: FixType,
        val tool: String,
        val ruleId: String?,
        val fixer: (Project, String, Int) -> Boolean  // (project, filePath, line) -> success
    )

    enum class FixType {
        PSI_LOCAL_FIX,    // IntelliJ PSI QuickFix (Alt+Enter)
        EXTERNAL_TOOL_FIX // 外部工具 --fix
    }

    private val registry = mutableListOf<QuickFixInfo>()

    fun register(info: QuickFixInfo) {
        registry.add(info)
    }

    fun lookup(tool: String, ruleId: String?): List<QuickFixInfo> {
        return registry.filter { info ->
            info.tool == tool && (info.ruleId == null || info.ruleId == ruleId)
        }
    }

    fun lookupAll(): List<QuickFixInfo> = registry.toList()

    companion object {
        @JvmStatic
        fun getInstance(project: Project): QuickFixRegistry = project.service()
    }
}