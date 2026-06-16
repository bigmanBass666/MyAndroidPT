# Checklist

## T1: SP 文件名 + 登出逻辑

- [x] `MainActivity.java` 中无 `"spfRecord"` 字符串，全部替换为 `"user_info"`
- [x] `WelcomeActivity.java` 登出逻辑为 `getSharedPreferences("user_info", MODE_PRIVATE).edit().clear().apply()`
- [x] `MainActivity.saveLoginState()` 和 `initData()` 使用同一文件名
- [x] `gradlew.bat assembleDebug` 构建通过

## T2: User.toString() 脱敏

- [x] `User.toString()` 输出中不包含 `psw` 字段
- [x] `gradlew.bat assembleDebug` 构建通过

## T3: MyBtnStyle 显式定义

- [x] `styles.xml` 中 `MyBtnStyle` 包含 `background` 和 `cornerRadius` 两个 item
- [x] `gradlew.bat assembleDebug` 构建通过

## T4: MainActivity 类注释

- [x] `MainActivity.java` 第 20 行前有单行类注释
- [x] `gradlew.bat assembleDebug` 构建通过

## T5: 注册页布局修复

- [x] `activity_register.xml` 中 `btn_register` 和 `rb_agree` 位于 `layout_weight=1` 的 LinearLayout 内部
- [x] `btn_register` 不再是根 LinearLayout 的直接子元素
- [x] `gradlew.bat assembleDebug` 构建通过

## T6: Todo 构造函数清理

- [x] `Todo.java` 中无 `Todo(String, String)` 构造函数
- [x] 仍保留 `Todo()` 无参构造函数
- [x] `gradlew.bat assembleDebug` 构建通过

## 最终验收

- [x] 6 个 commit 全部提交
- [x] 每个 commit 后 `gradlew.bat assembleDebug` 通过
- [x] 模拟器验证：登录 / 注册 / 待办 CRUD 流程正常
