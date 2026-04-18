# YanceLint 项目规范

## 构建命令

```bash
gradle :yance-lint:compileKotlin    # 编译
gradle :yance-lint:buildPlugin       # 构建插件
gradle :yance-lint:runIde            # 启动沙箱 IDE 测试
```

## 代码规约检查

每次编辑或创建 Java/JavaScript/TypeScript/CSS/SCSS/LESS 文件后，**必须**调用 `get_file_violations` 工具检查该文件是否有规约违规。如果发现违规，主动修复后再完成。

调用示例：
- 工具：`get_file_violations`
- 参数：`{ "file_path": "文件的绝对路径" }`

如果工具返回违规列表，根据违规信息修复代码，然后再次检查直到无违规。
