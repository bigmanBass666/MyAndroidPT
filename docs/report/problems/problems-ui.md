# UI/UX 相关问题与解决办法

## 问题 1：登录页密码输入框 inputType 错配

- **现象**：登录页密码输入框 `et_password` 只能输入数字，无法输入字母。用户在密码字段尝试输入字母时键盘只显示数字面板，导致所有含字母的密码都无法输入。

- **原因**：布局文件 `activity_main.xml` 中密码输入框的 `android:inputType` 被错误地设置为 `numberPassword`（数值密码模式），该模式限制输入仅为数字字符。正确的密码输入类型应为 `textPassword`（文本密码模式），支持字母、数字及特殊字符的组合输入。

- **解决办法**：将 `activity_main.xml` 中 `et_password` 的 `android:inputType` 从 `numberPassword` 改为 `textPassword`。同时该项目使用了 `TextInputLayout` 的 `password_toggle` 模式（`app:endIconMode="password_toggle"`），切换后用户可查看密码明文，确认输入内容。

- **涉及实训**：实训 1（登录模块）

- **代码片段**：
  ```xml
  <!-- 修复前：inputType="numberPassword" 导致限制数字输入 -->
  <com.google.android.material.textfield.TextInputEditText
      android:id="@+id/et_password"
      android:inputType="numberPassword" />

  <!-- 修复后：inputType="textPassword" 支持字母数字混合 -->
  <com.google.android.material.textfield.TextInputEditText
      android:id="@+id/et_password"
      android:inputType="textPassword" />
  ```

---

## 问题 2：待办编辑页文案复制粘贴错误（资源引用错位）

- **现象**：待办编辑页 `activity_todo_edit.xml` 中，"内容"标签显示为"密码"，"请输入待办内容"提示显示为"请输入密码"。用户在新建或编辑待办时，内容输入区域展示了错误的文案，造成严重的 UX 困惑。

- **原因**：典型的 Android 布局文件复制粘贴导致的资源引用错误。开发者在复制密码相关布局时，未完全替换其中的字符串资源引用。具体地，`label_content` 的 `android:text` 错误地引用了 `@string/label_password`（密码），而 `et_content` 的 `android:hint` 错误地引用了 `@string/hint_password`（请输入密码）。`strings.xml` 中已存在正确的 `label_todo_content`（内容：）和 `hint_todo_title`（标题（必填）），但缺少独立的提示资源 `hint_todo_content`。

- **解决办法**：
  1. 将 `activity_todo_edit.xml` 中 `label_content` 的 `android:text` 从 `@string/label_password` 改为 `@string/label_todo_content`
  2. 将 `et_content` 的 `android:hint` 从 `@string/hint_password` 改为 `@string/hint_todo_content`
  3. 在 `strings.xml` 中新增 `<string name="hint_todo_content">请输入待办内容</string>` 资源定义

- **涉及实训**：实训 2（待办模块）

- **代码片段**：
  ```xml
  <!-- 修复前：引用了密码相关的错误资源 -->
  <TextView
      android:id="@+id/label_content"
      android:text="@string/label_password" />  <!-- 显示"密码" -->

  <EditText
      android:id="@+id/et_content"
      android:hint="@string/hint_password" />  <!-- 显示"请输入密码" -->

  <!-- 修复后：引用正确的内容相关资源 -->
  <TextView
      android:id="@+id/label_content"
      android:text="@string/label_todo_content" />  <!-- 显示"内容：" -->

  <EditText
      android:id="@+id/et_content"
      android:hint="@string/hint_todo_content" />  <!-- 显示"请输入待办内容" -->
  ```

  ```xml
  <!-- strings.xml 新增 -->
  <string name="label_todo_content">内容：</string>
  <string name="hint_todo_content">请输入待办内容</string>
  ```

---

## 问题 3：空状态插画在有待办时仍然可见

- **现象**：当待办列表中有数据时，空状态的插画图标（`iv_empty`）仍然悬浮显示在列表上方。用户可以看到待办列表和空状态插画同时出现在屏幕上，视觉效果混乱。

- **原因**：`TodoListActivity.java` 的 `loadTodos()` 方法中，`empty_state_container` 内的 `iv_empty`（插画 ImageView）从未在代码中切换可见性。代码只切换了 `tv_empty_hint`（空状态文字）和 `rv_todo`（RecyclerView）的可见性，但忽略了 `iv_empty` 和整个 `empty_state_container` 的可见性控制。

- **解决办法**：引入整个 `empty_state_container` 的可见性控制，而非仅控制 `tvEmptyHint`。在空列表时显示整个容器（插画 + 文字），有数据时完全隐藏。
  - 添加 `private View emptyStateContainer;` 字段并在 `onCreate` 中绑定
  - 修改 `loadTodos()` 逻辑：切换 `emptyStateContainer` 和 `rv_todo` 的可见性

- **涉及实训**：实训 2（待办列表模块）

- **代码片段**：
  ```java
  // 修复前：只切换文字和列表的可见性，插画一直显示
  if (tvEmptyHint != null) {
      tvEmptyHint.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
      rvTodo.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
      // ← iv_empty 和 empty_state_container 从未被隐藏
  }

  // 修复后：切换整个空状态容器的可见性
  if (list.isEmpty()) {
      emptyStateContainer.setVisibility(View.VISIBLE);
      rvTodo.setVisibility(View.GONE);
  } else {
      emptyStateContainer.setVisibility(View.GONE);
      rvTodo.setVisibility(View.VISIBLE);
  }
  ```

---

## 问题 4：TodoDetailActivity CheckBox 监听器重复注册

- **现象**：待办详情页的完成状态 CheckBox 在快速切换时可能触发多次数据库更新，或在页面从后台返回时行为异常。虽然在简单操作中不易察觉，但在连续快速切换时存在隐患。

- **原因**：`TodoDetailActivity` 的 `loadTodo()` 在 `onResume` 生命周期中调用，每次调用都重新通过 `bindCheckListener()` 设置 `cbDone` 的 `OnCheckedChangeListener`。尽管代码中先解绑了旧监听器，但由于 `new Thread()` + `runOnUiThread` 的异步时序特性，如果在短时间内连续触发两次 `onResume`（如快速切到其他 Activity 再返回），第二个线程的 `runOnUiThread` 可能在第一个之后执行，导致监听器绑定状态不确定。

- **解决办法**：将监听器绑定逻辑移到 `onCreate` 中执行（只绑定一次），`onResume` 只负责加载数据和刷新 UI，不再重新绑定监听器。

- **涉及实训**：实训 3（待办详情模块）

- **代码片段**：
  ```java
  // 修复前：每次 onResume 重新绑定监听器
  @Override
  protected void onResume() {
      super.onResume();
      loadTodo();  // → 内部调用 bindCheckListener()
  }

  private void loadTodo() {
      // ...
      cbDone.setOnCheckedChangeListener(null);        // 临时解绑
      cbDone.setChecked(todo.isDone());               // 设置状态
      cbDone.setOnCheckedChangeListener(doneListener); // 重新绑定
  }

  // 修复后：只在 onCreate 绑定一次监听器
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      // ...
      cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
          new Thread(() -> {
              todoDao.updateStatus(todoId, isChecked);
              runOnUiThread(() -> updateStatusUI(isChecked));
          }).start();
      });
  }

  @Override
  protected void onResume() {
      super.onResume();
      loadTodo();  // 只加载数据，不重新绑定
  }
  ```

---

## 问题 5：密码强度实时检测与表单提交校验标准不一致

- **现象**：用户在注册页输入 "abcdef"（6 位纯字母）时，实时密码强度检测显示为"中"（绿色友好提示），但点击注册按钮后弹出 Toast "密码必须包含字母和数字"，注册被阻止。实时反馈显示"密码强度尚可"，但提交时却说"不行"，造成用户体验冲突。

- **原因**：`RegisterActivity` 中有两套不同的密码校验逻辑：
  1. `TextWatcher` 触发的 `checkPasswordStrength()`：长度 < 6=弱，>=6 含大小写数字中的部份=强，其他=中
  2. 表单提交时的按钮点击校验：长度 >= 6 + 必须同时包含字母和数字

  两套逻辑的判断标准不一致：TextWatcher 认为"abcdef"（6位纯字母）属于"中"强度，但表单提交却将其判定为不合法密码。

- **解决办法**：统一 `checkPasswordStrength()` 的评分逻辑与表单提交校验标准一致：
  - 弱（0）：长度 < 6，或虽长度 >= 6 但不同时包含字母和数字
  - 中（1）：长度 >= 6，同时包含字母和数字，但不满足强条件
  - 强（2）：长度 >= 8，包含大小写字母和数字

  修复后，输入 "abcdef" 直接显示"弱"，输入 "abc123" 显示"中"，实时提示与提交验证完全一致。同时可移除按钮点击处理中重复的密码校验代码。

- **涉及实训**：实训 5（注册模块密码强度）

- **代码片段**：
  ```java
  // 修复后：统一的密码强度检测逻辑
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

  | 输入密码 | 修复前 | 修复后 |
  |---------|-------|-------|
  | abcdef（6位纯字母） | 中（误导性） | 弱 |
  | 123456（6位纯数字） | 中（误导性） | 弱 |
  | abc123 | 中 | 中 |
  | Test99Pass | 强 | 强 |

---

## 问题 6：待办列表页 Toolbar 缺少返回按钮

- **现象**：待办列表页的 Toolbar 没有显示返回（导航）按钮，用户只能通过系统返回键回到欢迎页。对于不熟悉 Android 系统导航的用户，可能找不到返回上一层的方法，降低了导航的直观性。

- **原因**：`activity_todo_list.xml` 中的 `MaterialToolbar` 声明中没有设置 `android:navigationIcon` 属性，导致 Toolbar 没有返回图标。而其他页面（如待办编辑页、详情页）的 Toolbar 都正确设置了返回按钮，形成了不一致的导航体验。

- **解决办法**：在 `activity_todo_list.xml` 的 `MaterialToolbar` 中添加 `android:navigationIcon="@drawable/ic_arrow_back"`，并在 `TodoListActivity.java` 中设置 `setSupportActionBar(toolbar)` 配合 `onOptionsItemSelected` 处理返回事件。

- **涉及实训**：实训 2（待办列表模块）

- **代码片段**：
  ```xml
  <!-- 修复前：缺少 navigationIcon -->
  <com.google.android.material.appbar.MaterialToolbar
      android:id="@+id/toolbar"
      android:layout_width="match_parent"
      android:layout_height="?attr/actionBarSize"
      android:background="?attr/colorPrimary"
      android:titleTextColor="?attr/colorOnPrimary"
      android:title="@string/title_todo_list"
      app:popupTheme="@style/ThemeOverlay.Material3.Light" />

  <!-- 修复后：添加 navigationIcon -->
  <com.google.android.material.appbar.MaterialToolbar
      android:id="@+id/toolbar"
      android:layout_width="match_parent"
      android:layout_height="?attr/actionBarSize"
      android:background="?attr/colorPrimary"
      android:titleTextColor="?attr/colorOnPrimary"
      android:title="@string/title_todo_list"
      android:navigationIcon="@drawable/ic_arrow_back"
      app:popupTheme="@style/ThemeOverlay.Material3.Light" />
  ```

---

## 问题 7：Material Design 2 到 Material Design 3 主题升级冲突

- **现象**：项目主题系统完全采用了 Material Design 3（Material You）规范，与教材实训要求的 Material Design 2（MaterialComponents）有显著差异。具体表现为：
  - 主题使用 `Theme.Material3.DayNight.NoActionBar` 而非教材要求的 `Theme.MaterialComponents.DayNight.DarkActionBar`
  - `MyBtnStyle` 样式简化为继承 `Widget.Material3.Button`，移除了教材要求的自定义 background drawable
  - `MyEditStyle` 移除了 `background=@drawable/edit_text_bg` 和 `layout_height=50dp` 等属性

- **原因**：项目在迭代过程中自主升级到了 M3 设计体系。M3 组件（如 MaterialButton、MaterialCardView、MaterialCheckBox 等）默认使用 M3 的语义色值体系（Primary、Secondary、Tertiary、Error、Surface 等），不再需要通过 XML drawable 定义按钮背景色等传统方式来自定义样式。同时 M3 的 DayNight 主题内置了深色主题支持，通过 `values-night/themes.xml` 自动切换。

- **解决办法**：
  1. 定义了完整的 M3 颜色角色体系：Primary（green_500）、Secondary（teal_700）、Tertiary（amber_500）、Error（red_600）以及 Surface 分层色
  2. `MyBtnStyle` 继承 `Widget.Material3.Button`，利用 M3 组件的默认背景色和状态变化，不再需要自定义 `btn_bg_selector.xml`
  3. `MyEditStyle` 简化至仅保留 `textSize=16sp` 和 `paddingLeft=10dp`，依赖 TextInputLayout 容器提供 OutlinedBox 样式
  4. 在 `values-night/themes.xml` 中定义了独立的深色主题颜色，包含 surface、onSurface、outline 等色值
  5. 定义了 5 级 Shape 样式（ShapeExtraSmall 到 ShapeExtraLarge）和完整的 M3 Typography 字体体系

- **涉及实训**：实训 1-6（全局影响）—— 需注意在严格对标教材评分标准时，M3 升级可能被视为"偏离原文要求"

- **代码片段**：
  ```xml
  <!-- themes.xml（M3） -->
  <style name="Theme.MyAndroidPT" parent="Theme.Material3.DayNight.NoActionBar">
      <item name="colorPrimary">@color/green_500</item>
      <item name="colorSecondary">@color/teal_700</item>
      <item name="colorTertiary">@color/amber_500</item>
      <item name="colorError">@color/red_600</item>
      <!-- Surface 分层色 -->
      <item name="colorSurface">@color/grey_100</item>
      <item name="colorOnSurface">@color/grey_900</item>
  </style>

  <!-- 教材要求（M2）做对比 -->
  <!-- <style name="Theme.MyAndroidPT" parent="Theme.MaterialComponents.DayNight.DarkActionBar"> -->
  ```

---

## 问题 8：欢迎页仪表盘不刷新（数据加载生命周期错误）

- **现象**：登录进入欢迎页后，仪表盘显示待办统计。但当用户跳转到其他页面（如待办列表、待办编辑）进行操作（新建、修改、删除）后返回欢迎页时，仪表盘仍然显示之前的数据，不会自动更新。创建了待办后计数仍然显示 "— / —"。

- **原因**：`WelcomeActivity.java` 的 `loadDashboardData()` 只在 `onCreate` 生命周期方法中调用一次。当用户从其他 Activity 返回时，如果 Activity 没有被销毁重建（standard launch mode），`onCreate` 不会再次执行，因此 `loadDashboardData()` 不会被触发。Activity 的生命周期方法 `onResume`（每次回到该 Activity 时都会调用）中缺少对 `loadDashboardData()` 的调用。

- **解决办法**：在 `WelcomeActivity.java` 中添加 `onResume()` 方法，在其中调用 `loadDashboardData()`，确保每次回到欢迎页时仪表盘数据都重新加载。

- **涉及实训**：实训 2（欢迎页仪表盘模块）

- **代码片段**：
  ```java
  // 修复前：只在 onCreate 加载一次仪表盘
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_welcome);
      // ... 初始化逻辑
      loadDashboardData();  // ← 只在这里调用
  }

  // 修复后：添加 onResume 确保每次显示都刷新
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_welcome);
      // ... 初始化逻辑
      loadDashboardData();
  }

  @Override
  protected void onResume() {
      super.onResume();
      loadDashboardData();  // ← 从其他页面返回时刷新数据
  }
  ```

---

## 问题 9：WelcomeActivity SQLite 数据库连接泄漏

- **现象**：每次进入欢迎页（包括从其他 Activity 返回），都会创建一个新的 `TodoDBHelper` 实例但从不关闭。多次操作后可能导致数据库连接耗尽，长期运行可能引发内存压力。

- **原因**：`WelcomeActivity` 的 `loadDashboardData()` 方法在后台线程中创建了 `TodoDBHelper` 实例来查询待办数据，但完全没有调用 `helper.close()` 来释放资源。这是典型的 Android SQLite 资源泄漏问题。

- **解决办法**：在后台线程的查询完成后的 `finally` 块中调用 `helper.close()`，确保无论如何都会释放数据库连接。更彻底的解决方案是直接使用 `TodoDao` 替代（已在其他修复中实施）。

- **涉及实训**：实训 2（欢迎页模块）

- **代码片段**：
  ```java
  // 修复前：helper 创建后未关闭
  private void loadDashboardData() {
      new Thread(() -> {
          TodoDBHelper helper = new TodoDBHelper(this);
          List<Todo> allTodos = helper.queryAll(userId);
          // ... 计数逻辑
          // ← helper.close() 从未调用！
      }).start();
  }

  // 修复后：finally 块确保关闭
  private void loadDashboardData() {
      new Thread(() -> {
          TodoDBHelper helper = new TodoDBHelper(this);
          try {
              List<Todo> allTodos = helper.queryAll(userId);
              // ... 计数逻辑
          } finally {
              helper.close();  // ← 确保资源释放
          }
          runOnUiThread(() -> updateDashboardUI(...));
      }).start();
  }
  ```

---

## 问题 10：注册页 Toast 使用硬编码字符串

- **现象**：注册页的多处错误提示使用了硬编码的中文字符串，而登录成功的提示却正确使用了 `R.string.toast_register_success` 的字符串资源引用。风格不统一，且不利于后续的国际化和文案修改。

- **原因**：开发者在编写 `RegisterActivity.java` 的输入校验逻辑时，直接使用 `Toast.makeText(this, "中文字符串", ...)` 的方式编写错误提示，没有抽取到 `strings.xml` 中。这属于常见的代码质量问题。

- **解决办法**：将注册页所有硬编码的 Toast 字符串抽取到 `strings.xml` 中，并在 Java 代码中替换为 `R.string.*` 的引用。已在 `strings.xml` 中新增以下资源：
  - `toast_incomplete_input` — "输入信息不完整，请重新输入！"
  - `toast_password_too_short` — "密码长度至少6位"
  - `toast_password_need_letter_digit` — "密码必须包含字母和数字"
  - `toast_password_mismatch` — "两次输入的密码不一致，请重新输入！"
  - `toast_invalid_email` — "请输入正确的邮箱地址"
  - `toast_agree_protocol` — "请勾选同意用户协议"
  - `toast_user_exists` — "该用户名已被注册"
  - `toast_register_failed` — "注册失败，请稍后重试"

- **涉及实训**：实训 5（注册模块反馈提示）

- **代码片段**：
  ```java
  // 修复前：硬编码字符串
  Toast.makeText(this, "输入信息不完整，请重新输入！", Toast.LENGTH_SHORT).show();
  Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
  Toast.makeText(this, "密码必须包含字母和数字", Toast.LENGTH_SHORT).show();
  Toast.makeText(this, "该用户名已被注册", Toast.LENGTH_SHORT).show();

  // 修复后：引用字符串资源
  Toast.makeText(this, R.string.toast_incomplete_input, Toast.LENGTH_SHORT).show();
  Toast.makeText(this, R.string.toast_password_too_short, Toast.LENGTH_SHORT).show();
  Toast.makeText(this, R.string.toast_password_need_letter_digit, Toast.LENGTH_SHORT).show();
  Toast.makeText(this, R.string.toast_user_exists, Toast.LENGTH_SHORT).show();
  ```
