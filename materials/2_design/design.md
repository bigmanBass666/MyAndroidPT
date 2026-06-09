# 简易待办应用 — 设计文档

---

## 一、项目背景

日常生活中，人们频繁使用待办事项工具来管理日程与任务。在 Android 移动平台上，待办应用是最经典的学习型项目之一，能够覆盖 Activity 生命周期、SQLite 持久化、UI 交互等核心知识点。

本课程设计以"基于安卓的简易待办应用开发"为主题，在已完成的登录注册功能基础上，实现完整的待办管理功能（创建、查看、编辑、删除、状态切换）。通过本项目，系统掌握 Android 原生开发流程、SQLite 数据持久化机制以及 Activity 间通信方式。

---

## 二、功能需求分析

### 2.1 功能需求总览

| 功能编号 | 功能名称 | 功能描述 | 优先级 |
|----------|----------|----------|--------|
| F-01 | 待办创建 | 用户输入文本创建新的待办事项，保存到 SQLite | 高 |
| F-02 | 待办查看 | 以列表形式展示所有待办，点击可查看详情 | 高 |
| F-03 | 待办编辑 | 修改已有待办的标题或内容 | 高 |
| F-04 | 待办删除 | 删除不再需要的待办，带有确认提示 | 高 |
| F-05 | 状态切换 | 将待办标记为"已完成"或"未完成" | 高 |

### 2.2 界面需求

- 主列表界面简洁直观，每条待办显示标题与完成状态
- 操作按钮（新增、编辑、删除）布局合理，符合 Material Design 交互规范
- 编辑界面提供清晰的输入区域和保存/取消按钮
- 删除操作前弹窗二次确认，防止误删
- 状态切换使用 CheckBox 或滑动开关，反馈即时

### 2.3 技术需求

- **开发语言**：Java
- **界面组件**：Activity（主列表、新增/编辑、详情、欢迎页） + RecyclerView
- **数据存储**：SQLite（SQLiteOpenHelper），替代 MySQL 本地持久化
- **数据传递**：Intent 携带 todo_id 在 Activity 间跳转
- **兼容性**：minSdkVersion ≥ 21，targetSdkVersion ≥ 34，覆盖 Android 5.0 及以上

---

## 三、设计思路

### 3.1 模块划分

```
com.ljx.pt/
├── MainActivity.java          # 登录入口（已有）
├── RegisterActivity.java      # 注册入口（已有）
├── WelcomeActivity.java       # 登录成功后欢迎页 / 待办列表入口
├── TodoListActivity.java      # 待办主列表（RecyclerView）
├── TodoEditActivity.java      # 新增 / 编辑待办
├── TodoDetailActivity.java    # 查看待办详情
├── bean/
│   └── Todo.java              # 待办实体类（id, title, content, isDone, createTime）
├── dao/
│   └── TodoDao.java           # 待办数据访问层（增删改查 + 分页）
├── dbunit/
│   └── TodoDBHelper.java      # SQLiteOpenHelper，管理 todo 表
└── adapter/
    └── TodoAdapter.java       # RecyclerView 适配器
```

### 3.2 流程说明

```
登录 → WelcomeActivity
  ├─ 点击"待办列表" → TodoListActivity（展示所有待办）
  │     ├─ 点击 + 按钮 → TodoEditActivity（mode=CREATE） → 保存 → 刷新列表
  │     ├─ 点击条目 → TodoDetailActivity → 可切换完成状态 / 删除
  │     └─ 长按条目 → TodoEditActivity（mode=EDIT） → 保存 → 刷新列表
  └─ 退出登录 → 返回 MainActivity
```

### 3.3 涉及知识点

| 知识点 | 具体应用 |
|--------|----------|
| Activity 生命周期 | onCreate / onResume / onPause — 列表刷新时机 |
| SQLiteOpenHelper | TodoDBHelper — 创建/升级 todo 表 |
| RecyclerView + Adapter | TodoAdapter — 列表展示、点击/长按事件绑定 |
| Intent 传值 | 携带 todoId 跳转详情页 / 编辑页 |
| Cursor 游标遍历 | TodoDao.queryAll() — SQLite 查询结果处理 |
| AlertDialog 弹窗 | 删除确认、操作提示 |
| SharedPreferences | 记住用户登录态（已有） |

---

## 四、功能实现步骤

### F-01 待办创建

1. `TodoEditActivity.onCreate()` — 判断 Intent 携带的 `EXTRA_TODO_ID`，为 null 则进入创建模式
2. 用户输入标题和内容后点击"保存"
3. 调用 `TodoDao.insert(Todo todo)` → `db.insert("todo", null, values)`
4. SQLite 自动生成 `_id`，设置 `create_time = System.currentTimeMillis()`
5. setResult(RESULT_OK) 返回列表页，列表自动刷新

**关键代码：**

```java
ContentValues values = new ContentValues();
values.put("title", todo.getTitle());
values.put("content", todo.getContent());
values.put("is_done", 0);
values.put("create_time", System.currentTimeMillis());
db.insert("todo", null, values);
```

### F-02 待办查看

1. `TodoListActivity.onCreate()` — 初始化 RecyclerView
2. 调用 `TodoDao.queryAll()` 获取 Cursor → 映射为 `List<Todo>`
3. `TodoAdapter` 绑定数据，每条显示标题 + 完成图标
4. 点击条目：`Intent intent = new Intent(this, TodoDetailActivity.class); intent.putExtra("todo_id", todo.getId());`

### F-03 待办编辑

1. `TodoEditActivity` 接收 `EXTRA_TODO_ID`，查询数据库中该条记录
2. 将标题和内容填充到 EditText
3. 用户修改后点击"保存"，调用 `TodoDao.update(Todo todo)`
4. 更新 `title`、`content` 两列，`is_done` 和 `create_time` 不变

### F-04 待办删除

1. `TodoDetailActivity` 点击删除按钮，弹出 `AlertDialog` 二次确认
2. 确认后调用 `TodoDao.deleteById(id)` → `db.delete("todo", "_id=?", new String[]{id})`
3. finish() 返回列表页，列表自动刷新

### F-05 状态切换

1. 列表页和详情页均提供 CheckBox / Switch 切换 `is_done`
2. 调用 `TodoDao.updateStatus(id, isDone)` → 仅更新 `is_done` 列
3. UI 即时反馈：已完成条目添加删除线样式 + 变灰

---

## 五、数据库设计

### 5.1 表结构：todo

```sql
CREATE TABLE IF NOT EXISTS todo (
    _id         INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT NOT NULL,
    content     TEXT,
    is_done     INTEGER NOT NULL DEFAULT 0,  -- 0=未完成, 1=已完成
    create_time INTEGER NOT NULL              -- 时间戳（毫秒）
);
```

### 5.2 字段说明

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `_id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 自增主键， RecyclerView 必须 _id 列 |
| `title` | TEXT | NOT NULL | 待办标题，必填 |
| `content` | TEXT | NULL | 待办内容，选填 |
| `is_done` | INTEGER | NOT NULL DEFAULT 0 | 完成状态 |
| `create_time` | INTEGER | NOT NULL | 创建时间戳 |

### 5.3 索引

```sql
CREATE INDEX idx_todo_done ON todo(is_done);
```

### 5.4 TodoDBHelper

```java
public class TodoDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "todo.db";
    private static final int DB_VERSION = 1;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE todo ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "title TEXT NOT NULL, "
            + "content TEXT, "
            + "is_done INTEGER NOT NULL DEFAULT 0, "
            + "create_time INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_todo_done ON todo(is_done)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS todo");
        onCreate(db);
    }
}
```

---

## 六、预判问题与解决方案

### 问题 1：主线程操作数据库导致 ANR

- **原因分析**：直接在主线程调用 `db.insert()` / `db.query()` 会阻塞 UI 线程，Android 8.0+ 严格禁止主线程 SQLite 操作。
- **解决方案**：所有数据库操作放到子线程执行。使用 `new Thread(() -> { ... }).start()` 封装 Dao 调用，结果通过 `runOnUiThread()` 回传更新 UI。已有登录功能的 JdbcHelper 也是同样模式。

### 问题 2：RecyclerView 列表刷新后光标泄漏

- **原因分析**：`queryAll()` 返回的 Cursor 若未及时关闭，会导致内存泄漏。Activity 销毁时若 Cursor 未释放，会产生数据库连接残留。
- **解决方案**：采用 Cursor → List 的方式，在方法内部完成遍历后立即关闭 Cursor。不在 Activity 中持有 Cursor 引用。Adapter 仅接收 List<Todo> 数据。

### 问题 3：旋转屏幕导致 Activity 重建，数据丢失

- **原因分析**：默认情况下屏幕旋转会销毁当前 Activity 并重建，未保存的数据（如编辑中未提交的输入）会丢失。
- **解决方案**：
  - 列表页：在 `onSaveInstanceState()` 中保存 RecyclerView 的滚动位置
  - 编辑页：`setRetainInstance(true)` 或使用 ViewModel 保存输入内容
  - 采用 `android:configChanges="orientation|screenSize"` 最小化重建（简易方案）

### 问题 4：SQLite 表升级缺失导致崩溃

- **原因分析**：开发过程中数据库表结构变更后，若 `onUpgrade()` 未正确实现或未升级 version 号，应用启动时会抛出 `IllegalStateException`。
- **解决方案**：`DB_VERSION` 随表结构调整递增，`onUpgrade()` 使用 `DROP TABLE IF EXISTS` 后重建（开发阶段可接受）。正式版应使用迁移方案。

---

## 七、界面原型描述

### 7.1 欢迎页（WelcomeActivity）

- **布局**：ConstraintLayout，居中显示"欢迎，用户名"大标题
- **内容**：欢迎语 + 当前用户名 + "待办列表"按钮（跳转 TodoListActivity）+ "退出登录"按钮（返回 MainActivity）
- **色调**：沿用现有项目蓝色主题 (#2196F3)

### 7.2 待办列表页（TodoListActivity）

- **顶部**：Toolbar，标题"我的待办"，支持返回上一页
- **主体**：RecyclerView，纵向排列，每条显示：
  - 左侧 CheckBox（切换完成状态）
  - 中间标题文本（已完成则加删除线）
  - 右侧"删除"图标按钮（点击弹出确认弹窗）
- **底部**：悬浮按钮（FAB）+，点击跳转 TodoEditActivity（创建模式）

### 7.3 待办编辑页（TodoEditActivity）

- **顶部**：Toolbar，标题根据模式显示"新增待办"或"编辑待办"
- **表单**：两个 EditText
  - 标题输入框（必填，hint="请输入标题"）
  - 内容输入框（选填，hint="请输入内容"，多行）
- **底部**：两个按钮横向排列，"取消"（finish）和"保存"（验证后写入 SQLite）

### 7.4 待办详情页（TodoDetailActivity）

- **顶部**：Toolbar，标题"待办详情"，支持返回
- **内容区**：垂直 LinearLayout
  - 标题：粗体 TextView
  - 状态标签：CheckBox + "已完成"/"未完成"
  - 内容：可滚动 TextView
  - 创建时间：较小字号的灰色文本
- **底部**：两个按钮，"编辑"（跳转 TodoEditActivity 编辑模式）和"删除"（确认后删除并 finish）

---

## 八、数据流图

### 8.1 整体数据流

```
用户操作(View层) → Intent → Activity(代码层) → 子线程 → SQLite DB → Cursor → UI刷新
```

所有数据库操作通过 `new Thread() + runOnUiThread()` 模式放到子线程执行，避免主线程ANR。

### 8.2 待办创建数据流

用户输入 → TodoEditActivity.saveTodo() → 子线程: TodoDBHelper.insert() → ContentValues → SQLite INSERT → 返回rowId → setResult(RESULT_OK) → finish() → TodoListActivity.onResume() → queryAll() → RecyclerView刷新

### 8.3 状态切换数据流

用户点击CheckBox → TodoAdapter回调 onStatusChanged → 子线程: TodoDBHelper.updateStatus() → SQLite UPDATE → runOnUiThread: loadTodos() → adapter更新UI

### 8.4 登录数据流

用户输入账号密码 → UserDao.findByName/insert → UserDBHelper: SQLite SELECT/INSERT → 返回User对象 → Toast反馈 + Activity跳转

---

## 九、核心功能时序图

### 9.1 待办创建时序

```
用户          TodoEditActivity     TodoDBHelper     SQLite
 │                  │                  │             │
 │--输入标题/内容-->│                  │             │
 │--点击保存------>│                  │             │
 │                 │--new Thread()--->│             │
 │                 │                  │--insert()-->│
 │                 │                  |<-rowId------│
 │                 |<-finish()--------│             │
 │<-返回列表--------│                  │             │
```

### 9.2 待办查看时序

```
用户      TodoListActivity      TodoDBHelper      SQLite
 │             │                   │              │
 │--进入------>│                   │              │
 │             |--onResume()----->  │              │
 │             |----new Thread()--->|              │
 │             |                    |--queryAll()-->│
 │             |                    |<-Cursor------│
 │             |<-List<Todo>---------|              │
 │<-列表显示----|<-adapter.setTodos()|              │
```

### 9.3 状态切换时序

```
用户         RecyclerView      TodoListActivity    TodoDBHelper     SQLite
 │             │                    │                  │              │
 │--点击------>│                    │                  │              │
 │             |--onStatusChanged-->│                  │              │
 │             |                    |--new Thread()--->│              │
 │             |                    |                   |--updateStatus>│
 │             |                    |<------------------|--done--------│
 │<--刷新------|--loadTodos()-------|                  │              │
```

### 9.4 待办删除时序

```
用户       TodoDetailActivity    TodoDBHelper     SQLite
 │              │                   │              │
 |--点击删除---->│                   │              │
 │              |--AlertDialog------>│              │
 |<--确认--------|<--对话框----------│              │
 │              |--new Thread()----->│              │
 │              |                    |--delete()---->│
 │              |                    |<--------------│
 │              |<-finish()----------|              │
 |<--返回列表-----|                    │              │
```

---

## 十、测试方案

### 10.1 自动化测试

本项目包含两类 Instrumented 测试，共 25 个测试用例：

| 测试类 | 数量 | 覆盖范围 |
|--------|------|---------|
| TodoDBHelperTest | 8 | insert / queryById / update / updateStatus / delete / queryAll排序 / 边界条件 |
| UserDaoTest | 4 | insert / findByName / 空查询 / 重复插入约束 |
| TodoEditActivityTest | 6 | 空标题校验 / 新建 / 取消 / 编辑预填 / 编辑保存 |
| TodoDetailActivityTest | 7 | 展示 / 占位符 / 状态切换 / 删除确认 / 删除 / 编辑入口 / 无效ID |

运行方式：`adb shell am instrument -w -e class "com.ljx.pt.*" com.ljx.pt.test/androidx.test.runner.AndroidJUnitRunner`

### 10.2 手动测试要点

- FAB按钮点击 → 成功跳转新建页面
- 新建待办 → 列表中可见
- 条目点击 → 详情页展示完整信息
- 状态切换 → CheckBox勾选后数据库is_done更新
- 删除 → 确认弹窗 → 确认后条目消失
- 编辑 → 标题/内容修改后保存 → 列表同步更新
