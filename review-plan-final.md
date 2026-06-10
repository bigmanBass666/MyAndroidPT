# 教材大纲审查 — 正式修复计划

> 基于 6 段实训材料 + 任务书 + 评分细则 + 报告模板的全面对照审查
> 共发现 11 个问题，按严重程度分 4 档

## 修复范围

| 档位 | 数量 | 性质 | 是否修复 |
|------|------|------|----------|
| 🔴 P0 | 2 | Bug | 是 |
| 🟠 P1 | 3 | Bug/缺陷 | 是 |
| 🟢 P2 | 2 | 架构偏差 | 报告中说明 |
| 🟡 P3 | 4 | 报告对齐 | 报告中说明 |

## 步骤

### 1. TodoDetailActivity — 修复 CheckBox listener 绑定时机
- **文件**: `TodoDetailActivity.java`
- **问题**: `setOnCheckedChangeListener` 在 `loadTodo()` 之前绑定，`cbDone.setChecked()` 会触发多余 DB 写
- **修复**: 将 listener 设置移到 `loadTodo()` 的 `runOnUiThread` 回调中，在数据加载完成后绑定

### 2. RegisterActivity — 注册前判重防 crash
- **文件**: `RegisterActivity.java`
- **问题**: 直接 `dao.insert()` 无判重，重复用户名触发 `SQLiteConstraintException` crash
- **修复**: 插入前 `findByName()` 检查，已存在则 Toast 提示并 return

### 3. Todo.java — 修复 isDone 命名不合规范
- **文件**: `bean/Todo.java`
- **问题**: field 名 `isDone` + getter `isDone()` 违反 JavaBean 规范（应为 `done` + `isDone()`）
- **修复**: field 改名为 `done`，其余引用同步修改（`TodoDBHelper` 中 `todo.isDone()` 调用）

### 4. TodoDBHelper — insert 忽略传入 isDone
- **文件**: `dbunit/TodoDBHelper.java`
- **问题**: `insert()` L48 写死 `values.put("is_done", 0)`，忽略 `todo.isDone()` 传参
- **修复**: 改为 `values.put("is_done", todo.isDone() ? 1 : 0)`

### 5. 报告追加说明（无需改代码）
- **SQLite 选型说明**: "功能实现步骤"中说明 MySQL→SQLite 的原因（Android 平台兼容性 + 离线可用性）
- **模块划分表格**: 3.1 节补充用户模块（MainActivity/RegisterActivity/WelcomeActivity）
- **涉及知识点**: 3.2 节补充 SharedPreferences
- **注册回填 key**: 代码片段中注明使用 `"userName"` 而非教材的 `"username"`

## 验证

每步修复后执行 `gradlew assembleDebug` 确认编译通过。
