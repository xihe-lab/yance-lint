plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellijPlatform)
}

group = "com.xihe-lab.yance"
version = "1.0.0-SNAPSHOT"

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("JavaScript")
        bundledPlugin("org.intellij.plugins.markdown")
//        plugin("com.alibaba.p3c.xenoamess")
    }

    // Test dependencies - use only JUnit 5 without IntelliJ Platform test framework
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
}

intellijPlatform {
    pluginConfiguration {
        id = "com.xihe-lab.yance.yance-lint"
        name = "YanceLint"
        version = "1.0.0-SNAPSHOT"
        description = """
            Enterprise code convention and review plugin for IntelliJ IDEA.<br>
            <br>
            Features:<br>
            - Unified code convention checks (P3C, ESLint, Stylelint, Checkstyle)<br>
            - Real-time code review and automatic fixes<br>
            - Quality metrics and dashboard<br>
            - CI/CD integration support
            """.trimIndent()

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }

        changeNotes = """
            <ul>
                <li>Phase 1: Core model layer with P3C naming rules support</li>
                <li>Rule engine with extensible RuleProvider architecture</li>
                <li>PSI-based inspection with auto-fix support</li>
                <li>Tool Window for violation display</li>
                <li>Settings page for rule configuration</li>
            </ul>
        """.trimIndent()
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    // Disable the default IntelliJ Platform test task by name
    named<Test>("test") {
        enabled = false
    }

    // Register test task for JUnit 5
    register<Test>("junitTest") {
        group = "verification"
        description = "Run JUnit 5 tests"

        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        systemProperty("idea.testsandbox.disabled", "true")
    }.configure {
        // Get the Kotlin test output directory - it should exist after compileTestKotlin
        val testKotlinOutput = File(buildDir, "classes/kotlin/test")
        val testResources = File(buildDir, "resources/test")
        val runtimeClasspath = configurations.testRuntimeClasspath.get().files

        // Build the classpath - convert to FileCollection
        val allFiles = mutableListOf<File>()
        allFiles.add(testKotlinOutput)
        allFiles.add(testResources)
        allFiles.addAll(runtimeClasspath)

        // Use the correct property name
        classpath = files(allFiles)
    }
}
