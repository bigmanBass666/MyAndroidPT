# UI 精进：待办详情页 (`activity_todo_detail.xml`)

> 截图：`screenshots/06_todo_detail.png`

---

## 当前布局摘要

| 组件 | ID | 说明 |
|------|-----|------|
| Toolbar | `toolbar` | MaterialToolbar，标题"待办详情" |
| 标题 | `tv_detail_title` | TextView，显示待办标题，大字 |
| 状态标签 | `tv_detail_status` | TextView，"已完成"/"未完成" |
| 完成勾选 | `cb_detail_done` | MaterialCheckBox，"标记为已完成" |
| 内容标题 | — | TextView，"内容：" |
| 内容正文 | `tv_detail_content` | TextView，显示待办内容 |
| 时间戳 | `tv_detail_time` | TextView，显示创建时间 |
| 编辑按钮 | `btn_detail_edit` | MaterialButton，OutlinedButton 风格 |
| 删除按钮 | `btn_detail_delete` | MaterialButton，OutlinedButton 风格 |

---

## UI 问题与改进建议

### P0 — 状态标签颜色应语义化

**问题**：`tv_detail_status` 显示"已完成"/"未完成"但可能只是纯文字，缺少颜色语义。

**建议**：
- "已完成"→ 绿色（`?attr/colorPrimary`）
- "未完成"→ 灰色或橙色（`?attr/colorOnSurfaceVariant` 或自定义色）

### P0 — 内容区域缺少排版美化

**问题**：内容部分使用"内容："标签 + 正文的简单布局，内容文本若较长则无间距不适于阅读。

**建议**：
- 内容正文使用 `lineSpacingExtra="4dp"`
- 用 MaterialCardView 包裹内容区域，底部留足够 padding

### P0 — 编辑/删除按钮无危险色区分

**问题**：编辑和删除使用相同 OutlinedButton 风格，删除作为危险操作无红色警示。

**建议**：
- 删除按钮使用 `app:strokeColor="@color/red"` + `android:textColor="@color/red"`
- 或改用 TextButton 并放置在靠下的位置，降低误触概率

### P1 — 状态切换反馈

**问题**：`cb_detail_done` 勾选后状态切换，但 UI 上无过渡动画。

**建议**：为状态切换添加动画过渡，或至少使用 `animateLayoutChanges="true"`。

### P1 — 标题和状态区域的间距

**问题**：标题在下，状态在上（或反之），视觉上这两个信息应该是一个整体。

**建议**：将标题和状态用同一个 CardView 头部区域包裹，视觉上形成"标题 + 状态标签"的卡片头部。

### P2 — 删除确认对话框风格

**问题**：删除时弹出 `AlertDialog`，默认系统对话框风格，没有 M3 样式。

**建议**：使用 M3 `MaterialAlertDialogBuilder`（`com.google.android.material.dialog.MaterialAlertDialogBuilder`）获取与主题一致的对话框。

### P2 — 时间戳格式

**问题**：时间戳可能显示"2026-06-16 17:45"格式，缺少友好的相对时间。

**建议**：添加显示"3小时前"、"昨天"等相对时间。

### P3 — 内容溢出处理

**问题**：长内容文本可能被截断或换行不雅。

**建议**：
- 设置 `maxLines="20"` + `ellipsize="end"`（限制过长内容撑爆页面）
- 或支持内容展开/收起（"查看更多"）

---

## 精进方向总结

| 优先级 | 项 | 难度 | 影响面 |
|--------|----|------|--------|
| P0 | 状态标签语义颜色 | 极低（color 属性） | 视觉明确度 |
| P0 | 内容区域 CardView 包裹 | 低 | 阅读舒适度 |
| P0 | 删除按钮红色警告 | 极低（xml 颜色属性） | 安全提示 |
| P1 | 状态切换动画 | 低（1 行 xml） | 交互平滑度 |
| P1 | 标题+状态卡片分组 | 低（布局调整） | 信息架构 |
| P2 | MaterialAlertDialog | 极低（改类名） | 主题一致性 |
| P2 | 相对时间 | 中（代码） | 友好性 |