# 待办按用户隔离 (Multi-user Todo Isolation)

> **日期**: 2026-06-16
> **作者**: Claude
> **关联 bug**: 用户实测发现多个账号登录后看到同一份待办列表
> **优先级**: P1 — 影响核心功能正确性（不同用户的待办被串数据是设计 bug）
> **范围限定**: 仅**代码层**：实现"每用户独立 todo 列表"。**不引入 MySQL**（沿用现有 SQLite 方案，参见 `materials/1_practical_trainning/SQLite替代MySQL的说明.md`）

---

## 1. 现状与根因

### 1.1 现象

注册两个账号（userA / userB），分别登录后都看到全部待办，包括另一个账号创建的。

### 1.2 根因（代码层）

- `TodoDBHelper.onCreate()` 建表时 **没有 `user_id` 字段**：
  ```sql
  CREATE TABLE todo (
      _id INTEGER PRIMARY KEY AUTOINCREMENT,
      title TEXT NOT NULL,
      content TEXT,
      is_done INTEGER NOT NULL DEFAULT 0,
      create_time INTEGER NOT NULL
  )    -- 无 user_id
  ```
- `TodoDBHelper.queryAll()` / `queryById()` / `updateStatus()` / `delete()` 全部不接 userId 参数，**SQL 没有 WHERE user_id=? 过滤**。
- `TodoListActivity.loadTodos()` 调用 `todoDao.queryAll()`，**所有用户拉同一份数据**。
- `TodoEditActivity.saveTodo()` 调 `todoDao.insert(todo)` 时也未注入 userId。

### 1.3 与教材要求的差距

- 教材（实训 1-6）只覆盖"登录注册 + 自动登录 + 共享待办"，**未明确要求多用户 todo 隔离**。
- 课程设计任务书 `materials/2_design/01-assignment.md` 第 4 章功能需求只列 CRUD，并未限定为单用户。
- 但功能语义上"待办列表"是**当前登录用户的**待办，不能跨用户共享 — 这是用户合理预期，否则会出现"用户 A 看到用户 B 的私人备忘"的隐私事故。

### 1.4 业务影响

| 用户场景 | 现状 | 应当 |
|---|---|---|
| A 注册 → 创建"还信用卡" 待办 | 数据库 1 条 todo | 仅 A 可见 |
| B 注册 → 进入待办列表 | **看到 A 的"还信用卡"** ❌ | 仅 B 自己的（空列表）✅ |

---

## 2. 设计目标

实现"用户级 todo 数据隔离"：
- 每个 todo 必须**归属某个 user** (`user_id`)
- 查询 / 更新 / 删除 时**强制按 user_id 过滤**，防止跨用户串数据
- 登录成功后，待办页面只呈现**当前登录用户**的 todo
- 注册成功时，新用户应当从空开始
- 已有数据需迁移（DB version 升级 + onUpgrade 改写）

---

## 3. 候选方案对比

### 方案 A：单表 + user_id 字段（推荐）

**核心思路**：todo 表加 `user_id` 列，所有 CRUD 都按 `WHERE user_id=? AND [其他条件]` 过滤。

- ✅ **结构最简**：单表，所有现有 CRUD 模式不变，仅添加 userId 参数
- ✅ **SQLite 友好**：本来就是一个轻量数据库，不分库/分表的复杂度
- ✅ **升级成本低**：DB_VERSION +1，`onUpgrade()` 给旧表 ALTER TABLE 加列、默认值取首个用户（或 −1 表示"匿名数据可忽略"）
- ✅ **未来易扩展**：易加 `WHERE ... AND user_id=?` 之外的额外过滤
- ❌ 必须保证**所有 DAO 方法都强制传 user_id**（漏一处即出 bug）— 通过编译器强制（构造 TodoDao 时传 userId）

### 方案 B：每用户独立表（`todo_<userId>`）

- ❌ 动态建表 / drop table / 迁移用户改名时表名跟着换 — 复杂度过高
- ❌ 单设备 demo，不必要

### 方案 C：单表 + 一对一外键约束 + view 拼接

- ❌ SQL view 在 SQLite 中可工作但带来 schema 复杂度
- ❌ join 性能在小数据量下没差别，方案 A 已足够

**结论：选 A**。

---

## 4. 数据模型变更

### 4.1 新 todo 表结构

```sql
CREATE TABLE todo (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,           -- 新增：所属 user.id
    title TEXT NOT NULL,
    content TEXT,
    is_done INTEGER NOT NULL DEFAULT 0,
    create_time INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES userinfo(_id) ON DELETE CASCADE
)
CREATE INDEX idx_todo_user_id ON todo(user_id);
```

### 4.2 迁移方案（DB_VERSION 1 → 2）

- `onCreate()`：直接按新 schema 建（含 FK、索引、外键约束）
- `onUpgrade(db, oldVersion, newVersion)`：
  - old=1 → new=2：`ALTER TABLE todo ADD COLUMN user_id INTEGER` — 旧数据 `user_id = NULL`
  - 应用启动时检测到 `user_id IS NULL` 的旧 todo，**默认归于当前登录用户**，或**直接删除**（更安全：1.x 没有用户隔离，所有 todo 都是"无主数据"，删除避免串数据 bug 复发）
  - 决策：**直接删除 user_id IS NULL 的旧 todo**（简单、可控、符合"隐私清理"预期）
  - 添加索引 `CREATE INDEX idx_todo_user_id ON todo(user_id)`

### 4.3 Todo 实体

- 新增字段：`private long userId;`
- getter / setter 同步

---

## 5. 关键接口设计

### 5.1 强制 user_id 传参 — 防止遗漏

```java
// TodoDBHelper
public List<Todo> queryAll(long userId)          // 强制接 userId
public Todo queryById(long userId, long id)
public int update(long userId, Todo todo)
public int updateStatus(long userId, long id, boolean isDone)
public int delete(long userId, long id)
public long insert(long userId, Todo todo)       // 内部注入 userId 到 todo
```

- **没有任何不带 userId 的 entry point** — 编译期强制
- 异常路径：若误用无 userId 的旧 API，编译期间即失败

### 5.2 TodoDao 层级透传

```java
public class TodoDao {
    private final long userId;          // 持有当前用户的 id
    public TodoDao(Context context, long userId) { ... }
    public List<Todo> queryAll()        // 仅用自身 userId，不再接收额外参数
    ...
}
```

### 5.3 Activity 传 userId 流程

- `WelcomeActivity` 已存 `userName`，新增查 `User.id` 写入 Intent，或者**登录时直接传 user.id**
- `TodoListActivity` / `TodoEditActivity` / `TodoDetailActivity` 通过 `getIntent().getLongExtra("user_id", -1)` 接收
- 登录成功跳转：
  ```java
  Intent intent = new Intent(this, WelcomeActivity.class);
  intent.putExtra("user_id", loginUser.getId());
  intent.putExtra("userName", loginUser.getName());
  startActivity(intent);
  ```
- 待办 4 个 Activity 互相跳转时也带 `user_id`

### 5.4 SharedPreferences 持久化 userId

- 在登录成功 + SP 写入时，同时存 `userId`（与 `userName` 一并存 "user_info"）
- 自动登录时（initData）读回 userId，连同 userName 一起用于登录跳页

---

## 6. 修改文件清单

| 文件 | 改动 |
|------|------|
| `Todo.java` | 新增 `userId` 字段 + getter/setter |
| `TodoDBHelper.java` | 新建 user_id 列 + 外键约束 + 索引；onUpgrade 升级脚本；所有 CRUD 方法加 `long userId` 参数并 WHERE 过滤 |
| `TodoDao.java` | 构造接 `long userId`，内部传递；CRUD 方法不再接 userId 参数 |
| `UserDBHelper.java` 或 `UserDao.java` | 新增 `findByName(String)` 已存在，需要在登录成功时一并返回 id（可能已返回 User 对象，检查并补全） |
| `MainActivity.java` | 登录成功跳转 WelcomeActivity 时 putExtra `user_id`；remember/auto login 流程回读 user_id |
| `WelcomeActivity.java` | 接收 user_id 并转发到待办页 |
| `TodoListActivity.java` | 从 Intent 取 user_id 构造 TodoDao，所有 loadTodos 等操作使用此 user_id |
| `TodoEditActivity.java` | 从 Intent 取 user_id，构造 TodoDao；新增待办时注入 user_id；启动时 putExtra("user_id", ...) |
| `TodoDetailActivity.java` | 从 Intent 取 user_id，构造 TodoDao；跳转编辑页带 user_id |
| `TodoAdapter.java` | 无需变动（仅展示，不涉及数据访问） |
| `app/src/androidTest/**` | 重建 androidTest（5 个文件 — 适配新签名 + userId 初始化） |

---

## 7. 边界 & 安全考虑

### 7.1 不可绕过的 userId 过滤

- 不开"管理员模式"或"全用户视图" — 用户 demo App 无此需求
- 不提供 `queryById(id)` 这种会忽略 user_id 的捷径

### 7.2 用户删除时 todo 自动清理

- SQL 外键 `ON DELETE CASCADE` — 删 user 时一并删其 todo（demo 阶段不实现用户删除功能，但保留扩展性）

### 7.3 兼容性

- 旧 Android 版本 `ALTER TABLE ADD COLUMN` 自 SQLite 3.2.0 起可用，minSdk=24 完全支持

### 7.4 隐私事故预防

- 即使 UI bug 误跳到待办页，没有 user_id 的 todo（v1 数据）也不会被任何 user 看到 — **`onUpgrade` 中清空 user_id IS NULL 是关键**

---

## 8. 执行顺序（8 个原子 commit）

按 `git_rules.md` 的原子提交要求逐项拆分：

1. **commit-1**: `Todo.java` 新增 `userId` 字段 + getter/setter（编译会报错 → 必须附带改 TodoDBHelper 等，但拆成逻辑单元：先改实体）
2. **commit-2**: `TodoDBHelper.java` — 加 user_id 列 + 索引 + 外键 + onUpgrade；CRUD 方法全签名加 `long userId`
3. **commit-3**: `TodoDao.java` — 改造为持有 userId 的实例状态
4. **commit-4**: `MainActivity.java` — 登录跳转带 user_id + SP 同步存 user_id
5. **commit-5**: `WelcomeActivity.java` / `TodoListActivity.java` / `TodoEditActivity.java` / `TodoDetailActivity.java` — 接收并传递 user_id
6. **commit-6**: `androidTest/` 5 文件适配新签名
7. **commit-7**: 验证 + 多账号 E2E 测试（手动开两个账号验证隔离）

> 每步后必须 `gradlew.bat :app:assembleDebug` 通过

---

## 9. 验证策略

### 9.1 单元验证（编译）

- `gradlew.bat :app:assembleDebug` 通过
- `gradlew.bat :app:assembleDebugAndroidTest` 编译通过（不一定运行，仅编译）

### 9.2 手工 E2E 测试（模拟器）

| 步骤 | 预期 |
|---|---|
| 注册 userA | userA 进入待办空列表 |
| userA 创建 todo: "A的任务" | 列表显示 1 条 |
| 登出 userA | — |
| 注册 userB | userB 列表空（**不应看到 A 的"任务"**） |
| userB 创建 todo: "B的私人" | 列表显示 1 条 |
| 登出 userB → 登录 userA（记住密码） | userA 看到 "A的任务"，**不应看到 B 的"私人"** |

### 9.3 边界检查

- `queryById(userA.id, B的todoId)` 应返回 null（双重过滤保护）
- `delete(userA.id, B的todoId)` 应返回 0（不会误删）
- DB 升级：v1 用户升级 v2 后旧数据被清空（避免泄漏）

---

## 10. 风险评估

| 风险 | 概率 | 影响 | 缓解 |
|---|---|---|---|
| 漏改某处 CRUD 漏加 userId → 编译报错 | 中 | 低 | 强制签名 + grep `'queryById\(long'` 确认 |
| onUpgrade 写错 SQL → 升级失败 | 低 | 中 | 加测试用例（手动模拟） |
| 现有 todo 数据迁移策略选择失误 | 低 | 中 | 选"清空旧数据"方案，不留隐患 |
| Activity 间 user_id 传丢 → 走到默认 -1 | 中 | 高 | userId=-1 时 TodoDao 拒绝操作，返回空 |
| auto login 流程没把 userId 存 SP | 中 | 中 | commit-4 + commit-5 联动测试 |

---

## 11. 不在本次范围

- **MySQL 改造**：已有 `SQLite替代MySQL的说明.md` 决策保留
- **用户删除（清空该用户所有 todo）**：UI 没触发点，不实现
- **多设备同步**：无服务器，无需考虑
- **跨 Activity 共享 userId 的别样方案（如 `ViewModel` + StateFlow）**：当前 demo 架构用 Intent 透传足够
- **课程设计报告编写**：此文档属代码规划，不替代报告

---

## 12. 预计工作量

| 阶段 | 估计 |
|---|---|
| TODO 实体 + DBHelper schema + onUpgrade | 1.5 h |
| TodoDao / MainActivity / 4 个 Activity 串联 | 2 h |
| androidTest 适配 | 0.5 h |
| 模拟器 E2E 双账号隔离验证 | 0.5 h |
| **总计** | **≈ 4.5 h（含 review）** |

---

## 13. 关键设计决策

| 决策 | 选择 | 理由 |
|---|---|---|
| user_id 类型 | `INTEGER` (long) | 外键一致性，对齐 User.id `INTEGER PRIMARY KEY` |
| 外键行为 | `ON DELETE CASCADE` | 保留扩展性，删除用户时自动清理 |
| 旧数据迁移策略 | **删除** `user_id IS NULL` 的 todo | 隐私安全优先；demo 数据无可保留价值 |
| userId 注入位置 | Activity 持有 → Dao 构造时注入 | 编译期强制，不会有"忘记传 userId"路径 |
| Intent 内 user_id key | `"user_id"` | 与已有 `"todo_id"` 一致风格（裸字符串约定沿用） |
| SP 中 user_id key | `"userId"` | 与已有 `"userName"` 风格一致 |
