# MyAndroidPT 项目保护规则

发生日期：2026-06-09
触发原因：`git reset --hard` 在错误目录执行，误删 77 个归档文件（不可恢复）

## 铁律 1：git 命令只能在项目目录内执行

执行任何 git 命令前，**必须**先确认：

```bash
git rev-parse --show-toplevel
```

正确输出：`D:/Working/Code/Android/MyAndroidPT`

如果输出不是这个路径，立即停止，回到正确目录再操作。

## 铁律 2：删除 / 重置操作必须预览

以下任何操作，必须先加 `-n` 或 `--dry-run` 预览：

| 命令 | 安全模式 |
|------|---------|
| `git rm` | `git rm -n --cached <path>` |
| `git clean` | `git clean -nd` |
| `Remove-Item` (PowerShell) | 先 `Get-ChildItem <pattern>` 确认 |
| `git reset --hard` | 先 `git diff --name-only` 确认 |

确认预览结果无误后，去掉 `-n` 执行。

## 铁律 3：工作产物尽早 commit

截图、报告备份、整理后的归档——完成后立即 `git add + commit`。
git 是最后一道防线，不在 index 里的文件永远恢复不了。

## 恢复检查清单

如果 git status 出现异常文件（不是项目内的），按此顺序排查：
1. `git rev-parse --show-toplevel` — 确认 git 根目录
2. `pwd` — 确认当前目录
3. 不一致 → 先回到正确目录，不执行任何写操作
