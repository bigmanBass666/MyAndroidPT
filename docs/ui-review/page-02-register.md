# UI 精进：注册页 (`activity_register.xml`)

> 截图：`screenshots/02_register.png`

---

## 当前布局摘要

| 组件 | ID | 说明 |
|------|-----|------|
| Toolbar | `toolbar` | MaterialToolbar，有返回箭头 |
| 账号 | `et_account` | TextInputLayout + OutlinedBox |
| 密码 | `et_password` | TextInputLayout + OutlinedBox |
| 确认密码 | `et_password_confirm` | TextInputLayout + OutlinedBox |
| 邮箱 | `et_email` | TextInputLayout + OutlinedBox |
| 同意协议 | `rb_agree` | MaterialCheckBox，`text="@string/agree_protocol"` |
| 注册按钮 | `btn_register` | MaterialButton，`@style/MyBtnStyle` |
| weight 容器 | — | 包含 `rb_agree` + `btn_register`（v3 已修复） |

---

## UI 问题与改进建议

### P0 — 缺少密码可见性切换

**问题**：密码框和确认密码框均无 `password_toggle`，用户无法确认输入内容，容易输错。

**建议**：为两个密码 TextInputLayout 添加 `app:endIconMode="password_toggle"`。

### P0 — 缺少密码强度指示器

**问题**：密码强度校验失败时仅在提交后 Toast 提示，无实时反馈。

**建议**：在密码框下方添加实时强度的文本提示（弱/中/强 + 颜色变化），帮助用户在提交前自我纠正。

### P1 — 表单分组不清晰

**问题**：4 个输入框平铺直叙，无区分"账户信息"和"安全信息"的视觉分组。

**建议**：将账号+邮箱、密码+确认密码分为两组，用轻量 `TextView` 标签或分割线区分。

### P1 — 用户协议交互流程

**问题**：用户协议只是一个 CheckBox，无查看协议详情的链接。

**建议**：将 `rb_agree` 改为 ClickableSpan 或添加链接按钮，点击可展开/查看协议全文（WebView 或 Dialog）。

### P2 — 注册按钮与协议 CheckBox 对齐

**问题**：`rb_agree` 在 weight 容器内，与 `btn_register` 使用 `layout_marginTop` 分隔。视觉上协议文字较长时可能换行导致不对齐。

**建议**：使用 `orientation="vertical"` + `gravity="center"` 确保两者始终居中对齐。

### P2 — Toolbar 返回键缺少绑定

**问题**：layout 中有 `navigationIcon`，但 `RegisterActivity.java` 未调用 `setNavigationOnClickListener()`（v3 报告已记录）。

**建议**：在 `onCreate` 添加 `toolbar.setNavigationOnClickListener(v -> finish())`。

### P3 — 邮箱输入键盘类型

**问题**：邮箱输入框 `inputType` 可能为 `text`，未设为 `textEmailAddress`，导致键盘不显示 `@` 和 `.com` 快捷键。

**建议**：添加 `android:inputType="textEmailAddress"`。

---

## 精进方向总结

| 优先级 | 项 | 难度 | 影响面 |
|--------|----|------|--------|
| P0 | 密码可见性切换 | 极低（2 行 xml） | 用户体验 |
| P0 | 密码强度指示器 | 中（需代码） | 引导用户设强密码 |
| P1 | 表单分组 | 低（+分割线/标签） | 视觉清晰度 |
| P1 | 协议查看链接 | 低 | 完整性 |
| P3 | 邮箱键盘类型 | 极低（1 属性） | 输入便利性 |
