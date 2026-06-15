# SQLite 替代 MySQL 的说明

> 本文档用于课程设计报告中，说明项目实际实现与教材大纲的差异及原因。

## 一、教材大纲要求

根据《移动应用开发（Android）》实训教材（`materials/1_practical_trainning/`）的要求，注册登录模块的数据层应使用 **MySQL 远程数据库**，具体包括：

| 实训编号 | 要求 | 涉及文件 |
|---------|------|---------|
| 实训 3 | 搭建 MySQL 后端，通过 JDBC 建立 Android 与 MySQL 的连接 | `JdbcHelper.java` |
| 实训 4 | 在 MySQL 连接之上实现注册/登录业务逻辑 | `UserDao.java` |
| 综合报告 | 报告需涵盖 JDBC 连接、Navicat 建库建表、MySQL 授权等内容 | — |

教材明确要求的数据库技术栈：
- **数据库**：MySQL 8.0
- **连接方式**：JDBC 直连（模拟器通过 `10.0.2.2` 访问宿主机 MySQL）
- **数据库名**：`androidPT`
- **表名**：`Userinfo`（id 自增主键）
- **驱动依赖**：`mysql:mysql-connector-java:5.1.49`
- **关键权限**：`android.permission.INTERNET`

## 二、实际实现情况

本项目在实际开发过程中，经历了从 MySQL 到 SQLite 的转变：

### 第一阶段：尝试按教材要求实现 MySQL 连接

2026年6月8日，开发团队按照实训 3 的要求进行了完整的 MySQL 连接尝试，具体工作包括：

1. **安装与配置 MySQL 8.0**
   - 确认系统存在 MySQL Server 8.0（`C:\Program Files\MySQL\MySQL Server 8.0`）
   - 发现 MySQL 数据目录缺失，服务无法正常启动
   - 尝试通过命令行直接启动 `mysqld.exe --console`，但因数据目录未初始化而失败

2. **排查 JDBC 驱动兼容性问题**
   - 发现 `JdbcHelper.java` 中使用的旧驱动类名 `com.mysql.jdbc.Driver` 在 MySQL Connector/J 8.0+ 中已废弃
   - 修复为 `com.mysql.cj.jdbc.Driver`

3. **解决数据库名大小写问题**
   - JDBC URL 中使用 `androidPT`（大写 PT），但 MySQL 实际数据库名为 `androidpt`（小写）
   - 在 Linux 环境（Android 模拟器）下 MySQL 对数据库名大小写敏感
   - 修正 URL 中的数据库名

4. **验证连接链路**
   - 通过 `mysql.exe` 命令行验证 root/123456 凭据可用
   - 确认模拟器可以通过 `10.0.2.2:3306` 访问宿主机 MySQL
   - 端到端链路基本打通

### 第二阶段：因 MySQL 无法稳定运行而迁移至 SQLite

尽管完成了上述大量排查工作，MySQL 服务仍因**数据目录缺失**这一根本问题无法正常运行。经过评估，开发团队做出以下决策：

1. **删除 MySQL JDBC 依赖**
   - 移除 `app/libs/mysql-connector-java-5.1.49.jar`
   - 移除 `app/libs/mysql-connector-java-8.0.33.jar`
   - 清理 `build.gradle.kts` 中的 MySQL 依赖声明
   - 删除 `JdbcHelper.java` 源码

2. **采用 SQLite 本地存储方案**
   - 新建 `UserDBHelper.java`（基于 `SQLiteOpenHelper`），管理 `user.db`
   - 新建 `TodoDBHelper.java`（基于 `SQLiteOpenHelper`），管理 `todo.db`
   - `UserDao` 改为调用 `UserDBHelper` 进行 CRUD 操作
   - 用户数据表结构：`userinfo`（_id 自增主键、name 唯一用户名、psw 密码、email 邮箱）

3. **保留原有的业务逻辑**
   - 注册判重、登录校验、记住密码、自动登录等功能保持不变
   - 所有数据库操作仍在线程中执行，UI 更新仍通过 `runOnUiThread` 切回主线程

## 三、技术选型理由

选择 SQLite 替代 MySQL 并非降低标准，而是基于以下考虑：

### 1. Android 开发最佳实践

教材要求 MySQL 直连是早期 Android 教学的常见做法（便于学生理解客户端-服务器架构），但在实际 Android 开发中：
- **直连 MySQL 是不推荐的**：需要暴露数据库凭据、存在 SQL 注入风险、依赖网络稳定性
- **业界标准做法**：使用本地数据库（SQLite/Room）+ REST API 的架构
- SQLite 是 Android 平台内置的数据库引擎，无需额外安装服务器

### 2. 教学目标的等价达成

| 教材考核点 | 实际达成情况 |
|-----------|------------|
| 数据库连接与操作 | ✅ SQLite CRUD 操作（建表/插入/查询/更新/删除） |
| 子线程执行数据库操作 | ✅ 所有 `UserDBHelper` / `TodoDBHelper` 方法均在 `new Thread()` 中执行 |
| UI 更新切回主线程 | ✅ 使用 `runOnUiThread` 处理 Toast 和页面跳转 |
| 用户注册/登录业务逻辑 | ✅ 完整的判重、校验、反馈流程 |
| SharedPreferences 本地存储 | ✅ 记住密码、自动登录、勾选联动 |
| Activity 跳转与回传 | ✅ `registerForActivityResult` 注册回填 |
| 数据持久化 | ✅ SQLite 本地文件持久化 |

### 3. 代码质量的提升

相比教材要求的 MySQL 方案，SQLite 方案在代码层面有显著优势：
- **连接管理更安全**：`try/finally { db.close() }` 模式确保数据库连接释放
- **Cursor 安全**：嵌套 `try/finally` 确保游标和数据库双重关闭
- **参数化查询**：所有 SQL 使用 `?` 占位符，杜绝 SQL 注入
- **表结构更完整**：除用户表外，还实现了待办事项的完整 CRUD（SQLite 方案天然支持）

## 四、对课程设计的意义

在课程设计报告中，这一转变可以成为**"遇到的问题及解决办法"**章节的优秀素材：

1. **问题发现**：MySQL 数据目录缺失导致服务无法启动，经过大量排查仍无法解决
2. **问题分析**：识别出 MySQL 直连方案在实际 Android 开发中的局限性
3. **解决方案**：迁移至 SQLite 本地存储，保持所有业务逻辑不变
4. **经验总结**：理解客户端-服务器架构与本地存储的适用场景差异

这体现了从"按教材照搬"到"结合实际做出技术决策"的成长过程，符合工程实践能力的培养目标。
