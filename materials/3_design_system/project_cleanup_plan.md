# 项目文件分类与清理方案

> 目标：为执行层提供一个干净的起始状态

---

## 当前 git 状态快照

```
已修改:   .gitignore
已删除:   goal.md（原为追踪文件，现在工作区中不存在）
未跟踪:   materials/3_design_system/m3_ui_refactor_execution_plan.md（新）
未跟踪:   教材大纲审查-正式计划.md（新）
未跟踪:   C/（目录）
```

---

## 一、核心项目文件 — KEEP（不讨论）

### 结构定义

```
AGENTS.md          - 项目北极星（技术栈、包结构、编码约定）
CLAUDE.md          - 项目级 AI 指令（@AGENTS.md）
GUARD.md           - 项目守护文件
.gitignore         - Git 忽略规则（需要清理）
```

### Gradle 构建系统

```
build.gradle.kts                        - 根级构建脚本
settings.gradle.kts                     - 项目设置
gradle.properties                       - Gradle 属性
gradle/libs.versions.toml               - 依赖版本目录
gradle/wrapper/gradle-wrapper.properties - Wrapper 配置
gradle/wrapper/gradle-wrapper.jar        - Wrapper JAR
local.properties                        - SDK 路径（已 gitignored？）
app/build.gradle.kts                    - App 模块构建脚本
app/proguard-rules.pro                  - ProGuard 规则
```

### Java 源代码（main）

```
app/src/main/java/com/ljx/pt/
  MainActivity.java
  RegisterActivity.java
  WelcomeActivity.java
  TodoListActivity.java
  TodoEditActivity.java
  TodoDetailActivity.java
  adapter/TodoAdapter.java
  bean/Todo.java
  bean/User.java
  dao/UserDao.java
  dbunit/TodoDBHelper.java
  dbunit/UserDBHelper.java
```

### 布局 XML（7 个 — Phase 2/3 将重写）

```
app/src/main/res/layout/
  activity_main.xml
  activity_register.xml
  activity_welcome.xml
  activity_todo_list.xml
  activity_todo_detail.xml
  activity_todo_edit.xml
  item_todo.xml
```

### 资源文件

```
app/src/main/res/
  drawable/btn_bg_selector.xml
  drawable/edit_text_bg.xml
  drawable/ic_launcher_background.xml
  drawable/ic_launcher_foreground.xml
  values/colors.xml
  values/strings.xml
  values/styles.xml
  values/themes.xml
  values-night/themes.xml
  mipmap-*/ic_launcher*.webp          (应用图标，6 个密度)
  mipmap-anydpi-v26/ic_launcher.xml
  mipmap-anydpi-v26/ic_launcher_round.xml
  xml/backup_rules.xml
  xml/data_extraction_rules.xml
  xml/network_security_config.xml
```

### Manifest 与测试

```
app/src/main/AndroidManifest.xml
app/src/test/java/com/ljx/pt/ExampleUnitTest.java
app/src/androidTest/java/com/ljx/pt/
  ExampleInstrumentedTest.java
  TodoDBHelperTest.java
  TodoDetailActivityTest.java
  TodoEditActivityTest.java
  UserDaoTest.java
```

---

## 二、教材材料 — KEEP（不动）

```
materials/
  1_practical_trainning/
    index.md                                          - 北极星纲要
    实训1-6.md                                        - 实训内容（6 个）
    综合实训报告模板.md                                 - 实训报告模板
    作业要求与评分标准.md                               - 考核标准
    userinfo.sql                                      - 测试数据
    original_docx/                                    - 原始教材 docx + 截图 + OCR
    old_md_archieve/                                  - 旧版 md 备份（冗余，但不删除）
  2_design/
    README.md
    01-assignment.md                                  - 课程设计任务书
    02-grading-rubric.md                              - 评分细则
    03-report-template.md                             - 报告模板
    original_docx/
    old_md_archieve/
  3_design_system/
    material3_expressive_analysis.md                  - M3 分析（新增）
    m3_ui_refactor_execution_plan.md                  - 执行编排（新增）
```

---

## 三、报告产物 — KEEP（最终交付物）

```
reports/
  202525350226_刘家暄_实训报告.docx                  - 已完成的实训报告
  实训报告.md                                         - 实训报告 Markdown 版
  课程设计报告.md                                     - 课程设计报告 Markdown 版
  screenshots/                                        - 报告截图（~30 张）
```

---

## 四、待清理文件（执行层操作清单）

### 4.1 无争议清理项（仅技术上确定无用的）

| 文件/目录 | 位置 | 原因 | 操作 |
|-----------|------|------|------|
| `app/src/main/AndroidManifest.xml.bak` | app/ | 备份文件，版本管理已覆盖 | **删除** |
| `screen1.png` | 根目录 | 散落的临时截图 | **删除** |
| `screen_main.png` | 根目录 | 散落的临时截图 | **删除** |
| `screenshot.png` | 根目录 | 散落的临时截图 | **删除** |
| `test_register.png` | 根目录 | 散落的临时截图 | **删除** |

### 4.2 需你指定后方可操作的

| 文件/目录 | 你的决定 | 原因 |
|-----------|---------|------|
| `goal.md` | [恢复 / 确认删除 / 交给执行层] | 原为追踪文件，现在工作区中不存在 |
| `教材大纲审查-正式计划.md` | [保留 / 移动到 materials/ / 删除] | 中文编码文件名，内容为你自己的审查计划 |
| `review-draft.md` | 你自己决定 | 你的工作产物 |
| `review-plan-draft.md` | 你自己决定 | 你的工作产物 |
| `review-plan-final.md` | 你自己决定 | 你的工作产物 |
| `C/` 目录 | [保留 / 删除 / 说明用途] | 用途不明 |

### 4.2 需要判断的

| 文件 | 判断 | 建议 |
|------|------|------|
| `scripts/fix_report_images.py` | 一次性脚本 | 保留（功能性脚本，非临时）|
| `insert_data.sql` | 数据库种子 | 保留（有用） |
| `screenshots/layout_*.json` | 布局调试 JSON | 保留（调试记录）|
| `screenshots/layout_edit.xml` | 布局调试 XML | 保留（调试记录）|
| `docx 文件（materials/下的）` | 原始教材，大 | 保留（原始交付物）|

### 4.3 `.gitignore` 需要修复的问题

当前 `.gitignore` 有几个问题：

| 问题 | 当前 | 问题 | 建议 |
|------|------|------|------|
| 缺少 `*.lock` | 只有 `scheduled_tasks.json` | `scheduled_tasks.lock` 未覆盖 | 加 `*.lock` |
| 根目录遗漏 | 无 `local.properties` 顶层 | 会导致 `local.properties` 被跟踪 | 确保有 `/local.properties` |
| Gradle 目录粒度 | `.gradle/` 通配 | `.gradle/` 下所有子目录 | 去掉 `*` 改成 `/.gradle/` |
| `gradlew` 笔误 | 有 `gradlew` 但没 `gradlew.bat` | `.bat` 未被忽略 | 两者都加 |
| 根级 `*.iml` vs 全局 | `*.iml` 全局 | IDEs 生成的 iml 在子目录也有 | 保留全局即可 |
| 缺少常见 Android 忽略 | 无 `*.so`, `*.aar`, `.cxx/`, `.externalNativeBuild/` | 可能意外跟踪 | 补充 |
| `C/` 已在根目录 | `.gitignore` 里有但文件还在 | gitignore 未生效（已跟踪过）| 需 `git rm -r --cached C/` |

---

## 五、Fresh 状态定义

执行层开始 Phase 1 前的最终状态应满足：

```
✓ git status 显示仅 3 个新文件：
    - materials/3_design_system/m3_ui_refactor_execution_plan.md
    - materials/3_design_system/material3_expressive_analysis.md
    - materials/3_design_system/project_cleanup_plan.md（本文）
✓ 4.1 无争议项已清理（AndroidManifest.xml.bak、散落截图）
✓ 4.2 中的每项已获得你的明确决定并执行
✓ .gitignore 有效（git status 不再显示被忽略的文件为 untracked）
✓ 当前在 feat/m3-ui-refactor 分支
✓ gradlew assembleDebug 通过
```

---

## 六、清理执行顺序

```
1. 删除 4.1 无争议项（AndroidManifest.xml.bak、散落截图）
2. 执行层/所有者对 4.2 各项做出决定
3. 修复 .gitignore
4. 对已不再需要但此前被追踪的文件执行 git rm --cached
5. commit: "chore: clean up workspace for M3 refactor"
```

**4.2 中的每一项需要你的明确指令才能操作。**
