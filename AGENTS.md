# MyAndroidPT — AGENTS.md

登录注册 + 待办事项（TODO）CRUD Android App。Java 11 + Classic Views + Gradle Kotlin DSL，SQLite 本地存储，无远程数据库。

## Dev Environment Tips

| 需求 | 命令 |
|------|------|
| 构建 APK | `cd app && gradlew.bat assembleDebug` |
| 安装到设备/模拟器 | 使用 [`android run`](#verification-loop) |
| 清理构建 | `cd app && gradlew.bat clean` |

- SDK 路径：`D:\apps\Android\Sdk`（已配置在 `local.properties`）
- **所有 `gradlew` 命令必须在 `app/` 目录下执行**，根目录无 Gradle 任务

## Build & Test

| 命令 | 用途 |
|------|------|
| `cd app && gradlew.bat assembleDebug` | 构建 debug APK |
| `cd app && gradlew.bat installDebug` | 构建并安装 |
| `cd app && gradlew.bat clean` | 清理构建 |

无单元测试（仅空的 Example 模板）。

## Project Structure

```
app/src/main/java/com/ljx/pt/
├── MainActivity.java       — 登录页（账号/密码 + 记住密码 + 自动登录）
├── RegisterActivity.java   — 注册页
├── WelcomeActivity.java    — 登录后欢迎页
├── TodoListActivity.java   — 待办列表（RecyclerView + FAB 新增）
├── TodoEditActivity.java   — 新增/编辑待办（共用）
├── TodoDetailActivity.java — 待办详情
├── bean/
│   ├── User.java
│   └── Todo.java
├── dao/
│   └── UserDao.java
├── dbunit/
│   ├── UserDBHelper.java
│   └── TodoDBHelper.java
└── adapter/
    └── TodoAdapter.java
```

## Data & Communication

两种独立数据库：`user.db`（账号密码）和 `todo.db`（待办数据）。

- `registerForActivityResult` 回传 key 为裸字符串 `"userName"` / `"password"`，无常量定义
- 待办传递使用 `TodoEditActivity.EXTRA_TODO_ID`（`"extra_todo_id"`），`-1` 表示新增
- `SharedPreferences("spfRecord")` 的四字段 key（`isRemember` / `isAutoLogin` / `userName` / `password`）散落在 MainActivity 和 WelcomeActivity 的硬编码中，修改时须同步两处

## Code Style & Conventions

- **语言**: Java 11，无 Kotlin
- **包名**: `com.ljx.pt`，子包 `bean` / `dao` / `dbunit` / `adapter`
- **命名**: `XxxActivity`；ID 前缀 `et_` `btn_` `tv_` `cb_` `rv_` `fab_`；DAO `findByXxx` / `insert`
- **布局**: XML + LinearLayout，无 Jetpack Compose
- **线程**: 数据库操作放子线程（`new Thread()`），UI 回调用 `runOnUiThread()`
- **Activity 注册**: 每个 Activity 必须在 `AndroidManifest.xml` 声明

## Boundaries

- ✅ **Always**: 在 `bean`/`dao`/`dbunit`/`adapter` 包内增删代码、修改布局 XML、修改字符串资源
- ⚠️ **Ask first**: 改动数据库表结构或 `onUpgrade`、修改 `applicationId`、新增/移除 Activity（涉及 Manifest）
- 🚫 **Never**: UI 线程直接执行数据库操作、删除 `AndroidManifest.xml` 中的 Activity 声明

## Common Pitfalls

- **SQLite ≠ MySQL**: 本地文件数据库，无 JDBC、无网络连接
- **TodoDBHelper 升级**: `onUpgrade()` 直接 `DROP TABLE` 重建，丢失所有待办数据
- **密码明文**: 教学演示用途，注意安全风险
- **Cursor 安全**: 查询均用参数化 WHERE（`name=?` / `_id=?`），无注入风险

## Git 提交规范

每个独立的修改（一个布局文件、一个 Activity 方法、一个资源文件）改完立即提交，**不要攒到最后统一提交**。永远保持一个可回滚的干净状态——Build 失败时能通过 `git revert` 恢复到上一个提交点，而不是从零重来。

## Emulator & Device Operations

- 使用 `/android-cli` skill 操作模拟器和截图。
