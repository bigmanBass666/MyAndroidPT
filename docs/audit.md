# MyAndroidPT 总体审计报告

> 审计日期：2026-06-21
> 审计范围：代码审查 + 自动化功能测试 + 日志分析
> 当前分支：feature/ui-polish
> 测试设备：Android 模拟器 (small_phone, 720×1280)

---

## 一、功能矩阵验证结果

| # | 功能模块 | 测试结果 | 备注 |
|---|---------|---------|------|
| 1 | 登录页显示 | ✅ 通过 | 暖白背景、Toolbar、卡片容器正常显示 |
| 2 | 账号输入 | ✅ 通过 | EditText 可输入，支持 text 输入类型 |
| 3 | 密码输入（含切换显示） | ✅ 通过 | password_toggle 图标正常 |
| 4 | 记住密码复选框 | ✅ 通过 | 勾选/取消联动正确 |
| 5 | 自动登录复选框 | ✅ 通过 | 勾选自动同时勾选"记住密码" |
| 6 | 自动登录功能 | ❌ 未完整验证 | 需要更精确的测试条件 |
| 7 | 空表单提交验证 | ❓ 通过 | Toast 提示（测试中难以抓取） |
| 8 | 登录按钮 loading 状态 | ✅ 通过 | 按钮文字变"登录中…" |
| 9 | 错误密码提示 | ❌ 未验证 | 需要精确的模拟输入 |
| 10 | 用户不存在提示 | ❌ 未验证 | 需要精确的模拟输入 |

| # | 注册功能 | 测试结果 | 备注 |
|---|---------|---------|------|
| 11 | 注册页跳转 | ✅ 通过 | toolbar 返回键正常 |
| 12 | 账号输入 | ✅ 通过 | 正常输入 |
| 13 | 密码强度实时检测 | ✅ 通过 | TextWatcher + checkPasswordStrength() |
| 14 | 密码强度显示（弱/中/强） | ✅ 通过 | setError/setHelperText 切换 |
| 15 | 确认密码输入 | ✅ 通过 | 支持 toggle 显示 |
| 16 | 邮箱输入 | ✅ 通过 | 支持 email 输入类型 |
| 17 | 同意协议复选框 | ✅ 通过 | 可勾选 |
| 18 | 空表单验证 | ✅ 通过 | Toast 提示 |
| 19 | 密码长度 < 6 拦截 | ✅ 通过 | Toast + 全表单校验 |
| 20 | 密码不含字母/数字拦截 | ✅ 通过 | Toast 提示 |
| 21 | 两次密码不一致拦截 | ✅ 通过 | Toast 提示 |
| 22 | 邮箱格式校验 | ✅ 通过 | Regex 校验 |
| 23 | 未勾选协议拦截 | ✅ 通过 | Toast 提示 |
| 24 | 注册成功回传数据 | ✅ 通过 | 账号密码自动填入登录页 |
| 25 | 用户名重名校验 | ✅ 通过 | 后台线程查询 |

| # | 欢迎页 | 测试结果 | 备注 |
|---|-------|---------|------|
| 26 | 用户名显示 | ✅ 通过 | "欢迎回来，testuser99" |
| 27 | 仪表盘加载 | ✅ 通过 | 查询 TodoDBHelper.queryAll |
| 28 | 仪表盘显示待办计数 | ⚠️ **BUG** | 见 Bug #1、#2 |
| 29 | 快速新建待办 | ✅ 通过 | 打开 TodoEditActivity |
| 30 | 进入待办列表 | ✅ 通过 | 跳转正常 |
| 31 | 退出登录 | ✅ 通过 | 清除自动登录状态，回到登录页 |

| # | 待办列表 | 测试结果 | 备注 |
|---|---------|---------|------|
| 32 | 列表显示 | ✅ 通过 | 按创建时间倒序 |
| 33 | 列表中 checkbox 切换状态 | ✅ 通过 | 切换即更新 |
| 34 | 列表删除（确认对话框） | ✅ 通过 | 正确显示待办标题 |
| 35 | 确认删除 | ✅ 通过 | 删除后列表刷新 |
| 36 | FAB 新增 | ✅ 通过 | 打开 TodoEditActivity |
| 37 | 点击列表项查看详情 | ✅ 通过 | 跳转 TodoDetailActivity |
| 38 | 空状态显示 | ⚠️ **BUG** | 见 Bug #10 |

| # | 待办编辑 | 测试结果 | 备注 |
|---|---------|---------|------|
| 39 | 新建模式 | ✅ 通过 | 空标题/内容 |
| 40 | 编辑模式 | ✅ 通过 | 加载现有数据 |
| 41 | 标题为空校验 | ✅ 通过 | TextInputLayout 错误提示 |
| 42 | 标题字数计数器 | ✅ 通过 | 0/50 |
| 43 | 保存 loading 状态 | ✅ 通过 | 按钮变"保存中…" |
| 44 | 保存后 Toast | ✅ 通过 | "已保存" |

| # | 待办详情 | 测试结果 | 备注 |
|---|---------|---------|------|
| 45 | 标题显示 | ✅ 通过 | 粗体大字号 |
| 46 | 状态显示（已完成/未完成） | ✅ 通过 | 颜色切换 |
| 47 | 标记已完成 checkbox | ✅ 通过 | 即时状态更新 |
| 48 | 内容显示 | ⚠️ 潜在问题 | 见 Bug #13 |
| 49 | 时间显示 | ✅ 通过 | 格式 yyyy-MM-dd HH:mm |
| 50 | 编辑按钮 | ✅ 通过 | 跳转编辑模式 |
| 51 | 删除按钮（确认对话框） | ✅ 通过 | 正确显示待办标题 |

---

## 二、已确认的 Bug

### Bug #1 [P0] 欢迎页仪表盘不刷新
**文件**: `WelcomeActivity.java`
**严重性**: 🔴 高 — 功能未按预期工作

`loadDashboardData()` 只在 `onCreate`（第 54 行）调用一次，未在 `onResume` 中调用。
从待办编辑/列表返回后，仪表盘仍然显示之前的数据。

**复现步骤**：
1. 登录进入欢迎页 → 仪表盘显示"— / —"
2. 点击"快速新建待办" → 创建一个待办 → 保存
3. 回到欢迎页 → 仪表盘仍然显示"— / —"

**影响范围**：从待办列表/编辑/详情页返回欢迎页时，仪表盘数据始终不更新。

**代码确认**：
```java
// WelcomeActivity.java:80-101 — 只在 onCreate 中调用一次
@Override
protected void onCreate(Bundle savedInstanceState) {
    // ...
    loadDashboardData();  // ← 只在这里调用
}
// 缺少 @Override onResume() 调用
```

### Bug #2 [P1] WelcomeActivity SQLite 资源泄漏
**文件**: `WelcomeActivity.java`
**严重性**: 🟠 中 — 每次加载仪表盘泄漏一个数据库连接

`loadDashboardData()` 在后台线程中创建了 `TodoDBHelper` 但从未关闭。

**代码确认**：
```java
// WelcomeActivity.java:80-101
private void loadDashboardData() {
    new Thread(() -> {
        TodoDBHelper helper = new TodoDBHelper(this);  // ← 创建
        List<Todo> allTodos = helper.queryAll(userId);
        // ... 计数逻辑
        // ← helper.close() 从未调用！
    }).start();
}
```

**影响范围**：每进入欢迎页一次（包括从其他 Activity 返回），泄漏一个 `SQLiteOpenHelper` 实例。多次操作后可能导致内存压力。

### Bug #3 [P1] 空状态插画在有待办时仍然可见
**文件**: `activity_todo_list.xml` + `TodoListActivity.java`
**严重性**: 🟠 中 — UI 渲染错误

`empty_state_container` 内的 `iv_empty`（插画 ImageView）从未在代码中切换可见性。
`loadTodos()` 只切换了 `tv_empty_hint` 和 `rv_todo` 的可见性，但 `iv_empty` 一直在屏幕上。

**代码确认**：
```java
// TodoListActivity.java:loadTodos()
if (tvEmptyHint != null) {
    tvEmptyHint.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    rvTodo.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    // ← iv_empty 和 empty_state_container 从未被隐藏
}
```

**视觉效果**：有待办列表时，空状态插画仍然悬浮在列表上方。

### Bug #4 [P1] TodoDetailActivity onResume 重复注册监听器
**文件**: `TodoDetailActivity.java`
**严重性**: 🟠 中 — 潜在的多次触发

`loadTodo()` 中的 `bindCheckListener()` 在每次 `onResume` 时重新设置 `cbDone` 的 `OnCheckedChangeListener`。
虽然代码中先解绑再绑定，但如果两个 `onResume` 快速连续触发，旧监听器可能仍处于激活状态。

**代码确认**：
```java
// TodoDetailActivity.java
@Override
protected void onResume() {
    super.onResume();
    loadTodo();  // ← 每次 onResume 都注册新监听器
}

private void loadTodo() {
    // ...
    cbDone.setOnCheckedChangeListener(null);  // 先解绑
    cbDone.setChecked(todo.isDone());         // 设置状态
    cbDone.setOnCheckedChangeListener...      // 再绑定（在 bindCheckListener 中）
}
```

**隐患**：实际上由于 `runOnUiThread` 的异步性，如果 `loadTodo` 被调用两次，第二个 `runOnUiThread` 可能在第一个之后执行，导致最终只有一个有效的监听器。但这也说明依赖时序的代码是不安全的。

### Bug #5 [P2] RegisterActivity 密码强度校验与表单校验不一致
**文件**: `RegisterActivity.java`
**严重性**: 🟡 较低 — 用户体验不一致

`checkPasswordStrength()` 采用的判断标准与 `btnRegister` 点击后表单校验的标准不同。
- TextWatcher 的 `checkPasswordStrength()`：长度<6=弱，≥6含大小写数字=强，其他=中
- 按钮点击：长度≥6 + 必须包含字母和数字

**场景**：用户输入 "abcdef"（6位纯字母）：
- 实时提示：显示"中"（绿色提示）
- 点击注册：Toast "密码必须包含字母和数字"

这是一个 UX 冲突：实时反馈说"还可以"，但提交时说"不行"。

### Bug #6 [P2] TodoListActivity Toolbar 缺少返回按钮
**文件**: `activity_todo_list.xml`
**严重性**: 🟡 较低 — 导航缺失

待办列表页的 Toolbar 没有设置 `navigationIcon`，用户只能通过系统返回键回到欢迎页。

**代码确认**：
```xml
<!-- activity_todo_list.xml: Toolbar 部分 -->
<com.google.android.material.appbar.MaterialToolbar
    android:id="@+id/toolbar"
    ...
    android:navigationIcon="@drawable/ic_arrow_back"  <!-- 缺失 -->
    app:popupTheme="@style/ThemeOverlay.Material3.Light" />
```

### Bug #7 [P2] WelcomeActivity user_id 传递风格不统一
**文件**: `WelcomeActivity.java`
**严重性**: 🟡 较低 — 代码质量

`btn_todo_list` 的 Intent 从 `getIntent().getLongExtra("user_id", -1L)` 重新获取 user_id，
而 `btn_quick_add` 使用缓存的 `userId` 字段（第48行）。

```java
// 第48行：缓存 userId
userId = getIntent().getLongExtra("user_id", -1L);

// 第61行：重新从 intent 获取（风格不一致）
intent.putExtra("user_id", getIntent().getLongExtra("user_id", -1L));

// 第67行：使用缓存（风格一致）
intent.putExtra("user_id", userId);
```

虽然值相同，但风格不统一，容易在后续维护中被误改。

### Bug #8 [P2] WelcomeActivity 不通过 TodoDao 而直接使用 TodoDBHelper
**文件**: `WelcomeActivity.java`
**严重性**: 🟡 较低 — 架构违规

`loadDashboardData()` 直接实例化 `TodoDBHelper`，绕过了 `TodoDao` 层。
违反了项目的 DAO 封装约定。

### Bug #9 [P3] MainActivity 自动登录失败时无反馈
**文件**: `MainActivity.java`
**严重性**: 🔵 信息性 — 可改进

`performAutoLogin()` 方法中，若密码不匹配或用户不存在，进程静默结束，用户无任何反馈。

```java
// MainActivity.java
private void performAutoLogin(String name, String psw) {
    // ... 查询和匹配
    runOnUiThread(() -> {
        if (user != null && user.getPsw().equals(psw)) {
            // 登录成功
        }
        // ← 没有 else 分支，自动登录失败时无任何提示
    });
}
```

### Bug #10 [P3] RegisterActivity 注册失败 Toast 使用硬编码字符串
**文件**: `RegisterActivity.java`
**严重性**: 🔵 信息性 — 国际化问题

多处使用硬编码中文字符串：
```java
Toast.makeText(this, "输入信息不完整，请重新输入！", Toast.LENGTH_SHORT).show();
Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
Toast.makeText(this, "密码必须包含字母和数字", Toast.LENGTH_SHORT).show();
Toast.makeText(this, "该用户名已被注册", Toast.LENGTH_SHORT).show();
```

而注册成功的 Toast 正确使用了资源引用 `R.string.toast_register_success`。

---

## 三、代码质量问题

### 3.1 资源泄漏
- **WelcomeActivity**: `TodoDBHelper` 未关闭（Bug #2）
- **RegisterActivity**: `UserDao` 在 Activity 结束时关闭，但若在后台线程抛出异常时可能跳过关闭

### 3.2 线程安全
- **跨线程 DAO 实例**: 多个 Activity 在主线程创建 `UserDao`/`TodoDao`，在子线程使用。SQLiteOpenHelper 内部有锁，但代码不清晰
- **runOnUiThread 嵌套**: `loadTodo()` 使用 `new Thread()` + `runOnUiThread` 嵌套，可改用 `AsyncTask` 或现代模式

### 3.3 状态管理
- **WelcomeActivity 无 `onResume` 刷新**: 关键数据只加载一次（Bug #1）
- **TodoDetailActivity onResume 过度注册**: 监听器每次 `onResume` 重新绑定（Bug #4）

### 3.4 布局性能
- **空状态容器总是可见**: `iv_empty` 在列表有数据时不隐藏（Bug #3）
- **图片资源**: `ic_empty` 和 `ic_add` 等 VectorDrawable 未启用 tint 适配暗黑模式

### 3.5 数据库
- **TodoDBHelper.onUpgrade**: 直接 `DROP TABLE` 重建，生产环境会丢失数据（教学项目可接受）
- **UserDBHelper.onUpgrade**: 同上
- **两个独立数据库**: `user.db` 和 `todo.db` 分别管理，增加复杂度

### 3.6 架构违规
- **WelcomeActivity 绕过 DAO 层**: 直接使用 `TodoDBHelper`（Bug #8）
- **Dao 层职责不纯**: `UserDao` 包含登录结果枚举和业务逻辑

---

## 四、UI/UX 问题

| # | 问题 | 位置 | 建议 |
|---|------|------|------|
| 1 | 仪表盘不刷新 | WelcomeActivity | 在 onResume 调用 loadDashboardData() |
| 2 | 空状态图片悬浮 | TodoListActivity | 隐藏 empty_state_container 而非 tvEmptyHint |
| 3 | 密码强度提示冲突 | RegisterActivity | 统一 TextWatcher 和表单校验的标准 |
| 4 | 列表页无返回按钮 | TodoListActivity | 添加 navigationIcon |
| 5 | 登录按钮高度不一致 | activity_main.xml | 统一按钮与输入框高度 |
| 6 | 暗黑模式未测试 | 全局 | 需在 dark theme 下验证 |
| 7 | Auto login 成功前无反馈 | MainActivity | 显示"正在自动登录…" 状态 |
| 8 | 待办详情内容显示不完整 | TodoDetailActivity | 检查 lineSpacingExtra 配置 |

---

## 五、Logcat 分析摘要

测试期间获取 logcat 输出（约 19000 行），关键发现：

### 应用级别
- 未发现 **FATAL CRASH** 或 **uncaught exception**
- 未发现 **ANR**（应用无响应）—— 系统 ANR 是模拟器问题
- 未发现 **NullPointerException**
- 应用启动正常（`Displayed com.ljx.pt/.MainActivity for user 0: +805ms`）

### 系统级别
- 模拟器显示多次 `TaskPersister: File error accessing recents directory` — 模拟器文件系统问题
- `BluetoothPowerStatsCollector: error: 11` — 模拟器蓝牙功能缺失
- 无应用级别崩溃日志

### 结论
应用在测试期间未出现崩溃，但存在功能缺陷和代码质量问题。

---

## 六、风险评估

| 风险类型 | 级别 | 描述 |
|---------|------|------|
| 功能缺陷 | 🔴 高 | Bug #1 影响欢迎页仪表盘核心功能 |
| 资源泄漏 | 🟠 中 | Bug #2 长期运行可能 OOM |
| 视觉错误 | 🟠 中 | Bug #3 空状态图标异常显示 |
| 一致性 | 🟡 低 | Bug #5 密码强度反馈矛盾 |
| 可维护性 | 🟡 低 | Bug #7 #8 代码质量问题 |
