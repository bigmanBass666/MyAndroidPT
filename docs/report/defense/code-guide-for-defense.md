# MyAndroidPT 代码理解指南（答辩专用）

> 读者定位：对 Android 编程只有基础认知，需要快速掌握核心代码以便应对答辩追问
> 目标：老师问到哪个文件、哪段逻辑，你能直接打开指给他看，并且说出「这段代码做了什么」

---

## 一、项目文件骨架（先认路）

整个项目的 Java 代码放在 `app/src/main/java/com/ljx/pt/` 下面，一共 **8 个 Activity + 4 个工具类**：

```
com/ljx/pt/
├── MainActivity.java          ← 登录页（程序入口）
├── RegisterActivity.java      ← 注册页
├── WelcomeActivity.java       ← 欢迎页（登录后看到的第一页）
├── TodoListActivity.java      ← 待办列表页
├── TodoDetailActivity.java    ← 待办详情页
├── TodoEditActivity.java      ← 新增/编辑待办页
├── bean/
│   ├── User.java              ← 用户"模板"（id / 用户名 / 密码 / 邮箱）
│   └── Todo.java              ← 待办"模板"（id / 标题 / 内容 / 状态 / 时间）
├── dao/
│   └── TodoDao.java           ← 待办数据库操作（增删改查都在这里）
├── dbunit/
│   ├── UserDBHelper.java      ← 用户数据库（user.db，存账号密码）
│   └── TodoDBHelper.java      ← 待办数据库（todo.db，存待办事项）
└── adapter/
    └── TodoAdapter.java       ← 把待办数据"翻译"成列表视图的适配器
```

### 老师可能问的基础问题

**"你项目有几个 Activity？"**
> 6 个：MainActivity（登录）、RegisterActivity（注册）、WelcomeActivity（欢迎）、TodoListActivity（待办列表）、TodoDetailActivity（待办详情）、TodoEditActivity（新增/编辑）

**"数据存在哪里？"**
> 两个 SQLite 数据库：`user.db`（存用户）和 `todo.db`（存待办），都在手机本地，不需要联网

---

## 二、演示路线图（老师问到哪，打开哪）

按答辩演示的顺序，标注每个环节要打开的代码文件和关键行数：

```
┌─ 开场：一句话盖过
│
├─ ① M3 双主题演示                  →  themes.xml（亮色主题定义）
│                                      values-night/themes.xml（深色主题覆盖）
│                                      老师追问"怎么切换"→ 打开这两文件对比
│
├─ ② 密码强度检测演示               →  RegisterActivity.java 第 55-87 行
│                                      老师追问"检测逻辑"→ 跳到第 188-201 行
│
├─ ③ 示例待办演示                   →  RegisterActivity.java 第 132-161 行
│                                      老师追问"什么时候创建"→ 指第 135 行 newUser
│
├─ ④ Welcome 仪表盘演示            →  WelcomeActivity.java 第 126-160 行（加载逻辑）
│                                                           第 162-250 行（UI 构建）
│                                      老师追问"为什么用代码"→ 指第 179 行 for 循环
│
└─ ⑤ 第二梯队选讲                   →  TodoDao.java（无参构造器被删除）
                                       MainActivity.java（CheckBox 联动）
```

---

## 三、核心代码逐段解读（按文件分）

### 1. RegisterActivity.java — 注册页（最重要的文件）

这个文件是答辩的**重中之重**，老师大概率会问这里。

#### 第一段：成员变量声明（第 29-36 行）

```java
private EditText etAccount;     // 账号输入框
private EditText etPassword;    // 密码输入框
// ... 还有确认密码、邮箱、复选框等
private UserDao userDao;        // 操作用户数据库的对象
private CheckBox cbAgree;       // 同意协议复选框（注意变量名：cb=CheckBox）
```

> **如果你忘了：** 看到 `et` 开头是输入框，`btn` 开头是按钮，`cb` 开头是复选框。

#### 第二段：密码强度检测（第 55-87 行）← 老师必问！

```java
etPassword.addTextChangedListener(new TextWatcher() {
    // ... 每次输入变化都触发 afterTextChanged
    @Override
    public void afterTextChanged(Editable s) {
        String psw = s.toString();
        if (psw.isEmpty()) { /* 清空提示 */ return; }

        int strength = checkPasswordStrength(psw);
        // 0=弱 1=中 2=强
        switch (strength) {
            case 0: tilPassword.setError("弱"); break;     // 红色错误提示
            case 1: tilPassword.setHelperText("中"); break; // 绿色友好提示
            case 2: tilPassword.setHelperText("强"); break;
        }
    }
});
```

**怎么给老师讲：**
> "我用了 `TextWatcher` 监听密码输入框，每输入一个字符都调用 `checkPasswordStrength()` 方法判断密码强弱，结果显示在输入框下方的辅助文字里。"

#### 第三段：checkPasswordStrength() 方法（第 188-201 行）← 可能会追问

```java
private int checkPasswordStrength(String psw) {
    if (psw.length() < 6) return 0;              // 长度不够→弱
    boolean hasLetter = false, hasDigit = false;  // 是否有字母、数字
    boolean hasUpper = false, hasLower = false;   // 是否有大小写
    for (char c : psw.toCharArray()) {            // 逐个字符判断
        if (Character.isUpperCase(c)) { hasUpper = true; hasLetter = true; }
        else if (Character.isLowerCase(c)) { hasLower = true; hasLetter = true; }
        else if (Character.isDigit(c)) hasDigit = true;
    }
    if (!hasLetter || !hasDigit) return 0;        // 不同时含字母数字→弱
    if (psw.length() >= 8 && hasUpper && hasLower && hasDigit) return 2; // 强
    return 1;                                     // 其余情况→中
}
```

**怎么给老师讲：**
> "就是一个 for 循环逐个字符分类，判断是否存在大写字母、小写字母、数字。
> 纯字母或纯数字都是弱，同时包含字母数字是中，大小写数字都有且长度够 8 位是强。"

#### 第四段：创建示例待办（第 132-161 行）← 亮点

```java
// 插入用户成功后：
int rows = userDao.insert(new User(name, psw, email));
if (rows > 0) {
    User newUser = userDao.findByName(name);  // 查到新用户的ID
    TodoDao todoDao = new TodoDao(this, newUser.getId()); // 用userId创建待办Dao

    Todo t1 = new Todo();
    t1.setTitle("欢迎使用 MyAndroid！");
    t1.setDone(false);
    todoDao.insert(t1);  // 插入到待办数据库

    Todo t2 = new Todo();  // 第 2 条
    Todo t3 = new Todo();  // 第 3 条（已完成的）
}
```

**怎么给老师讲：**
> "用户注册成功后，我查到他的 userId，然后用这个 userId 创建了 3 条示例待办，其中第 3 条直接标记为已完成。所以新用户登录后不会看到空表。"

---

### 2. WelcomeActivity.java — 欢迎页（第二个重点）

#### 第五段：仪表盘数据加载（第 126-160 行）← 老师可能会问

```java
private void loadDashboardData() {
    new Thread(() -> {                     // 开个子线程做数据库操作（防卡死）
        TodoDao todoDao = new TodoDao(this, userId);
        try {
            List<Todo> allTodos = todoDao.queryAll();  // 查出所有待办
            // 统计：数出已完成和待完成的各多少条
            int done = 0, pending = 0;
            for (Todo t : allTodos) {
                if (t.isDone()) done++; else pending++;
            }
            List<Todo> recentList = allTodos.subList(0, Math.min(3, 总条数));

            runOnUiThread(() -> {          // 切回主线程更新UI
                tvDoneCount.setText(String.valueOf(done));
                tvPendingCount.setText(String.valueOf(pending));
                updateRecentTodos(recentList);  // 构建最近待办列表
            });
        } finally {
            todoDao.close();               // 记得关闭数据库连接
        }
    }).start();
}
```

**怎么给老师讲：**
> "先开子线程防止主线程卡顿，查出所有待办后统计已完成和待完成的数量，再取最近的 3 条动态构建成 UI 列表。数据库用完马上关掉。"

**老师可能追问「为什么要用子线程？」**
> "Android 不允许在主线程做数据库操作，否则超过 5 秒会弹出 ANR 无响应对话框，所以必须开 `new Thread()`。"

#### 第六段：onResume 自动刷新（第 79-82 行）

```java
@Override
protected void onResume() {
    super.onResume();
    loadDashboardData();  // 每次回到这个页面都重新加载数据
}
```

**怎么给老师讲：**
> "`onResume` 是 Activity 每次显示到前台都会调用的方法。把数据加载放在这里，用户从编辑页新建完待办回来，仪表盘会自动更新。"

---

### 3. TodoAdapter.java — 待办列表适配器

#### 第七段：回调接口（第 25-31 行）

```java
public interface OnTodoActionListener {
    void onToggleDone(long todoId, boolean isDone);  // 切换完成状态
    void onDelete(long todoId, String todoTitle);    // 删除待办
    void onItemClick(long todoId);                   // 点击进入详情
}
```

**怎么给老师讲：**
> "这是一个回调接口，列表项发生状态切换、长按删除、点击查看时，会通知 TodoListActivity 去处理。"

#### 第八段：已完成/未完成状态样式（第 81-92 行）

```java
if (todo.isDone()) {
    holder.tvStatus.setText("✓");               // 已完成 → 绿色勾
    holder.tvStatus.setTextColor(0xFF4CAF50);    // 绿色
    // 背景变成半透明灰色
} else {
    holder.tvStatus.setText("●");               // 未完成 → 灰色圆点
    holder.tvStatus.setTextColor(0xFF9E9E9E);    // 灰色
    holder.itemView.setBackgroundColor(Color.TRANSPARENT);  // 透明背景
}
```

---

### 4. TodoDetailActivity.java — 待办详情页

#### 第九段：删除确认对话框（第 81-96 行）

```java
btnDelete.setOnClickListener(v -> {
    new AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除「" + 待办标题 + "」吗？")
            .setPositiveButton("删除", (dialog, which) -> {
                // 用户点了"删除"
                new Thread(() -> {
                    todoDao.delete(todoId);           // 数据库删掉
                    runOnUiThread(() -> {
                        Toast.makeText(this, "已删除").show();
                        new Handler(getMainLooper())
                            .postDelayed(() -> finish(), 800); // 等 Toast 显示完再关
                    });
                }).start();
            })
            .setNegativeButton("取消", null)           // 点取消什么都不做
            .show();
});
```

**怎么给老师讲：**
> "点删除按钮会弹出确认框，防止误删。确认后在子线程删除数据，弹 Toast，等 800 毫秒才关页面——这样 Toast 能完整显示出来。"

**老师可能追问「为什么要 postDelayed 800ms？」**
> "因为 `finish()` 会立即销毁 Activity，Toast 刚弹出来页面就关了，用户看不到提示。延迟 800ms 确保 Toast 显示完整。"

---

## 四、老师可能问的问题库（按概率排序）

### 高概率问题（准备好回答）

**Q1：「你这个项目用了哪些数据库？」**
> "两个 SQLite 数据库。`user.db` 存用户账号密码，`todo.db` 存待办事项。都在手机本地，不需要联网。"

**Q2：「你数据库操作是在主线程做的吗？不怕 ANR？」**
> "所有数据库操作都在 `new Thread()` 子线程里，更新 UI 的时候用 `runOnUiThread` 切回主线程。从来没出现过 ANR。"

**Q3：「你这个密码强度是怎么判断的？」**
> "一个 for 循环遍历密码的每个字符，判断有没有大写字母、小写字母、数字。纯字母或纯数字就是弱，同时包含字母数字是中，大小写数字都有且够 8 位是强。"

**Q4：「你的记住密码和自动登录是怎么实现的？」**
> "用 SharedPreferences 存储。登录成功时，如果勾选了记住密码，就把用户名密码写到 SP 里。下次启动检查 SP 有没有数据，有就直接回填。自动登录就是检查 SP 的自动登录标志位，为 true 就跳过登录页直接进欢迎页。"

**Q5：「你两个 CheckBox 是怎么联动的？」**
> "监听每个 CheckBox 的状态变化。用户勾选自动登录时，自动把记住密码也勾上。用户取消记住密码时，自动把自动登录也取消。这样逻辑不会冲突。"

### 中概率问题（准备一下）

**Q6：「为什么用 SQLite 不用 MySQL？」**
> "因为这是手机 App，SQLite 是 Android 自带的内嵌数据库，不需要装服务器、不需要联网、不需要配置，开箱即用。MySQL 需要远程服务器，手机端不适合直连。"

**Q7：「你的待办状态切换是怎么实现的？」**
> "列表页通过 TodoAdapter 中的 CheckBox 切换，会触发回调通知 TodoListActivity 更新数据库。详情页也有一个 CheckBox，改变后直接调用 `todoDao.updateStatus()` 更新。"

**Q8：「一个用户的待办会不会被另一个用户看到？」**
> "不会。TodoDao 强制要求传入 userId，所有查询都带 `WHERE user_id = ?` 条件。而且我把旧的不传 userId 的构造器删掉了，编译阶段就防止这种错误。"

**Q9：「你页面之间的数据是怎么传的？」**
> "通过 Intent 的 `putExtra()` 方法。比如登录页跳转到欢迎页时，把用户名和 userId 附加到 Intent 里。注册页回传数据用 `registerForActivityResult`。"

**Q10：「你这个深色模式怎么实现的？」**
> "用了 Material Design 3 的 DayNight 主题，系统切深色模式时自动切换到 `values-night/themes.xml` 定义的深色配色方案。亮色和深色各有一套颜色值，页面布局不用改。"

---

## 五、如果老师问到你不会的问题

记住三句话：

1. **「这个我还没深入研究，但我可以大概说一下我的理解……」**
2. **「这部分我主要是参考了官方文档 / 实训教材的做法。」**
3. **「这个地方如果我后续优化，我打算……」**

比硬编答案好得多。老师和颜悦色是你的优势。

---

## 六、快速复习清单（出门前看一遍）

- [ ] 项目有几个 Activity？分别叫什么？
- [ ] 数据库用的是什么（SQLite）？两个库分别叫什么？
- [ ] 密码强度检测逻辑（for 循环逐字符判断）
- [ ] 为什么用子线程做数据库操作（防 ANR）
- [ ] 记住密码用的什么（SharedPreferences）
- [ ] 待办 Delete 为什么延迟 800ms 关页面（Toast 显示完整）
- [ ] CheckBox 联动规则（自动登录→记住密码；取消记住密码→取消自动登录）
- [ ] TodoDao 为什么没有无参构造器（强制传 userId，防数据泄漏）
- [ ] M3 主题跟 M2 的区别（DayNight 明暗切换、语义色值体系）
- [ ] 欢迎页仪表盘用的哪个生命周期方法刷新（onResume）
