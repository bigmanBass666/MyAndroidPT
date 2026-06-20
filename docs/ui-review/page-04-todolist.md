# UI 精进：待办列表页 (`activity_todo_list.xml`)

> 截图：`screenshots/04_todolist_empty.png`（空态）、`screenshots/04_todolist.png`（有数据）

---

## 当前布局摘要

| 组件 | ID | 说明 |
|------|-----|------|
| Toolbar | `toolbar` | MaterialToolbar，标题"待办事项" |
| RecyclerView | `rv_todo` | 列表容器，`layout_marginTop="?attr/actionBarSize"` |
| 空状态提示 | `tv_empty_hint` | TextView，"暂无待办" |
| 新增 FAB | `fab_add` | FloatingActionButton，绿色，右下角 |
| Item 布局 | `item_todo.xml` | CardView + CheckBox + 标题 + 时间 + 删除按钮 |

---

## UI 问题与改进建议

### P0 — 空状态设计太简陋

**问题**：空态仅显示文字"暂无待办"，无图标、无操作引导。用户看到这个状态不知道下一步要做什么（虽然 FAB 存在，但视觉上缺少引导）。

**建议**：
- 添加空状态插画（使用 VectorDrawable，大小 120x120dp）
- 引导文字改为"还没有待办事项\n点击下方 + 按钮创建一个"

### P0 — Item 布局缺少视觉层次

**问题**：列表项用 CardView 包裹但：
- CardView 圆角和阴影不明显（可能被覆盖）
- 复选框、标题、删除按钮散落在同一行，无清晰的视觉分组
- 删除按钮使用 `content-desc` 无文字提示，用户可能不知道那是删除

**建议**：
- 为 Completed 状态添加 strikethrough（删除线）样式
- 给删除按钮添加轻触反馈
- 使用 `app:cardCornerRadius="12dp"` 增大圆角到 12dp
- 增加 `cardElevation="2dp"` 层次感

### P1 — 列表项高度和间距

**问题**：当前列表项内容紧凑，标题和时间垂直排列缺少呼吸空间。

**建议**：
- 增大 item 的 padding（vertical 12dp → 16dp）
- 标题和时间的间距加大（4dp → 8dp）
- 删除按钮和右侧边缘增加 `marginEnd`

### P1 — 状态切换缺失过渡动画

**问题**：CheckBox 勾选/取消时状态切换无动画效果。

**建议**：为 CheckBox 添加 `android:animateLayoutChanges="true"` 或编程实现 RecyclerView item 动画。

### P2 — Toolbar 缺少操作菜单

**问题**：Toolbar 只有标题，没有搜索/筛选/排序功能。

**建议**：添加 Toolbar 菜单项：
- "按时间排序" / "按状态排序"
- 可选：搜索功能

### P2 — 缺少 `app:layout_behavior`

**问题**：v3 报告记录 RecyclerView 未使用 `app:layout_behavior="@string/appbar_scrolling_view_behavior"`，虽不影响功能但不符合最佳实践。

**建议**：补充 layout_behavior 属性。

### P3 — 底部空白区域

**问题**：FAB 下方到屏幕底部有过多空白，RecyclerView 内容无法利用该区域。

**建议**：给 RecyclerView 添加 `android:clipToPadding="false"` 和 `android:paddingBottom="80dp"`，使内容可滚动到 FAB 下方。

---

## 精进方向总结

| 优先级 | 项 | 难度 | 影响面 |
|--------|----|------|--------|
| P0 | 空态插画 + 引导 | 低（+VectorDrawable） | 首次用户体验 |
| P0 | 列表项圆角/阴影增强 | 低（改 xml 属性） | 视觉质感 |
| P1 | 列表项 padding 优化 | 极低（改 dp 值） | 呼吸感 |
| P1 | CheckBox 过渡动画 | 低（1 行 xml） | 交互平滑度 |
| P2 | Toolbar 操作菜单 | 中 | 功能扩展 |
| P2 | layout_behavior | 极低（1 行 xml） | 最佳实践 |