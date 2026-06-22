# Layout Redesign — 布局结构重建设计 Spec

## Why
上一轮 layout-polish 仅完成了代码级改进（dimen token、shape token、Loading 态），页面布局结构本身未改变——7 个页面仍是「Toolbar → 垂直堆叠」的同一公式。本次从页面结构层进行重组，目标是去掉学生作业感，达到工业级水准。

## What Changes
- **Welcome 页重组**：三按钮 → 横向操作卡片 + 最近待办预览 + 退出收进菜单
- **登录页重构**：去 Toolbar + 品牌标识区 + leading icon + Checkbox 独立
- **注册页统一**：leading icon + Toolbar 文案优化
- **列表页交互升级**：删除改长按 + 状态指示 + hairline 替代 Card
- **详情页结构美化**：状态 Chip + 编辑入 Toolbar + 删除沉底 + Divider 分区
- **编辑页精简**：保存入 Toolbar + 取消用返回替代 + leading icon

## Impact
- Affected layouts: `activity_main.xml`, `activity_register.xml`, `activity_welcome.xml`, `activity_todo_list.xml`, `item_todo.xml`, `activity_todo_detail.xml`, `activity_todo_edit.xml`
- Affected code: `WelcomeActivity.java`, `TodoListActivity.java`, `TodoDetailActivity.java`, `TodoEditActivity.java`
- Affected resources: `dimens.xml` (新增 gap_large 等), `strings.xml` (新增标签文案), drawable (新增 ic_person/ic_lock)
- 无 Breaking Change（所有功能保持向后兼容）

## ADDED Requirements
### Requirement: 欢迎页重组
The system SHALL provide a restructured Welcome page with horizontal operation cards and recent todo preview.

#### Scenario: Welcome 页显示
- **WHEN** 用户登录成功进入欢迎页
- **THEN** 看到 Toolbar（surface 背景，右侧菜单含退出登录）、横向操作卡片（待办数/完成数/新建）、最近待办预览列表、右下 FAB

### Requirement: 登录页品牌化
The system SHALL display a brand identity area on the login page instead of a Toolbar.

#### Scenario: 登录页打开
- **WHEN** 用户打开登录页
- **THEN** 看到顶部品牌标识（icon + 应用名 + tagline）、带 leading icon 的输入框、独立的 Checkbox 行

### Requirement: 列表页删除改长按
The system SHALL support todo deletion via long-press on list items.

#### Scenario: 长按删除
- **WHEN** 用户长按列表项
- **THEN** 弹出 AlertDialog 确认删除 → 用户确认 → 删除待办 → 刷新列表

### Requirement: 详情页状态 Chip
The system SHALL display todo status as a Material Chip component instead of plain text.

#### Scenario: 状态显示
- **WHEN** 待办详情页加载
- **THEN** 状态区域显示为 Chip（已完成=green filled，未完成=grey outlined）

## MODIFIED Requirements
### Requirement: 编辑页操作路径
**修改前：** Save button at bottom + Cancel button next to it
**修改后：** Save button in Toolbar (right side), Cancel via Toolbar back button

### Requirement: 列表项结构
**修改前：** Card 包裹 + 删除图标始终可见
**修改后：** Hairline 分隔 + 删除通过长按触发 + 右侧状态指示

## REMOVED Requirements
无功能删除。所有 F1-F5 核心功能保持完整。