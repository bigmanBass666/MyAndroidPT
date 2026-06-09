# MyAndroidPT — AGENTS.md

Android 登录注册 App（实训项目），Java + Classic Views + Gradle Kotlin DSL，通过 JDBC 直连 MySQL。

## Dev Environment Tips

| 需求 | 命令 |
|------|------|
| 构建 APK | `cd app && ./gradlew assembleDebug` (Windows 用 `gradlew.bat`) |
| 安装到设备 | `cd app && ./gradlew installDebug` |
| 清理构建 | `cd app && ./gradlew clean` |
| 真机/模拟器 | 使用 `/android-cli` skill |

- SDK 路径：`D:\apps\Android\Sdk`（已配置在 `local.properties`）
- **所有 `gradlew` 命令必须在 `app/` 目录下执行**
- 数据库连接：MySQL 运行在宿主机 `10.0.2.2:3306`，库名 `androidPT`

## Build & Test

| 命令 | 用途 |
|------|------|
| `cd app && gradlew.bat assembleDebug` | 构建 debug APK |
| `cd app && gradlew.bat installDebug` | 构建并安装到设备/模拟器 |
| `cd app && gradlew.bat clean` | 清理构建 |

无单元测试运行脚本（只有空的 Example 测试）。

## Project Structure

```
app/src/main/java/com/ljx/pt/
├── MainActivity.java        — 登录页（账号/密码 + 记住密码 + 自动登录）
├── RegisterActivity.java    — 注册页
├── WelcomeActivity.java     — 登录后欢迎页（含退出登录）
├── bean/
│   └── User.java            — 用户数据模型（id, name, psw）
├── dao/
│   └── UserDao.java         — 用户操作（findByName, insert）
└── dbunit/
    └── JdbcHelper.java      — 数据库连接（MySQL via JDBC）
```

## Code Style & Conventions

- **语言**: Java 11，不使用 Kotlin
- **包名**: `com.ljx.pt`，子包按职责分 `bean` / `dao` / `dbunit`
- **命名**: 采用 Android 标准约定
  - Activity：`XxxActivity`，ID：`et_xxx`、`btn_xxx`、`tv_xxx`、`cb_xxx`
  - DAO：`findByXxx`、`insert`；实体类：字段 + getter/setter
- **布局**: 全部使用 XML + Classic Views（LinearLayout），不引入 Jetpack Compose
- **线程**: 数据库操作必须放在子线程（`new Thread()`），UI 操作通过 `runOnUiThread()` 回调
- **Activity 注册**: 每个 Activity 必须在 `AndroidManifest.xml` 中声明

## Boundaries

- ✅ **Always**: 在 `bean`/`dao`/`dbunit` 包内增删业务代码、修改布局 XML、修改字符串资源
- ⚠️ **Ask first**: 改动数据库表结构或 JDBC 连接参数、修改包名/applicationId
- 🚫 **Never**: 在 UI 线程直接执行数据库操作、删除 `AndroidManifest.xml` 中的 Activity 声明、修改数据库密码等硬编码凭据不通知用户

## Common Pitfalls

- **gradlew 目录**: 必须在 `app/` 目录下运行，根目录没有可执行的 Gradle 任务
- **数据库地址**: `10.0.2.2` 是 Android 模拟器访问宿主机的特殊 IP，真机上无法连接
- **JDBC 驱动**: 使用已过时的 `com.mysql.jdbc.Driver`，新增 DAO 时保持统一的 try-catch-finally 关闭模式
- **密码明文**: 当前密码以明文存储于数据库，修改相关逻辑时注意安全风险
