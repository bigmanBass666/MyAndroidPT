# MyAndroidPT 修复计划

> 基于 `docs/audit.md` 审计结果，制定修复方案
> 当前分支：`feature/ui-polish`，修复完成后合并到 `main`

---

## 优先级定义

| 级别 | 含义 | 处理时限 |
|------|------|---------|
| 🔴 P0 | 功能缺陷，必须修复 | 立即 |
| 🟠 P1 | 功能/UI 错误，重要 | 当前迭代 |
| 🟡 P2 | 代码质量/架构违规 | 下一个迭代 |
| 🔵 P3 | 可改进项 | 择机处理 |

---

## 任务概要

```
Phase 1: P0 Bug 修复
├── [P0] Fix#1: WelcomeActivity 仪表盘 onResume 刷新
├── [P1] Fix#2: WelcomeActivity SQLite 连接泄漏
├── [P1] Fix#3: TodoListActivity 空状态插画显示异常
└── [P1] Fix#4: TodoDetailActivity 监听器重复注册

Phase 2: UI/UX 修复
├── [P2] Fix#5: RegisterActivity 密码强度校验一致化
├── [P2] Fix#6: TodoListActivity Toolbar 添加返回按钮
└── [P2] Fix#7: WelcomeActivity user_id 传递统一

Phase 3: 代码重构
├── [P2] Fix#8: WelcomeActivity 改用 TodoDao
├── [P3] Fix#9: MainActivity 自动登录失败反馈
└── [P3] Fix#10: RegisterActivity 字符串资源化
```

---

## Phase 1 — P0/P1 必须修复

### Fix #1 [P0] WelcomeActivity 仪表盘 onResume 刷新

**问题**：`loadDashboardData()` 只在 `onCreate` 调用，返回时不刷新。
**方案**：添加 `onResume` 方法调用 `loadDashboardData()`。

**涉及文件**：
- `app/src/main/java/com/ljx/pt/WelcomeActivity.java`

**修改内容**：
```java
@Override
protected void onResume() {
    super.onResume();
    loadDashboardData();
}
```

**验证**：
1. 登录进入欢迎页 → 仪表盘显示统计数据
2. 新建待办 → 返回 → 仪表盘计数更新 "+1"
3. 删除待办 → 返回 → 仪表盘计数更新 "-1"
4. 切换待办完成状态 → 返回 → 已完成/待完成计数切换

---

### Fix #2 [P1] WelcomeActivity SQLite 连接泄漏

**问题**：`loadDashboardData()` 中 `TodoDBHelper` 创建后未关闭。
**方案**：在后台线程中使用 try-with-resources（需手动 close），或在查询后显式关闭 helper。

**涉及文件**：
- `app/src/main/java/com/ljx/pt/WelcomeActivity.java`

**修改内容**：
```java
private void loadDashboardData() {
    new Thread(() -> {
        TodoDBHelper helper = new TodoDBHelper(this);
        try {
            List<Todo> allTodos = helper.queryAll(userId);
            // ... 计数逻辑
        } finally {
            helper.close();  // ← 确保关闭
        }
        // ... runOnUiThread 更新 UI
    }).start();
}
```

**验证**：
- 多次进出欢迎页后，SQLite 连接数不增长
- `adb shell dumpsys dbinfo com.ljx.pt` 确认连接数正常

---

### Fix #3 [P1] TodoListActivity 空状态插画显示异常

**问题**：`loadTodos()` 只切换了 `tvEmptyHint` 和 `rv_todo` 的可见性，但 `iv_empty` 和 `empty_state_container` 一直可见。
**方案**：切换整个 `empty_state_container` 的可见性，而非 `tvEmptyHint`。

**涉及文件**：
- `app/src/main/java/com/ljx/pt/TodoListActivity.java`

**修改内容**：
```java
// TodoListActivity.java — 添加 emptyStateContainer 字段
private View emptyStateContainer;
// 在 onCreate 中：
emptyStateContainer = findViewById(R.id.empty_state_container);

// loadTodos() 中修改为：
if (list.isEmpty()) {
    emptyStateContainer.setVisibility(View.VISIBLE);
    rvTodo.setVisibility(View.GONE);
} else {
    emptyStateContainer.setVisibility(View.GONE);
    rvTodo.setVisibility(View.VISIBLE);
}
```

**验证**：
1. 有待办时 → 列表显示，空状态完全隐藏
2. 删除所有待办 → 列表隐藏，空状态（插画+文字）完全显示

---

### Fix #4 [P1] TodoDetailActivity onResume 重复注册监听器

**问题**：`loadTodo()` 在 `onResume` 中重新设置 `cbDone` 的监听器。
**方案**：将绑定监听器逻辑移到 `onCreate`，`onResume` 只加载数据不重新绑定。

**涉及文件**：
- `app/src/main/java/com/ljx/pt/TodoDetailActivity.java`

**修改内容**：
```java
// 在 onCreate 中绑定监听器（只一次）
cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
    new Thread(() -> {
        todoDao.updateStatus(todoId, isChecked);
        runOnUiThread(() -> {
            if (currentTodo != null) {
                currentTodo.setDone(isChecked);
                tvStatus.setText(isChecked ? "已完成" : "未完成");
                tvStatus.setTextColor(isChecked ?
                    getColor(R.color.green_500) : getColor(R.color.grey_500));
            }
        });
    }).start();
});

// loadTodo 中移除非绑定操作
private void loadTodo() {
    new Thread(() -> {
        Todo todo = todoDao.queryById(todoId);
        runOnUiThread(() -> {
            if (todo == null) { /* ... finish */ return; }
            currentTodo = todo;
            tvTitle.setText(todo.getTitle());
            tvStatus.setText(todo.isDone() ? "已完成" : "未完成");
            tvStatus.setTextColor(todo.isDone() ? ... : ...);
            // 只设值，不绑定
            cbDone.setOnCheckedChangeListener(null);  // 临时解除
            cbDone.setChecked(todo.isDone());
            cbDone.setOnCheckedChangeListener(doneListener);  // 恢复（引用绑定的 listener 对象）
            tvContent.setText(...);
            tvTime.setText(...);
        });
    }).start();
}
```

**验证**：
1. 进入详情页 → checkbox 状态正确
2. 切换 checkbox → 状态更新正确
3. 返回列表再进入 → checkbox 状态正确
4. 连续快速切换 → 无异常触发

---

## Phase 2 — P2 UI/UX 修复

### Fix #5 [P2] RegisterActivity 密码强度校验一致化

**问题**：TextWatcher 实时提示与表单提交校验标准不一致。
**方案**：统一两者使用完全相同的校逻辑，或让 TextWatcher 使用与表单提交相同的判断规则。

**涉及文件**：
- `app/src/main/java/com/ljx/pt/RegisterActivity.java`

**修改内容**：
调整 `checkPasswordStrength()` 使其与表单校验一致：
- 弱（0）：长度 < 6 **或** 不含字母或数字
- 中（1）：长度 ≥ 6，包含字母和数字，但不满足强条件
- 强（2）：长度 ≥ 8，包含大小写字母和数字

```java
private int checkPasswordStrength(String psw) {
    if (psw.length() < 6) return 0;
    boolean hasLetter = false, hasDigit = false;
    boolean hasUpper = false, hasLower = false;
    for (char c : psw.toCharArray()) {
        if (Character.isUpperCase(c)) { hasUpper = true; hasLetter = true; }
        else if (Character.isLowerCase(c)) { hasLower = true; hasLetter = true; }
        else if (Character.isDigit(c)) hasDigit = true;
    }
    // 必须同时包含字母和数字
    if (!hasLetter || !hasDigit) return 0;  // 弱
    if (psw.length() >= 8 && hasUpper && hasLower && hasDigit) return 2;  // 强
    return 1;  // 中
}
```

同时，可以移除 `btnRegister` 点击处理中重复的密码校验逻辑（第 105-124 行），因为 `checkPasswordStrength()` 已经覆盖了这些检查，且 TextWatcher 中的 return 0 已经能阻止弱密码。

**验证**：
1. 输入 "abc123" → 显示"中"
2. 输入 "abc" → 显示"弱"
3. 输入 "123456" → 显示"弱"（之前显示"中"）
4. 输入 "Test99Pass" → 显示"强"
5. 点击注册时无需再单独检查字母数字

---

### Fix #6 [P2] TodoListActivity Toolbar 添加返回按钮

**问题**：待办列表页 Toolbar 缺少返回按钮。
**方案**：在布局中添加 `navigationIcon`。

**涉及文件**：
- `app/src/main/res/layout/activity_todo_list.xml`

**修改内容**：
```xml
<com.google.android.material.appbar.MaterialToolbar
    android:id="@+id/toolbar"
    ...
    android:navigationIcon="@drawable/ic_arrow_back"
    app:popupTheme="@style/ThemeOverlay.Material3.Light" />
```

**注意**：此 Toolbar 在 `CoordinatorLayout` 中，需确保添加返回按钮后其功能正常。

---

### Fix #7 [P2] WelcomeActivity user_id 传递统一

**问题**：`btn_todo_list` 从 `getIntent()` 重新获取 user_id，而 `btn_quick_add` 使用缓存字段。
**方案**：统一使用缓存的 `userId` 字段。

**涉及文件**：
- `app/src/main/java/com/ljx/pt/WelcomeActivity.java`

**修改内容**：
```java
// onClick 中 btn_todo_list 分支（第61行）
Intent intent = new Intent(this, TodoListActivity.class);
intent.putExtra("user_id", userId);  // ← 使用缓存字段
startActivity(intent);
```

---

## Phase 3 — P2/P3 代码重构

### Fix #8 [P2] WelcomeActivity 改用 TodoDao

**问题**：`WelcomeActivity` 直接创建 `TodoDBHelper`，绕过 `TodoDao` 层。
**方案**：使用 `TodoDao` 替代。

**涉及文件**：
- `app/src/main/java/com/ljx/pt/WelcomeActivity.java`

**修改内容**：
```java
// 在 loadDashboardData 中
new Thread(() -> {
    TodoDao todoDao = new TodoDao(this, userId);
    try {
        List<Todo> allTodos = todoDao.queryAll();
        // ... 计数逻辑
    } finally {
        todoDao.close();
    }
    // ... UI 更新
}).start();
```

**注意**：此修改自动解决了 Fix #2（资源泄漏），因为 `TodoDao.close()` 内部会调用 `TodoDBHelper.close()`。

---

### Fix #9 [P3] MainActivity 自动登录失败反馈

**问题**：`performAutoLogin()` 失败时无用户反馈。
**方案**：添加失败 Toast 提示。

**涉及文件**：
- `app/src/main/java/com/ljx/pt/MainActivity.java`

**修改内容**：
```java
private void performAutoLogin(String name, String psw) {
    new Thread(() -> {
        userDao = new UserDao(MainActivity.this);
        User user = userDao.findByName(name);
        long userId = user != null ? user.getId() : 0;
        runOnUiThread(() -> {
            if (user != null && user.getPsw().equals(psw)) {
                // 登录成功
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                intent.putExtra("userName", name);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
            } else {
                // 自动登录失败，静默停留在登录页
                // 可以不清除用户已填入的账号密码
            }
        });
    }).start();
}
```

实际上此处无需额外代码，自动登录失败时用户已处于登录页，可以手动登录。仅在 UI 上可增加轻微反馈如编辑框高亮。

---

### Fix #10 [P3] RegisterActivity 字符串资源化

**问题**：多处硬编码 Toast 字符串。
**方案**：抽取到 `strings.xml`。

**涉及文件**：
- `app/src/main/java/com/ljx/pt/RegisterActivity.java`
- `app/src/main/res/values/strings.xml`

**strings.xml 新增**：
```xml
<string name="toast_incomplete_input">输入信息不完整，请重新输入！</string>
<string name="toast_password_too_short">密码长度至少6位</string>
<string name="toast_password_need_letter_digit">密码必须包含字母和数字</string>
<string name="toast_password_mismatch">两次输入的密码不一致，请重新输入！</string>
<string name="toast_invalid_email">请输入正确的邮箱地址</string>
<string name="toast_agree_protocol">请勾选同意用户协议</string>
<string name="toast_user_exists">该用户名已被注册</string>
<string name="toast_register_failed">注册失败，请稍后重试</string>
```

---

## 实施路径

### 依赖关系

```
Phase 1（P0/P1）
├── Fix#1 — 无依赖
├── Fix#2 — 无依赖
├── Fix#3 — 无依赖
└── Fix#4 — 无依赖

Phase 2（P2 UI）
├── Fix#5 — 无依赖
├── Fix#6 — 无依赖
└── Fix#7 — 无依赖

Phase 3（P2/P3 重构）
├── Fix#8 — 无依赖（但会 supersede Fix#2）
├── Fix#9 — 无依赖
└── Fix#10 — 无依赖
```

所有修复**互不依赖**，可并行执行。建议按优先级顺序依次处理。

### 建议执行顺序

```
Week 1: Phase 1 所有修复
  Day 1: Fix#1 (WelcomeActivity onResume)
  Day 2: Fix#2 (WelcomeActivity 连接泄漏) + Fix#3 (空状态插画)
  Day 3: Fix#4 (TodoDetailActivity 监听器)

Week 2: Phase 2 修复
  Day 1: Fix#5 (密码强度一致化) + Fix#6 (返回按钮)
  Day 2: Fix#7 (user_id 统一)

Week 3: Phase 3 重构
  Day 1: Fix#8 (改用 TodoDao)
  Day 2: Fix#9 + Fix#10

Week 4: 编译验证 + 功能测试
  Day 1: gradlew assembleDebug 编译确认
  Day 2: 模拟器全流程回归测试
  Day 3: 截图存档 + 合并 main
```

### 编译验证

每完成一个 Fix，执行：
```bash
cd app && gradlew.bat assembleDebug
```

全部完成后执行完整验证：
```bash
cd app && gradlew.bat assembleDebug && gradlew.bat lint
```

---

## 回归测试清单

- [ ] 登录：账号密码输入 → 登录 → 来到欢迎页
- [ ] 登录：错误的密码 → Toast 提示
- [ ] 登录：空的账号/密码 → Toast 提示
- [ ] 记住密码：勾选后重启应用 → 账号密码保留
- [ ] 自动登录：勾选后重启应用 → 自动进入欢迎页
- [ ] 注册：所有字段填入 → 注册成功 → 返回登录页自动填入
- [ ] 注册：密码强度实时提示（弱/中/强）
- [ ] 欢迎页：仪表盘显示待办统计
- [ ] 欢迎页：快速新建 → 创建待办 → 返回后仪表盘更新
- [ ] 欢迎页：进入待办列表 → 返回后仪表盘更新
- [ ] 欢迎页：退出登录 → 回到登录页
- [ ] 待办列表：空状态显示（插画+文字）
- [ ] 待办列表：创建待办后出现在列表
- [ ] 待办列表：checkbox 切换状态
- [ ] 待办列表：删除确认对话框
- [ ] 待办列表：FAB 新增
- [ ] 待办列表：Toolbar 返回按钮
- [ ] 待办编辑：新建模式 → 空标题校验
- [ ] 待办编辑：编辑模式 → 加载数据 → 修改 → 保存成功
- [ ] 待办详情：标题/状态/内容/时间正确显示
- [ ] 待办详情：标记已完成/未完成
- [ ] 待办详情：编辑按钮跳转
- [ ] 待办详情：删除确认