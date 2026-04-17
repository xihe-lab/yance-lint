package com.xihe_lab.yance.core.provider.p3c

import com.intellij.openapi.project.Project
import com.xihe_lab.yance.core.engine.RuleProvider
import com.xihe_lab.yance.core.model.LanguageType

/**
 * P3cRuleProvider
 *
 * 提供阿里巴巴 Java 开发手册相关规则元数据。
 * 支持两种路径:
 * - 路径 A (默认): 可选依赖官方 Alibaba Java Coding Guidelines 插件，复用其 Inspection 实现
 * - 路径 B (按需): 自研核心 Inspection 实现，用于企业级深度定制
 */
class P3cRuleProvider : RuleProvider {
    override val source: String = "p3c"

    override fun isAvailable(project: Project?): Boolean = true

    override fun getActiveRules(project: Project?, language: LanguageType): List<com.xihe_lab.yance.core.model.YanceRule> {
        return P3cRuleMetadata.getAllRules().filter { it.language == language || it.language == com.xihe_lab.yance.core.model.LanguageType.ALL }
    }

    override fun provideRules(): List<com.xihe_lab.yance.core.model.YanceRule> {
        return P3cRuleMetadata.getAllRules()
    }
}
