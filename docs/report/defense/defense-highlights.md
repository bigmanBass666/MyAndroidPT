# MyAndroidPT 期末答辩亮点提案

> 生成日期：2026-06-23
> 用途：讨论文档，确定 5 分钟答辩的展示内容

---

## 原则

- 考试要求（注册登录、待办 CRUD、记住密码、自动登录）全部已实现，用一句话带过
- 重点全放在**考试要求之外**的差异点
- 只讲能现场指代码给老师看的

---

## 亮点总览

### 第一梯队（必讲，老师一眼能看出「别人没有」的）

| # | 亮点 | 一句话说 | 涉及文件 | 预计时长 |
|---|------|---------|---------|---------|
| 1 | **M3 双主题完整体系** | Light/Dark 双模式自动切换，完整的语义色值+Shape+Typography 体系 | `themes.xml` ×2、`colors.xml`、`type.xml`、`dimens.xml` | 50s |
| 2 | **密码强度实时检测** | 边输入边显示弱/中/强，三段彩色提示 | `RegisterActivity.java` → `checkPasswordStrength()` | 45s |
| 3 | **示例待办自动创建** | 新用户注册后自动创建 3 条示例待办，其中 1 条已完成 | `RegisterActivity.java` → insert 后初始化循环 | 45s |
| 4 | **WelcomeActivity 动态仪表盘** | 纯代码构建的统计卡片+最近待办预览，引用主题色+动态创建 View+Ripple 效果 | `WelcomActivity.java` → `loadDashboardData()` + `updateRecentTodos()` | 50s |

### 第二梯队（选 2-3 个，深度超过基本要求）

| # | 亮点 | 考试要求只到 | 实际做了 |
|---|------|------------|---------|
| 5 | **注册回填流程** | 注册成功跳回登录页 | 还预填了账号密码到输入框（`registerForActivityResult`） |
| 6 | **CheckBox 双向联动** | 各自独立勾选 | 勾选自动登录→自动勾选记住密码；取消记住密码→自动取消自动登录 |
| 7 | **多用户数据隔离** | 单用户 CRUD | TodoDao 强制传入 userId、双独立数据库、删除无参构造器防幽灵数据 |
| 8 | **加载状态处理** | — | 按钮文字切"登录中…" + 禁用防重复提交 |
| 9 | **删除确认对话框** | 删除功能 | 弹出 AlertDialog 确认 + 显示待办标题 |
| 10 | **空状态 UI 设计** | 空列表 | 插画 + 引导文案，有数据时完全隐藏 |

### 第三梯队（单个不大，老师追问时展开）

| # | 亮点 |
|---|------|
| 11 | 3 个独立菜单文件（欢迎页、详情页、编辑页各一个） |
| 12 | 12 个矢量图标（person / lock / email / note / delete / edit / save / add 等） |
| 13 | 应用品牌：App 名"MyAndroid" + tagline"记录每一个完成时刻" |
| 14 | 4 个仪器化测试类（`UserDaoTest` / `TodoDBHelperTest` / `TodoDetailActivityTest` / `TodoEditActivityTest`） |
| 15 | 完整的 dimens.xml 间距/尺寸体系（gap_mini/small/medium、page_padding、form_field_spacing） |
| 16 | 详情页 Chip 状态标签（已完成绿色 / 未完成灰色） |
| 17 | 退出登录延迟销毁（Handler postDelayed 800ms 确保 Toast 显示完整） |

---

## 第一梯队逐项展开

### 亮点 1：M3 双主题完整体系

**位置：** `app/src/main/res/values/themes.xml` + `values-night/themes.xml`

**一句话：**
> "项目使用了 Material Design 3 的 DayNight 主题，明暗模式自动切换。"

**现场展示：**
1. 打开模拟器/真机 → 系统设置切深色模式
2. 回到 App → 观察所有页面自动变暗
3. 切回亮色 → 恢复

**老师可能的追问：**

| 追问 | 回答要点 |
|------|---------|
| 「M3 和默认主题有什么区别？」 | M3 有语义色值体系（Primary/Secondary/Tertiary/Error/Surface），不再需要手动定义按钮背景色 |
| 「深色主题怎么实现的？」 | 两个 `themes.xml`：亮色用 `surface_light`，深色用 `surface_dark`，DayNight 主题自动根据系统设置切换 |
| 「你的颜色体系怎么定义的？」 | 21 个语义色值：4 个主色系（green/teal/amber/red）+ Surface 分层色 + 深浅两套 |
| 「Shape 是什么？」 | 5 级圆角体系（ExtraSmall~ExtraLarge），统一控件的圆角风格 |

**涉及代码：**
- `themes.xml`：`parent="Theme.Material3.DayNight.NoActionBar"`，各 `colorPrimary` / `colorSecondary` / … 定义
- `values-night/themes.xml`：深色模式的 `colorSurface` / `colorOnSurface` / … 覆盖
- `type.xml`：7 级 Typography（HeadlineSmall ~ LabelSmall）
- `dimens.xml`：间距/尺寸体系

---

### 亮点 2：密码强度实时检测

**位置：** `RegisterActivity.java` 第 55-87 行（TextWatcher）+ 第 188-201 行（checkPasswordStrength）

**一句话：**
> "注册时密码输入框会实时检测密码强度，显示弱/中/强三段式提示。"

**现场展示：**
1. 打开注册页
2. 输入 `abc` → 显示"弱"
3. 输入 `abc123456` → 显示"中"
4. 输入 `Test99Pass` → 显示"强"

**老师可能的追问：**

| 追问 | 回答要点 |
|------|---------|
| 「检测逻辑怎么写的？」 | 长度 < 6 → 弱；必须同时含字母和数字否则弱；长度 ≥ 8 + 含大小写字母和数字 → 强；其余 → 中 |
| 「实时反馈怎么实现的？」 | `TextWatcher` 绑定 `etPassword`，每次输入变化调用 `checkPasswordStrength()`，结果通过 `tilPassword.setHelperText()` 或 `setError()` 显示 |
| 「为什么不用正则？」 | 逐个字符遍历 + 布尔标志位组合，可读性更好，性能也足够 |
| 「弱密码能提交吗？」 | 不能，实时检测和提交校验统一标准，弱密码在按钮点击时会被 Toast 拦截 |

**关键代码片段：**
```java
private int checkPasswordStrength(String psw) {
    if (psw.length() < 6) return 0;                          // 弱
    boolean hasLetter = false, hasDigit = false;
    boolean hasUpper = false, hasLower = false;
    for (char c : psw.toCharArray()) {                       // 逐个字符分类
        if (Character.isUpperCase(c)) { hasUpper = true; hasLetter = true; }
        else if (Character.isLowerCase(c)) { hasLower = true; hasLetter = true; }
        else if (Character.isDigit(c)) hasDigit = true;
    }
    if (!hasLetter || !hasDigit) return 0;                   // 不同时含字母数字 → 弱
    if (psw.length() >= 8 && hasUpper && hasLower && hasDigit) return 2; // 强
    return 1;                                                // 中
}
```

---

### 亮点 3：示例待办自动创建

**位置：** `RegisterActivity.java` 第 132-161 行

**一句话：**
> "新用户注册成功后，自动创建 3 条示例待办，让首页不空。"

**现场展示：**
1. 注册一个新用户（如 `test123`/`pass1234`/`test@test.com`）
2. 登录后 → 欢迎页仪表盘直接显示统计数据
3. 进入待办列表 → 直接看到 3 条示例
4. 其中 1 条已完成（"查看待办列表"），2 条未完成

**老师可能的追问：**

| 追问 | 回答要点 |
|------|---------|
| 「什么时候创建示例待办？」 | `RegisterActivity` 的 `insert` 成功后，立即查到新用户的 `userId`，用 `TodoDao` 创建 3 条 |
| 「会不会重复创建？」 | 示例创建在注册流程中，注册只触发一次，不会重复 |
| 「创建完后怎么通知用户？」 | 每条创建完后弹 Toast"已为您创建示例待办" |

**关键代码片段：**
```java
// RegisterActivity.java — 注册成功后自动创建示例待办
TodoDao todoDao = new TodoDao(RegisterActivity.this, newUser.getId());
long now = System.currentTimeMillis();

Todo t1 = new Todo();
t1.setTitle("欢迎使用 MyAndroid！");
t1.setContent("试试编辑、标记完成、删除 — 所有操作都可以在这里完成。");
t1.setDone(false);
t1.setCreateTime(now);
todoDao.insert(t1);
// … t2（"试着标记我为已完成"）／ t3（"查看待办列表"）同理
```

---

### 亮点 4：WelcomeActivity 动态仪表盘

**位置：** `WelcomeActivity.java` 第 126-250 行

**一句话：**
> "欢迎页不是简单的文字，而是一个动态仪表盘，显示待办统计和最近 3 条待办预览。"

**现场展示：**
1. 登录后 → 显示"你好，xxx"
2. 仪表盘显示：已完成 N 条 / 待完成 N 条
3. 下方展示最近 3 条待办的标题+时间
4. 每条可点击进入详情
5. 去编辑页新建一条 → 返回 → 仪表盘自动刷新

**老师可能的追问：**

| 追问 | 回答要点 |
|------|---------|
| 「为什么不直接用 XML 写布局？」 | 因为列表项是动态的（数量、内容、状态随时变化），用代码构建更灵活 |
| 「View 怎么引用主题色？」 | 用 `TypedValue()` + `getTheme().resolveAttribute(R.attr.colorOnSurface, …)` 解析当前主题色 |
| 「最近待办怎么排序的？」 | SQL 查询按 `create_time DESC` 排序，Java 端只取前 3 条 |
| 「从其他页面回来后数据会刷新吗？」 | 会的，`loadDashboardData()` 在 `onResume` 中也调用了，每次回到欢迎页都重新加载 |
| 「这个代码量多少？」 | 仪表盘相关代码约 90 行（加载逻辑）+ 60 行（动态 UI 构建），共约 150 行 |

**关键代码结构：**
```java
// 加载逻辑（子线程）
private void loadDashboardData() {
    new Thread(() -> {
        TodoDao todoDao = new TodoDao(this, userId);
        try {
            List<Todo> allTodos = todoDao.queryAll();
            // 统计 done/pending + 取前 3 条
            runOnUiThread(() -> { /* 更新 UI */ });
        } finally {
            todoDao.close();
        }
    }).start();
}

// 动态 UI 构建
private void updateRecentTodos(List<Todo> todos) {
    recentTodosContainer.removeAllViews();
    for (int i = 0; i < todos.size(); i++) {
        // 动态创建 LinearLayout + TextView + 分割线
        // TypedValue 解析主题色
        // 设置 Ripple 点击效果
        // 组装到容器
    }
}
```

---

## 第二梯队展开（选讲）

### 亮点 5：注册回填流程

**位置：** `MainActivity.java` → `registerLauncher` + `RegisterActivity.java` → `setResult()`

**一句话：**
> "注册成功后，自动跳回登录页，并把账号密码填好，不用重输。"

**关键技术点：**
- `registerForActivityResult(new StartActivityForResult(), …)` 接收回传
- `RegisterActivity.setResult(RESULT_OK, intent.putExtra("userName", name).putExtra("password", psw))`
- 裸字符串 key `"userName"` / `"password"`（无常量定义——可提一句这是改进点）

---

### 亮点 6：CheckBox 双向联动

**位置：** `MainActivity.java` 的 CheckBox `OnCheckedChangeListener`

**一句话：**
> "勾选自动登录→自动勾选记住密码；取消记住密码→自动取消自动登录。"

**联动规则：**
| 操作 | 记住密码 | 自动登录 |
|------|---------|---------|
| 用户勾选自动登录 | ✅ 自动勾选 | ✅ |
| 用户取消记住密码 | ❌ | ❌ 自动取消 |
| 用户勾选记住密码 | ✅ | 不变 |
| 用户取消自动登录 | 不变 | ❌ |

---

### 亮点 7：多用户数据隔离

**位置：** `TodoDao.java` / `TodoDBHelper.java` / 各 Activity 的 userId 传递

**一句话：**
> "两个独立 SQLite 数据库，userId 强制传参，用户 A 看不到用户 B 的待办。"

**技术实现：**
- 用户数据 → `user.db`（`UserDBHelper`）
- 待办数据 → `todo.db`（`TodoDBHelper`）
- `TodoDao` 只有一个带 `(Context, long userId)` 的构造器
- 所有 CRUD 都携带 `WHERE user_id = ?`
- 删除了旧的无参构造器，编译期就防止幽灵数据

---

## 答辩流程建议

```
0:00  一句话开场 + 盖过考试要求

0:10  亮点① M3 双主题
      现场切深色模式，展示所有页面自动适配
      → 老师追问：怎么实现的？

0:55  亮点② 密码强度检测
      注册页输入不同密码，展示弱/中/强
      → 老师追问：检测逻辑？

1:40  亮点③ 示例待办
      新注册用户 → 登录后直接看到数据
      → 老师追问：什么时候创建的？

2:25  亮点④ 仪表盘
      展示统计和最近待办预览
      → 老师追问：为什么用代码构建？

3:10  选一个第二梯队
      推荐 CheckBox 联动 + 多用户隔离（一句话带过）

3:30  收尾
      "以上是 MyAndroidPT 在考试要求之外的主要特色。"

3:30~5:00  老师自由提问
```

---

## 待定事项

- [ ] 你确定要讲的第一梯队亮点（可以全讲，也可以砍到 3 个）
- [ ] 第二梯队你选哪几个讲？
- [ ] 你最有信心现场指代码的是哪些？
- [ ] 老师如果问「代码测试」相关的问题，你要不要准备？项目有 4 个测试类
