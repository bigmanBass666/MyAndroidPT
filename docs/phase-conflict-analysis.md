# 两阶段考试要求冲突分析

> 对比对象：`materials/1_practical_trainning/`（实训 1-6） vs `materials/2_design/`（课程设计/考试）
> 审查日期：2026-06-22

## 一、综述

实训阶段与课程设计（期末考试）是同一门课的两套独立要求体系，存在 6 项实质冲突。其中数据库方案是唯一影响代码实现的核心冲突，其余为范围定位和提交物层面的差异。

若两者冲突，以 **课程设计（考试）** 要求为准——它是最终评分依据。

---

## 二、冲突清单

### 冲突 1：数据库方案（核心冲突）

| 维度 | 实训要求 | 课程设计要求 |
|------|---------|-------------|
| 数据库 | MySQL 8.0 | SQLite |
| 连接方式 | JDBC 直连（10.0.2.2:3306） | 本地文件（`user.db` / `todo.db`） |
| 建库 | Navicat 手动建 `androidPT` | `SQLiteOpenHelper.onCreate()` 自动建 |
| JDBC 驱动 | `mysql:mysql-connector-java:5.1.49` | 无 |
| 网络权限 | `android.permission.INTERNET` | 不需要 |
| 核心类 | `JdbcHelper.java` | `UserDBHelper.java` / `TodoDBHelper.java` |

**处理状态**：✅ 已按课程设计采用 SQLite，`SQLite替代MySQL的说明.md` 已有完整决策记录。

**项目现状**：无任何 MySQL / JDBC / `INTERNET` 引用残留，build.gradle.kts 零 MySQL 依赖。

### 冲突 2：功能定位（范围差异）

| 维度 | 实训聚焦 | 课程设计聚焦 |
|------|---------|-------------|
| 核心功能 | 注册 + 登录 + 校验 | F1 创建 / F2 查看 / F3 编辑 / F4 删除 / F5 状态切换 |
| 扩展功能 | 记住密码（可选） + 自动登录（可选） | — |
| 用户系统 | 必需 | 未明确要求 |
| 待办系统 | 未要求 | F1-F5 全量 |

**项目现状**：同时实现了两套体系的要求——注册登录（实训必做）+ 待办 CRUD（课程设计必做），且做了用户关联（每个用户的待办隔离），无遗漏。

### 冲突 3：核心类与包结构

| 项 | 实训要求 | 课程设计实际需要 |
|----|---------|----------------|
| dbunit 包 | `JdbcHelper.java` | `UserDBHelper.java` + `TodoDBHelper.java` |
| bean 包 | `User.java` | `User.java` + `Todo.java` |
| dao 包 | `UserDao.java` | `UserDao.java` + `TodoDao.java` |
| adapter 包 | 未要求 | `TodoAdapter.java`（RecyclerView 必需） |

**项目现状**：三包扩展为四包（+adapter），所有类均存在且功能完整。

### 冲突 4：主题方案

| 维度 | 实训 1 要求 | 项目实际 |
|------|-----------|---------|
| 主题名 | `Theme.Chapter1` | `Theme.MyAndroidPT` |
| parent | `Theme.MaterialComponents.DayNight.DarkActionBar` | `Theme.Material3.DayNight.NoActionBar` |
| 组件版本 | MaterialComponents | **Material 3** |
| 按钮样式 | 自定义 `MyBtnStyle`（带 margin） | `MyBtnStyle` parent=`Widget.Material3.Button` |
| 输入框样式 | `MyEditStyle` + `edit_text_bg`（圆角描边） | `TextInputLayout.OutlinedBox` 替代 |

**说明**：这是 UI 升级而非违背要求。Material 3 是 MaterialComponents 的官方后继，符合"安卓设计规范"要求。

**影响**：`edit_text_bg.xml` 虽然文件存在但已不被任何控件引用（成为死代码）。

### 冲突 5：提交物命名规范

| 项 | 实训要求 | 课程设计要求 |
|----|---------|-------------|
| 代码包 | `学号_姓名_实训代码.zip` | `学号_姓名_简易待办代码.zip` |
| 报告 | `学号_姓名_实训报告.docx` | `学号_姓名_简易待办课程设计报告.docx` |
| 视频 | 未要求 | `学号_姓名_展示视频`（3分钟，本人声音） |

**说明**：这是两个独立的提交任务（实训作业 + 期末考试），互不冲突，但需注意区分命名。

### 冲突 6：报告内容方向

| 章节 | 实训报告模板要求 | 课程设计报告模板要求 |
|------|----------------|-------------------|
| 项目背景 | 注册登录场景 | 待办应用场景（个人 GTD、任务清单） |
| 功能需求 | 注册 / 登录 / 邮箱校验 | F1-F5（创建/查看/编辑/删除/状态切换） |
| 设计思路 | 注册模块划分 + Activity跳转 | 待办模块划分 + RecyclerView + SQLite |
| 实现步骤 | 6 个实训顺序 | 按模块（数据库→实体→列表→详情→编辑） |
| 界面展示 | 登录页 / 注册页 / 欢迎页截图 | 待办列表 / 创建 / 详情 / 编辑 / 删除截图 |
| 涉及知识点 | Intent / Toast / SharedPreferences / JDBC | Activity / RecyclerView / SQLite / 布局XML |

**说明**：两份报告的章节结构类似但内容完全不同。需要决定按哪个模板写。

---

## 三、无冲突项（双方要求一致）

以下维度两份要求完全一致，不影响评分：

| 维度 | 一致性 |
|------|--------|
| 开发语言（Java） | ✅ 完全一致 |
| UI 组件（Activity） | ✅ 完全一致 |
| 界面设计规范（Material 风格） | ✅ 完全一致 |
| dp/sp 单位（无硬编码 px） | ✅ 完全一致 |
| 命名规范（PascalCase / camelCase / snake_case） | ✅ 完全一致 |
| 子线程数据库操作 | ✅ 完全一致 |
| UI 更新切回主线程 | ✅ 完全一致 |
| 包结构（bean/dao/dbunit 三包） | ✅ 兼容（项目扩展为四包） |

---

## 四、对当前修复工作的影响

冲突分析不影响当前 `.agents/specs/fix-code-quality-issues/` 中的 4 项修复任务。硬编码字符串、硬编码颜色、命名一致性问题同时违反了两套体系的代码规范要求。

| 修复项 | 违反实训要求 | 违反课程设计要求 |
|--------|------------|----------------|
| 硬编码 Toast | ✅ 代码规范 - 资源集中管理 | ✅ 代码质量 - 命名恰当/规范 |
| 硬编码颜色 | ✅ 界面设计 - 颜色集中管理 | ✅ 代码质量 |
| 命名不一致 | ✅ 代码规范 - 命名规范 | ✅ 代码质量 |

> 文档末尾
