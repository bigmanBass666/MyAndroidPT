# UI 精进：待办编辑/新增页 (`activity_todo_edit.xml`)

> 截图：`screenshots/05_todo_edit.png`

---

## 当前布局摘要

| 组件 | ID | 说明 |
|------|-----|------|
| Toolbar | `toolbar` | MaterialToolbar，标题"新增待办"或"编辑待办" |
| 标题标签 | `label_title` | TextView，"标题：" |
| 标题输入框 | `et_title` | EditText，`hint="标题（必填）"` |
| 内容标签 | `label_content` | TextView，"内容：" |
| 内容输入框 | `et_content` | EditText，`hint="请输入待办内容"`，多行 |
| 取消按钮 | `btn_cancel` | TextButton |
| 保存按钮 | `btn_save` | MaterialButton，绿色填充 |

---

## UI 问题与改进建议

### P0 — 标签和输入框分离、布局过时

**问题**：当前使用 `TextView` 标签（"标题：""内容："）+ 下方 EditText 的分离布局，看起来像传统 HTML 表单。M3 的 TextInputLayout 自带 hint 和 floating label，不需要额外标签。

**建议**：
- 移除 `label_title` 和 `label_content` 两个 TextView
- 将 `et_title` 改为 M3 TextInputLayout（OutlinedBox）风格，自带浮动标签
- `et_content` 同理，使用 TextInputLayout 包裹

### P0 — 内容输入框缺少高度

**问题**：`et_content` 为单行/短区域，用户输入较长内容时只能水平滚动或看到截断文本。

**建议**：
- 设置 `minLines="5"`，`gravity="top|start"`，让内容框有足够垂直空间
- 使用 `ScrollView` 包裹整个表单，确保键盘弹出时可滚动

### P0 — 缺少必填校验的视觉提示

**问题**："标题（必填）"写在 hint 中，不够醒目。用户提交空标题时弹 Toast 而不是内联校验。

**建议**：
- 使用 M3 TextInputLayout 的 `app:errorEnabled="true"` 做内联错误提示
- 空标题时标题框变红 + 显示 "标题不能为空" 错误文字

### P1 — 取消/保存按钮位置

**问题**：底部两个按钮分散在左右两侧，但登录页的全宽按钮风格更一致。编辑页的按钮布局与之不同。

**建议**：统一为"取消（左）+ 保存（右）"风格，或改为全宽保存按钮（类比登录页），`btn_cancel` 放在 Toolbar 位置（作为导航返回）。

### P1 — 工具栏标题不随模式切换

**问题**：Toolbar 标题在新增模式应为"新增待办"，编辑模式应为"编辑待办"，需确认当前代码是否正确切换。

**建议**：若未实现，在 `onCreate` 中根据 `EXTRA_TODO_ID == -1` 切换标题。

### P2 — 内容输入框行高

**问题**：多行 EditText 的 `lineSpacingExtra` 可能为默认值，行间距偏紧影响长文阅读。

**建议**：添加 `android:lineSpacingExtra="4dp"` 增加行间距。

---

## 精进方向总结

| 优先级 | 项 | 难度 | 影响面 |
|--------|----|------|--------|
| P0 | 改用 TextInputLayout + 移除冗余标签 | 中（布局重写） | 核心视觉 |
| P0 | 内容框 minLines + ScrollView | 低 | 输入体验 |
| P0 | 内联错误校验 | 中 | 表单交互 |
| P1 | 按钮布局统一 | 低 | 视觉一致 |
| P1 | 标题模式切换 | 极低 | 上下文提示 |
| P2 | 行间距 | 极低（1 属性） | 长文可读性 |