plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellijPlatform)
}

group = "com.xihe-lab.yance"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":yance-common"))
    implementation(project(":yance-idea"))
    implementation(project(":yance-p3c"))
    implementation(project(":yance-eslint"))
    implementation(project(":yance-stylelint"))
    implementation(project(":yance-checkstyle"))

    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledPlugin("org.jetbrains.kotlin")
//        bundledPlugin("JavaScript")
        bundledPlugin("org.intellij.plugins.markdown")
//        bundledPlugin("com.intellij.css")
//        bundledPlugin("com.intellij.react")
//        bundledPlugin("org.jetbrains.plugins.less")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
}

intellijPlatform {
    pluginConfiguration {
        id = "com.xihe-lab.yance.yance-lint"
        name = "YanceLint"
        version = "1.0.0-SNAPSHOT"
        description = """
            YanceLint — 企业级代码规约检查插件

            支持四种规约引擎：P3C（阿里巴巴 Java 开发手册）、ESLint、Stylelint、Checkstyle。
            通过 MCP Server 让 AI（Claude Code）实时感知代码规约违规。

            核心功能：
            - 多工具统一面板：P3C、ESLint、Stylelint、Checkstyle 扫描结果集中展示
            - 实时检查：编辑时自动触发规约检查，编辑器高亮违规
            - 项目级扫描：一键扫描整个项目（Shift+Alt+P）
            - AI 集成（MCP）：内置 HTTP Server，让 AI 实时获取规约违规并自动修复
            - 跨平台支持：IntelliJ IDEA（Java）和 WebStorm（JS/CSS）
            """.trimIndent()

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }

        changeNotes = """
            1.0.0 版本：
            - 多工具统一面板：P3C、ESLint、Stylelint、Checkstyle 扫描结果集中展示
            - 结构化违规列表：左侧严重等级色条 + 双击跳转代码位置
            - 项目级扫描：Tools → Scan YanceLint Rules（Shift+Alt+P）
            - 气球通知：扫描完成后右下角通知，不再阻断式弹窗
            - 品牌图标：墨律蓝 + 衍策金司南设计
            - MCP Server：内置 HTTP Server（端口 63742），支持 AI 实时获取规约违规
            - WebStorm 支持：Java 模块可选加载，ESLint/Stylelint 在 WebStorm 可用
            """.trimIndent()
    }
}

tasks {
    named<Test>("test") {
        enabled = false
    }

    register<Test>("junitTest") {
        group = "verification"
        description = "Run JUnit 5 tests"
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        systemProperty("idea.testsandbox.disabled", "true")
    }
}
