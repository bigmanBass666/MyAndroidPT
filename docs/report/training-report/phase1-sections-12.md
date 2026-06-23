# 综合实训报告（第一阶段）

> 课程：移动应用开发
> 实训题目：Android 注册登录模块实验成果报告
> 报告人：（填写）
> 学　号：（填写）
> 所属学院：人工智能学院

---

## 一、实训要求

本实训项目以「MyAndroidPT」为载体，综合运用 Android UI 设计、Activity 跳转机制、SQLite 本地数据库、SharedPreferences 持久化存储等技术，实现一个包含用户注册、登录校验、记住密码、自动登录四项核心功能的 Android 应用。

实训任务按递进式分为 6 个阶段：从 UI 页面搭建（实训 1）起步，叠加 Activity 跳转与 Toast 交互（实训 2），接入数据库层实现真实的注册与登录业务（实训 3/4），再通过 SharedPreferences 实现记住密码（实训 5）和自动登录（实训 6），最终形成一个完整的用户认证闭环。

> 技术选型说明：本项目数据库层采用 **SQLite 本地存储**方案（课程设计以 SQLite 为准），未使用实训教材要求的 MySQL + JDBC 远程连接模式。因此在后续的实现步骤和问题记录中，不涉及 MySQL 连接配置、Navicat 建库、JDBC 授权等步骤。具体的技术选型决策说明见 [docs/sqlite-migration-decision.md]。

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
// UserDBHelper.java
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

DAO 层封装了用户相关的全部数据库操作：

```java
public class UserDao {
    private UserDBHelper dbHelper;

    // 注册：先判重再插入
    public User findByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(UserDBHelper.TABLE_NAME, null,
                "name=?", new String[]{name}, null, null, null);
        if (cursor.moveToFirst()) {
            User user = new User(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3)
            );
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }

    // 登录：直接按用户名 + 密码查询
    public User login(String name, String psw) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(UserDBHelper.TABLE_NAME, null,
                "name=? AND psw=?", new String[]{name, psw},
                null, null, null);
        // ...
    }
}
```

说明：实训教材要求使用 MySQL + JDBC 连接，本项目因课程设计需求改用 SQLite 本地存储。核心业务逻辑（注册判重、登录比对）与 JDBC 版本完全一致，仅数据库驱动层不同。以 `findByName()` 为例，对应教材中的 `SELECT * FROM Userinfo WHERE username = ?` 查询。

#### 4.2 注册逻辑

注册验证流程：输入不为空 → 密码一致性 → 邮箱格式 → 勾选协议 → 用户名判重 → 插入数据库 → 创建示例待办 → 回传数据 → 跳回登录页。

密码强度实时检测使用 `TextWatcher` 监听输入变化：

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
    if (!hasLetter || !hasDigit) return 0;  // 弱
    if (psw.length() >= 8 && hasUpper && hasLower && hasDigit) return 2;  // 强
    return 1;  // 中
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
            // 记住密码与自动登录状态持久化
            saveLoginPreferences(name, psw, isRemember, isAutoLogin);
            // 跳转到欢迎页
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
// 写入记住密码
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
    // 直接执行登录并跳转到欢迎页
    directLogin(savedName, savedPsw);
}
```

#### 6.2 勾选联动

实现"自动登录"→"记住密码"的自动勾选，以及"取消记住密码"→"取消自动登录"：

```java
// 勾选自动登录 → 自动勾选记住密码
cbAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
    if (isChecked) {
        cbPassRemember.setChecked(true);
    }
});

// 取消记住密码 → 自动取消自动登录
cbPassRemember.setOnCheckedChangeListener((buttonView, isChecked) -> {
    if (!isChecked) {
        cbAutoLogin.setChecked(false);
    }
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