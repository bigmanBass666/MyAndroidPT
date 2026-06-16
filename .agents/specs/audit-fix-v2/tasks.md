# Tasks

## 任务分组

| 任务 | 改动文件 | 类型 | 耗时 |
|------|---------|------|------|
| T1 | MainActivity.java, WelcomeActivity.java | 合并 commit | 10 min |
| T2 | User.java | 独立 commit | 2 min |
| T3 | styles.xml | 独立 commit | 5 min |
| T4 | MainActivity.java | 独立 commit | 2 min |
| T5 | activity_register.xml | 独立 commit | 10 min |
| T6 | Todo.java | 独立 commit | 2 min |

## 执行顺序

T1 → T2 → T3 → T4 → T5 → T6（线性顺序，无并行依赖）

---

- [x] **T1**: 合并 commit — SP 文件名 `spfRecord` → `user_info` + 登出逻辑改为 `clear().apply()`
  - `MainActivity.java` 第 97 行：`"spfRecord"` → `"user_info"`
  - `MainActivity.java` 第 112 行：`"spfRecord"` → `"user_info"`
  - `WelcomeActivity.java` 登出处理：改为 `getSharedPreferences("user_info", MODE_PRIVATE).edit().clear().apply()`
  - 验证：`gradlew.bat assembleDebug` 通过

- [x] **T2**: 独立 commit — User.toString() 移除密码字段
  - `User.java` 第 32 行：`return "User{id=" + id + ", name='" + name + "'}";`
  - 验证：`gradlew.bat assembleDebug` 通过

- [x] **T3**: 独立 commit — MyBtnStyle 显式添加 btn_bg_selector 和圆角
  - `styles.xml` 第 24-27 行：添加 `<item name="background">@drawable/btn_bg_selector</item>` 和 `<item name="cornerRadius">8dp</item>`
  - 验证：`gradlew.bat assembleDebug` 通过

- [x] **T4**: 独立 commit — MainActivity 补充类注释
  - `MainActivity.java` 第 20 行前添加 `/** 登录页面，提供用户登录功能（账号/密码 + 记住密码 + 自动登录） */`
  - 验证：`gradlew.bat assembleDebug` 通过

- [x] **T5**: 独立 commit — 注册页布局 weight 容器修复
  - `activity_register.xml`：将第 92 行的 `</LinearLayout>` 移至文件末尾（原 111 行后），使 `btn_register` 和 `rb_agree` 成为 weight 容器子元素
  - 验证：`gradlew.bat assembleDebug` 通过

- [x] **T6**: 独立 commit — 删除 Todo 未使用构造函数
  - `Todo.java` 第 13-18 行：删除 `Todo(String title, String content)` 构造函数
  - 验证：`gradlew.bat assembleDebug` 通过
