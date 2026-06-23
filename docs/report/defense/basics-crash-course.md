# Android 基础速成（答辩救命版）

> 场景：老师指着屏幕问「这是什么？」，你必须在 3 秒内答出来

---

## 一、布局文件 — 老师最爱问

### 你的布局文件在哪？

`app/src/main/res/layout/` 下面，6 个文件：

```
activity_main.xml         ← 登录页
activity_register.xml      ← 注册页
activity_welcome.xml       ← 欢迎页（登录后第一页）
activity_todo_list.xml     ← 待办列表页
activity_todo_detail.xml   ← 待办详情页
activity_todo_edit.xml     ← 新增/编辑待办页
item_todo.xml              ← 列表里每一行的样式
```

### 老师问「布局怎么构成的？」

**标准回答模板：**
> "用 XML 文件定义界面，根容器是 LinearLayout（线性布局），里面从上到下放 TextView（文字）、EditText（输入框）、Button（按钮）。每个控件用 `android:id` 设置唯一编号，Java 代码通过 `findViewById(R.id.xxx)` 找到它。"

### 三种最常见布局

| 布局 | 意思 | 在你的项目里 |
|------|------|------------|
| **LinearLayout** | 线性排列（从上到下/从左到右） | 登录页、注册页、欢迎页都用这个 |
| **ConstraintLayout** | 自由定位（可以放在任何位置） | 没有用 |
| **RecyclerView** | 列表（很多行数据自动复用） | 待办列表页 |

**老师问「LinearLayout 怎么用？」**
> "设置 `android:orientation="vertical"` 就是从上到下排列，每个控件按顺序放。宽度用 `match_parent` 填满父容器，高度用 `wrap_content` 根据内容自适应。"

### 老师问「RecyclerView 是什么？」

> "RecyclerView 是 Android 的列表组件。数据很多条时，它只创建屏幕上能看到的几个 View，滚动时复用，不会卡顿。需要配合 Adapter（适配器）使用。"

### 常用控件

| 控件名 | 中文意思 | 对应 Java 类型 | 在你的项目里 |
|--------|---------|--------------|------------|
| `TextView` | 显示文字 | `TextView` | 标题、标签、提示文字 |
| `EditText` | 输入框 | `EditText` | 账号/密码输入 |
| `Button` | 按钮 | `Button` | 登录/注册按钮 |
| `CheckBox` | 复选框 | `CheckBox` | 记住密码/自动登录 |
| `ImageView` | 图片 | `ImageView` | Logo、空状态插画 |
| `TextInputLayout` | 带浮动标签的输入框容器 | `TextInputLayout` | 包裹 EditText，显示密码强度 |

---

## 二、Activity — 老师第二个爱问的

### Activity 是什么？

> "Activity 就是一个屏幕页面。一个 Activity = 一个页面。项目有 6 个 Activity = 6 个页面。"

### 页面 = XML + Java

```
activity_main.xml（长相）    ←→    MainActivity.java（行为）
activity_register.xml（长相） ←→    RegisterActivity.java（行为）
```

XML 文件定义"长什么样"，Java 文件定义"能做什么"。

### 最重要的三个生命周期方法

```java
onCreate()  // 页面刚创建时执行（只执行一次）
           // 在这里做：findViewById、设置按钮监听器、开线程加载数据

onResume()  // 页面显示到前台时执行（每次显示都执行）
           // 在这里做：刷新数据

onDestroy() // 页面销毁时执行
           // 在这里做：关闭数据库连接
```

**老师问「onCreate 做了什么事？」**
> "绑定布局 `setContentView(R.layout.activity_main)`，用 `findViewById` 找到控件，设置按钮的点击监听器，初始化数据。"

---

## 三、Java 代码基础 — 看到不慌

### findViewById 是干什么的？

```java
Button btnLogin = findViewById(R.id.btn_login);
//            ↑                      ↑
//         Java变量                   XML里的编号
// 意思：去 XML 布局里找到 id 为 btn_login 的按钮，赋值给 Java 变量 btnLogin
```

### R.id.xxx 是什么？

> "`R` 是一个自动生成的类。XML 里每个控件的 `android:id` 都会变成 `R.id.xxx`，Java 代码通过它找到对应的控件。"

```
activity_main.xml 里：android:id="@+id/btn_login"
                            ↓
Java 代码里引用：R.id.btn_login
```

### setOnClickListener 是干什么的？

```java
btnLogin.setOnClickListener(v -> {
    // 用户点了按钮后，这里面的代码会执行
    Toast.makeText(...).show();  // 弹提示
    startActivity(...);          // 跳转页面
});
```

> "给按钮设置一个点击监听器。用户点按钮时，大括号里的代码就会执行。"

### Intent 是干什么的？

> "Intent 是 Android 里页面跳转的工具。就像写了一个地址，告诉系统「我要去这个页面」。"

```java
// 从 A 页面跳转到 B 页面
Intent intent = new Intent(A页面.this, B页面.class);
startActivity(intent);

// 跳转时带数据
intent.putExtra("userName", "张三");  // 附加数据
intent.putExtra("user_id", 1L);
```

### 子线程是干什么的？

```java
new Thread(() -> {
    // 这里面是子线程代码
    // 数据库操作必须放这里，不能放外面
}).start();
```

> "Android 不允许在主线程（UI 线程）做耗时的数据库操作，否则超过 5 秒会弹出 ANR 无响应对话框。所以所有数据库查询都用 `new Thread()` 放到子线程执行。"

### runOnUiThread 是干什么的？

```java
runOnUiThread(() -> {
    // 这里面的代码会切回主线程更新界面
    tvTitle.setText("新标题");
});
```

> "子线程不能直接修改界面。数据库查完数据后，必须用 `runOnUiThread` 切回主线程，才能更新 TextView 的文字、按钮的状态等 UI。"

---

## 四、数据库 — 可能会问

### SQLite 是什么？

> "SQLite 是 Android 手机自带的轻量级数据库，不需要安装、不需要联网。数据存在手机本地文件里（`user.db` 和 `todo.db`）。"

### 你的项目两个数据库

| 数据库文件 | 存什么 | 对应的 Java 文件 |
|-----------|-------|----------------|
| `user.db` | 用户账号密码 | `UserDBHelper.java` |
| `todo.db` | 待办事项 | `TodoDBHelper.java` |

### DAO 是什么？

> "DAO 是 Data Access Object（数据访问对象），专门用来操作数据库的类。项目里的 `TodoDao` 封装了待办的增删改查方法，Activity 只需要调用 `todoDao.insert(todo)` 就行，不需要写 SQL 语句。"

---

## 五、老师可能马上问的，准备好

### Q：「你这个登录页面怎么构成的？」

**打开 `activity_main.xml`，指给他看：**
> "最外层是 LinearLayout（垂直排列），从上到下依次是：
> 1. ImageView（App Logo）
> 2. TextView（App 名称）
> 3. TextView（标语）
> 4. CardView 卡片里面包两个 TextInputLayout（账号输入框和密码输入框）
> 5. 两个 CheckBox（记住密码和自动登录）
> 6. Button（登录按钮）
> 7. 可点击的 TextView（还没有账号？立即注册）"

### Q：「你怎么在代码里控制这些控件？」

**打开 `MainActivity.java`，指 `findViewById`：**
> "用 `findViewById(R.id.et_account)` 找到输入框，然后用 `.getText()` 拿到用户输入的内容。设置 `.setOnClickListener()` 监听按钮点击。"

### Q：「你点击登录按钮后发生了什么？」

**打开 `MainActivity.java`，指 `btnLogin.setOnClickListener`：**
> "1. 获取账号密码输入框的文字
> 2. 检查输入是否为空
> 3. 开子线程查询数据库
> 4. 查到用户 → 跳转欢迎页；没查到 → 弹 Toast 提示"

### Q：「你注册流程是怎样的？」

**打开 `RegisterActivity.java`，指 `btnRegister.setOnClickListener`：**
> "1. 获取表单输入（账号/密码/确认密码/邮箱）
> 2. 校验输入合法性（空值、密码强度、邮箱格式、同意协议）
> 3. 开子线程插入数据库
> 4. 插入成功后创建 3 条示例待办
> 5. 带着账号密码跳回登录页"

### Q：「你数据是怎么从注册页传到登录页的？」

> "注册页用 `Intent.putExtra("userName", name)` 把数据附加到 Intent 里，
> 通过 `setResult(RESULT_OK, intent)` 回传给登录页。
> 登录页用 `registerForActivityResult` 接收。"

---

## 六、终极救命原则

**老师问到你完全不会的，用这三句：**

1. **「这个我还没深入研究，但我可以大概说一下我的理解……」**
2. **「这部分我主要是参考了官方文档的做法。」**
3. **「这个地方如果我后续优化，我打算……」**

**绝对不要说：**
- ❌ "我不知道"（直接冷场）
- ❌ "代码不是我写的"（印象极差）
- ❌ 沉默 10 秒以上（比说错话还糟）

---

## 附录：快速复习清单（出门前必过）

- [ ] 项目有几个页面？（6 个 Activity）
- [ ] 布局文件在哪？（`res/layout/`）
- [ ] `findViewById` 是做什么的？（按照 ID 找到控件）
- [ ] `setOnClickListener` 是做什么的？（监听按钮点击）
- [ ] Intent 是做什么的？（页面跳转）
- [ ] 为什么用子线程？（防 ANR）
- [ ] 密码强度怎么判断？（for 循环逐字符）
- [ ] 记住密码用的什么？（SharedPreferences）
- [ ] 两个 DB 叫什么？（`user.db`、`todo.db`）
- [ ] 欢迎页仪表盘什么时候刷新？（onResume）