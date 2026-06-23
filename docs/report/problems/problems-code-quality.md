# 代码质量问题与解决办法

## 问题 1：硬编码 Toast 提示字符串未提取到 strings.xml

- **现象**：多个 Activity 中的用户面向提示信息直接使用中文字面量调用 `Toast.makeText()`，未提取到 `strings.xml` 管理。只有 `toast_login_success` 一条被提取为资源，其余均为硬编码。
- **原因**：开发初期未遵循 Android 国际化最佳实践，直接硬编码方便快速开发，后期也未统一回收到资源文件。
- **解决办法**：
  - 在 `strings.xml` 新增 7 个字符串资源：`toast_login_user_not_found`、`toast_login_wrong_password`、`toast_login_success`、`toast_auto_login_failed`、`toast_invalid_params`、`toast_load_failed`、`toast_title_required`
  - 在 `MainActivity.java` 替换 5 处硬编码 Toast：
    - `"输入信息不完整，请重新输入！"` → `R.string.toast_incomplete_input`
    - `"该用户不存在"` → `R.string.toast_login_user_not_found`
    - `"密码错误，请重新输入！"` → `R.string.toast_login_wrong_password`
    - `"登录成功！"` → `R.string.toast_login_success`（2 处重复）
    - `"自动登录失败，请手动登录"` → `R.string.toast_auto_login_failed`
  - 在 `TodoEditActivity.java` 替换 2 处：
    - `tilTitle.setError("标题不能为空")` → `getString(R.string.toast_title_required)`
    - `Toast "加载失败"` → `R.string.toast_load_failed`
  - 在 `TodoDetailActivity.java` 替换 2 处：
    - `"参数错误"` → `R.string.toast_invalid_params`
    - `"加载失败"` → `R.string.toast_load_failed`
- **涉及文件**：`app/src/main/res/values/strings.xml`、`app/src/main/java/com/ljx/pt/MainActivity.java`、`app/src/main/java/com/ljx/pt/TodoEditActivity.java`、`app/src/main/java/com/ljx/pt/TodoDetailActivity.java`
- **代码对比**：
  - 修改前：`Toast.makeText(MainActivity.this, "该用户不存在", Toast.LENGTH_SHORT).show();`
  - 修改后：`Toast.makeText(MainActivity.this, R.string.toast_login_user_not_found, Toast.LENGTH_SHORT).show();`

## 问题 2：TodoAdapter 硬编码颜色值

- **现象**：`TodoAdapter.java` 中待办状态指示器的已完成/未完成颜色使用 ARGB 十六进制字面量，未引用 `colors.xml` 资源。这意味着在夜间主题下颜色不会自动切换。
- **原因**：原始实现未使用资源引用，采用直接写死色值的方式快速实现视觉效果。
- **解决办法**：将两处硬编码色值替换为 `ContextCompat.getColor()` + `R.color.*` 引用：
  - `0xFF4CAF50`（已完成绿色）→ `ContextCompat.getColor(context, R.color.status_done)`
  - `0xFF9E9E9E`（未完成灰色）→ `ContextCompat.getColor(context, R.color.status_undone)`
- **涉及文件**：`app/src/main/java/com/ljx/pt/adapter/TodoAdapter.java`
- **代码对比**：
  - 修改前：`holder.tvStatus.setTextColor(0xFF4CAF50);`
  - 修改后：`holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_done));`

## 问题 3：RegisterActivity 变量名命名不一致（rbAgree 实为 CheckBox）

- **现象**：`RegisterActivity.java` 中使用变量名 `rbAgree`（RadioButton 前缀 `rb`），但实际布局中对应的控件是 `CheckBox`，命名严重违背 Android 控件前缀约定。
- **原因**：早期版本使用 `RadioButton`，后期改为 `CheckBox` 但变量名未同步修改，也未更新注释。
- **解决办法**：将所有 `rbAgree` 引用统一改为 `cbAgree`，涉及声明、`findViewById()`、`isChecked()` 共 3 处。
  - 注意：布局 XML 中的 ID `@+id/rb_agree` 未改为 `cb_agree`（属历史遗留，本次重构限定范围）
- **涉及文件**：`app/src/main/java/com/ljx/pt/RegisterActivity.java`
- **代码对比**：
  - 修改前：`private CheckBox rbAgree;` / `rbAgree = findViewById(R.id.rb_agree);` / `if (rbAgree.isChecked())`
  - 修改后：`private CheckBox cbAgree;` / `cbAgree = findViewById(R.id.rb_agree);` / `if (cbAgree.isChecked())`

## 问题 4：activity_todo_detail.xml 中 Chip 控件 ID 命名错误

- **现象**：`activity_todo_detail.xml` 中 `Chip` 控件的 `android:id` 为 `@+id/tv_detail_status`，使用了 `tv_`（TextView）前缀。Chip 是 Material Design 组件，继承自 `TextView` 但功能定位完全不同，命名无法反映其控件类型。
- **原因**：开发初期用 `TextView` 实现，后期改为 `Chip` 但 ID 未同步更新。
- **解决办法**：
  - 布局文件：`android:id="@+id/tv_detail_status"` → `android:id="@+id/chip_status"`
  - Java 代码：`findViewById(R.id.tv_detail_status)` → `findViewById(R.id.chip_status)`
  - 变量名 `chipStatus` 本身已正确（与 Chip 类型匹配），无需修改
- **涉及文件**：`app/src/main/res/layout/activity_todo_detail.xml`、`app/src/main/java/com/ljx/pt/TodoDetailActivity.java`

## 问题 5：edit_text_bg.xml 死代码残留

- **现象**：`drawable/edit_text_bg.xml` 文件存在于项目中，但已不被任何布局控件引用。该文件是为旧版 `MyEditStyle` 的自定义 `background` 准备的 selector drawable。
- **原因**：`MyEditStyle` 从实训 1 要求的自定义 background drawable 方案迁移到 `TextInputLayout.OutlinedBox` 风格后，`edit_text_bg.xml` 成为孤儿资源。当前 `MyBtnStyle` 已简化为仅保留 `textSize=16sp` 和 `paddingLeft=10dp`，移除了原来的 `background=@drawable/edit_text_bg` 属性。
- **解决办法**：确认该文件无引用后删除（或标记为待清理）。该文件不会影响功能，但增加 APK 体积和资源维护成本。
- **涉及文件**：`app/src/main/res/drawable/edit_text_bg.xml`

## 问题 6：colors.xml 遗留硬编码颜色别名资源

- **现象**：`colors.xml` 中存在 6 个"布局兼容别名"颜色资源，注释明确标注"硬编码，Night 主题下不跟随"：
  - `text_primary`（`#FF212121`）
  - `text_secondary`（`#FF757575`）
  - 以及其他 4 个同类颜色别名
- **原因**：这些是 Material Design 2 到 Material Design 3 迁移过程中遗留的兼容层。项目已全面使用 M3 的 `?attr/colorOnSurface` 等动态 token 引用，硬编码颜色不再被任何控件使用。
- **解决办法**：在全面确认零引用后清理这些 Legacy 颜色别名。其中 `btn_disabled` 也仅被已废弃的 `btn_bg_selector` 引用。
- **涉及文件**：`app/src/main/res/values/colors.xml`

## 问题 7：SharedPreferences 密码明文存储

- **现象**：`MainActivity.java` 中"记住密码"和"自动登录"功能将密码明文存储到 `SharedPreferences("spfRecord")` 中，`isRemember`、`isAutoLogin`、`userName`、`password` 四字段直接以明文方式持久化。
- **原因**：教学演示项目，未考虑安全存储。SharedPreferences 文件在 root 设备上可被直接读取。
- **解决办法**：教学用途可暂维持现状。生产环境需使用 `EncryptedSharedPreferences` 或 Android Keystore。
- **风险说明**：`spfRecord` 的 key 散落在 `MainActivity` 和 `WelcomeActivity` 两个文件中，修改密码存储方案时必须同步更新两处。

## 问题 8：Toast + finish() 竞态条件导致 Toast 被提前销毁

- **现象**：在 `TodoDetailActivity.java` 中删除待办后，同时调用 `Toast.show()` 和 `finish()`，Activity 关闭后 Toast 显示不足 1 秒就被销毁。
- **原因**：`Toast.LENGTH_SHORT` 约 2 秒，但 `finish()` 执行后 Activity 上下文被回收，Toast 随之消失。
- **解决办法**：添加 `Handler.postDelayed(() -> finish(), 800)` 延迟 800ms 关闭 Activity，给 Toast 足够的显示时间。需要额外导入 `android.os.Handler`。
- **涉及文件**：`app/src/main/java/com/ljx/pt/TodoDetailActivity.java`
- **代码对比**：
  - 修改前：`Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show(); finish();`
  - 修改后：`Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show(); new Handler().postDelayed(() -> finish(), 800);`

## 问题 9：registerForActivityResult 回传 key 为裸字符串

- **现象**：`RegisterActivity` 通过 `setResult()` 回传数据给 `MainActivity` 时，key 使用裸字符串 `"userName"` 和 `"password"`，无常量定义。这意味着两处 Activity 中如果有一处拼写错误，编译期无法发现。
- **原因**：快速开发，未提取常量。
- **解决办法**：提取为 `public static final String EXTRA_USERNAME = "userName"` 等常量，或沿用现状（项目内仅两处引用，改动价值有限）。
- **涉及文件**：`app/src/main/java/com/ljx/pt/MainActivity.java`、`app/src/main/java/com/ljx/pt/RegisterActivity.java`

## 问题 10：TodoEditActivity 编辑模式下标题错误显示"新增待办"

- **现象**：创建待办后进入详情页，点击编辑按钮进入编辑模式，工具栏标题仍显示"新增待办"而非"编辑待办"。`strings.xml` 中 `title_todo_edit` 资源已定义，但编辑模式下未正确设置。
- **原因**：编辑模式复用创建界面时，未根据操作模式（新增/编辑）动态更新 `Toolbar` 标题。
- **解决办法**：在 `TodoEditActivity.onCreate()` 中根据 `EXTRA_TODO_ID` 判断模式：`id == -1` 时设为"新增待办"，否则设为"编辑待办"。
- **涉及文件**：`app/src/main/java/com/ljx/pt/TodoEditActivity.java`

## 总结

上述代码质量问题的共性根源：

| 问题类型 | 出现次数 | 严重程度 |
|---------|---------|---------|
| 硬编码字符串 | 5+ 文件 | 高（影响国际化） |
| 硬编码颜色 | 1 文件 | 中（影响主题切换） |
| 命名不一致 | 2 文件 | 中（影响可读性） |
| 死代码残留 | 2 文件 | 低（不影响功能） |
| 竞态条件 | 1 文件 | 中（影响体验） |
| 密码明文 | 1 文件 | 低（教学项目可接受） |

所有高严重度问题（硬编码 Toast 字符串和颜色值）已在 6 月 22 日的代码清理任务中全部修复，`assembleDebug` 编译验证通过。命名不一致和死代码问题已完成分析，可根据需要择机处理。