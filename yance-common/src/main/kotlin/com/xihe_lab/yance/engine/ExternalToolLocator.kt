package com.xihe_lab.yance.engine

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class ExternalToolLocator(private val project: Project) {

    private val cache = ConcurrentHashMap<String, CachedEntry>()

    data class CachedEntry(
        val path: String,
        val timestamp: Long,
        val configModified: Long
    )

    fun locate(toolName: String): String? {
        val cached = cache[toolName]
        if (cached != null && !isExpired(cached)) {
            if (File(cached.path).exists()) return cached.path
            cache.remove(toolName)
        }

        val basePath = project.basePath ?: return null

        // 1. 递归向上查找 node_modules/.bin/<tool>
        val localTool = findLocalTool(toolName, basePath)
        if (localTool != null) {
            cache[toolName] = CachedEntry(localTool, System.currentTimeMillis(), 0)
            return localTool
        }

        // 2. 全局 npx 回退
        val npxPath = findGlobalTool(toolName)
        if (npxPath != null) {
            cache[toolName] = CachedEntry(npxPath, System.currentTimeMillis(), 0)
            return npxPath
        }

        return null
    }

    fun clearCache() {
        cache.clear()
    }

    fun locateNode(): String? {
        val cached = cache["node"]
        if (cached != null && !isExpired(cached)) {
            if (File(cached.path).exists()) return cached.path
            cache.remove("node")
        }

        // 1. Check current PATH
        val pathNode = findGlobalTool("node")
        if (pathNode != null) {
            cache["node"] = CachedEntry(pathNode, System.currentTimeMillis(), 0)
            return pathNode
        }

        // 2. Common node locations (nvm, fnm, volta, homebrew)
        val home = System.getProperty("user.home")
        val candidates = mutableListOf<String>()

        // nvm — pick latest version directory
        val nvmDir = File(home, ".nvm/versions/node")
        if (nvmDir.isDirectory) {
            nvmDir.listFiles()?.filter { it.isDirectory }?.maxByOrNull { it.name }?.let {
                candidates.add(File(it, "bin/node").absolutePath)
            }
        }

        // fnm
        val fnmMultishell = System.getenv("FNM_MULTISHELL_PATH")
        if (fnmMultishell != null) {
            candidates.add(File(fnmMultishell, "node").absolutePath)
        }
        val fnmPath = System.getenv("FNM_PATH")
        if (fnmPath != null) {
            File(fnmPath).listFiles()?.filter { it.isDirectory }?.maxByOrNull { it.name }?.let {
                candidates.add(File(it, "installation/bin/node").absolutePath)
            }
        }

        // volta
        val voltaHome = System.getenv("VOLTA_HOME") ?: "$home/.volta"
        candidates.add("$voltaHome/bin/node")

        // homebrew (macOS)
        candidates.add("/usr/local/bin/node")
        candidates.add("/opt/homebrew/bin/node")

        for (candidate in candidates) {
            val f = File(candidate)
            if (f.exists() && f.canExecute()) {
                cache["node"] = CachedEntry(f.absolutePath, System.currentTimeMillis(), 0)
                return f.absolutePath
            }
        }

        return null
    }

    private fun findLocalTool(toolName: String, basePath: String): String? {
        var dir = File(basePath)
        val toolNames = listOf(toolName, "$toolName.cmd", "$toolName.ps1")

        while (dir != null && dir.exists()) {
            for (name in toolNames) {
                val candidate = File(dir, "node_modules/.bin/$name")
                if (candidate.exists()) return candidate.absolutePath
            }
            dir = dir.parentFile
        }
        return null
    }

    private fun findGlobalTool(toolName: String): String? {
        val path = System.getenv("PATH") ?: return null
        for (dir in path.split(File.pathSeparator)) {
            val candidate = File(dir, toolName)
            if (candidate.exists() && candidate.canExecute()) {
                return candidate.absolutePath
            }
        }
        return null
    }

    private fun isExpired(entry: CachedEntry): Boolean {
        val age = System.currentTimeMillis() - entry.timestamp
        return age > CACHE_TTL_MS
    }

    companion object {
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24h
    }
}
