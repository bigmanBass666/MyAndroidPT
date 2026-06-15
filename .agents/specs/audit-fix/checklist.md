# 审计问题修复 — 验收清单

## A组：代码清理

- [x] A1: `MainActivity.java` 中无 `etEmail` 字段声明和 `findViewById` 调用，注册回传 email 死分支已删除
- [x] A2: `AndroidManifest.xml` 中无 `<uses-permission android:name="android.permission.INTERNET" />`
- [x] A3: `TodoDetailActivity.java` 无 `import android.database.Cursor`；`TodoEditActivity.java` 无冗余 `import com.ljx.pt.R`；`TodoAdapter.java` import 与实际使用类型一致
- [x] A4: `RegisterActivity.java` 中的 `putExtra("userName")` 等已替换为常量（`EXTRA_USER_NAME`、`EXTRA_PASSWORD`、`EXTRA_EMAIL`），`MainActivity.java` 接收方同步使用同一常量
- [x] A4: 构建通过（`gradlew.bat assembleDebug`）

## B组：注册安全

- [x] B1: `activity_register.xml` 密码框和确认密码框 `inputType` 均为 `textPassword`
- [x] B1: 自动化测试方案 `docs/android_automation_best_practices.md` 仍能适用（`textPassword` 对 `adb shell input text` 可用）

## C组：样式系统

- [x] C1: `btn_bg_selector.xml` 正常态和按压态使用不同颜色，按压可见视觉反馈
- [x] C1: 无引用已不存在的 `@color/colorPrimaryDark`
- [x] C2: `colors.xml` 包含 `<color name="colorAccent">#F4511E</color>`
- [x] C3: `activity_main.xml` 登录按钮引用了 `MyBtnStyle`
- [x] C3: `activity_register.xml` 注册按钮引用了 `MyBtnStyle`
- [x] C3: 输入框引用了 `MyEditStyle`（登录页 2 个 + 注册页 4 个）
- [x] C3: 构建通过（`gradlew.bat assembleDebug`）

## D组：UI 组件

- [x] D1: 所有 6 处旧版内置图标（`@android:drawable/ic_menu_revert` ×4, `@android:drawable/ic_input_add` ×1, `@android:drawable/ic_menu_delete` ×1）已替换为 Material 矢量图标
- [x] D2: 4 处 `app:popupTheme` 的 `ThemeOverlay.AppCompat.Light` 已替换为 `ThemeOverlay.Material3.Light`
- [x] D3: 待办列表空时显示"暂无待办"提示，有数据时提示隐藏
- [x] D1+D2: 构建通过（`gradlew.bat assembleDebug`）

## E组：待办模块

- [x] E1: `TodoDetailActivity.bindCheckListener()` 中的 UI 操作（`tvStatus.setText()`, `tvStatus.setTextColor()`）封装在 `runOnUiThread()` 中
- [x] E2: `bindCheckListener()` 中状态切换后调用了 `currentTodo.setDone(isChecked)`
- [x] E3: 在详情页切换状态 → 按返回 → 列表中该项状态已更新（`TodoListActivity.onResume()` 自动重载）
- [x] E3: 构建通过（`gradlew.bat assembleDebug`）

## F组：登录页

- [x] F1: `initData()` 中 `isRemember = true` 时同时恢复 `cbAutoLogin.setChecked(isAutoLogin)`
- [x] F2: `WelcomeActivity.java` 退出登录处有注释说明为何保留密码而非 `clear()`
- [x] F1+F2: 构建通过（`gradlew.bat assembleDebug`）

## G组：架构

- [x] G1: `dao/TodoDao.java` 文件存在，包含全部 CRUD 方法委托给 `TodoDBHelper`
- [x] G1: `TodoListActivity`, `TodoEditActivity`, `TodoDetailActivity` 使用 `TodoDao` 替代直接使用 `TodoDBHelper`
- [x] G1: 构建通过（`gradlew.bat assembleDebug`）

## H组：全局

- [x] H1: `res/values/dimens.xml` 存在，定义了 `dp_4`, `dp_8`, `dp_12`, `dp_16`, `dp_24`, `dp_32` 等常量
- [x] H2: 10 个 Java 文件均有类注释
- [x] H2: 构建通过（`gradlew.bat assembleDebug`）

---

## 最终验收

- [x] 全部 19 项 P1/P2 问题已修复或处理
- [x] `gradlew.bat assembleDebug` 构建成功
- [x] 代码验证通过：无旧版图标、无 AppCompat overlay、无死代码、样式引用到位、常量化完成、DAO 层建立、类注释 100% 覆盖

**预估课程设计评分提升：77/100 → 90+/100（功能 50 + UI 18-19 + 代码 18-19 + 文档待定）**