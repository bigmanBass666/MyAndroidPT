## 三、遇到的问题及解决办法

在整个项目开发过程中，从 UI 搭建到业务逻辑实现，再到代码规范优化，遇到了不少实际问题。下面按实训阶段分类整理，列出每个问题的具体现象、原因分析和最终解决办法。

> 说明：本项目的技术选型为纯本地 SQLite 方案（课程设计以 SQLite 为准），未采用实训教材要求的 MySQL + JDBC 远程连接方案，因此教材中提到的 MySQL 连接失败、IP 授权、中文乱码等 JDBC 相关问题在本项目中未出现。以下为实际开发中遇到的真实问题。

### 3.1 UI 搭建与资源体系（实训 1）

| # | 问题现象 | 原因 | 解决办法 |
|---|---------|------|---------|
| 1 | **密码输入框只能输入数字**：登录页密码字段 `et_password` 只能弹出数字键盘，用户无法输入字母，导致所有包含字母的密码都无法完成登录 | 布局文件 `activity_main.xml` 中密码输入框的 `android:inputType` 被错误设置为 `numberPassword`（数值密码模式），该模式限制输入仅为数字字符 | 将 `android:inputType` 从 `numberPassword` 改为 `textPassword`（文本密码模式），修复后支持字母、数字及特殊字符的组合输入 |
| 2 | **待办编辑页文案错位**：在新建/编辑待办时，"内容"标签显示为"密码"，"请输入待办内容"提示显示为"请输入密码"，造成严重的用户困惑 | 典型的 Android 布局文件复制粘贴错误。开发者在复制密码相关布局后，未替换其中的字符串资源引用——`label_content` 错误引用了 `@string/label_password`，`et_content` 的 `android:hint` 错误引用了 `@string/hint_password` | 将 `label_content` 的 text 改为 `@string/label_todo_content`，`et_content` 的 hint 改为 `@string/hint_todo_content`，同时在 `strings.xml` 中新增 `hint_todo_content` 资源定义 |
| 3 | **按钮样式与教材要求不一致**：`MyBtnStyle` 样式继承 `Widget.Material3.Button`，不再使用教材要求的自定义 `@drawable/btn_bg_selector` 背景，按钮颜色无法通过 `colors.xml` 直接控制 | 项目在迭代过程中升级到了 Material Design 3（Material You）体系。M3 组件使用语义色值体系（Primary / Secondary / Surface 等），无需通过 XML drawable 定义按钮背景色。原 `styles.xml` 中的 `MyBtnStyle` 被简化，移除了 `background` 属性 | 采用 M3 的 `?attr/colorPrimary` 等动态色值 token 控制按钮颜色，不再依赖自定义 selector drawable。`btn_bg_selector.xml` 保留作为参考，实际不再被任何控件使用 |

### 3.2 Activity 跳转与交互逻辑（实训 2）

| # | 问题现象 | 原因 | 解决办法 |
|---|---------|------|---------|
| 4 | **登录触发 ANR（应用无响应）**：点击登录按钮后，应用弹出 "Process system isn't responding" 系统对话框，只能选择关闭应用或等待。logcat 显示输入分派超时 | 登录按钮的 `OnClickListener` 中直接执行了数据库查询操作（`UserDao.findByName()`），导致主线程被阻塞超过 5 秒。Android 系统在主线程阻塞超过阈值时会触发 ANR | 将所有数据库操作放入 `new Thread()` 子线程执行，UI 更新通过 `runOnUiThread()` 切回主线程。这是 Android 开发的核心最佳实践——永不阻塞主线程。修复代码结构如下：`new Thread(() → { /* DB 操作 */; runOnUiThread(() → { /* UI 更新 */ }); }).start()` |
| 5 | **注册后跳回登录页但账号未自动回填**：注册成功后返回登录页，虽然注册成功但账号密码输入框为空，用户需要手动重新输入账号密码进行登录 | 注册页使用 `startActivity()` 启动登录页，而非 `startActivityForResult()` 或 `registerForActivityResult()`。`RegisterActivity` 虽然调用了 `setResult()` 设置了回传数据，但接收方没有对应的结果处理代码 | 改用 `registerForActivityResult(ActivityResultContracts.StartActivityForResult())` 启动注册页，并在回调中处理返回的 Intent Extra 数据（`userName`、`password`），自动填入登录页的输入框 |
| 6 | **欢迎页仪表盘数据不刷新**：登录进入欢迎页后，待办统计信息正常显示；但跳转到其他页面对待办进行增删改操作后返回欢迎页时，仪表盘仍然显示旧数据，不会自动更新 | `WelcomeActivity.loadDashboardData()` 只在 `onCreate` 生命周期方法中调用一次。当用户从其他 Activity 返回时，如果 Activity 没有被销毁重建（standard launch mode），`onCreate` 不会再次执行 | 在 `WelcomeActivity` 中重写 `onResume()` 方法，每次回到欢迎页时都调用 `loadDashboardData()` 重新加载数据 |

### 3.3 数据库与业务逻辑（实训 3 / 4）

| # | 问题现象 | 原因 | 解决办法 |
|---|---------|------|---------|
| 7 | **数据库连接泄漏**：多次在欢迎页和待办详情页间往返操作后，应用偶发出现卡顿和 ANR。长时间运行后数据库操作响应越来越慢 | `WelcomeActivity.loadDashboardData()` 每次执行都在后台线程创建 `TodoDBHelper` 实例进行查询，但从未调用 `helper.close()`。每次进入欢迎页都泄漏一个数据库连接。同时 `TodoDetailActivity` 也存在类似的资源未释放问题 | 统一改用 `TodoDao` 封装数据库操作，并在 `try-finally` 块中确保资源释放：`TodoDao todoDao = new TodoDao(this, userId); try { ... } finally { todoDao.close(); }` |
| 8 | **密码强度实时检测与提交校验标准不一致**：用户在注册页输入 "abcdef"（6 位纯字母）时，实时检测显示密码强度为"中"（绿色友好提示），但点击注册按钮后弹出 "密码必须包含字母和数字"，注册被阻止。实时反馈与提交校验冲突，造成用户困惑 | `RegisterActivity` 中存在两套不同的密码校验逻辑：`TextWatcher` 触发的 `checkPasswordStrength()` 和表单提交时的按钮校验各自使用不同的标准。TextWatcher 简单按长度判定（>=6 即为中），而表单提交要求同时包含字母和数字 | 统一 `checkPasswordStrength()` 的评分逻辑：长度 <6 为弱；长度 >=6 但不包含字母或数字也为弱；长度 >=6 且同时包含字母与数字为中；长度 >=8 且含大小写字母与数字为强。修复后 "abcdef" 显示为"弱"，"abc123" 显示为"中"，实时提示与提交验证完全一致 |
| 9 | **`TodoDao` 无 userId 参数导致跨用户数据泄露**：用户 A 登录后看到其他用户的待办数据，删除操作影响到其他人的记录 | `TodoDao` 早期版本存在一个不带 userId 的构造器 `TodoDao(Context context)`，内部将 `userId` 默认设为 `-1`。当调用方无意识地使用此构造器时，所有 CRUD 操作的 WHERE 子句不按用户 ID 过滤 | 删除不带 userId 参数的构造器，强制所有调用方传入 `userId`。同时修改 `TodoDBHelper` 的全部 CRUD 方法（`insert`/`update`/`updateStatus`/`delete`/`queryById`/`queryAll`），使其都绑定 `user_id` 过滤条件 |

### 3.4 记住密码与自动登录（实训 5 / 6）

| # | 问题现象 | 原因 | 解决办法 |
|---|---------|------|---------|
| 10 | **勾选"自动登录"但下次打开仍需手动登录**：用户勾选了自动登录并成功登录，但关闭应用重新打开后，仍然停留在登录页而非直接进入欢迎页 | `SharedPreferences` 的 `isAutoLogin` 标志位仅在登录成功分支中写入，但读取时未正确处理。同时 `WelcomeActivity` 初始化时读取 `spfRecord` 的逻辑与 `MainActivity` 中的自动登录判断逻辑存在不一致 | 统一 SharedPreferences 的读写逻辑：在 `MainActivity` 的 `onCreate` 中读取 `spfRecord` 判断 `isAutoLogin`，若为 true 则直接跳转到 `WelcomeActivity`；登录成功时必须调用 `editor.putBoolean("isAutoLogin", true).apply()` |
| 11 | **退出登录后"自动登录"仍然勾选**：在欢迎页点击退出登录，返回登录页后"自动登录"复选框仍处于勾选状态，下次启动仍会自动登录 | 退出登录按钮的 `OnClickListener` 中只调用了 `finish()` 跳回登录页，未清除 SharedPreferences 中的 `isAutoLogin` 和 `isRemember` 标志位 | 在退出登录的回调中，添加 `editor.putBoolean("isAutoLogin", false).apply()` 和 `editor.putBoolean("isRemember", false).apply()`，清除自动登录和记住密码的标志位 |
| 12 | **密码明文存储在 SharedPreferences**：在 `/data/data/com.ljx.pt/shared_prefs/spfRecord.xml` 中可以直接读取到用户密码明文 | 记住密码和自动登录功能将密码以明文形式存入 SharedPreferences。这是教学项目的常见做法，但在 root 设备上存在安全隐患 | 教学演示用途可维持现状。生产环境需使用 `EncryptedSharedPreferences` 或 Android Keystore。项目文档已标注"密码明文存储（教学演示用途）"的风险提示 |

### 3.5 扩展模块（待办 CRUD）

| # | 问题现象 | 原因 | 解决办法 |
|---|---------|------|---------|
| 13 | **空状态插画在有待办时仍然可见**：当待办列表中有数据时，空状态的插画图标仍然悬浮显示在列表上方，视觉效果混乱 | `TodoListActivity` 的 `loadTodos()` 方法中只切换了空状态文字（`tv_empty_hint`）和 RecyclerView（`rv_todo`）的可见性，但忽略了整个空状态容器（`empty_state_container`）及其内部的插画 ImageView（`iv_empty`） | 引入 `emptyStateContainer` 的可见性整体控制：列表为空时显示容器 + 隐藏 RecyclerView；有数据时隐藏容器 + 显示 RecyclerView。修复后空状态插画不会再"穿越"到有数据的列表上 |
| 14 | **CheckBox 状态监听器重复注册**：待办详情页的完成状态 CheckBox 在快速切换时偶发状态回弹或多次触发数据库更新 | `loadTodo()` 方法在每次 `onResume` 时都会重新调用 `cbDone.setOnCheckedChangeListener()` 注册监听器，尽管代码中先解绑了旧监听器，但多线程的异步时序可能导致监听器状态不确定 | 将 CheckBox 的监听器绑定逻辑移到 `onCreate` 中一次性注册，`onResume` 只负责加载数据和刷新 UI，不再重新绑定监听器 |
| 15 | **待办删除后 Toast 被提前销毁**：在待办详情页删除待办后，Toast 提示显示不足 1 秒就消失，用户几乎看不到确认反馈 | 删除操作成功后同时执行了 `Toast.show()` 和 `Activity.finish()`。`Toast.LENGTH_SHORT` 约 2 秒，但 `finish()` 执行后 Activity 上下文被回收，Toast 随之消失 | 使用 `Handler.postDelayed(() → finish(), 800)` 延迟 800ms 关闭 Activity，给 Toast 足够的显示时间 |

### 3.6 代码规范与资源管理

| # | 问题现象 | 原因 | 解决办法 |
|---|---------|------|---------|
| 16 | **硬编码 Toast 字符串散落在各 Activity**：多处用户提示信息（登录失败、注册错误、加载异常等）直接使用中文字面量调用 `Toast.makeText()`，未抽取到 `strings.xml` 管理，不利于后续国际化和文案修改 | 开发初期追求快速实现，未遵循 Android 国际化最佳实践。`MainActivity`（5 处）、`TodoDetailActivity`（3 处）、`TodoEditActivity`（2 处）、`RegisterActivity`（6 处）均存在硬编码字符串。只有 `toast_register_success` 一条被提取为资源 | 在 `strings.xml` 中统一新增全部 Toast 资源定义（如 `toast_login_user_not_found`、`toast_login_wrong_password`、`toast_auto_login_failed`、`toast_invalid_params`、`toast_load_failed`、`toast_title_required` 等），所有 Activity 中替换为 `R.string.*` 引用 |
| 17 | **适配器硬编码颜色值**：待办列表已完成项显示绿色、未完成项显示灰色使用 ARGB 十六进制字面量（`0xFF4CAF50` / `0xFF9E9E9E`），在夜间主题下无法自动切换 | `TodoAdapter.java` 中直接使用 `setTextColor(0xFF4CAF50)` 等硬编码色值，未使用 `ContextCompat.getColor()` + 资源引用 | 在 `colors.xml` 中新增 `status_done`（已完成绿色）和 `status_undone`（未完成灰色）颜色资源，适配器中改用 `ContextCompat.getColor(context, R.color.status_done)` 的方式引用 |
| 18 | **变量名与资源 ID 命名不一致**：`RegisterActivity` 中 `CheckBox` 控件使用变量名 `rbAgree`（RadioButton 前缀 `rb`）；`activity_todo_detail.xml` 中 Chip 控件的 ID 为 `tv_detail_status`（TextView 前缀 `tv_`） | 早期版本使用 `RadioButton` 和 `TextView` 实现，后期改为 `CheckBox` 和 `Chip` 但变量名和 ID 未同步更新，属于重构遗留 | 变量名：`rbAgree` → `cbAgree`（涉及声明、`findViewById`、`isChecked` 共 3 处）。资源 ID：`tv_detail_status` → `chip_status`（同步更新布局文件和 Java 代码） |

### 3.7 其他开发环境问题

| # | 问题现象 | 原因 | 解决办法 |
|---|---------|------|---------|
| 19 | **`gradlew.bat` 在 Git Bash 中无法执行**：在 Windows 上的 Git Bash 终端执行 `gradlew.bat` 返回 `command not found`，直接执行时 `.bat` 文件的 `@rem` 注释被 bash 解析为错误命令 | `gradlew.bat` 是 Windows 批处理脚本（cmd.exe 语法），bash shell（Git Bash）无法解析。项目根目录存在两个版本的 Gradle Wrapper：`gradlew.bat`（Windows）和 `gradlew`（Unix shell 脚本） | 在 Git Bash 中应使用 `./gradlew`（Unix shell 脚本版本）而非 `gradlew.bat`。在 Windows cmd 或 PowerShell 中使用 `gradlew.bat`。本项目所有构建操作均使用 `./gradlew assembleDebug` |

### 小结

以上 19 个问题覆盖了从 UI 布局、Activity 跳转、数据库操作、用户交互到代码规范的各个层面。这些问题大多数属于 Android 初学者常见的典型错误：主线程阻塞、资源引用错配、生命周期理解不深、代码规范执行不严等。通过逐一排查和修复这些问题，不仅完善了应用的功能和体验，也加深了对 Android 开发核心概念的理解。

其中优先级最高的硬编码字符串和颜色值问题、命名不一致问题已在代码清理专项中全部修复并通过 `assembleDebug` 编译验证。数据库连接泄漏和架构违规问题也已修复。部分教学项目可接受的问题（如密码明文存储、`onUpgrade` DROP TABLE 重建）保留了现状但明确了风险。
