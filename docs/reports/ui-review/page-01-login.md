# UI 精进：登录页 (`activity_main.xml`)

> 截图：`screenshots/01_login.png`

---

## 当前布局摘要

| 组件 | ID | 说明 |
|------|-----|------|
| Toolbar | `toolbar` | M3 MaterialToolbar，`colorPrimary` 绿色背景 |
| 账号输入框 | `et_account` | TextInputLayout + OutlinedBox，`@style/MyEditStyle` |
| 密码输入框 | `et_password` | 同上，`inputType="textPassword"` |
| 记住密码 | `cb_pass_remember` | MaterialCheckBox |
| 自动登录 | `cb_auto_login` | MaterialCheckBox，`layout_marginStart="32dp"` |
| 登录按钮 | `btn_login` | MaterialButton，`@style/MyBtnStyle`，全宽 |
| 注册链接 | `tv_register` | TextButton，`layout_gravity="center_horizontal"` |

---

## UI 问题与改进建议

### P0 — 缺少明确的视觉层次

**问题**：登录页所有组件垂直堆叠，无分组视觉分割。输入框、CheckBox、按钮之间仅靠 `marginTop` 分隔，缺少卡片或分割线来建立视觉层次。

**建议**：将登录表单（输入框 + CheckBox）放入 MaterialCardView，与操作按钮区域形成两个视觉区块。CardView 自带阴影和圆角，提升质感。

### P0 — 密码切换可见性

**问题**：密码框使用 `inputType="textPassword"`，没有密码可见性切换图标（`endIconMode="password_toggle"`）。

**建议**：在 TextInputLayout 添加 `app:endIconMode="password_toggle"`——这是 M3 TextInputLayout 原生支持，仅需一行 xml。

### P1 — 登录按钮缺少 Loading 状态

**问题**：`btn_login` 点击后直接进入异步登录，按钮无 loading 态。若网络/数据库慢用户可能重复点击。

**建议**：添加 `ProgressBar` 到按钮或使用 MaterialButton 的 `app:icon` 在点击时显示加载图标。

### P1 — 注册链接与登录按钮间距偏紧

**问题**：`tv_register` 距 `btn_login` 仅 `12dp`，视觉上不够舒展。对比整个页面 16dp padding，12dp 偏小。

**建议**：增大至 20dp，使两个操作按钮有清晰的分组感。

### P2 — 页面左右 padding 在宽屏上过大

**问题**：`padding="16dp"` 写死，在小屏上正常，但在平板或横屏上输入框会显得过宽。

**建议**：约束最大宽度（例如 480dp），使用 `layout_width` 配合 `layout_gravity="center"`，保持可读性。

### P2 — 缺少应用 Logo / 品牌标识

**问题**：Toolbar 下方无 Logo 或 App 名称，用户进入登录页第一眼缺乏品牌认知。

**建议**：在 Toolbar 与输入框之间添加 App 名称文字或图标（如 `ImageView` + `TextView` 组合），建立品牌识别。

### P3 — CheckBox 文字与勾选框对齐偏差

**问题**：`cb_pass_remember` 和 `cb_auto_login` 使用 `wrap_content` + `marginStart`，在部分系统字体缩放下可能错行。

**建议**：使用 `android:gravity="center_vertical"` 确保文字与勾选框基线对齐。

---

## 精进方向总结

| 优先级 | 项 | 难度 | 影响面 |
|--------|----|------|--------|
| P0 | 添加密码可见性切换 | 极低（1 行 xml） | 用户体验 |
| P0 | 表单卡片容器分组 | 低（+CardView） | 视觉质感 |
| P1 | 按钮 Loading 态 | 中（代码改动） | 防重复提交 |
| P1 | 注册链接间距调整 | 极低（改数字） | 呼吸感 |
| P2 | 宽屏约束 + Logo | 低 | 品牌识别 |
