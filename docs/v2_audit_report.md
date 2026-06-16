# MyAndroidPT v2 审查报告

> **生成时间**：2026-06-16
> **审查依据**：`materials/1_practical_trainning/`（实训 1-6）+ `materials/2_design/`（课程设计任务书 + 评分细则）
> **当前版本**：v1.0.0（commit b24cbdd）

---

## 总体评估

| 维度 | 满分 | 当前评分 | 说明 |
|------|------|----------|------|
| 一、功能实现 | 50 | **44** | F1-F5 全部实现，运行时正常 |
| 二、界面设计 | 20 | **16** | M3 视觉统一流畅，注册页底部按钮布局有瑕疵 |
| 三、代码质量 | 20 | **14** | 结构清晰、注释完整，但部分教程要求未对齐 |
| 四、文档撰写 | 10 | **0** | 课程设计报告未完成（7 个章节均未写） |
| **合计** | **100** | **74** | 功能可用，文档缺失拉低总分 |

> 拿分性价比提示：文档按模板填满即可拿 8-10 分，是最划算的提升块。

---

## 部分一：功能实现（44/50）

### F1 待办创建 ✅

| 教材要求 | 实际实现 | 结论 |
|---------|---------|------|
| 支持输入文本 | `TodoEditActivity`（标题 + 内容） | 满足 |
| 操作入口清晰 | RecyclerView + FAB | 满足 |
| 新增 `-1` 表示新增 | `EXTRA_TODO_ID = -1` 做判断 | 满足 |
| Dao `insert()` 方法 | `TodoDao.insert()` → `TodoDBHelper` | 满足 |
| 子线程数据库操作 | `new Thread()` + `runOnUiThread` | 满足 |

**唯一小问题**：`Todo` 实体有 `Todo(String title, String content)` 构造函数，但 `setTitle()` 时先创建空 Todo 再 setter（可读性一般），建议直接用构造函数。

### F2 待办查看 ✅

| 教材要求 | 实际实现 | 结论 |
|---------|---------|------|
| 列出所有已保存待办 | `TodoListActivity` + `RecyclerView` | 满足 |
| 点击列表项查看详情 | `onItemClick()` → `TodoDetailActivity` | 满足 |
| 详情页展示完整信息 | 标题、状态、完成标记、内容、时间 | 满足 |
| **教程实训 4 要求：从数据库读回用户名传 WelcomeActivity** | `WelcomeActivity` 直接用 Intent 拿 `userName`，不查数据库 | ⚠️ **偏离教程要求**（见代码质量节） |

### F3 待办编辑 ✅

| 教材要求 | 实际实现 | 结论 |
|---------|---------|------|
| 编辑已有待办 | `TodoEditActivity` 按 `todoId != -1` 切换 | 满足 |
| Dao `update()` 方法 | `TodoDao.update()` → `TodoDBHelper.update()` | 满足 |
| 子线程 | `new Thread()` | 满足 |

### F4 待办删除 ✅

| 教材要求 | 实际实现 | 结论 |
|---------|---------|------|
| 删除不再需要的待办 | 长按 → 确认对话框 → `TodoDao.delete()` | 满足 |
| 确认对话框 | `AlertDialog.Builder` | 满足 |
| Dao `delete()` 方法 | 完整实现 | 满足 |

### F5 待办状态切换 ✅

| 教材要求 | 实际实现 | 结论 |
|---------|---------|------|
| 切换已完成/未完成 | CheckBox + `callback` → `updateStatus()` | 满足 |
| 列表 + 详情页双入口 | Adapter + TodoDetailActivity | 满足 |
| Dao `updateStatus()` | 完整实现 | 满足 |

---

## 部分二：界面设计（16/20）

### 符合要求的部分
- Material 3 组件库（TextInputLayout、MaterialToolbar、MaterialButton、MaterialCheckBox、MaterialCardView、FAB）
- 所有布局使用 `?attr/colorSurface` / `?attr/colorPrimary` / `?attr/colorOnPrimary`，不硬编码颜色
- 主题在 `themes.xml` 定义为 `Theme.MaterialComponents.DayNight.DarkActionBar`
- 各页面 Toolbar Navigation 返回逻辑正确
- `btn_bg_selector` 和 `edit_text_bg` drawable 存在（M3 重构后保留）

### 界面问题

**UI-1 [中] 注册页底部注册按钮在 weight 容器外部，视觉位置偏下飘**

`activity_register.xml` 结构：`LinearLayout` 外层是垂直的，但 `<LinearLayout weight=1>` 只包了四个输入框，注册按钮和协议勾选框在 weight 容器外面（第 92-111 行）。在小屏设备上，按钮可能出现在很远的下方，偏离"按钮布局合理"的要求。

教材实训 1 要求"编写 `activity_register.xml`（注册布局）"，应使用一个整体垂直 LinearLayout，让所有元素（输入框 + 按钮 + 勾选）在一个流内自然堆叠。

**UI-2 [低] 登录页缺少注册跳转按钮的居中处理**

登录页 `btn_register` 使用 `layout_gravity="end"` 右对齐。功能正常，但视觉上不像标准登录页（通常"注册"会是居中的次要操作，或者与登录按钮半径等距排列）。影响不大，但属于"设计小瑕疵"档。

**UI-3 [低] `activity_welcome.xml` 布局未纳入审查范围，无法确认是否符合 M3 规范**

---

## 部分三：代码质量（14/20）

**良好部分：**
- 分包清晰：`bean/` / `dao/` / `dbunit/` / `adapter/`
- 所有 Activity 都有 `@Override onDestroy()` 来关闭 DAO，SQLite 连接泄漏已修复
- `Cursor` 查询均用 try-finally 或参数化 WHERE，无风险
- 线程模型统一：所有数据库操作进 `new Thread()`，UI 回切用 `runOnUiThread()`
- 所有类（除 MainActivity 外）有类注释
- 字段命名符合 ID 前缀约定（`et_`、`btn_`、`cb_`、`tv_`、`rv_`）

### 教程要求未对齐

**码-1 [高] 教程实训 1 要求 `style.xml` 中定义 `MyBtnStyle` 和 `MyEditStyle`，但功能等同样式用 `@style/Widget.Material3.Button` 替代**

`activity_todo_edit.xml` 和 `activity_register.xml` 均使用 M3 系统样式 `Widget.Material3.Button` / `Widget.Material3.Button.TextButton`，而非自定义的 `MyBtnStyle` / `MyEditStyle`。M3 UI 重构后自定义样式可能已被置为 M3 样式别名，但关键差异在于：

- 实训 1 要求 M3 重构项目原本应该有 `MyBtnStyle`（自定义 Button 样式，包含自定义背景、按下态、圆角等），替换后学生代码中自定义样式"自己写"的部分消失了。
- `style.xml` 中应显式定义 `MyBtnStyle` 继承 Material3 组件，同时保留自定义触控反馈逻辑。
- **建议**：在 `style.xml` 中显式定义 `MyBtnStyle`（即使继承 M3），确保能追溯演示过"自己写样式"的理解。

**码-2 [高] 教程实训 5 要求 SharedPreferences 文件名为 `user_info`，实际为 `spfRecord`**

| 教材要求 | 实际代码 |
|---------|---------|
| `SharedPreferences("user_info", MODE_PRIVATE)` | `getSharedPreferences("spfRecord", MODE_PRIVATE)` |

这是教程"记住密码"实训的硬性要求。文件名不同本身不影响功能，但老师对照教材评分时可能标记为"未按要求完成"。

- `MainActivity.saveLoginState()` 和 `initData()` 涉及此文件名
- `WelcomeActivity` 登出时也是 `"spfRecord"`
- 两处必须同时修改，不能只改一处

**码-3 [中] `registerForActivityResult` 回传 key 是裸字符串 `"userName"` / `"password"`，无常量定义 — 属于已知技术债，不影响功能**

**码-4 [中] `Todo` 实体 `id` 字段类型：SQLite 为 `INTEGER`，Java 用 `int`**

`TodoDBHelper.onCreate()` 中 `_id INTEGER PRIMARY KEY AUTOINCREMENT`，Java 实体 `private int id`。`cursor.getInt()` 返回 int，`String.valueOf(int)` 也没问题。实际没有 BUG，但在《数据库》课程的评分中，如果教师要求 `long` 与 `AUTOINCREMENT` 的 SQL 类型（`long`）对齐，可能扣分。

**码-5 [中] `User.toString()` 包含密码明文**

`User.toString()` 返回 `"User{id=..., name='...', psw='...', email='...'}"`，如果该 toString 出现在 logcat 或错误报告里，密码会暴露。建议修改为：

```java
@Override
public String toString() {
    return "User{id=" + id + ", name='" + name + "'}";
}
```

**码-6 [低] `MainActivity` 缺少类注释**

AGENTS.md 要求："100% 类注释覆盖"，但 6 月份当天提交的审计修复中 `H2 类注释` 任务已标记完成，`MainActivity` 应该是遗漏。

**码-7 [低] `activity_todo_list.xml` RecyclerView 高度与 Toolbar 重叠的风险**

```xml
<androidx.recyclerview.widget.RecyclerView
    android:layout_height="match_parent"
    android:layout_marginTop="?attr/actionBarSize" />
```

`match_parent` + `layout_marginTop` 在 CoordinatorLayout 下工作正常，但标准做法是用 AppBar + RecyclerView 的 `app:layout_behavior` 组合，避免 margin 不支持状态栏时出现顶部空白或内容被 Toolbar 遮挡。当前实现功能正常，属于潜在兼容性问题。

---

## 部分四：文档撰写（0/10）

**课程设计报告（7 个章节）均未完成。**

**章 0 — 封面** ❌
- 课程名称、设计题目、设计人、学号、学院 均未写入

**章 一 — 项目背景** ❌
- 要求 200-400 字，描述"为什么做这个待办 App"

**章 二 — 功能需求分析** ❌
- F1-F5 需用业务语言描述 + 用户故事

**章 三 — 设计思路** ❌
- 3.1 模块划分（可基于当前 `bean/dao/dbunit/adapter` 结构写）
- 3.2 涉及知识点（SQLite、Activity 生命周期、Thread、Intent、RecyclerView 等）

**章 四 — 功能实现步骤** ❌
- 按时间顺序写实现过程（8 个子步骤），关键代码片段 5-15 行

**章 五 — 遇到的问题及解决办法** ❌
- 至少 2 个真实问题（双问题模板），代码审查历史中有现成的

**章 六 — 功能界面展示** ❌
- 至少 5 张截图：列表、创建、详情、编辑、删除

**章 七 — 总结** ❌
- 知识收获 + 能力提升 + 不足与改进（200-400 字）

> 📌 性价比提示：严格按 `materials/2_design/03-report-template.md` 7 段式填满即可拿 8-10 分，是最划算的提分项。

---

## 严格按教材逐条对照（实训 1-6）

| 实训 | 核心要求 | 当前状态 | 说明 |
|------|---------|---------|------|
| **实训 1** | `MyBtnStyle` / `MyEditStyle` 自定义样式 + `btn_bg_selector` | ⚠️ 部分满足 | M3 重构后样式语义被 M3 组件替代，原始自定义样式可能已不存在 |
| **实训 1** | `style.xml` / `colors.xml` / `themes.xml` 配置 | ✅ 满足 | 绿色主色板 + M3 主题配置完整 |
| **实训 1** | `activity_main.xml` / `activity_register.xml` 布局 | ✅ 满足 | 实训自主动手部分已完成 |
| **实训 2** | `Intent` + `startActivity()` 跳转 | ✅ 满足 | 登录→欢迎 + 登录→注册 |
| **实训 2** | `Toast.makeText()` 提示 | ✅ 满足 | 各类提示均已使用 |
| **实训 2** | 非空校验 + 密码比对 → Toast | ✅ 满足 | 功能对齐 |
| **实训 3** | 转为 SQLite（替代 MySQL） | ✅ 满足 | 有专门的 SQLite 替代说明文档 |
| **实训 3** | 子线程数据库操作 | ✅ 满足 | 全局统一 |
| **实训 3** | 三包结构 `bean/dao/dbunit` | ✅ 满足 | 分包正确 |
| **实训 4** | `UserDao.register()` 先 SELECT 判重再 INSERT | ✅ 满足 | `findByName() != null` 判断 |
| **实训 4** | `UserDao.login()` 按用户名+密码查询 | ✅ 满足 | `LoginResult` 枚举区分失败原因 |
| **实训 4** | 登录成功 → WelcomeActivity | ✅ 满足 | Intent 携带 userName |
| **实训 4** | 登录失败 → Toast 提示 | ✅ 满足 | 账号不存在 / 密码错误 |
| **实训 5** | `SharedPreferences("user_info")` | ❌ **不满足** | 实际为 `"spfRecord"` |
| **实训 5** | 验证登录成功且勾选"记住密码" → 写入 SP | ✅ 满足 | 文件名差异除外 |
| **实训 5** | 启动时读回账号密码填入输入框 | ✅ 满足 | `initData()` |
| **实训 5** | 登出时清空 SP（clear().apply()） | ⚠️ **偏离** | WelcomeActivity 仅置 `isAutoLogin=false`，保留 `isRemember` 以方便下次登录，**与实训 5 明确要求矛盾**（实训 5 明确说登出清空） |
| **实训 6** | Two CheckBox 联动 | ✅ 满足 | `OnCheckedChangeListener` |
| **实训 6** | 自动登录 → 直接跳欢迎页 | ✅ 满足 | `performAutoLogin()` |
| **实训 6** | 欢迎页"退出"→ isAutoLogin=false | ✅ 满足 | 代码行为符合 |
| **实训 6** | 注册成功回传用户名密码 setResult | ✅ 满足 | `EXTRA_USER_NAME/PASSWORD` 常量 |

---

## 优先级汇总

| 优先级 | 编号 | 问题 | 扣分 | 修复难度 |
|--------|------|------|------|---------|
| P1 | 码-1 | `MyBtnStyle` / `MyEditStyle` 教程练习题标记缺失 | 代码 2-3 分 | 低 |
| P1 | 码-2 | SP 文件名应为 `user_info` 而非 `spfRecord` | 代码 2-3 分 | 低 |
| P1 | 码-5 | `User.toString()` 暴露密码明文 | 安全/代码 1-2 分 | 低 |
| P1 | **文档-0~7** | 课程设计报告 7 个章节全部未完成 | 文档 10 分 | 中 |
| P2 | 码-5 | 实训 5 登出时应 `clear().apply()` | 实训 2-3 分 | 低 |
| P2 | 码-6 | `MainActivity` 缺少类注释 | 代码质量 1-2 分 | 低 |
| P2 | UI-1 | 注册页注册按钮在 weight 容器外部 | 界面 2-3 分 | 中 |
| P2 | UI-2 | 登录页注册按钮布局可优化 | 界面 1 分 | 低 |
| P3 | 码-4 | Todo.id 类型可改为 `long` 对齐 SQLite INTEGER | 代码 1 分 | 低 |
| P3 | 码-7 | `Todo.java` 多余构造函数 | 代码质量 1 分 | 低 |

---

## 后续建议

1. **按 P1 优先级修复**：SP 文件名、`MyBtnStyle`、`User.toString()`，这三项改动小但能提 5-8 分
2. **按 P2 修复 UI 布局问题**：注册页的 weight 容器问题，改动小
3. **优先写课程设计报告**：这是最高性价比提分块（0→8 分只需按模板填内容），建议按 7 段模板逐章节完成
4. 代码审查完后再跑一次自动化验证，确保 UI 修复后点击流程正常
