# 实训报告撰写计划

> 项目：MyAndroidPT — 综合实训报告（基于综合实训报告模板）
> 分支：`docs/report-draft`
> 当前阶段：第一阶段（Section 一、二、三）

---

## 总体策略

分两阶段产出。第一阶段聚焦文本内容（一、二、三节），基于现有代码和 claude-mem 记录即可完成；第二阶段补截图和收尾（四、五、六节），需模拟器/真机配合。

| 阶段 | 内容 | 依赖 |
|------|------|------|
| **Phase 1** | 第一节 实训要求 + 第二节 实现步骤 + 第三节 遇到的问题及解决办法 | 代码库 + claude-mem |
| **Phase 2** | 第四节 功能界面展示 + 第五节 总结 + 第六节 提交资料 | 模拟器截图 + 填写 |

---

## 分支管理

```bash
git checkout -b docs/report-draft master
```

工作目录下已有未提交的代码修改（MainActivity.java 等 7 个文件），需先 **stash** 再切分支。报告文档统一放在 `docs/report/` 目录下。

---

## 具体执行计划

### Step 1：环境准备

- `git stash` 暂存未提交的代码改动
- 创建 `docs/report-draft` 分支
- 在 `docs/report/` 目录下创建 `phase1-report.md` 作为报告工作文件

---

### Step 2：claude-mem 深入挖掘

这是 **第三节（遇到的问题及解决办法）** 的核心素材来源。子代理使用 claude-mem 工具进行系统性的数据提取，涵盖以下维度：

#### 子代理 A — 数据库与数据层问题

检索以下类型的问题记录：
- SQLite 连接泄漏（TodoDBHelper 未 close）
- DAO 层架构违规（WelcomeActivity 直接使用 TodoDBHelper）
- `onUpgrade()` DROP TABLE 导致数据丢失
- seed 数据库路径问题
- MySQL vs SQLite 方案冲突

通过 claude-mem `search` / `timeline` / `get_observations` 检索 bugfix 类型记录。

#### 子代理 B — UI/UX 问题

检索以下类型的问题记录：
- 密码输入框 `inputType` 设错（numberPassword → textPassword）
- 待办编辑页文案复制粘贴错误（label_password 代替 label_todo_content）
- Material2 → Material3 主题升级冲突
- 空状态插画显示异常（有待办时仍可见）
- CheckBox 监听器重复注册
- 密码强度检测前后端不一致
- Dashboard 不刷新（loadDashboardData 只在 onCreate 调用）
- Toolbar 缺少返回按钮

通过 claude-mem 检索 bugfix / discovery 类型记录。

#### 子代理 C — 代码质量问题

检索以下类型的问题记录：
- 硬编码 Toast 字符串（遍布所有 Activity）
- 硬编码颜色值（TodoAdapter 中的 0xFF4CAF50 / 0xFF9E9E9E）
- 变量命名不一致（rbAgree → cbAgree）
- 资源 ID 命名不一致（tv_detail_status → chip_status）
- edit_text_bg.xml 死代码
- MyBtnStyle 自定义背景丢失
- 10 项审计 Bug（docs/audit.md）

通过 claude-mem 检索 refactor / bugfix 类型记录。

#### 子代理 D — 构建环境与测试问题

检索以下类型的问题记录：
- gradlew.bat vs gradlew 跨平台兼容性问题
- 父级 .git 导致多项目文件污染
- ADB 自动化测试坐标偏移（误点 Personal Safety）
- 登录 ANR（主线程网络阻塞）
- Toast 被 Activity finish() 提前销毁
- monkey 启动方案验证
- 中文文件名编码问题
- app/bin/ 构建产物未 gitignore

通过 claude-mem 检索 bugfix / discovery 类型记录。

---

### Step 3：报告初稿撰写

在 claude-mem 挖掘完成后，按模板结构撰写报告。使用工作文件 `docs/report/phase1-report.md`：

#### 第一节：实训要求（~500 字）

源材料：综合实训报告模板第一节 + 实训 1-6 的项目描述。

将 6 个实训浓缩为一段总体要求表述，覆盖：
1. UI 设计（实训 1）— 登录/注册页面
2. Activity 跳转与 Toast（实训 2）— 页面交互 + 表单校验
3. 数据库连接（实训 3）— 建库建表 + JDBC 工具
4. 注册/登录业务（实训 4）— 真实的注册登录逻辑
5. 记住密码（实训 5）— SharedPreferences
6. 自动登录（实训 6）— 勾选联动 + 注册回填

**注意**：实际项目中数据库采用 **SQLite 本地方案**而非教材要求的 MySQL，此处需注明因课程设计实际要求采用 SQLite。

#### 第二节：实现步骤（~2000 字）

按 6 个实训递进顺序写，每个步骤含：
- 做了什么
- 关键代码片段（带注释，展示代码规范）
- 关键文件路径

重点突出：
- 包结构（bean / dao / dbunit / adapter）
- 资源体系（colors / styles / themes / drawable）
- 线程模型（子线程 DB 操作 + runOnUiThread）
- 命名规范（PascalCase / camelCase / snake_case）
- Intent 跳转与回传

#### 第三节：遇到的问题及解决办法（~3000 字，**重点章节**）

根据 claude-mem 挖掘结果，分类整理为表格形式：

| 问题 | 原因 | 解决办法 | 涉及实训 |
|------|------|---------|---------|
| 密码输入框只能输入数字 | `inputType` 错配为 `numberPassword` | 改为 `textPassword`，支持字母+数字混合 | 实训 1 |
| 待办编辑页显示"密码"标签 | 复制登录页布局后未替换资源引用 | 修正 `label_password` → `label_todo_content` | 实训 2 |
| 编译时 `gradlew.bat command not found` | 在 Git Bash 中执行了 `.bat` 批处理文件 | 使用 `./gradlew` （Unix shell 脚本版本） | 实训 1 |
| 登录触发 ANR（应用无响应） | 数据库操作在主线程执行，阻塞 UI | 使用 `new Thread()` 子线程执行 DB 操作 | 实训 4 |
| WelcomeActivity 仪表盘不刷新 | `loadDashboardData()` 只在 `onCreate` 调用 | 增加 `onResume` 回调中的刷新逻辑 | 扩展模块 |
| WelcomeActivity 数据库连接泄漏 | `TodoDBHelper` 实例从未调用 `close()` | 在 `onDestroy` 中释放资源 | 扩展模块 |
| 注册后跳回登录页但账号没回填 | 使用了 `startActivity` 而非 `startActivityForResult` | 改用 `registerForActivityResult` 回传数据 | 实训 6 |
| 记住密码/自动登录状态混乱 | SharedPreferences 写入时机不对 | 确保登录成功时写入、退出登录时清除 | 实训 5/6 |
| 空状态插画在有数据时仍可见 | 只隐藏了文字，未隐藏插画 ImageView | 同步控制 ImageView 的 visibility | 扩展模块 |
| CheckBox 监听器重复注册 | `onResume` 每次重新注册 listener | 移到 `onCreate` 中一次性注册 | 扩展模块 |
| 密码强度检测前后不一致 | 实时检测与提交校验使用不同标准 | 统一校验逻辑 | 实训 4 |
| EditText 样式不生效 | 从 `MyEditStyle` 迁移到 `TextInputLayout.OutlinedBox` 后背景 drawable 废弃 | 删除未使用的 `edit_text_bg.xml` 样式文件 | 实训 1 |
| ADB 自动化测试坐标点击偏移 | 不同模拟器状态下布局变化 | 改用 `am start` 命令启动 Activity | 测试 |
| 硬编码字符串散落在各 Activity | 开发初期未遵循资源化原则 | 提取到 `strings.xml`，使用 `R.string.*` 引用 | 代码规范 |
| `rbAgree` 命名与控件类型不符 | CheckBox 变量名用了 RadioButton 前缀 | 统一改为 `cbAgree` | 代码规范 |

> **说明**：由于项目实际演进中已从纯「注册登录模块」扩展为「注册登录 + 待办 CRUD」双模块应用，部分问题（WelcomeActivity 仪表盘、空态状态、CheckBox 监听器）属于扩展模块的 Bug，在报告中可标注为"扩展功能"而非核心实训范围。

---

### Step 4：截图清单

以下截图由用户自行处理，我提供清单和说明：

| # | 截图内容 | 说明文字 | 对应实训 |
|---|---------|---------|---------|
| 1 | 登录页（空态） | 展示账号、密码输入框、登录按钮、"记住密码/自动登录"复选框、"去注册"入口 | 实训 1 |
| 2 | 登录页（输入校验） | 输入为空时弹出 Toast 提示 | 实训 2 |
| 3 | 注册页（空态） | 展示账号/密码/确认密码/邮箱输入框、注册按钮 | 实训 1 |
| 4 | 注册页（输入校验） | 密码强度实时检测展示（弱/中/强）| 实训 4 |
| 5 | 注册页（邮箱格式错误） | 非法邮箱格式的 Toast 提示 | 实训 4 |
| 6 | 注册成功回填登录页 | 注册成功后自动跳回登录页并预填账号密码 | 实训 6 |
| 7 | 登录成功 → 欢迎页 | 展示欢迎信息 + 待办统计仪表盘 + "退出登录"按钮 | 实训 4 |
| 8 | 记住密码（重启后回填） | 第二次打开应用时账号密码已自动填入 | 实训 5 |

---

## 依赖关系

```
Step 1 (环境准备)
  └─→ Step 2 (claude-mem 挖掘) ── 4 个子代理可并行
       └─→ Step 2a 数据库问题挖掘
       └─→ Step 2b UI/UX 问题挖掘
       └─→ Step 2c 代码质量问题挖掘
       └─→ Step 2d 构建/测试问题挖掘
            └─→ Step 3 (报告撰写) ── 第一节/二/三节顺序依赖
                 │
                 └─→ Step 4 (截图清单) ── 用户自行处理，非阻塞
```

---

## 下一步

确认本计划后，依次执行：

1. 切分支 → `docs/report-draft`
2. spawn 4 个子代理并行挖掘 claude-mem
3. 汇总 → 撰写第一节/二/三节
4. 输出截图清单
