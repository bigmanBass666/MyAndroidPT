# Verdant Design System — Spec

## Why

当前项目有完整的 Material3 主题骨架（色板、形状、暗色模式），但视觉表现平庸——色板是 Material 默认配方，排版和间距没有体系化，按钮质感因教学要求的 `MyBtnStyle` 而停留在扁平 drawable 时代。需要一次系统化的设计语言升级，在不违反教材要求的前提下让 UI 有作者性和专业感。

## What Changes

- **BREAKING**: colors.xml 色值全面更换为"Verdant"深翠主题（保留颜色名但换 hex）
- **NEW**: type.xml — M3 排版缩放体系映射
- **NEW**: dimens.xml — 具名间距资源
- **MODIFIED**: MyBtnStyle 改为继承 `Widget.Material3.Button`，保留 `btn_bg_selector` 为 fallback
- **NEW**: 注册页密码强度指示器（三段式：弱/中/强）
- **MODIFIED**: 注册页添加 ScrollView + MaterialCardView 容器（与登录页保持一致）
- **MODIFIED**: 欢迎页扩展为微型仪表盘（待办速览卡片 + 快速创建入口）
- **NEW**: 欢迎页标题改用 `?attr/colorOnSurface`（消除绿色链接感）
- **MODIFIED**: 详情页删除按钮文字色改为 `?attr/colorError`
- **MODIFIED**: 详情页内层卡片改为简单容器（减少嵌套）
- **NEW**: 编辑页标题输入框开启 `counterEnabled` + `counterMaxLength`
- **NEW**: 登录/保存按钮点击后 disabled + 文字变化（简易加载状态）
- **MODIFIED**: 待办列表页 item_todo ShapeSmall 样式覆盖修复
- **MODIFIED**: FAB 图标 tint 改为 `?attr/colorOnPrimary`

## Impact

- Affected specs: UI polish, frontend design review
- Affected code:
  - `app/src/main/res/values/colors.xml` — 色值更新
  - `app/src/main/res/values-night/themes.xml` — 暗色模式色值同步
  - `app/src/main/res/values/themes.xml` — typeAppearance 引用
  - `app/src/main/res/values/type.xml` — **NEW** 排版定义
  - `app/src/main/res/values/dimens.xml` — **NEW** 间距定义
  - `app/src/main/res/values/styles.xml` — MyBtnStyle 增强
  - `app/src/main/res/drawable/btn_bg_selector.xml` — 适配 M3 状态
  - `app/src/main/res/layout/activity_main.xml` — 颜色引用调整
  - `app/src/main/res/layout/activity_register.xml` — ScrollView + 卡片 + 密码强度
  - `app/src/main/res/layout/activity_welcome.xml` — 仪表盘扩展
  - `app/src/main/res/layout/activity_todo_list.xml` — padding 清理 + FAB tint
  - `app/src/main/res/layout/item_todo.xml` — ShapeSmall 修复
  - `app/src/main/res/layout/activity_todo_detail.xml` — 删除按钮危险色 + 卡片简化
  - `app/src/main/res/layout/activity_todo_edit.xml` — counterEnabled
  - `app/src/main/java/com/ljx/pt/RegisterActivity.java` — 密码强度检测逻辑
  - `app/src/main/java/com/ljx/pt/WelcomeActivity.java` — 仪表盘数据加载
  - `app/src/main/java/com/ljx/pt/MainActivity.java` — 登录按钮 loading 状态
  - `app/src/main/java/com/ljx/pt/TodoEditActivity.java` — 保存按钮 loading 状态
  - `app/src/main/res/drawable/ic_add.xml` — tint 改为 ?attr/colorOnPrimary

---

## ADDED Requirements

### Requirement: Color Palette — Verdant

The system SHALL replace the current Material default green palette with a custom "Verdant" deep-green palette.

**Color mapping**:

| Current Name | Current Hex | New Hex | Role |
|-------------|------------|---------|------|
| `green_200` | #C5E1A5 | #A5D6A7 | Primary container light |
| `green_500` | #8BC34A | #2E7D32 | Primary (main) |
| `green_700` | #689F38 | #1B5E20 | Primary dark / pressed |
| `green_800` | #558B2F | #145214 | Unused, kept for compatibility |
| `green_900` | #33691E | #0D3B0D | On primary container |
| `teal_50` | #E0F2F1 | #ECEFF1 | Secondary container light |
| `teal_200` | #80CBC4 | #B0BEC5 | Secondary dark |
| `teal_700` | #00796B | #546E7A | Secondary (main) |
| `teal_900` | #004D40 | #29434E | On secondary container |
| `amber_100` | #FFE082 | #FFE082 | Tertiary container (unchanged) |
| `amber_500` | #FFC107 | #F9A825 | Tertiary (main) |
| `amber_700` | #FFA000 | #F57F17 | Tertiary container dark |
| `amber_900` | #FF6F00 | #E65100 | On tertiary container |
| `red_100` | #FFCDD2 | #FFCDD2 | Error container (unchanged) |
| `red_600` | #E53935 | #C62828 | Error (main) |
| `red_900` | #B71C1C | #B71C1C | On error (unchanged) |
| `surface_light` | #FFFBFE | #FFFDF5 | Surface (warm white) |
| `on_surface_light` | #1C1B1F | #1C1B1F | On surface (unchanged) |
| `surface_variant` | #F0EDE8 | #F5F0EB | Surface variant (warm gray) |
| `on_surface_variant` | #49454F | #49454F | On surface variant (unchanged) |
| `outline_light` | #79747E | #A79B8E | Outline (warm) |
| `status_done` | #FF9800 | #F9A825 | Done status (align with tertiary) |
| `status_undone` | #4CAF50 | #2E7D32 | Undone status (align with primary) |
| `danger` | #E53935 | #C62828 | Danger (align with error) |
| `red_error` | #D32F2F | #D32F2F | Keep for now, mark as deprecated |
| `btn_disabled` | #BDBDBD | #BDBDBD | Unchanged |
| `colorAccent` | #F4511E | #F4511E | Keep for compatibility |

#### Scenario: Dark mode mapping

- **GIVEN** dark mode is active
- **THEN** `colorPrimary` SHALL use `@color/green_200` (#A5D6A7)
- **THEN** `colorSecondary` SHALL use `@color/teal_200` (#B0BEC5)
- **THEN** `colorTertiary` SHALL use `@color/amber_100` (#FFE082)
- **THEN** `colorError` SHALL use `@color/red_100` (#FFCDD2)
- **THEN** `colorSurface` SHALL remain #FF1C1B1F
- **THEN** Remaining dark-mode color pairs SHALL invert from their light counterparts following standard M3 pattern

### Requirement: Typography System

The system SHALL provide a `type.xml` resource file defining five M3 text appearance styles.

- **WHEN** a layout references `textAppearanceHeadlineSmall`
- **THEN** it SHALL resolve to 22sp Bold
- **WHEN** a layout references `textAppearanceTitleLarge`
- **THEN** it SHALL resolve to 22sp Bold
- **WHEN** a layout references `textAppearanceTitleMedium`
- **THEN** it SHALL resolve to 18sp Medium
- **WHEN** a layout references `textAppearanceBodyLarge`
- **THEN** it SHALL resolve to 16sp Regular
- **WHEN** a layout references `textAppearanceBodyMedium`
- **THEN** it SHALL resolve to 14sp Regular
- **WHEN** a layout references `textAppearanceLabelLarge`
- **THEN** it SHALL resolve to 14sp Medium
- **WHEN** a layout references `textAppearanceLabelSmall`
- **THEN** it SHALL resolve to 12sp Regular

### Requirement: Spacing System

The system SHALL provide a `dimens.xml` resource file defining named spacing values currently hardcoded across layouts.

- `page_padding` = 16dp
- `form_field_spacing` = 16dp
- `card_content_padding` = 16dp
- `button_margin_top` = 24dp
- `list_item_padding_vertical` = 12dp
- `list_card_margin` = 4dp
- `toolbar_elevation` = 0dp

### Requirement: Password Strength Indicator

The register page SHALL provide visual feedback for password strength.

#### Scenario: Weak password
- **WHEN** user types a password < 6 characters
- **THEN** the helper text SHALL show "弱 — 至少 6 个字符" in red

#### Scenario: Medium password
- **WHEN** user types a password >= 6 characters with only one case type
- **THEN** the helper text SHALL show "中 — 建议包含大小写字母和数字" in amber

#### Scenario: Strong password
- **WHEN** user types a password >= 8 characters with mixed case + digits
- **THEN** the helper text SHALL show "强 — 密码强度良好" in green

### Requirement: Welcome Dashboard

The welcome page SHALL display a todo summary in addition to the existing welcome message and logout button.

#### Scenario: Has data
- **WHEN** user has todo items
- **THEN** display two stat cards: "已完成 N 项" and "待完成 M 项"
- **THEN** display a "快速新建待办" quick-action button

#### Scenario: No data
- **WHEN** user has zero todo items
- **THEN** display "还没有待办事项" message instead of counters
- **THEN** display "创建一个待办" quick-action button

### Requirement: Button Loading State

Buttons that trigger asynchronous database operations SHALL show a loading state to prevent double-submission.

- **WHEN** user taps login button
- **THEN** button SHALL disable and show "登录中..."
- **WHEN** login completes (success or failure)
- **THEN** button SHALL re-enable and restore "登录"

- **WHEN** user taps save button on edit page
- **THEN** button SHALL disable and show "保存中..."
- **WHEN** save completes
- **THEN** button SHALL re-enable and restore "保存"

---

## MODIFIED Requirements

### Requirement: MyBtnStyle Enhancement

`MyBtnStyle` SHALL retain its name (for teaching material compliance) but SHALL change its parent to `Widget.Material3.Button` to gain ripple, elevation, and state layer animations.

- The `background` attribute SHALL be removed from MyBtnStyle (M3 button handles backgrounds)
- The `cornerRadius` SHALL be removed (M3 button uses shape appearance from theme)
- `btn_bg_selector` SHALL NOT be deleted but SHALL be unused by MyBtnStyle (kept for document compliance)

### Requirement: Register Page Container

The register page SHALL wrap its input fields in a `MaterialCardView` consistent with the login page, and wrap the entire content in `ScrollView`.

- Card corner radius: 12dp
- Card elevation: 2dp
- Card content padding: 16dp

### Requirement: Detail Page Delete Button

The delete button on the todo detail page SHALL use `?attr/colorError` for its text color to visually distinguish destructive action from the edit action.

### Requirement: Detail Page Card Nesting

The inner MaterialCardView on the detail page SHALL be replaced with a themed `LinearLayout` with a subtle background color to reduce visual nesting.

### Requirement: Edit Page Counter

The title field of the todo edit page SHALL enable `counterEnabled` and set `counterMaxLength="50"`.

### Requirement: Item Todo ShapeSmall

`item_todo.xml` SHALL remove the redundant `app:cardCornerRadius` attribute that overrides the `@style/ShapeSmall` style, OR remove the `@style/ShapeSmall` style and keep the inline attribute (choose one, not both).

---

## REMOVED Requirements

N/A — No features are removed. Only colors and implementations change.
