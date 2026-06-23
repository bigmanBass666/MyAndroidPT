# 数据库相关问题与解决办法

## 问题 1：SQLite 资源泄漏 — TodoDBHelper 创建后未关闭

- **现象**：`WelcomeActivity.loadDashboardData()` 每次被调用时在后台线程中创建 `TodoDBHelper` 实例，但从未调用 `close()`，导致每次进入欢迎页（包括从其他 Activity 返回）都泄漏一个数据库连接。多次操作后可能导致内存压力甚至 OOM。

- **原因**：`loadDashboardData()` 原实现直接在后台线程中 `new TodoDBHelper(this)` 并执行查询，查询完成后未在 `finally` 块中关闭 helper。代码路径：
  ```java
  // 旧代码 — TodoDBHelper 从未 close()
  private void loadDashboardData() {
      new Thread(() -> {
          TodoDBHelper helper = new TodoDBHelper(this);
          List<Todo> allTodos = helper.queryAll(userId);  // ← 查询后 helper 未关闭
          // ... 计数逻辑
      }).start();
  }
  ```

- **解决办法**：改用 `TodoDao` 封装 + `try-finally` 确保资源释放。`TodoDao.close()` 内部会调用 `TodoDBHelper.close()`：
  ```java
  // 修复后 — 使用 TodoDao + try-finally
  private void loadDashboardData() {
      new Thread(() -> {
          TodoDao todoDao = new TodoDao(this, userId);
          try {
              List<Todo> allTodos = todoDao.queryAll();
              // ... 计数逻辑
          } finally {
              todoDao.close();  // ← 确保关闭
          }
      }).start();
  }
  ```

- **涉及实训**：该问题不属于任何实训要求，是代码质量缺陷，在后续审计中被标记为 P1 严重性。

- **当前状态**：已在当前分支中修复。`WelcomeActivity.java:126-160` 使用了 `TodoDao` + `try-finally` 模式，资源泄漏已解决。

---

## 问题 2：onUpgrade 直接 DROP TABLE 重建，数据全量丢失

- **现象**：数据库版本升级时，`UserDBHelper` 和 `TodoDBHelper` 的 `onUpgrade()` 方法都执行 `DROP TABLE IF EXISTS` + 重建表，导致所有已有数据被清空。这在生产环境是灾难性的。

- **原因**：两个 DBHelper 的 `onUpgrade` 实现均未做数据迁移：
  ```java
  // UserDBHelper.java:35-38
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
      onCreate(db);
  }

  // TodoDBHelper.java:38-41
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
      onCreate(db);
  }
  ```
  具体场景：`TodoDBHelper.DB_VERSION` 从 `1` 升级到 `2`（为了支持多用户数据隔离），用户安装新版本后已有的待办数据全部丢失。

- **解决办法**：正确的做法是在 `onUpgrade` 中执行 `ALTER TABLE` 或创建临时表做数据迁移：
  ```java
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion < 2) {
          // 从 v1 升级到 v2：添加 user_id 列
          db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN user_id INTEGER NOT NULL DEFAULT 0");
      }
      // 后续版本升级继续追加条件分支
  }
  ```

- **涉及实训**：教学项目可接受 `DROP TABLE` 重建（文档已标注"仅教学演示"），但该代码曾导致开发过程中的真实数据丢失。如果项目要用于演示或提交，建议至少改为数据迁移模式。

- **当前状态**：未修复。两个 DBHelper 的 `onUpgrade` 仍为直接 `DROP TABLE` 重建。教学项目可保留现状，但需知晓风险。

---

## 问题 3：MySQL vs SQLite 两阶段考试要求冲突

- **现象**：项目同时面对两套考试要求体系——实训（1-6 阶段）要求使用 **MySQL 8.0 + JDBC 直连**，课程设计（期末考核）要求使用 **SQLite 本地存储**。实训文件明确要求 "MySQL 8.0+ 以上版本数据库，通过 JDBC 直连"。

- **原因**：这是课程设计本身的矛盾——实训强调企业级数据库（MySQL），而课程设计则因为移动端限制采用 SQLite。项目处于两套要求体系的交界处，前期按实训要求开发了 SQLite 方案，但审查时发现与实训材料要求不符。

- **解决办法**：按冲突解决原则——**两者冲突时以课程设计（考试）要求为准**，因为课程设计是最终评分依据。项目已确认采用 SQLite 纯本地方案：
  - `build.gradle.kts` 零 MySQL/JDBC 依赖
  - `app/src` 中无 `JdbcHelper`/`mysql`/`MySQL`/`INTERNET`/`connector`/`jdbc` 任何引用
  - 应用实际使用 `SQLiteOpenHelper` + 双数据库架构

- **涉及实训**：实训 3/4 要求 MySQL，课程设计（`materials/2_design/README.md`）要求 SQLite。详细冲突分析见 `docs/phase-conflict-analysis.md`。

- **当前状态**：已解决。采用课程设计的 SQLite 方案。

---

## 问题 4：WelcomeActivity 绕过 DAO 层直接操作 TodoDBHelper（架构违规）

- **现象**：`WelcomeActivity.loadDashboardData()` 曾直接实例化 `TodoDBHelper` 执行数据库查询，完全跳过了 `TodoDao` 封装层。这导致两个后果：
  1. 数据库操作未经过 DAO 层的事务管理和 userId 隔离
  2. 资源泄漏风险（与问题 1 耦合）

- **原因**：原始实现直接在 Activity 的业务逻辑中创建 `TodoDBHelper`：
  ```java
  // 旧代码 — 直接使用 TodoDBHelper
  private void loadDashboardData() {
      new Thread(() -> {
          TodoDBHelper helper = new TodoDBHelper(this);  // ← 绕过 TodoDao
          List<Todo> allTodos = helper.queryAll(userId);
          // ... 计数逻辑
          // ← 也未 close()
      }).start();
  }
  ```

- **解决办法**：改用 `TodoDao`（该构造函数需传入 Context + userId，内部会正确绑定 userId）：
  ```java
  TodoDao todoDao = new TodoDao(this, userId);
  try {
      List<Todo> allTodos = todoDao.queryAll();
      // ...
  } finally {
      todoDao.close();
  }
  ```

- **涉及实训**：代码质量/架构合规性问题，非实训具体要求。

- **当前状态**：已修复。`WelcomeActivity.java:128` 使用 `TodoDao todoDao = new TodoDao(this, userId);`

---

## 问题 5：TodoDao 无 userId 参数导致跨用户数据泄漏（幽灵数据）

- **现象**：`TodoDao` 早期版本存在一个**不传 userId 的构造器**：
  ```java
  public TodoDao(Context context) {
      this.dbHelper = new TodoDBHelper(context);
      this.userId = -1;  // ← 默认值，导致查询不按用户隔离
  }
  ```
  使用此构造器时，`TodoDBHelper.queryAll(-1)` 会查询 `user_id = -1` 的记录。更糟的是，如果调用方忘记传 userId，可能出现：
  - 用户 A 看到用户 B 的待办
  - 删除/修改操作影响到其他用户的数据

- **原因**：早期设计未考虑多用户数据隔离，`Todo` 表结构最初没有 `user_id` 列。加入多用户支持后，旧的构造器被保留下来，导致调用方可能无意识地使用了不带 userId 的版本。

- **解决办法**：**删除无参数的构造器**，强制所有调用方传入 userId：
  ```java
  // 删除此构造器
  // public TodoDao(Context context) { ... }

  // 只保留：
  public TodoDao(Context context, long userId) {
      this.dbHelper = new TodoDBHelper(context);
      this.userId = userId;
  }
  ```
  同时配套修改了 `TodoDBHelper` 的全部 CRUD 方法（`insert`/`update`/`updateStatus`/`delete`/`queryById`/`queryAll`）使其都绑定 `user_id` 过滤条件。

- **涉及实训**：多用户数据隔离要求来自课程设计的功能需求，非实训具体要求。

- **当前状态**：已修复。`TodoDao.java` 只有一个带 `userId` 的构造器，所有 CRUD 操作均按 `user_id` 过滤。

---

## 问题 6：RegisterActivity 注册插入异常导致 ANR 与进程被 Kill

- **现象**：在注册流程中，`UserDBHelper.insert()` 在子线程中执行，当数据库插入发生异常时（如用户名重名约束冲突、数据库文件异常），异常未被捕获，导致整个线程崩溃。同时 ANR (Application Not Responding) 日志显示输入分派超时，进程被操作系统 Kill。

- **原因**：`RegisterActivity` 的注册按钮点击处理中，`userDao.insert()` 和 `userDao.findByName()` 均在 `new Thread()` 中执行，但未对可能抛出的 `SQLiteException` 做 `try-catch` 处理。当 `"name"` 字段 UNIQUE 约束冲突（快速连续点击注册按钮）或数据库写入异常时，未捕获的异常导致线程终止且无用户反馈。

- **解决办法**：在子线程的数据库操作外围添加 `try-catch`，并在异常时通过 `runOnUiThread` 给用户友好提示：
  ```java
  new Thread(() -> {
      userDao = new UserDao(RegisterActivity.this);
      try {
          if (userDao.findByName(name) != null) {
              runOnUiThread(() -> Toast...);
              return;
          }
          int rows = userDao.insert(new User(name, psw, email));
          // ...
      } catch (SQLiteException e) {
          e.printStackTrace();
          runOnUiThread(() -> Toast.makeText(... R.string.toast_register_failed ...));
      }
  }).start();
  ```

- **涉及实训**：不属于实训要求，是编码健壮性问题。实训 5 要求"注册信息有效性验证"，可归入此范畴。

- **当前状态**：当前代码已通过 `userDao.findByName(name) != null` 预检查用户名重复，但外层仍无全局的 `try-catch`。快速连续点击注册按钮时仍存在异常风险。

---

## 问题 7：用户密码明文存储（SharedPreferences + 数据库）

- **现象**：用户密码在数据库和 SharedPreferences 中均以明文形式存储：
  - `user.db` 的 `userinfo` 表的 `psw` 列：明文存储
  - `SharedPreferences("spfRecord")` 四字段中的 `password` 字段：明文存储（用于记住密码和自动登录功能）

- **原因**：教学演示项目，未做任何加密处理：
  ```java
  // UserDBHelper.java — insert 方法直接存明文 psw
  public long insert(User user) {
      ContentValues values = new ContentValues();
      values.put("name", user.getName());
      values.put("psw", user.getPsw());  // ← 明文密码
      values.put("email", user.getEmail());
      return db.insert(TABLE_NAME, null, values);
  }
  ```

- **解决办法**：至少使用简单的哈希处理（如 SHA-256 + salt）：
  ```java
  // 注册时存储哈希
  String hashedPsw = hashWithSalt(psw);
  user.setPsw(hashedPsw);
  
  // 登录时比对哈希
  if (hashWithSalt(inputPsw).equals(user.getPsw())) { ... }
  ```
  注意：SharedPreferences 的密码字段也要对应修改为存储哈希值。

- **涉及实训**：教学演示用途，文档已标注为可接受。如果要在实训 5/6 中扩展安全性，这是可选增强项。

- **当前状态**：未修复。明文存储保留（教学项目可接受）。

---

## 总结

| 问题 | 严重性 | 当前状态 | 备注 |
|------|--------|---------|------|
| 1. SQLite 资源泄漏 | P1 | **已修复** | TodoDao + try-finally |
| 2. onUpgrade DROP TABLE 丢数据 | P3 | 未修复 | 教学项目可接受 |
| 3. MySQL vs SQLite 冲突 | — | **已解决** | 按课程设计采用 SQLite |
| 4. 绕过 DAO 层 | P2 | **已修复** | 改为 TodoDao |
| 5. TodoDao 无 userId 幽灵数据 | P1 | **已修复** | 强制传入 userId |
| 6. 注册插入异常 ANR | P2 | 部分修复 | 缺全局 try-catch |
| 7. 密码明文存储 | P3 | 未修复 | 教学项目可接受 |
