# 审计问题修复 Spec

## Why

根据 `materials/审计报告.md` 的教材符合性审查，项目存在 5 项 P1（重要）和 14 项 P2（轻微）问题，合计 19 项。修复这些问题可将课程设计评分从 77/100 提升至 90+/100，消除功能缺陷、安全风险和 UI 不一致。

## What Changes

### 一、代码清理（4 项）
- 删除 `MainActivity.etEmail` 死代码及关联逻辑（P1-#9）
- 移除 `AndroidManifest.xml` 残留 `INTERNET` 权限（P2-#5）
- 清理 3 个文件的未使用 import（P2-#22）
- `RegisterActivity.java` Intent Extra key 改为常量（P2-#9dup）

### 二、注册安全修复（1 项）
- 注册页密码框 `inputType` 从 `text` 改为 `textPassword`（P1-#13）

### 三、样式系统修复（3 项）
- `btn_bg_selector.xml` 修复按压态，使按下时变色（P2-#3）
- `colors.xml` 补回 `colorAccent` 定义（P2-#4）
- 让 `MyBtnStyle` / `MyEditStyle` 在布局文件中被引用，使教材要求产出有效（P2-#1, #2）

### 四、UI 组件修复（3 项）
- 替换 4 处 Toolbar + 1 处 FAB + 1 处列表的旧版内置图标为 Material 矢量图标（P2-#14-16）
- 替换 4 处 Toolbar 的 `ThemeOverlay.AppCompat.Light` 为 `ThemeOverlay.Material3.Light`（P2-#17）
- 待办列表添加空状态提示（P2-#12）

### 五、待办模块修复（3 项）
- `TodoDetailActivity.bindCheckListener()` 线程模型统一，将 UI 更新封装到 `runOnUiThread()`（P1-#21）
- 详情页状态切换后同步 `currentTodo` 对象（P2-#11）
- 确认详情页状态切换后列表刷新机制，必要时补充刷新逻辑（P1-#10）

### 六、登录页修复（1 项 + 1 项讨论）
- `initData()` 恢复 `cbAutoLogin` UI 状态（P2-#8）
- **讨论项**: 退出登录 `clear()` 策略（P2-#7）— 设计选择，暂不强制修改，仅在注释中说明理由

### 七、架构改进（1 项）
- 新增 `dao/TodoDao.java`，封装 `TodoDBHelper` 的 CRUD 操作（P1-#6）

### 八、全局改进（2 项）
- 创建 `dimens.xml` 统一管理间距值（P2-#18）
- 为 10 个缺少类注释的 Java 文件补全类注释（P2-#20）

## Impact

- **Affected specs**: 代码质量、界面设计、功能完整性
- **Affected code**:
  - Java: `MainActivity.java`, `RegisterActivity.java`, `WelcomeActivity.java`, `TodoDetailActivity.java`, `TodoEditActivity.java`, `TodoListActivity.java`, `TodoAdapter.java`, `User.java`, `UserDao.java`, `UserDBHelper.java` + 新建 `dao/TodoDao.java`
  - XML 布局: `activity_main.xml`, `activity_register.xml`, `activity_todo_list.xml`, `activity_todo_detail.xml`, `activity_todo_edit.xml`, `item_todo.xml`
  - 资源: `colors.xml`, `styles.xml`, `btn_bg_selector.xml`, `dimens.xml`（新建）
  - Manifest: `AndroidManifest.xml`
- **课程设计评分提升预估**: 77/100 → 90+/100（功能 50 + UI 18 + 代码 18 + 文档待定）

## 注意事项

- M3 组件（MaterialButton、TextInputLayout 等）的样式机制与普通 Button 不同，引用 `MyBtnStyle` / `MyEditStyle` 时应适配 MaterialButton 的 `app:backgroundTint` / `style="@style/..."` 机制，而非简单加 `android:background`
- `dimens.xml` 仅提取常见的 8/12/16/24/32dp 间距到统一常量，不改变布局结构，不引入投机性抽象
- 类注释使用 `/** 单行描述 */` 格式，不做多余的方法级注释
