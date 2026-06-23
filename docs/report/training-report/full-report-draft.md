# 综合实训报告

> 课程：移动应用开发  
> 实训题目：Android 注册登录模块实验成果报告  
> 报告人：刘家暄  
> 学　号：202525350226  
> 所属学院：人工智能学院  

---

## 一、实训要求

本实训项目以「MyAndroidPT」为载体，综合运用 Android UI 设计、Activity 跳转机制、SQLite 本地数据库、SharedPreferences 持久化存储等技术，实现一个包含用户注册、登录校验、记住密码、自动登录四项核心功能的 Android 应用。

实训任务按递进式分为 6 个阶段：从 UI 页面搭建（实训 1）起步，叠加 Activity 跳转与 Toast 交互（实训 2），接入数据库层实现真实的注册与登录业务（实训 3/4），再通过 SharedPreferences 实现记住密码（实训 5）和自动登录（实训 6），最终形成一个完整的用户认证闭环。

> **技术选型说明**：本项目数据库层采用 **SQLite 本地存储**方案（课程设计以 SQLite 为准），未使用实训教材要求的 MySQL + JDBC 远程连接模式。因此在后续的实现步骤和问题记录中，不涉及 MySQL 连接配置、Navicat 建库、JDBC 授权等步骤。

### 功能完整性自查清单

- [x] 注册：支持用户名/密码/邮箱输入，合法性校验（用户名判重、密码强度检测、邮箱格式校验）
- [x] 登录：校验用户名+密码正确性，成功跳转欢迎页，失败反馈具体错误提示
- [x] 记住密码：勾选后下次启动自动回填账号密码
- [x] 自动登录：勾选后下次启动直接进入欢迎页（跳过登录页）
- [x] 勾选联动：勾选自动登录自动勾选记住密码；取消记住密码自动取消自动登录
- [x] 注册回填：注册成功后自动跳回登录页并预填账号密码

---

## 二、实训实现步骤

### 步骤 1：创建项目与搭建 UI 资源体系（实训 1）

#### 1.1 创建 Android 项目

在 Android Studio 中创建名为 `MyAndroidPT` 的新项目，包名为 `com.ljx.pt`。项目使用 Java 11 + Classic Views 技术栈，compileSdk=35，minSdk=24，targetSdk=35。

#### 1.2 配置颜色资源（colors.xml）

定义应用主色调和语义色值。项目采用 Material Design 3 色值体系，以绿色为主色调：

```xml
<color name="green_500">#8BC34A</color>
<color name="green_700">#689F38</color>
<color name="teal_200">#FF03DAC5</color>
<color name="teal_700">#FF018786</color>
<color name="amber_500">#FFC107</color>
<color name="red_600">#E53935</color>
```

完整色板包含 21 个语义色值，覆盖 Primary（主色）、Secondary（辅色）、Tertiary（强调色）、Error（错误色）以及 Surface 分层色体系。

#### 1.3 配置主题（themes.xml）

使用 Material Design 3 的 DayNight 主题，支持明暗双模式自动切换：

```xml
<style name="Theme.MyAndroidPT" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="colorPrimary">@color/green_500</item>
    <item name="colorSecondary">@color/teal_700</item>
    <item name="colorTertiary">@color/amber_500</item>
    <item name="colorError">@color/red_600</item>
</style>
```

同时在 `values-night/themes.xml` 中定义深色模式的色值，确保夜间主题下所有控件自动切换为深色配色。

#### 1.4 配置按钮和输入框样式（styles.xml）

定义统一的按钮样式和输入框样式，复用 UI 风格：

```xml
<style name="MyBtnStyle" parent="Widget.Material3.Button">
    <item name="android:textColor">@color/white</item>
    <item name="android:textSize">20sp</item>
    <item name="android:layout_marginTop">20dp</item>
    <item name="android:layout_marginRight">20dp</item>
    <item name="android:layout_marginLeft">20dp</item>
</style>

<style name="MyEditStyle">
    <item name="android:textSize">16sp</item>
    <item name="android:paddingLeft">10dp</item>
</style>
```

#### 1.5 编写登录与注册布局

**登录页（activity_main.xml）**：使用 LinearLayout + TextInputLayout 实现。包含应用 Logo、应用名称标题、账号输入框（带用户图标）、密码输入框（带密码切换可见性图标）、登录按钮、"记住密码/自动登录"复选框、"还没有账号？立即注册"入口。采用 OutlinedBox 风格的 TextInputLayout 容器包裹输入框，提供 Material Design 标准的聚焦/错误状态动画。

**注册页（activity_register.xml）**：同样使用 LinearLayout 布局。包含顶部 MaterialToolbar（带返回按钮）、账号输入框、密码输入框（带密码强度实时检测提示）、确认密码输入框、邮箱输入框、同意用户协议复选框、注册按钮。密码输入框附加 TextInputLayout 的 helperText 用于实时显示密码强度（弱/中/强）。

---

### 步骤 2：Activity 跳转与 Toast 交互（实训 2）

#### 2.1 登录页→注册页跳转

在登录页的"立即注册"按钮点击事件中使用 Intent 跳转到注册页，并通过 `registerForActivityResult` 接收回传数据：

```java
private final ActivityResultLauncher<Intent> registerLauncher =
    registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            String userName = data.getStringExtra("userName");
            String password = data.getStringExtra("password");
            etAccount.setText(userName);
            etPassword.setText(password);
        }
    });

tvRegister.setOnClickListener(v -> {
    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
    registerLauncher.launch(intent);
});
```

#### 2.2 登录成功→欢迎页跳转

登录成功验证通过后，使用 `startActivity` 跳转到欢迎页：

```java
Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
intent.putExtra("userName", name);
startActivity(intent);
finish();
```

#### 2.3 表单校验与 Toast 反馈

在登录按钮和注册按钮的点击事件中对输入进行合法性校验，不合法时弹出 Toast 提示：

```java
if (name.isEmpty() || psw.isEmpty()) {
    Toast.makeText(this, R.string.toast_incomplete_input, Toast.LENGTH_SHORT).show();
    return;
}
if (user == null) {
    Toast.makeText(this, R.string.toast_login_user_not_found, Toast.LENGTH_SHORT).show();
    return;
}
if (!user.getPsw().equals(psw)) {
    Toast.makeText(this, R.string.toast_login_wrong_password, Toast.LENGTH_SHORT).show();
    return;
}
```

---

### 步骤 3：数据库层搭建（实训 3）

#### 3.1 创建数据库与表

项目采用双数据库架构——`user.db`（用户数据）和 `todo.db`（待办数据），使用 `SQLiteOpenHelper` 管理数据库生命周期：

```java
public class UserDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "user.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "userinfo";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name VARCHAR(20) NOT NULL, " +
                "psw VARCHAR(20) NOT NULL, " +
                "email VARCHAR(50))";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
```

#### 3.2 包结构划分

项目按分层架构拆分包：

```
com.ljx.pt/
├── bean/          — 实体类（User.java, Todo.java）
├── dao/           — 数据访问层（UserDao.java, TodoDao.java）
├── dbunit/        — 数据库辅助类（UserDBHelper.java, TodoDBHelper.java）
└── adapter/       — 列表适配器（TodoAdapter.java）
```

#### 3.3 线程模型

所有数据库操作必须在子线程中执行，UI 更新切回主线程：

```java
new Thread(() -> {
    UserDao userDao = new UserDao(RegisterActivity.this);
    User existing = userDao.findByName(name);
    if (existing != null) {
        runOnUiThread(() -> Toast.makeText(...).show());
        return;
    }
    int rows = userDao.insert(new User(name, psw, email));
    runOnUiThread(() -> {
        if (rows > 0) { /* 跳转回登录页 */ }
    });
}).start();
```

---

### 步骤 4：注册与登录业务实现（实训 4）

#### 4.1 UserDao 实现

DAO 层封装了用户相关的全部数据库操作。以注册判重和登录比对为例：

```java
public class UserDao {
    private UserDBHelper dbHelper;

    // 注册判重：查询用户名是否已存在
    public User findByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(UserDBHelper.TABLE_NAME, null,
                "name=?", new String[]{name}, null, null, null);
        if (cursor.moveToFirst()) {
            User user = new User(cursor.getLong(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3));
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }

    // 登录验证：按用户名 + 密码查询
    public User login(String name, String psw) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(UserDBHelper.TABLE_NAME, null,
                "name=? AND psw=?", new String[]{name, psw},
                null, null, null);
        // ... 逻辑同上，多一个密码条件
    }
}
```

说明：实训教材要求使用 MySQL + JDBC 连接，本项目因课程设计需求改用 SQLite 本地存储。核心业务逻辑（注册判重、登录比对）与 JDBC 版本完全一致，仅数据库驱动层不同——`findByName()` 对应教材中的 `SELECT * FROM Userinfo WHERE username = ?`。

#### 4.2 注册逻辑

注册验证流程：输入不为空 → 密码一致性 → 邮箱格式 → 勾选协议 → 用户名判重 → 插入数据库 → 创建示例待办 → 回传数据 → 跳回登录页。

密码强度实时检测使用 `TextWatcher` 监听输入变化：

```java
private int checkPasswordStrength(String psw) {
    if (psw.length() < 6) return 0;                                     // 弱
    boolean hasLetter = false, hasDigit = false;
    boolean hasUpper = false, hasLower = false;
    for (char c : psw.toCharArray()) {
        if (Character.isUpperCase(c)) { hasUpper = true; hasLetter = true; }
        else if (Character.isLowerCase(c)) { hasLower = true; hasLetter = true; }
        else if (Character.isDigit(c)) hasDigit = true;
    }
    if (!hasLetter || !hasDigit) return 0;                              // 弱
    if (psw.length() >= 8 && hasUpper && hasLower && hasDigit) return 2; // 强
    return 1;                                                           // 中
}
```

#### 4.3 登录流程

登录验证流程：检查输入不为空 → 子线程查询数据库 → 判断用户是否存在 → 密码比对 → 成功跳转欢迎页 / 失败 Toast 提示 → 记录登录状态到 SharedPreferences。

```java
new Thread(() -> {
    UserDao userDao = new UserDao(MainActivity.this);
    User user = userDao.findByName(name);
    runOnUiThread(() -> {
        if (user == null) {
            Toast.makeText(this, R.string.toast_login_user_not_found, ...).show();
        } else if (!user.getPsw().equals(psw)) {
            Toast.makeText(this, R.string.toast_login_wrong_password, ...).show();
        } else {
            saveLoginPreferences(name, psw, isRemember, isAutoLogin);
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.putExtra("userName", name);
            startActivity(intent);
            finish();
        }
    });
}).start();
```

---

### 步骤 5：记住密码功能（实训 5）

使用 `SharedPreferences` 实现密码的本地持久化。登录成功时将账号密码写入 `spfRecord`：

```java
SharedPreferences spf = getSharedPreferences("spfRecord", MODE_PRIVATE);
SharedPreferences.Editor editor = spf.edit();
editor.putString("userName", userName);
editor.putString("password", password);
editor.putBoolean("isRemember", isRemember);
editor.putBoolean("isAutoLogin", isAutoLogin);
editor.apply();
```

应用启动时（`MainActivity.onCreate`）读取存储的信息并自动回填：

```java
SharedPreferences spf = getSharedPreferences("spfRecord", MODE_PRIVATE);
boolean isRemember = spf.getBoolean("isRemember", false);
if (isRemember) {
    String savedName = spf.getString("userName", "");
    String savedPsw = spf.getString("password", "");
    etAccount.setText(savedName);
    etPassword.setText(savedPsw);
    cbPassRemember.setChecked(true);
}
```

---

### 步骤 6：自动登录与勾选联动（实训 6）

#### 6.1 自动登录

在 `MainActivity.onCreate()` 中，在 UI 初始化后检查自动登录标志：

```java
SharedPreferences spf = getSharedPreferences("spfRecord", MODE_PRIVATE);
boolean isAutoLogin = spf.getBoolean("isAutoLogin", false);
if (isAutoLogin) {
    String savedName = spf.getString("userName", "");
    String savedPsw = spf.getString("password", "");
    directLogin(savedName, savedPsw);
}
```

#### 6.2 勾选联动

实现"自动登录"→"记住密码"的自动勾选，以及"取消记住密码"→"取消自动登录"：

```java
cbAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
    if (isChecked) cbPassRemember.setChecked(true);
});

cbPassRemember.setOnCheckedChangeListener((buttonView, isChecked) -> {
    if (!isChecked) cbAutoLogin.setChecked(false);
});
```

#### 6.3 注册回填

注册成功后通过 `setResult()` 将注册信息回传给登录页，使登录页自动填入新注册的账号密码（详见步骤 2.1）。

---

### 步骤 7：扩展模块——待办 CRUD（课程设计扩展）

在核心实训要求之外，项目还扩展实现了完整的待办 CRUD 功能：

- **待办列表页**（`TodoListActivity`）：RecyclerView + FAB，展示全部待办，支持空状态提示
- **新增/编辑页**（`TodoEditActivity`）：同一页面处理新增和编辑两种模式，通过 `EXTRA_TODO_ID`（`-1` 为新增）区分
- **详情页**（`TodoDetailActivity`）：展示标题、内容、时间、完成状态，支持删除和状态切换
- **状态切换**：CheckBox + Chip 标签双重状态展示，支持实时更新数据库

关键数据流：Activity 间通过 Intent Extra 传递 `todo_id`，DAO 层通过 `userId` 实现多用户数据隔离，所有数据库操作在子线程执行。

---

### 代码规范自查清单

- [x] 包结构按 `bean` / `dao` / `dbunit` / `adapter` 拆分
- [x] 类名 `PascalCase`（`UserDao`、`MainActivity`）
- [x] 方法/变量名 `camelCase`（`findByName`、`loadDashboardData`）
- [x] 常量 `UPPER_SNAKE_CASE`（`EXTRA_USER_NAME`）
- [x] 资源文件 `snake_case`（`activity_main.xml`、`btn_bg_selector.xml`）
- [x] 关键方法和关键 SQL 包含注释
- [x] 数据库操作在子线程执行（`new Thread()`）
- [x] UI 更新切回主线程（`runOnUiThread()`）
- [x] 字符串资源统一在 `strings.xml` 管理（无硬编码 Toast 字符串）
- [x] 颜色值统一在 `colors.xml` 管理（无硬编码 ARGB 色值）

---

## 三、遇到的问题及解决办法

在整个项目开发过程中，从 UI 搭建到业务逻辑实现，再到代码规范优化，遇到了不少实际问题。下面按实训阶段分类整理，列出每个问题的具体现象、原因分析和最终解决办法。

> 说明：本项目的技术选型为纯本地 SQLite 方案（课程设计以 SQLite 为准），未采用实训教材要求的 MySQL + JDBC 远程连接方案，因此教材中提到的 MySQL 连接失败、IP 授权、中文乱码等 JDBC 相关问题在本项目中未出现。以下为实际开发中遇到的真实问题。

---

### 3.1 UI 搭建与资源体系（实训 1）

#### 问题 1：密码输入框只能输入数字
- **涉及**：实训 1（登录页布局）
- **现象**：登录页密码字段 `et_password` 只能弹出数字键盘，用户无法输入字母，导致所有包含字母的密码都无法完成登录。
- **原因**：布局文件 `activity_main.xml` 中密码输入框的 `android:inputType` 被错误设置为 `numberPassword`（数值密码模式），该模式限制输入仅为数字字符。
- **解决办法**：将 `android:inputType` 从 `numberPassword` 改为 `textPassword`（文本密码模式），修复后支持字母、数字及特殊字符的组合输入。

#### 问题 2：待办编辑页文案错位
- **涉及**：实训 2（待办编辑页）
- **现象**：在新建/编辑待办时，"内容"标签显示为"密码"，"请输入待办内容"提示显示为"请输入密码"，造成严重的用户困惑。
- **原因**：典型的 Android 布局文件复制粘贴错误。开发者在复制密码相关布局后，未替换其中的字符串资源引用——`label_content` 错误引用了 `@string/label_password`，`et_content` 的 `android:hint` 错误引用了 `@string/hint_password`。
- **解决办法**：将 `label_content` 的 text 改为 `@string/label_todo_content`，`et_content` 的 hint 改为 `@string/hint_todo_content`，同时在 `strings.xml` 中新增 `hint_todo_content` 资源定义。

#### 问题 3：按钮样式与教材要求不一致
- **涉及**：实训 1（样式体系）
- **现象**：`MyBtnStyle` 样式继承 `Widget.Material3.Button`，不再使用教材要求的自定义 `@drawable/btn_bg_selector` 背景，按钮颜色无法通过 `colors.xml` 直接控制。
- **原因**：项目在迭代过程中升级到了 Material Design 3 体系。M3 组件使用语义色值体系（Primary / Secondary / Surface 等），无需通过 XML drawable 定义按钮背景色。
- **解决办法**：采用 M3 的 `?attr/colorPrimary` 等动态色值 token 控制按钮颜色，不再依赖自定义 selector drawable。

---

### 3.2 Activity 跳转与交互逻辑（实训 2）

#### 问题 4：登录触发 ANR（应用无响应）
- **涉及**：实训 2（登录交互）
- **现象**：点击登录按钮后，应用弹出 "Process system isn't responding" 系统对话框，只能选择关闭应用或等待。
- **原因**：登录按钮的 `OnClickListener` 中直接执行了数据库查询操作（`UserDao.findByName()`），导致主线程被阻塞超过 5 秒。Android 系统在主线程阻塞超过阈值时会触发 ANR。
- **解决办法**：将所有数据库操作放入 `new Thread()` 子线程执行，UI 更新通过 `runOnUiThread()` 切回主线程。修复后的代码结构为 `new Thread(() → { /* DB 操作 */; runOnUiThread(() → { /* UI 更新 */ }); }).start()`。

#### 问题 5：注册后跳回登录页但账号未自动回填
- **涉及**：实训 6（注册回填）
- **现象**：注册成功后返回登录页，虽然注册成功但账号密码输入框为空，用户需要手动重新输入。
- **原因**：注册页使用 `startActivity()` 启动登录页，而非 `registerForActivityResult()`。`RegisterActivity` 虽然调用了 `setResult()` 回传数据，但接收方没有对应的结果处理代码。
- **解决办法**：改用 `registerForActivityResult(ActivityResultContracts.StartActivityForResult())` 启动注册页，在回调中处理 Intent Extra 数据（`userName`、`password`），自动填入登录页的输入框。

#### 问题 6：欢迎页仪表盘数据不刷新
- **涉及**：实训 2（欢迎页生命周期）
- **现象**：登录进入欢迎页后，待办统计信息正常显示；但跳转到其他页面操作后返回时，仪表盘仍显示旧数据，不会自动更新。
- **原因**：`loadDashboardData()` 只在 `onCreate` 生命周期方法中调用一次。当用户从其他 Activity 返回时，`onCreate` 不会再次执行（standard launch mode）。
- **解决办法**：在 `WelcomeActivity` 中重写 `onResume()` 方法，每次回到欢迎页时都调用 `loadDashboardData()` 重新加载数据。

---

### 3.3 数据库与业务逻辑（实训 3 / 4）

#### 问题 7：数据库连接泄漏
- **涉及**：实训 3 / 4（数据库资源管理）
- **现象**：多次在欢迎页和待办详情页间往返操作后，应用偶发出现卡顿和 ANR。长时间运行后数据库操作响应越来越慢。
- **原因**：`WelcomeActivity.loadDashboardData()` 每次执行都在后台线程创建 `TodoDBHelper` 实例进行查询，但从未调用 `helper.close()`。每次进入欢迎页都泄漏一个数据库连接。
- **解决办法**：统一改用 `TodoDao` 封装数据库操作，并在 `try-finally` 块中确保资源释放：`TodoDao todoDao = new TodoDao(this, userId); try { ... } finally { todoDao.close(); }`。

#### 问题 8：密码强度实时检测与提交校验标准不一致
- **涉及**：实训 4（注册校验）
- **现象**：用户在注册页输入 "abcdef"（6 位纯字母）时，实时检测显示密码强度为"中"（绿色友好提示），但点击注册按钮后弹出 "密码必须包含字母和数字"，注册被阻止。实时反馈与提交校验冲突，造成用户困惑。
- **原因**：`RegisterActivity` 中存在两套不同的密码校验逻辑——`TextWatcher` 触发的 `checkPasswordStrength()` 简单按长度判定（>=6 即为中），而表单提交要求同时包含字母和数字。
- **解决办法**：统一 `checkPasswordStrength()` 的评分逻辑：长度 <6 为弱；长度 >=6 但不包含字母或数字也为弱；长度 >=6 且同时包含字母与数字为中；长度 >=8 且含大小写字母与数字为强。修复后 "abcdef" 显示为"弱"，"abc123" 显示为"中"，实时提示与提交验证完全一致。

#### 问题 9：TodoDao 无 userId 参数导致跨用户数据泄露
- **涉及**：实训 4（数据隔离）
- **现象**：用户 A 登录后看到其他用户的待办数据，删除操作影响到其他人的记录。
- **原因**：`TodoDao` 早期版本存在一个不带 userId 的构造器 `TodoDao(Context context)`，内部将 `userId` 默认设为 `-1`。所有 CRUD 操作的 WHERE 子句不按用户 ID 过滤。
- **解决办法**：删除不带 userId 参数的构造器，强制所有调用方传入 `userId`。同时修改 `TodoDBHelper` 的全部 CRUD 方法（`insert`/`update`/`updateStatus`/`delete`/`queryById`/`queryAll`），使其都绑定 `user_id` 过滤条件。

---

### 3.4 记住密码与自动登录（实训 5 / 6）

#### 问题 10：勾选"自动登录"但下次打开仍需手动登录
- **涉及**：实训 6（自动登录）
- **现象**：用户勾选了自动登录并成功登录，但关闭应用重新打开后，仍然停留在登录页而非直接进入欢迎页。
- **原因**：`SharedPreferences` 的 `isAutoLogin` 标志位仅在登录成功分支中写入，但 `WelcomeActivity` 初始化时读取 `spfRecord` 的逻辑与 `MainActivity` 中的自动登录判断逻辑存在不一致。
- **解决办法**：统一 SharedPreferences 的读写逻辑——在 `MainActivity` 的 `onCreate` 中读取 `spfRecord` 判断 `isAutoLogin`，若为 true 则直接跳转到 `WelcomeActivity`；登录成功时必须调用 `editor.putBoolean("isAutoLogin", true).apply()`。

#### 问题 11：退出登录后"自动登录"仍然勾选
- **涉及**：实训 6（退出逻辑）
- **现象**：在欢迎页点击退出登录，返回登录页后"自动登录"复选框仍处于勾选状态，下次启动仍会自动登录。
- **原因**：退出登录按钮的 `OnClickListener` 中只调用了 `finish()` 跳回登录页，未清除 SharedPreferences 中的 `isAutoLogin` 和 `isRemember` 标志位。
- **解决办法**：在退出登录的回调中，添加 `editor.putBoolean("isAutoLogin", false).apply()` 和 `editor.putBoolean("isRemember", false).apply()`，清除自动登录和记住密码的标志位。

#### 问题 12：密码明文存储在 SharedPreferences
- **涉及**：实训 5 / 6（数据安全）
- **现象**：在 `/data/data/com.ljx.pt/shared_prefs/spfRecord.xml` 中可以直接读取到用户密码明文。
- **原因**：记住密码和自动登录功能将密码以明文形式存入 SharedPreferences。这是教学项目的常见做法，但在 root 设备上存在安全隐患。
- **解决办法**：教学演示用途可维持现状。生产环境需使用 `EncryptedSharedPreferences` 或 Android Keystore。项目文档已标注"密码明文存储（教学演示用途）"的风险提示。

---

### 3.5 扩展模块（待办 CRUD）

#### 问题 13：空状态插画在有待办时仍然可见
- **涉及**：扩展模块（待办列表）
- **现象**：当待办列表中有数据时，空状态的插画图标仍然悬浮显示在列表上方，视觉效果混乱。
- **原因**：`loadTodos()` 中只切换了空状态文字（`tv_empty_hint`）和 RecyclerView（`rv_todo`）的可见性，但忽略了整个空状态容器（`empty_state_container`）及其内部的插画 ImageView（`iv_empty`）。
- **解决办法**：引入 `emptyStateContainer` 的可见性整体控制——列表为空时显示容器 + 隐藏 RecyclerView；有数据时隐藏容器 + 显示 RecyclerView。修复后空状态插画不会再"穿越"到有数据的列表上。

#### 问题 14：CheckBox 状态监听器重复注册
- **涉及**：扩展模块（待办详情）
- **现象**：待办详情页的完成状态 CheckBox 在快速切换时偶发状态回弹或多次触发数据库更新。
- **原因**：`loadTodo()` 在每次 `onResume` 时都会重新注册 `OnCheckedChangeListener`，多线程异步时序可能导致监听器状态不确定。
- **解决办法**：将 CheckBox 的监听器绑定移到 `onCreate` 中一次性注册，`onResume` 只负责加载数据和刷新 UI，不再重新绑定监听器。

#### 问题 15：待办删除后 Toast 被提前销毁
- **涉及**：扩展模块（待办删除）
- **现象**：在待办详情页删除待办后，Toast 提示显示不足 1 秒就消失，用户几乎看不到确认反馈。
- **原因**：删除操作成功后同时执行了 `Toast.show()` 和 `Activity.finish()`。`Toast.LENGTH_SHORT` 约 2 秒，但 `finish()` 执行后 Activity 上下文被回收，Toast 随之消失。
- **解决办法**：使用 `Handler.postDelayed(() → finish(), 800)` 延迟 800ms 关闭 Activity，给 Toast 足够的显示时间。

---

### 3.6 代码规范与资源管理

#### 问题 16：硬编码 Toast 字符串散落在各 Activity
- **涉及**：代码规范（全局）
- **现象**：多处用户提示信息直接使用中文字面量调用 `Toast.makeText()`，未抽取到 `strings.xml`，不利于国际化和文案修改。
- **原因**：开发初期追求快速实现，未遵循 Android 资源化管理最佳实践。`MainActivity`（5 处）、`TodoDetailActivity`（3 处）、`TodoEditActivity`（2 处）、`RegisterActivity`（6 处）均存在硬编码字符串。只有 `toast_register_success` 一条被提取为资源。
- **解决办法**：在 `strings.xml` 中统一新增全部 Toast 资源定义（`toast_login_user_not_found`、`toast_login_wrong_password`、`toast_auto_login_failed`、`toast_invalid_params`、`toast_load_failed`、`toast_title_required` 等），所有 Activity 中替换为 `R.string.*` 引用。

#### 问题 17：适配器硬编码颜色值
- **涉及**：代码规范（颜色资源）
- **现象**：待办列表已完成项显示绿色、未完成项显示灰色使用 ARGB 十六进制字面量（`0xFF4CAF50` / `0xFF9E9E9E`），在夜间主题下无法自动切换。
- **原因**：`TodoAdapter.java` 中直接使用 `setTextColor(0xFF4CAF50)`，未使用 `ContextCompat.getColor()` + 资源引用。
- **解决办法**：在 `colors.xml` 中新增 `status_done`（已完成绿色）和 `status_undone`（未完成灰色）颜色资源，适配器中改用 `ContextCompat.getColor(context, R.color.status_done)` 的方式引用。

#### 问题 18：变量名与资源 ID 命名不一致
- **涉及**：代码规范（命名规范）
- **现象**：`RegisterActivity` 中 `CheckBox` 控件使用变量名 `rbAgree`（RadioButton 前缀 `rb`）；`activity_todo_detail.xml` 中 Chip 控件的 ID 为 `tv_detail_status`（TextView 前缀 `tv_`）。
- **原因**：早期版本使用 `RadioButton` 和 `TextView` 实现，后期改为 `CheckBox` 和 `Chip` 但变量名和 ID 未同步更新，属于重构遗留。
- **解决办法**：变量名 `rbAgree` → `cbAgree`（涉及声明、`findViewById`、`isChecked` 共 3 处）。资源 ID `tv_detail_status` → `chip_status`（同步更新布局文件和 Java 代码的 `findViewById` 引用）。

---

### 3.7 其他开发环境问题

#### 问题 19：gradlew.bat 在 Git Bash 中无法执行
- **涉及**：开发环境配置
- **现象**：在 Git Bash 终端执行 `gradlew.bat` 返回 `command not found`，直接执行时 `.bat` 文件的 `@rem` 注释被 bash 解析为错误命令。
- **原因**：`gradlew.bat` 是 Windows 批处理脚本（cmd.exe 语法），bash shell（Git Bash / WSL）无法解析。项目根目录存在两个版本的 Gradle Wrapper 启动脚本——`gradlew.bat`（Windows 批处理）和 `gradlew`（Unix shell 脚本）。
- **解决办法**：在 Git Bash 中应使用 `./gradlew`（Unix shell 脚本版本）而非 `gradlew.bat`。在 Windows cmd 或 PowerShell 中使用 `gradlew.bat`。本项目所有构建操作均使用 `./gradlew assembleDebug`。

---

### 小结

以上 19 个问题覆盖了从 UI 布局、Activity 跳转、数据库操作、用户交互到代码规范的各个层面。这些问题大多数属于 Android 初学者常见的典型错误：主线程阻塞、资源引用错配、生命周期理解不深、代码规范执行不严等。通过逐一排查和修复这些问题，不仅完善了应用的功能和体验，也加深了对 Android 开发核心概念的理解。

其中优先级最高的硬编码字符串和颜色值问题、命名不一致问题已在代码清理专项中全部修复并通过 `assembleDebug` 编译验证。数据库连接泄漏和架构违规问题也已修复。部分教学项目可接受的问题（如密码明文存储、`onUpgrade` DROP TABLE 重建）保留了现状但明确了风险。

---

## 四、功能界面展示

> 📸 **截图操作说明**：以下截图在 Android 模拟器上完成。请在模拟器中打开对应的页面，使用截图工具捕获后插入本节对应位置。

---

### 4.1 登录页（空态）

**操作**：启动应用，停留在登录页，不输入任何内容直接截图。

**应展示**：应用 Logo、标题"记录每一个完成时刻"、账号输入框（hint"请输入用户名或手机号"）、密码输入框（hint"请输入密码"）、登录按钮、"记住密码 / 自动登录"复选框、"还没有账号？立即注册"入口。

**说明**：登录页采用 Material Design 3 OutlinedBox 风格的 TextInputLayout，主色调为绿色。账号输入框左侧有用户图标，密码输入框右侧有密码可见性切换图标。

> [截图占位 — 在此插入登录页空态截图]

---

### 4.2 注册页（空态）

**操作**：在登录页点击"立即注册"，进入注册页后截图。

**应展示**：顶部 Toolbar（标题"注册"，左侧返回箭头）、账号输入框（hint"请输入用户名"）、密码输入框（hint"请输入密码"）、确认密码输入框（hint"请再次输入密码"）、邮箱输入框（hint"请输入邮箱"）、同意用户协议复选框、注册按钮。

**说明**：注册页同样使用 M3 OutlinedBox 风格。密码输入框带有密码强度实时检测功能，输入内容后会在输入框下方显示 helperText 提示（弱 / 中 / 强）。

> [截图占位 — 在此插入注册页空态截图]

---

### 4.3 登录页空输入校验

**操作**：在登录页不输入任何内容，直接点击"登录"按钮，立即截图。

**应展示**：屏幕底部弹出 Toast 提示"输入信息不完整，请重新输入！"。

**说明**：表单校验在用户点击登录按钮时触发。所有必填字段为空时弹出 Toast 警告，防止无效提交。

> [截图占位 — 在此插入空输入 Toast 截图]

---

### 4.4 密码强度实时检测

**操作**：在注册页密码输入框中输入 "abc"（不足 6 位），截图展示密码强度提示。

**应展示**：密码输入框下方显示红色"弱"提示文字。

**说明**：密码强度通过 `TextWatcher` 实时监听输入变化：弱（红色）= 长度 <6 或未同时包含字母和数字；中（绿色）= 长度 ≥6 且包含字母和数字；强（绿色）= 长度 ≥8 且包含大小写字母和数字。

> [截图占位 — 在此插入密码强度弱/中/强截图]

---

### 4.5 邮箱格式校验

**操作**：在注册页输入非法邮箱格式（如 "abc"），点击注册按钮，立即截图。

**应展示**：Toast 提示"请输入正确的邮箱地址"。

**说明**：注册提交时对邮箱字段进行正则校验（`^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$`），非法格式弹出错误 Toast。

> [截图占位 — 在此插入邮箱校验 Toast 截图]

---

### 4.6 注册成功回填登录页

**操作**：在注册页填写完整的合法信息，勾选同意协议，点击注册。注册成功后自动跳回登录页，立即截图。

**应展示**：登录页的账号和密码输入框已自动填入刚注册的账号和密码。

**说明**：注册成功通过 `setResult(RESULT_OK, data)` 将用户名和密码回传给登录页。登录页在 `registerForActivityResult` 回调中接收并自动填入输入框，用户无需重新输入即可直接登录。

> [截图占位 — 在此插入注册回填截图]

---

### 4.7 登录成功 → 欢迎页

**操作**：使用已注册的账号密码登录，成功进入欢迎页后截图。

**应展示**：顶部 Toolbar（显示"欢迎你，xxx"）、待办统计仪表盘（总待办数 / 已完成数 / 未完成数）、"快速新建待办"入口、"查看待办列表"入口、"退出登录"按钮。

**说明**：欢迎页展示个性化的欢迎信息和待办统计概览。仪表盘数据通过子线程从 `TodoDBHelper` 查询，`runOnUiThread` 更新 UI。下方提供两个主要操作入口和退出登录功能。

> [截图占位 — 在此插入欢迎页截图]

---

### 4.8 记住密码回填

**操作**：确保"记住密码"已勾选并登录成功后，关闭应用。重新打开应用，立即截图。

**应展示**：登录页的账号和密码输入框已自动填入之前登录的账号和密码，"记住密码"复选框处于勾选状态。

**说明**：记住密码通过 SharedPreferences 持久化账号密码。应用启动时在 `MainActivity.onCreate()` 中读取 `spfRecord` 的 `isRemember` 标志，为 true 则自动回填账号密码到输入框。

> [截图占位 — 在此插入记住密码回填截图]

---

### 界面设计自查清单

- [x] 颜色集中在 `colors.xml`，没有硬编码
- [x] 使用 `dp` / `sp`，没有硬编码 `px`
- [x] 复用了 `MyBtnStyle` / `MyEditStyle` 这类自定义样式
- [x] 输入框、按钮都有按压 / 聚焦等状态变化（Material Design 3 组件内置状态管理）
- [x] 登录 / 注册界面都有清晰、友好的 hint 和错误提示

---

## 五、总结

通过本次「Android 注册登录模块」实训项目的完整开发，从项目创建、UI 搭建到数据库接入、业务逻辑实现，再到记住密码和自动登录的功能完善，对整个 Android 应用开发流程有了系统性的认识和实践。

### 5.1 知识收获

**（1）Android UI 资源体系**

深入理解了 Android 的资源管理体系。`colors.xml` 定义色板并通过语义化命名（`colorPrimary`、`colorSecondary`、`colorTertiary`）实现全局颜色统一管理；`styles.xml` 和 `themes.xml` 分离组件样式和全局主题，实现了按钮/输入框的复用样式定义；`drawable` 目录下的 selector 和 shape 资源提供了控件状态变化的视觉反馈。特别是从 Material Design 2 升级到 Material Design 3 的过程中，学习了语义色值 token（`?attr/colorOnSurface`）和动态主题切换的原理。

**（2）Activity 生命周期与跳转机制**

掌握了 Activity 的核心生命周期方法（`onCreate`、`onResume`、`onDestroy`）及其在数据加载和资源释放中的正确使用时机。理解了 `Intent` 的显式跳转和携带数据传递，`startActivity` 与 `registerForActivityResult` 的区别，以及通过 `setResult()` 回传数据的完整流程。

**（3）SQLite 数据库与数据持久化**

掌握了 Android 中 SQLite 数据库的使用：`SQLiteOpenHelper` 管理数据库创建与版本升级，`ContentValues` 封装插入数据，`Cursor` 遍历查询结果，参数化查询防止 SQL 注入。理解了子线程执行数据库操作的必要性和 `runOnUiThread` 切回主线程更新 UI 的标准模式。

**（4）SharedPreferences 本地存储**

学习了 `SharedPreferences` 键值对存储的使用方法及其在记住密码、自动登录场景中的应用。掌握了 `SharedPreferences.Editor` 的提交方式（`apply()` 异步 vs `commit()` 同步）、`OnCheckedChangeListener` 实现 CheckBox 勾选联动、以及应用启动时恢复持久化状态的数据流设计。

### 5.2 能力提升

**（1）全流程调试能力**

从最初的 UI 布局错位、输入框属性配置错误，到运行时的 ANR 崩溃、数据库连接泄漏，再到逻辑层面的密码强度标准不一致、自动登录状态异常——每次问题排查都提升了定位和解决 Bug 的能力。特别是在处理 ANR 问题时，学会了通过 logcat 分析主线程阻塞的原因，理解了 Android 线程模型对应用稳定性的重要性。

**（2）代码规范意识**

在项目中期进行了代码规范专项清理，将散落在各 Activity 的硬编码 Toast 字符串提取到 `strings.xml`、将适配器中的硬编码色值替换为 `colors.xml` 资源引用、统一变量命名规范（`rbAgree` → `cbAgree`、`tv_detail_status` → `chip_status`）。这些实践强化了"代码是写给下一个开发者看的"理念，理解了资源化管理和命名规范对项目可维护性的长期价值。

**（3）架构分层思维**

从最初的所有逻辑堆在 Activity 中，到逐步拆分为 `bean`（实体）、`dao`（数据访问）、`dbunit`（数据库辅助）、`adapter`（列表适配器）的包结构，再到通过构造器注入 `userId` 实现多用户数据隔离——这个演进过程建立了分层架构的直觉。特别是 DAO 层的引入，不仅解耦了数据库操作与 UI 逻辑，还通过 `try-finally` 模式系统性解决了资源泄漏问题。

### 5.3 不足与改进

**（1）界面美化仍有提升空间**

当前应用虽然采用了 Material Design 3 主题体系，但整体视觉风格较为简洁，缺少动画过渡和微交互设计。后续可以在页面切换时添加共享元素过渡动画、在列表项上添加滑动删除手势、以及在加载数据时提供骨架屏(Skeleton Screen)效果来提升用户体验。

**（2）缺少异常处理的全面覆盖**

应用的错误处理主要集中在用户输入校验层面，对数据库操作异常、文件 IO 异常等系统级异常的处理不够完善。部分数据库操作虽然放在了子线程，但外层缺少统一的 `try-catch` 捕获，异常发生时用户得不到明确的错误反馈。后续应当建立全局异常处理机制，确保所有可能崩溃的路径都有兜底处理。

**（3）数据安全性不足**

密码以明文形式存储在 SharedPreferences 和 SQLite 数据库中，这是教学项目可接受但不应被忽视的安全风险。在实际生产中至少应使用 SHA-256 + Salt 进行密码哈希，或使用 Android 的 `EncryptedSharedPreferences` 和 SQLCipher 对存储数据进行加密。此外，SharedPreferences 的 key 散落在多个 Activity 中，缺乏统一管理，后续可抽取为常量类或使用数据仓库模式集中管理。

**（4）自动化测试缺位**

当前应用没有任何单元测试或 UI 自动化测试代码。后续可为 `UserDao` 和 `TodoDao` 编写基于 SQLite 内存数据库的单元测试，并引入 Espresso 或 UI Automator 编写 UI 层面的集成测试，确保核心功能回归安全。

**（5）技术栈可进一步现代化**

项目基于教材约束使用了 Java + Classic Views 的技术栈。在后续的课程设计或实际项目中，可以考虑引入 Kotlin（减少样板代码、空安全）、Jetpack ViewModel + LiveData（分离 UI 与数据逻辑）、Room（替代裸 SQLiteOpenHelper）和协程（简化异步操作），以进一步提升代码质量和开发效率。

---

## 六、提交资料

> 请根据实际情况填写下列信息。

### 6.1 代码提交

| 项 | 你的实际值 |
|---|----------|
| 文件名 | `_________`（例：`2021001_张三_实训代码.zip`） |
| 内容 | 整个 Android 项目代码（含 `app/`、`build.gradle.kts`、`settings.gradle.kts` 等） |
| 排除项 | 删除 `build/`、`.gradle/`、`local.properties`、`app/bin/`、`*.db` 等本地构建产物后再打包 |
| 平台 | 课堂派 |

### 6.2 文档提交

| 项 | 你的实际值 |
|---|----------|
| 文件名 | `_________`（例：`2021001_张三_实训报告.docx`） |
| 内容 | 本报告 + 项目背景 + 功能需求分析 + 设计思路 + 功能实现步骤 + 遇到的问题及解决办法 |
| 平台 | 课堂派 |
| 提交时间 | 不晚于 2026-06-26 00:00 |

> ⚠️ 注意：两个提交物的文件名中的"学号"和"姓名"请替换为你自己的实际学号和姓名。提交前务必检查压缩包中不包含 `build/` 等构建产物。
