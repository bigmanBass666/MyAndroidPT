# MyAndroidPT 项目文件结构整理计划

> 讨论结果 — 经确认后执行
> 最后更新：2026-06-16

---

## 问题诊断

根目录目前有 **35+ 个条目**，正常 Android 项目根目录应控制在 10-15 个。

### 「乱」的三类表现

| 类别 | 问题 | 举例 |
|------|------|------|
| **IDE 残留** | Eclipse/VS Code 配置未清理 | `.project`, `.settings/`, `.vscode/`, `app/.classpath` |
| **工作痕迹泄漏** | AI/编辑器工作数据混入项目 | `.agents/specs/`, `.claude/worktrees/`, `.omc/state/` |
| **文件散落** | 本该归类的文件在根目录 | `ADBKeyboard.apk`, `tmp_screen.png`, `test_*.py` |
| **截图三处** | 同一类内容分三个文件夹，且有重复 | `screenshots/`, `docs/screenshots/`, `reports/screenshots/` |
| **目录名拼写错误** | archieve → archive | `archieve/` |

---

## 目标结构

整理后根目录应简洁清晰：

```
MyAndroidPT/
├── app/                          # Android 应用源码（不变）
├── build.gradle.kts              # 项目级构建（不变）
├── settings.gradle.kts           # 项目设置（不变）
├── gradle.properties             # Gradle 属性（不变）
├── gradle/                       # Gradle wrapper + 版本目录（不变）
├── gradlew / gradlew.bat         # Gradle wrapper 脚本（不变）
├── local.properties              # SDK 路径（不变）
│
├── archieve/                     # 保留（第三方参考，UI 设计参考）
├── materials/                    # 教学原始素材（不变）
├── reports/                      # 实训报告 + 课程设计报告（保留目录，内容后续重写）
├── scripts/                      # ⬆️ 收纳测试脚本 + 工具脚本
├── docs/                         # ⬇️ 审计报告 + 设计文档（plans/ 合并进来）
│
├── .gitignore                    # ⬆️ 补充 entries
├── AGENTS.md                     # （保留，项目级指导）
│
├── .idea/                        # Android Studio 配置（隐藏，保留）
├── .gradle/                      # Gradle 缓存（隐藏，保留）
├── .agents/                      # 保留（myAgent 工作数据，审计审查用）
├── .claude/                      # 保留目录，仅清理 worktrees/
└── .git/                         # Git 仓库（隐藏，不变）
```

**根目录预期条目：** 约 17 个（不含隐藏文件夹则为 12 个）

---

## 具体执行步骤

---

### 第一步：更新 `.gitignore`

**做什么：** 补充缺失的忽略规则

```gitignore
# IDE
.vscode/
.project
.settings/
*.classpath
*.project

# Temp files
tmp_*
*.tmp
ADBKeyboard.apk

# AI work data
.omc/

# Screenshots（自己截，不留源码仓库）
screenshots/
docs/screenshots/
reports/screenshots/
```

> `.agents/` → 保留（用户已确认）
> `.omc/` → 加入忽略（用户已确认要删除）
> `screenshots/` 等截图目录 → 加入忽略，同时从 git 中删除跟踪（详见第三步）

---

### 第二步：删除 IDE 残留

**做什么：** 清理 Eclipse / VS Code 配置文件

| 文件 | 操作 | 原因 |
|------|------|------|
| `.vscode/` | 删除 | VS Code 配置，非项目必需 |
| `.project` | 删除 | Eclipse 配置，非 Android 项目必需的 |
| `.settings/` | 删除 | Eclipse 配置 |
| `app/.classpath` | 删除 | Eclipse 配置 |
| `app/.project` | 删除 | Eclipse 配置 |
| `app/.settings/` | 删除 | Eclipse 配置 |

> 这些文件已在 `.gitignore` 中（第一步补充），删除后不再跟踪

---

### 第三步：删除所有截图目录

**做什么：** 删除全部截图目录，用户说"我自己截图"

| 目录 | 操作 | 原因 |
|------|------|------|
| `screenshots/`（18 张运行截图 + SQL） | **删除** | 运行证据，自己重截 |
| `docs/screenshots/`（14 张截图 + phase4 子目录） | **删除** | 审计报告配图，自己重截 |
| `reports/screenshots/`（28 张截图 + 调试文件） | **删除** | 报告配图，报告重写时重截 |

> `docs/` 中的审计报告（v2/v3）会暂时缺少图片引用，随后更新说明
> `reports/` 中的实训报告 / 课程设计报告待重写，不影响

---

### 第四步：收纳散落文件

**做什么：** 将根目录的测试脚本和临时文件移到 `scripts/`

| 当前位置 | 目标位置 | 说明 |
|----------|----------|------|
| `test_full_flow.py` | `scripts/test_full_flow.py` | 自动化测试脚本 |
| `test_full_flow_v2.py` | `scripts/test_full_flow_v2.py` | 自动化测试脚本 |
| `test_regression.py` | `scripts/test_regression.py` | 回归测试脚本 |
| `register_user.py` | `scripts/register_user.py` | 工具脚本 |
| `ADBKeyboard.apk` | `scripts/ADBKeyboard.apk` | 测试工具 APK |
| `tmp_screen.png` | **删除** | 临时文件，无保留价值 |

> `scripts/` 目录已存在（内有 `fix_report_images.py`），直接移入

---

### 第五步：合并文档目录

**做什么：** 将 `plans/` 移入 `docs/`

| 来源 | 目标 | 说明 |
|------|------|------|
| `plans/todo-user-isolation.md` | `docs/` | 设计规划，和审计报告同类 |
| `plans/邮箱字段+登录提示+注释.md` | `docs/` | 同上 |
| `plans/3_design_system/` | `docs/` | M3 设计分析 |
| `plans/file-structure-cleanup.md` | `docs/` | 当前计划本身（归档） |

> 迁移后 `plans/` 目录为空，删除

---

### 第六步：清理工作痕迹

**做什么：** 按确认决定删除工作数据目录

| 目录 / 文件 | 操作 | 原因 |
|-------------|------|------|
| `.omc/`（state/sessions 多会话数据） | **删除** | 用户确认删除，这是 OmniChat 的会话缓存（自动生成） |
| `.claude/worktrees/`（3 个 agent 工作副本） | **删除** | 每个都是完整 Git 仓库快照（各约 50MB+），agent 运行完即废弃 |
| `.claude/scheduled_tasks.json` + `.lock` | **保留** | Claude Code 的定时任务配置，不影响根目录整洁 |
| `.agents/specs/` | **保留** | 用户确认保留（审计审查用） |

---

## 执行顺序

```
Step 1: 更新 .gitignore
Step 2: 删除 IDE 残留（.vscode/ .project .settings/ app/.classpath 等）
Step 3: 删除所有截图目录（screenshots/ docs/screenshots/ reports/screenshots/）
Step 4: 收纳散落文件到 scripts/（test_*.py register_user.py ADBKeyboard.apk）
Step 5: 合并 plans/ → docs/，删除空 plans/
Step 6: 清理工作痕迹（.omc/ .claude/worktrees/）
```

每步一个原子 commit，按序执行，保证可追溯。

---

## 不会动的内容

| 内容 | 原因 |
|------|------|
| `app/` | 应用源码，不动 |
| `materials/` | 教学原始素材，不动 |
| `reports/` | 保留目录结构，内容后续重写 |
| `archieve/` | 保留（UI 设计参考） |
| `.agents/` | 保留（审计审查用） |
| `.idea/` | Android Studio 配置，保留 |
| `AGENTS.md` / `CLAUDE.md` / `GUARD.md` | 项目级指导文件，保留 |
| 构建系统文件 | `build.gradle.kts` / `settings.gradle.kts` / `gradle*` / `local.properties` 不动 |
| `.git/` | Git 仓库，不动 |

---

## 执行完成后目录预览

```
MyAndroidPT/
├── app/
├── archieve/
├── docs/
│   ├── v2_audit_report.md
│   ├── v2_fix_plan.md
│   ├── v3_audit_report.md
│   ├── 审计报告v1.md
│   ├── adb_automation_input_issues.md
│   ├── android_automation_best_practices.md
│   ├── todo-user-isolation.md
│   ├── 邮箱字段+登录提示+注释.md
│   ├── 3_design_system/
│   │   ├── m3_ui_refactor_execution_plan.md
│   │   ├── material3_expressive_analysis.md
│   │   └── xml_visual_improvement_plan.md
│   └── file-structure-cleanup.md
├── materials/
│   ├── 1_practical_trainning/
│   └── 2_design/
├── reports/
│   ├── 202525350226_刘家暄_实训报告.docx
│   ├── 实训报告.md
│   └── 课程设计报告.md
├── scripts/
│   ├── test_full_flow.py
│   ├── test_full_flow_v2.py
│   ├── test_regression.py
│   ├── register_user.py
│   ├── ADBKeyboard.apk
│   └── fix_report_images.py
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
├── gradlew
├── gradlew.bat
├── local.properties
├── .gitignore
├── AGENTS.md
├── CLAUDE.md        # 隐藏？不动
├── GUARD.md          # 同上
├── .agents/          # 隐藏
├── .claude/          # 隐藏
├── .gradle/          # 隐藏
├── .idea/            # 隐藏
└── .git/             # 隐藏
```

**根目录可见条目（不含隐藏）：12 个** — 清晰、专业、老师看了不头大。
**根目录条目（含隐藏）：17 个** — 全是必要的。

---

## 风险

1. `reports/实训报告.md` 和 `reports/课程设计报告.md` 中的截图引用会断裂 → 报告后续重写，可接受
2. `docs/v3_audit_report.md` 中引用了 `docs/screenshots/` 的图片 → 截图删除后引用断裂，后续更新审计报告的截图引用说明
3. `scripts/fix_report_images.py` 可能依赖旧的截图路径 → 该脚本也一并删除或重写，暂定方案为：保留但不再使用（后续报告重写时会清理）
