# MyAndroidPT — 当前目标

## 北极星

项目交付：`基于安卓的简易待办应用开发`（华南农业大学珠江学院 · 人工智能学院 · 2025-2026 学年第二学期）

**技术红线**：Java 11 + Activity 组件 + SQLite 本地存储。无 Kotlin、无 Room、无 Jetpack Compose。

**提交平台**：课堂派 · 课程设计考核
**提交截止**：2026-06-26 00:00（超时提交允许，但开启 100 分查重）
**总分**：100 分（按 4:6 折入总评：平时 40% + 课程设计 60%）

---

## 评分维度（满分 100）

| 维度 | 分值 | 优档标准 |
|------|------|----------|
| 功能实现 | 50 | F1–F5 全部完整实现，运行正常无问题 |
| 界面设计 | 20 | 简洁美观、按钮布局合理、交互流畅、符合安卓设计规范 |
| 代码质量 | 20 | 结构清晰、命名规范、注释充分 |
| 文档撰写 | 10 | 7 章节完整、内容充实、条理清晰 |

> 详细档位条件见 `materials/2_design/02-grading-rubric.md`

---

## 当前状态（2026-06-10）

代码模块（F1–F5）已全部实现，实训 1–6 用户模块已接入。进入课程设计全面打磨阶段。

### 功能验收清单

- [x] **F1 创建** — 输入标题/内容，保存到 todo.db
- [x] **F2 查看** — RecyclerView 列表展示全部待办；点击跳转详情页
- [x] **F3 编辑** — 列表项进入编辑页，修改后保存
- [x] **F4 删除** — 长按列表项触发删除确认
- [x] **F5 状态切换** — 已完成/未完成两种状态可切换

### 教材审查发现的问题（待修复）
> 参照 `教材大纲审查-正式计划.md`

| 档位 | 问题 | 文件 | 状态 |
|------|------|------|------|
| 🔴 P0 | CheckBox listener 绑定时机，打开详情页触发多余 DB 写 | TodoDetailActivity.java | ⏳ 待修复 |
| 🔴 P0 | Todo.java isDone 字段命名不合 JavaBean 规范 | bean/Todo.java + bean/后续引用 | ⏳ 待修复 |
| 🟠 P1 | 注册前未判重，重复用户名 SQLiteConstraintException crash | RegisterActivity.java | ⏳ 待修复 |
| 🟠 P1 | insert() 写死 is_done=0 忽略传入值 | dbunit/TodoDBHelper.java | ⏳ 待修复 |
| 🟢 P2 | MySQL→SQLite 架构偏差，报告中需说明选型原因 | reports/课程设计报告.md | ⏳ 待修复 |
| 🟢 P2 | 缺少邮箱字段（教材注册表单要求） | RegisterActivity.java + 报告 | ⏳ 报告说明 |
| 🟡 P3 | 报告模板表格/知识点未对齐 | reports/课程设计报告.md | ⏳ 待修复 |

---

## 目标条件

以下 4 项全部满足时，目标达成：

1. **构建通过**：`cd app && gradlew.bat assembleDebug` 零错误完成，生成 debug APK
2. **真机/模拟器验证**：APK 安装到设备，所有 Android 操作（包括模拟器管理、安装、截图）**必须调用 `android-cli` skill**（`android run` / `android emulator` / `android screen` 等），不得直接用 adb 命令。对话中须呈现以下证据之一证明运行正常： - 截图覆盖：登录页 → 注册页 → 列表页 → 详情页 → 编辑页，5 个 Activity 均启动 - 或 `android screen capture` 截得关键页面图 3. **CheckBox listener 绑定时机**：`TodoDetailActivity.java` 中 `cbDone.setOnCheckedChangeListener` 的绑定代码位于 `loadTodo()` 的 `runOnUiThread` 回调**之后**（先是 `loadTodo()` 里的 `cbDone.setChecked()` 执行完毕，再绑定 listener），消除打开详情页时触发多余 `updateStatus` DB 写的问题
3. **注册判重**：`RegisterActivity.java` 在 `dao.insert()` 之前调用 `dao.findByName(name)` 检查用户名是否已存在，已存在时 Toast 提示并 return，不再因 SQLite UNIQUE 约束触发 `SQLiteConstraintException` crash
4. **JavaBean 命名 + insert 参数传递**：`bean/Todo.java` 字段 `isDone` 改名为 `done`，getter 保持 `isDone()`（符合 JavaBean boolean 规范）；`TodoDBHelper.java` 中引用同步由 `todo.isDone` 改为 `todo.done` 时只改 field 访问；`dbunit/TodoDBHelper.java` 的 `insert()` 方法用 `todo.isDone() ? 1 : 0` 代替硬编码的 `0`，正确传递传入值

---

## 工作原则

1. **原子提交**：每个 commit 只做一件事，改完立即提交
2. **可编译**：每个 commit 后 `gradlew assembleDebug` 必须通过
3. **勿跑偏**：改动不得偏离 Java + Activity + SQLite 技术栈
4. **以页面实际呈现效果为准**：一切 UI 效果以模拟器/设备运行截图为准，不依赖 XML 静态推断
5. **对齐纲要**：功能/界面/代码/文档四个维度对齐 `02-grading-rubric.md` 评分标准
