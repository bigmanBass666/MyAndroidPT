# V2 审计问题修复 Spec

## Why

`docs/v2_audit_report.md` 审查发现 7 项代码问题（P1×4 + P2×2 + P3×1），与教材实训 1-6 要求及 AGENTS.md 代码规范存在偏差。修复后可提升代码质量评分（14→19），整体评分从 74/100 提升至约 83/100。

## What Changes

### P1 级修复（4 项）
- SharedPreferences 文件名从 `"spfRecord"` 改为教材要求的 `"user_info"`（P1-1）
- 登出逻辑从仅置 `isAutoLogin=false` 改为 `clear().apply()`（P1-2）
- `User.toString()` 移除密码字段防止敏感信息泄露（P1-3）
- `MyBtnStyle` 显式添加 `btn_bg_selector` 背景和圆角（P1-4）

### P2 级修复（2 项）
- `MainActivity` 补充类注释（P2-1）
- 注册页 `btn_register` / `rb_agree` 移入 weight 容器内部，修复小屏布局错位（P2-2）

### P3 级修复（1 项）
- 删除 `Todo.java` 未使用的 `Todo(String, String)` 构造函数（P3-1）

## Impact

- **Affected specs**: 代码质量（实训 1 要求样式对齐、实训 5 要求 SP 命名）
- **Affected code**:
  - Java: `MainActivity.java`, `WelcomeActivity.java`, `User.java`, `Todo.java`
  - XML: `activity_register.xml`, `res/values/styles.xml`
- **分组策略**: P1-1 与 P1-2 合并为同一 commit（共享 SP 文件名常量），其余 5 项各自独立 commit，共 6 个 commit

```{Dependencies}
T1: P1-1 + P1-2 (SP 文件标准化 + 登出逻辑)
  - 修改: MainActivity.java (×2 处), WelcomeActivity.java
  - 同一 commit，避免中间状态文件名不一致
  - 独立 commit #1

T2: P1-3 (User.toString 脱敏)
  - 修改: User.java (1 行)
  - 独立 commit #2

T3: P1-4 (MyBtnStyle 显式定义)
  - 修改: styles.xml (3 行)
  - 独立 commit #3

T4: P2-1 (MainActivity 类注释)
  - 修改: MainActivity.java (3 行添加)
  - 独立 commit #4

T5: P2-2 (注册页布局修复)
  - 修改: activity_register.xml (闭合标签位置调整)
  - 独立 commit #5

T6: P3-1 (Todo 构造函数清理)
  - 修改: Todo.java (删除 4 行)
  - 独立 commit #6
```
