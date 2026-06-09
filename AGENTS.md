# MyAndroidPT — AGENTS.md

登录注册 + 待办事项（TODO）CRUD Android App。Java 11 + Classic Views + Gradle Kotlin DSL，SQLite 本地存储。

> **项目来源**: 华南农业大学珠江学院《移动应用开发（Android）》课程设计 + 6 段实训材料
> **北极星纲要**: `materials/1_practical_trainning/index.md`（实训递进图）+ `materials/2_design/README.md`（课程设计考核材料）
> **演进关系**: 原始要求为注册登录模块（实训 1-6），项目演进为 注册登录 + 待办 CRUD 双模块应用

## Dev Environment Tips

| 需求 | 命令 |
|------|------|
| 构建 APK | `cd app && gradlew.bat assembleDebug` |
| 安装到设备/模拟器 | 使用 `android run` |
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

### Activities 与布局

```
app/src/main/res/layout/
├── activity_main.xml            — 登录页
├── activity_register.xml        — 注册页
├── activity_welcome.xml         — 欢迎页
├── activity_todo_list.xml       — 待办列表（RecyclerView + FAB）
├── activity_todo_edit.xml       — 新增/编辑待办（共用）
├── activity_todo_detail.xml     — 待办详情
└── item_todo.xml                — RecyclerView item 布局
```

### Java 源文件

```
app/src/main/java/com/ljx/pt/
├── MainActivity.java            — 登录页（账号/密码 + 记住密码 + 自动登录）
├── RegisterActivity.java        — 注册页
├── WelcomeActivity.java         — 登录后欢迎页
├── TodoListActivity.java        — 待办列表（RecyclerView + FAB 新增）
├── TodoEditActivity.java        — 新增/编辑待办（共用）
├── TodoDetailActivity.java      — 待办详情
├── bean/
│   ├── User.java                — 用户实体（id / username / password）
│   └── Todo.java                — 待办实体（id / title / content / status / createdAt）
├── dao/
│   └── UserDao.java             — 用户 DAO（login / register）
├── dbunit/
│   ├── UserDBHelper.java        — 用户数据库（user.db，SQLite）
│   └── TodoDBHelper.java        — 待办数据库（todo.db，SQLite）
└── adapter/
    └── TodoAdapter.java         — 待办列表适配器
```

## Data & Communication

### 两个独立数据库

`user.db`（账号密码，`UserDBHelper`）和 `todo.db`（待办数据，`TodoDBHelper`）—— 都是 SQLite，无远程数据库。原始实训 3 要求 MySQL，但项目已转为纯本地方案。

### 用户模块数据流

- `registerForActivityResult` 回传 key 为裸字符串 `"userName"` / `"password"`，无常量定义
- 注册成功 → 通过 `setResult()` 回传数据 → `onActivityResult()` 在 MainActivity 接收，自动填入输入框
- `SharedPreferences("spfRecord")` 四字段：`isRemember` / `isAutoLogin` / `userName` / `password`，散落在 MainActivity 和 WelcomeActivity，修改须同步两处
- CheckBox 联动：勾选"自动登录"→ 自动勾选"记住密码"；取消"记住密码"→ 自动取消"自动登录"
- 密码明文存储（教学演示用途）

### 待办模块数据流

- Todo 实体字段：`id`(Long) / `title`(String) / `content`(String) / `status`(Boolean) / `createdAt`(String)
- Activity 间传递使用 `TodoEditActivity.EXTRA_TODO_ID`（`"extra_todo_id"`），`-1` 表示新增
- 待办删除通过长按列表项触发删除确认
- 功能对齐 F1-F5：F1创建 / F2查看（列表+详情） / F3编辑 / F4删除 / F5状态切换

## Code Style & Conventions

- **语言**: Java 11，无 Kotlin
- **包名**: `com.ljx.pt`，子包 `bean` / `dao` / `dbunit` / `adapter`
- **Activity 命名**: `XxxActivity`；布局命名 `activity_xxx.xml` / `item_xxx.xml`
- **ID 前缀**: `et_`(EditText) `btn_`(Button) `tv_`(TextView) `cb_`(CheckBox) `rv_`(RecyclerView) `fab_`(FAB)
- **DAO 方法**: `findByXxx` / `insert` / `delete` / `update` / `login` / `register`
- **自定义样式**: `MyBtnStyle` / `MyEditStyle` / `btn_bg_selector` / `edit_text_bg`
- **布局**: XML + LinearLayout / ConstraintLayout，无 Jetpack Compose
- **线程**: 数据库操作必须放在 `new Thread()` 子线程；UI 更新用 `runOnUiThread()`
- **Activity 注册**: 每新增 Activity 必须在 `AndroidManifest.xml` 声明

## Boundaries

- **Always OK**: 在 `bean`/`dao`/`dbunit`/`adapter` 包内增删代码、修改布局 XML、修改字符串资源
- **Ask first**: 改动数据库表结构或 `onUpgrade`、修改 `applicationId`、新增/移除 Activity（涉及 Manifest）
- **Never**: UI 线程直接执行数据库操作、删除 `AndroidManifest.xml` 中的 Activity 声明

## Common Pitfalls

- **SQLite ≠ MySQL**: 原始实训 3/4 要求 MySQL，本项目已改为纯本地 SQLite。改 `UserDBHelper` / `TodoDBHelper` 时不要写 JDBC 代码
- **TodoDBHelper 升级**: `onUpgrade()` 直接 `DROP TABLE` 重建，丢失所有待办数据
- **密码明文**: 教学演示用途，注意安全风险
- **SharedPreferences 散列**: `"spfRecord"` 的四字段 key 散落在 MainActivity 和 WelcomeActivity，修改须同步两处
- **registerForActivityResult key 是裸字符串**: `"userName"` / `"password"`，无常量定义，不要凭空造常量
- **Cursor 安全**: 查询均用参数化 WHERE（`name=?` / `_id=?`），无注入风险，但也不要拼接 SQL

## Git 提交规范

原子提交：每个 commit 只做一件事，改完立即提交，不要攒到最后。

| 规则 | 说明 |
|------|------|
| 单一职责 | 一个 commit 只包含一个逻辑改动（一个布局修复、一个方法修改、一个资源变更） |
| 可编译 | 每个 commit 之后 `gradlew assembleDebug` 必须能通过 |
| 信息可定位 | 格式：`fix:` / `feat:` / `refactor:` + 一句话描述改动内容 |

不要提交"修复了一些问题""调整了布局"这类打包提交。每个 commit 都必须能单独 `revert` 而不破坏其他功能。

## Emulator & Device Operations

- 使用 `/android-cli` skill 操作模拟器和截图。