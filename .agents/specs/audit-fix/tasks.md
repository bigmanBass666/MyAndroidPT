# Tasks

## 任务分组与依赖关系

```
Group A (可并行)          Group B (独立)    Group C (有顺序)     Group D (可并行)
┌─────────────────┐      ┌──────────┐     ┌───────────┐       ┌─────────────────┐
│ A1: etEmail 死代码│      │ B1: 密码框│    │ C1: btn   │       │ D1: 旧版图标    │
│ A2: INTERNET 权限│      │  明文修复 │     │  selector  │       │ D2: AppCompat   │
│ A3: 未使用 import │      └──────────┘    │     ↓     │       │   overlay       │
│ A4: Extra 常量化  │                      │ C2: color │       │ D3: 空状态提示   │
└─────────────────┘                       │  Accent   │       └─────────────────┘
                                          │     ↓     │
                                          │ C3: 样式   │
                                          │  引用布局  │
                                          └───────────┘

Group E (有顺序)          Group F (独立)    Group G (独立)     Group H (可并行)
┌──────────────────┐     ┌────────────┐   ┌────────────┐    ┌─────────────────┐
│ E1: 线程模型统一   │     │ F1: init   │   │ G1: Todo   │    │ H1: dimens.xml  │
│      ↓            │     │  Data UI   │   │   DAO 层   │    │ H2: 类注释补全   │
│ E2: currentTodo   │     │ F2: clear()│   └────────────┘    └─────────────────┘
│      同步          │     │  策略讨论  │
│      ↓            │     └────────────┘
│ E3: 列表刷新确认   │
└──────────────────┘

依赖：C3 → C2 → C1；E3 → E2 → E1
其他组之间无依赖，可并行
```

---

- [x] **Task A1**: 删除 `MainActivity.etEmail` 死代码
  - 删除字段声明 `private EditText etEmail;` ✅
  - 删除 `findViewById(R.id.et_email)` 调用 ✅
  - 删除 `if (etEmail != null && email != null)` 死分支 ✅
  - 删除注册回传 email 相关的处理代码 ✅

- [x] **Task A2**: 移除 `AndroidManifest.xml` 残留 `INTERNET` 权限 ✅

- [x] **Task A3**: 清理未使用的 import ✅
  - `TodoDetailActivity.java`: 删除 `import android.database.Cursor` ✅
  - `TodoEditActivity.java`: 删除 `import com.ljx.pt.R`（冗余）✅
  - `TodoAdapter.java`: `import android.widget.CheckBox` → `MaterialCheckBox` ✅

- [x] **Task A4**: `RegisterActivity.java` Intent Extra key 常量化 ✅
  - 添加 `EXTRA_USER_NAME`, `EXTRA_PASSWORD`, `EXTRA_EMAIL` 常量 ✅

- [x] **Task B1**: 修复注册页密码框明文显示 ✅
  - `activity_register.xml:55`: `text` → `textPassword` ✅
  - `activity_register.xml:70`: `text` → `textPassword` ✅

- [x] **Task C1**: 修复 `btn_bg_selector.xml` 按压态 ✅
  - 正常态 `@color/colorPrimary` (green_500) ✅
  - 按压态 `@color/green_700` (green_700) ✅

- [x] **Task C2**: 补回 `colors.xml` 中的 `colorAccent` ✅
  - `<color name="colorAccent">#F4511E</color>` ✅

- [x] **Task C3**: 让 `MyBtnStyle` / `MyEditStyle` 在布局中被引用 ✅
  - `activity_main.xml`: 登录按钮 + 2 个输入框 ✅
  - `activity_register.xml`: 注册按钮 + 4 个输入框 ✅

- [x] **Task D1**: 替换旧版 Android 内置图标 ✅
  - 创建 3 个矢量 drawable: ic_arrow_back, ic_add, ic_delete ✅
  - 6 处引用全部替换 ✅

- [x] **Task D2**: 替换 Toolbar `ThemeOverlay.AppCompat.Light` ✅
  - 4 处布局文件全部替换为 `ThemeOverlay.Material3.Light` ✅

- [x] **Task D3**: 待办列表添加空状态提示 ✅
  - `activity_todo_list.xml`: TextView tv_empty_hint ✅
  - `TodoListActivity.java`: 可见性切换逻辑 ✅
  - `strings.xml`: tv_empty_hint 字符串资源 ✅

- [x] **Task E1**: 统一 `TodoDetailActivity.bindCheckListener()` 线程模型 ✅
  - UI 操作封装到 `runOnUiThread()` 中 ✅

- [x] **Task E2**: 详情页状态切换后同步 `currentTodo` 对象 ✅
  - `currentTodo.setDone(isChecked)` ✅

- [x] **Task E3**: 确认详情页状态切换后列表刷新机制 ✅
  - `TodoListActivity.onResume()` 已自动重载列表 ✅

- [x] **Task F1**: 修复 `initData()` 未恢复 `cbAutoLogin` UI 状态 ✅
  - `cbAutoLogin.setChecked(isAutoLogin)` ✅

- [x] **Task F2**: 退出登录 `clear()` 策略讨论 ✅
  - 代码注释说明保留密码的设计理由 ✅

- [x] **Task G1**: 新增 `dao/TodoDao.java` DAO 层 ✅
  - 新建 `dao/TodoDao.java` ✅
  - 更新 `TodoListActivity` 使用 `TodoDao` ✅
  - 更新 `TodoEditActivity` 使用 `TodoDao` ✅
  - 更新 `TodoDetailActivity` 使用 `TodoDao` ✅

- [x] **Task H1**: 创建 `dimens.xml` 统一间距管理 ✅
  - 定义 dp_4, dp_8, dp_12, dp_16, dp_24, dp_32 ✅

- [x] **Task H2**: 补全类注释（覆盖率从 ~17% 提升至 100%）✅
  - `MainActivity.java`: 登录页 ✅
  - `RegisterActivity.java`: 注册页 ✅
  - `WelcomeActivity.java`: 登录后欢迎页 ✅
  - `TodoListActivity.java`: 待办列表 ✅
  - `TodoEditActivity.java`: 新增/编辑待办 ✅
  - `TodoDetailActivity.java`: 待办详情 ✅
  - `bean/User.java`: 用户实体 ✅
  - `dao/UserDao.java`: 用户数据访问 ✅
  - `dbunit/UserDBHelper.java`: 用户数据库 ✅
  - `adapter/TodoAdapter.java`: 待办列表适配器 ✅

---

## Task Dependencies

| 任务 | 依赖 | 说明 |
|------|------|------|
| C2 | — | 无依赖（单纯添加颜色定义） |
| C1 | — | 无依赖（修复 drawable） |
| C3 | C1, C2 | 样式引用依赖 btn_bg_selector 修复和 colorAccent |
| E2 | E1 | 在同一个方法中修改，先统一线程模型再同步对象 |
| E3 | E2 | list 刷新确认在所有修复之后验证 |
| 其他任务 | — | 均无互斥依赖 |

## 并行策略

第一轮（无依赖，可并行执行）：
- A1, A2, A3, A4, B1, C1, C2, F1, F2, H1

第二轮（需要前置任务完成）：
- C3 ← C1, C2（样式引用）
- E2 ← E1（currentTodo 同步）
- E3 ← E2（列表刷新确认）

## 执行总结

| 阶段 | 代理 | 任务 | 状态 |
|------|------|------|------|
| 内联修复 | Claude | A2, A3, B1, C1, C2, F2, H1 | ✅ 全部完成 |
| 并行代理 1 | layout-fixer | C3, D1, D2, D3 | ✅ 全部完成 |
| 并行代理 2 | java-fixer | A1, A4, E1, E2, E3, F1, G1, H2 | ✅ 全部完成 |

## 构建验证

- `gradlew.bat assembleDebug` ✅ BUILD SUCCESSFUL
- 无旧版 `@android:drawable/` 引用 ✅
- 无 `ThemeOverlay.AppCompat` 引用 ✅
- 无 `etEmail` 死代码 ✅

---

## SQLite 连接泄漏修复（2026-06-16 logcat 发现）

- [x] **Spec**: `spec-sqlite-leak.md` 已写 ✅

- [x] L1: `UserDao.java` 添加 `close()` 方法 ✅
- [x] L2: `TodoDao.java` 添加 `close()` 方法 ✅
- [x] L3: `MainActivity` / `RegisterActivity` 的 `onDestroy()` 调用 `userDao.close()` ✅
- [x] L4: `TodoListActivity` / `TodoEditActivity` / `TodoDetailActivity` 的 `onDestroy()` 调用 `todoDao.close()` ✅
- [x] L5: 验证 `gradlew assembleDebug` 构建通过 ✅
