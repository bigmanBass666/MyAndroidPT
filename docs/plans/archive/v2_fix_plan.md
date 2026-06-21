# MyAndroidPT v2 修复计划

> **前提**：已执行审查（`docs/v2_audit_report.md`），评分 74/100  
> **范围**：仅代码问题，文档（课程设计报告）本次不处理  
> **时间**：2026-06-16

---

## 总览

| 优先级 | 任务 | 当前状态 | 改动类型 | 预估时间 |
|--------|------|---------|---------|---------|
| P1 | 修复 SP 文件名 | `"spfRecord"` → `"user_info"` | 2 行改动 | 5 min |
| P1 | 修复登出逻辑 | 只置 `isAutoLogin=false` → `clear().apply()` | 5 行改动 | 5 min |
| P1 | 修复 User.toString() | 暴露密码明文 → 脱敏 | 1 行改动 | 2 min |
| P1 | 保留 MyBtnStyle 定义 | 定义存在但极简，应体现自定义样式 | 3 行改动 | 5 min |
| P2 | 修复 MainActivity 类注释 | 缺类注释 | 3 行添加 | 2 min |
| P2 | 修复注册页布局 | btn_register / rb_agree 在 weight 容器外部 | XML 结构调整 | 10 min |
| P3 | 清理 Todo 多余构造函数 | `Todo(String, String)` 实际已不用 | 4 行删除 | 2 min |

---

## 任务详情

### P1-1 | SP 文件名改为 `user_info`

**问题**（码-2）：`SharedPreferences("spfRecord", …)` 与教材实训 5 要求的 `user_info` 不一致。

**改动位置**：
- `MainActivity.java` 第 97 行：`getSharedPreferences("spfRecord", MODE_PRIVATE)`
- `MainActivity.java` 第 112 行：`getSharedPreferences("spfRecord", MODE_PRIVATE)`
- `WelcomeActivity.java` 登出相关代码中同类调用（需确认具体行数）

**改动**：两处 `"spfRecord"` 均改为 `"user_info"`

**验证**：
```
1. 注册新账号，勾选"记住密码"，关闭 App，重新打开 → 账号密码自动填入
2. 勾选"自动登录"，关闭 App，重新打开 → 直接进入欢迎页
3. 欢迎页"退出" → 回到登录页、SharedPreferences 数据已清空
4. gradlew assembleDebug 通过
```

**提交信息**：`fix: 将 SharedPreferences 文件名从 spfRecord 改为 user_info`

---

### P1-2 | 登出时执行 `clear().apply()`

**问题**（码-5 衍生）：实训 5 要求登出时清空 SP，当前代码仅置 `isAutoLogin=false`。

**改动位置**：`WelcomeActivity.java` — 退出按钮点击处理

**改动**：当前登出代码需确认是哪种形式，改为：
```java
SharedPreferences spf = getSharedPreferences("user_info", MODE_PRIVATE);
spf.edit().clear().apply();
```

**验证**：
```
1. 登录并勾选"记住密码"
2. 点击"退出"
3. 重新打开 App → 登录页账号密码均为空（SP 已清空）
4. gradlew assembleDebug 通过
```

**注意**：与 P1-1 共享同一文件名常量，应在同一 commit 或相邻 commit 中修改，避免中间状态不一致。

**提交信息**：`fix: 登出时改为 clear().apply() 清空 SharedPreferences`

---

### P1-3 | User.toString() 脱敏

**问题**（码-5）：`User.toString()` 包含密码明文，logcat 或 crash 报告可能泄露。

**改动位置**：`User.java` 第 31-33 行 `toString()` 方法

**改动**：
```java
// 改前
return "User{id=" + id + ", name='" + name + "', psw='" + psw + "', email='" + email + "'}";

// 改后
return "User{id=" + id + ", name='" + name + "'}";
```

**验证**：
```
1. gradlew assembleDebug 通过
2. 登录操作 logcat 中 User 日志不再显示密码
```

**提交信息**：`fix: User.toString() 移除密码字段防止敏感信息泄露`

---

### P1-4 | MyBtnStyle 显式定义

**问题**（码-1）：实训 1 要求自定义按钮样式，当前存在但内容过于简化。

**改动位置**：`res/values/styles.xml` 第 24-27 行

**当前状态**：
```xml
<style name="MyBtnStyle">
    <item name="android:textColor">@color/white</item>
    <item name="android:textSize">16sp</item>
</style>
```

**改动**：添加背景和圆角，使其作为独立自定义样式存在：
```xml
<style name="MyBtnStyle">
    <item name="android:textColor">@color/white</item>
    <item name="android:textSize">16sp</item>
    <item name="background">@drawable/btn_bg_selector</item>
    <item name="cornerRadius">8dp</item>
</style>
```

**验证**：
```
1. styles.xml 中 MyBtnStyle 定义明确
2. btn_bg_selector 九宫格 drawable 存在
3. gradlew assembleDebug 通过
4. 注册页按钮视觉正常（背景 + 圆角 + 按下态）
```

**提交信息**：`refactor: MyBtnStyle 显式定义背景和圆角样式`

---

### P2-1 | MainActivity 类注释

**问题**（码-6）：MainActivity 缺少类注释，是 100% 注释覆盖规则的唯一遗漏。

**改动位置**：`MainActivity.java` 第 20 行 `public class MainActivity …` 之前

**改动**：添加类注释：
```java
/**
 * 登录页面，提供用户登录功能（账号/密码 + 记住密码 + 自动登录）
 */
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
```

**验证**：`gradlew assembleDebug` 通过

**提交信息**：`chore: 补充 MainActivity 类注释`

---

### P2-2 | 注册页布局：btn_register / rb_agree 移到 weight 容器内部

**问题**（UI-1）：`btn_register` 和 `rb_agree` 在 `layout_weight=1` 的 LinearLayout 外部（第 92 行闭合、第 94-110 行元素在根容器中），导致小屏设备按钮位置异常。

**改动位置**：`activity_register.xml` 第 21-111 行

**改前结构**：
```
LinearLayout (vertical, root)
├── Toolbar
├── LinearLayout (weight=1)   ← 仅含四个输入框
│   ├── TextInputLayout × 4
├── btn_register               ← 在 weight 容器外部
└── rb_agree                   ← 在 weight 容器外部
```

**改后结构**：
```
LinearLayout (vertical, root)
├── Toolbar
└── LinearLayout (weight=1)   ← 包含输入框 + 按钮 + 勾选
    ├── TextInputLayout × 4
    ├── btn_register
    └── rb_agree
```

**改动**：将第 92 行的 `</LinearLayout>`（闭合 weight 容器）移至文件末尾（原 111 行），让 btn_register 和 rb_agree 成为 weight 容器的子元素。

**验证**：
```
1. gradlew assembleDebug 通过
2. 小屏设备（或小屏模拟器）上注册页按钮贴合输入框下方
3. 功能无明显变化（点击注册 / 勾选协议正常）
```

**提交信息**：`fix: 注册页布局 weight 容器闭合位置修复`

---

### P3-1 | 清理 Todo 多余构造函数

**问题**（码-7）：`Todo(String title, String content)` 在代码中实际未被调用。

**改动位置**：`Todo.java` 第 13-18 行

**改动**：删除该构造函数（4 行）

**验证**：
```
1. 新建待办、编辑待办、查看待办均正常
2. gradlew assembleDebug 通过
3. grep -r "new Todo(" 确认无其他使用点
```

**提交信息**：`refactor: 移除 Todo 未使用的双参构造函数`

---

## 执行顺序建议

```
P1-1 SP 文件名
P1-2 登出逻辑          ← 与 P1-1 紧密关联，同一 commit
P1-3 User.toString()   ← 独立，不影响其他
P1-4 MyBtnStyle        ← 独立
P2-1 类注释            ← 独立最小改动
P2-2 注册页布局        ← 独立 XML 改动
P3-1 清理构造函数      ← 独立，放最后
```

7 个任务，其中 P1-1 和 P1-2 建议合并为同一次改动（SP 文件名统一 + 登出逻辑），
其余 5 个各自独立 commit。

---

## 未处理项

| 问题 | 原因 |
|------|------|
| Todo.id `int` → `long`（码-4） | 当前无 BUG，改动涉及多文件（DAO + DBHelper + 所有引用），风险高、收益低，暂缓 |
| 登录页注册按钮布局优化（UI-2） | 低优先级，不影响评审 |
| 课程设计报告 7 个章节（文档-0） | 用户明确本次不做 |
