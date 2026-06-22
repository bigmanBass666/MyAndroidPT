# MyAndroidPT 布局审查报告 — Taste-Skill 视角

> 工具：Leonxlnx/taste-skill (design-taste-frontend v2)
> 审查日期：2026-06-22
> 审查范围：7 个布局 XML + dimen tokens + 主题/样式/颜色资源

---

## 重要前提：taste-skill 的适用范围限制

taste-skill（§13 Out of Scope）明确声明不适用于原生移动端："Native mobile (use Apple HIG / Material directly)." 该 skill 的设计目标是为 Web 前端落地页（React/Next.js/Tailwind）提供防 AI 模板审查。

本报告将 taste-skill 中跨平台通用的设计原则（间距体系、布局多样性、组件状态覆盖、AI 模板调识别）应用于 Android 原生布局，同时忽略仅与网页相关的检查项（em-dash 禁止、hero viewport、CTA wrap、eyebrow 计数、zigzag 交替、logo wall 等）。每项检查均标注对应的 taste-skill 章节编号。

> 📘 **教材批注**：本报告中的每条改进建议已对照两份教材标准进行标注：
> - **实训** → `materials/1_practical_trainning/作业要求与评分标准.md` / `实训1-登录与注册页面.md`
> - **课程设计** → `materials/2_design/01-assignment.md` / `02-grading-rubric.md`
> 
> 标注含义（`>=` 原则：不低于教材要求，且允许做得更好）：
> - ✅ **教材要求** — 教材明确要求，应优先采纳
> - ➕ **超出教材 · 品质加分** — 教材未明确要求，但有助于品质提升，放心做（老师不会嫌学生过于优秀）
> - ❌ **不宜采用** — 与教材要求直接冲突，或大概率引入功能性 bug

---

## 设计读（Design Read）

**Reading this as:** Android 工具型 App（待办清单 + 登录注册），面向学生/个人用户，Material 3 设计语言，使用原生 XML Views，无 Compose。

## 三 Dials

| Dial | 设定值 | 理由 |
|------|--------|------|
| DESIGN_VARIANCE | **5** | M3 工具有结构化布局（登录/注册表单规整，待办列表列表驱动），页面间应有明显变化，不需要艺术性不对称。靠近"Offset"区间（4-7）。 |
| MOTION_INTENSITY | **3** | Android Activity Transition + 基本组件动画（密码切换图标、按钮涟漪）。无自定义动画、无 GSAP/Motion 级效果。处于"Static/Fluid CSS"边界。 |
| VISUAL_DENSITY | **6** | 工具型 App，适中密度。不是 Airy gallery，也不是 cockpit。表单和列表之间有合理间距。 |

---

## 1. 全局诊断

### 1.1 Pre-Flight Failures（taste-skill §14）

以下为关键 Pre-Flight 检查项的结论。适用项目保留，不适用的已跳过。

| 检查项 | 结果 | 说明 |
|--------|------|------|
| Shape Consistency Lock（§4.4） | **FAIL** | 主题中定义了完整的 shape token 体系（ShapeExtraSmall 4dp ~ ShapeExtraLarge 28dp），但所有布局文件一律硬编码 `cardCornerRadius="12dp"`，忽略了 ShapeMedium 引用 |
| Color Consistency Lock（§4.2） | PASS | 所有页面使用统一的主色（green_500）、辅色（teal_700）、三色（amber_500），无页面间颜色漂移 |
| Page Theme Lock（§4.11） | **WARN** | 当前仅定义了浅色主题（`DayNight.NoActionBar` parent 暗示「支持」深色但实际无 dark 主题覆盖。`themes.xml` 中没有 `values-night/themes.xml`）。 |
| Empty States | PASS | todo_list.xml 有完整空状态（空插画 + 引导文字），§4.5 达标 |
| Loading States | **FAIL** | 7 个页面零 loading 状态。数据库操作在子线程执行，但无任何 skeleton/loading indicator |
| Error States | **WARN** | 基于 Toast 的错误提示（Java 层），布局中除 todo_edit 的 `errorEnabled` 外无 inline error UI |
| Button Contrast（§4.5） | PASS | white-on-green 符合 WCAG AA，未发现白底白字 CTA |
| Form Contrast（§4.5） | PASS | 输入框使用 OutlinedBox，focus stroke color 绑定了 `?attr/colorPrimary` |
| Empty/Loading/Error | **FAIL** | 见 Loading States 行 |
| Cards only for hierarchy（§4.4） | **WARN** | Todo Detail 页面将所有内容包裹在一张 MaterialCardView 中，但卡片内又嵌套色块分层，三级容器层次过重 |

> 📘 **教材批注 — Pre-Flight 检查项教材符合性：**
> 
> | 检查项 | 判定 | 说明 |
> |--------|------|------|
> | Shape Consistency | ➕ 品质加分 | 教材要求"符合 Material Design 规范"，shape token 是 M3 的组成部分，值得做 |
> | Color Consistency | ✅ 教材要求 | 实训评分标准明确要求"颜色集中在 `colors.xml`，便于统一调整" |
> | Page Theme (Dark) | ➕ 品质加分 | 教材未要求 dark theme，但已定义 DayNight parent，补上 `values-night/` 成本极低，让项目更完整 |
> | Empty States | ✅ 教材要求 | 课程设计 F2"待办查看"隐含了列表为空时的展示要求 |
> | Loading States | ✅ 教材要求 | 教材要求"交互流畅"、"成功/失败有明确反馈"。操作无反馈会降低流畅感 |
> | Error States | ✅ 教材要求 | 教材明确要求"输入不合法时的红色/Toast 错误提示"、"成功/失败有明确反馈" |
> | Button/Form Contrast | ➕ 品质加分 | 教材未要求 WCAG 对比度，但当前已 PASS，无需再改 |
> | Cards for hierarchy | ➕ 品质加分 | 简化容器嵌套有助于改善间距一致性，不属于"违反教材"

### 1.2 间距体系（taste-skill §4.7 Layout Discipline）

dimens.xml 定义了完整的间距 token：

| Token | 值 | 引用次数 | 状态 |
|-------|------|---------|------|
| `@dimen/page_padding` | 16dp | **0** | 所有页面硬编码 `android:padding="16dp"` |
| `@dimen/form_field_spacing` | 16dp | **0** | 散布着 16dp、20dp、8dp、12dp 等硬编码值 |
| `@dimen/card_content_padding` | 16dp | **0** | `app:contentPadding="16dp"` 硬编码 |
| `@dimen/button_margin_top` | 24dp | **0** | login 页用 24dp，welcome 页用 16dp/12dp |
| `@dimen/list_item_padding_vertical` | 12dp | **0** | item_todo 硬编码 `android:padding="12dp"` |
| `@dimen/list_card_margin` | 4dp | **0** | item_todo 硬编码 `android:layout_margin="4dp"` |
| `@dimen/toolbar_elevation` | 0dp | **0** | 各 toolbar 未引用此值 |

**结论：dimen 的 token 系统完全失效。** 7 个 layout 文件中没有一个使用了 `@dimen/xxx` 引用。当前 dimen.xml 等价于死代码。

> 📘 **教材批注：** 实训评分标准明确要求"不同屏幕尺寸下能正常显示（避免硬编码像素）"以及"使用 dp/sp 而非 px"。引用 `@dimen/xxx` 是实现这一要求的标准化手段，且有助于达成"布局合理、美观易用"的评分目标。→ ✅ **教材要求**

**具体违反位置：**
- `activity_main.xml:9` — `android:padding="16dp"` 应使用 `@dimen/page_padding`
- `activity_main.xml:22` — `android:layout_marginTop="20dp"` 应使用 `@dimen/form_field_spacing`
- `activity_main.xml:50` — `android:layout_marginTop="20dp"` 同上
- `activity_welcome.xml:127` — `android:layout_marginTop="12dp"` 应使用 `@dimen/form_field_spacing`
- `activity_welcome.xml:136` — `android:layout_marginTop="12dp"` 同上
- `item_todo.xml:11` — `android:layout_margin="4dp"` 应使用 `@dimen/list_card_margin`
- 全部 `cardCornerRadius="12dp"` — 应使用 `@style/ShapeMedium`

### 1.3 布局多样性（taste-skill §4.3 Anti-Center Bias）

| 页面 | 布局方式 | 评判 |
|------|---------|------|
| Login | 顶部 Toolbar + 居中卡片 + 底部按钮 | 标准表单，可接受 |
| Register | 顶部 Toolbar + 滚动居中卡片 | 标准表单，可接受 |
| Welcome | Toolbar + 居中速览卡片 + 堆叠按钮 | 按钮堆叠缺乏层次 |
| Todo List | Toolbar + RecyclerView + 居中空状态 | 列表页面，布局合理 |
| Todo Edit | Toolbar + 堆叠表单 + 按钮栏 | 标准编辑页 |
| Todo Detail | Toolbar + 卡片包裹全部内容 | 卡片使用过重 |
| Item | 横向 CheckBox + 文本 + 删除按钮 | 标准列表项 |

**整体评估：** 对于工具型 App，大多数字段输入页使用垂直居中堆叠是合理的。但 Welcome 页面严重缺乏布局多样性——三个大小相同、样式几乎相同的按钮垂直堆叠，缺乏视觉层次和功能优先级区分（§9.C：NO 3-column equal feature cards 的 Android 等价问题）。

> 📘 **教材批注：** 课程设计评分标准要求"按钮布局合理"、"操作路径短"。三个等大按钮堆叠导致用户无法快速判断"哪个是主要操作"，直接降低"简洁美观"和"交互流畅"的评分。→ ✅ **教材要求**
> 
> ➕ **品质加分：** 教材未要求"不对称布局"或"布局多样性"，但 Welcome 页按钮层级区分是好设计。对于表单页用居中堆叠是完全合理的，不需改为分栏。

### 1.4 组件状态覆盖（taste-skill §4.5）

| 状态 | 登录 | 注册 | 欢迎 | 列表 | 编辑 | 详情 |
|------|------|------|------|------|------|------|
| Default | ok | ok | ok | ok | ok | ok |
| Empty | N/A | N/A | N/A | **ok** | N/A | N/A |
| Loading | **FAIL** | **FAIL** | **FAIL** | **FAIL** | **FAIL** | **FAIL** |
| Error | Toast only | Toast only | N/A | N/A | Partial (title error) | N/A |
| Disabled | **FAIL** | **FAIL** | N/A | N/A | N/A | N/A |
| Tactile | ripple | ripple | ripple | selectableItemBg | ripple | ripple |

**Loading 是最大的缺失。** 登录时网络/数据库操作、注册提交、列表加载、编辑保存——所有操作都是子线程执行但 UI 层没有反馈。用户点击按钮后界面不做任何变化，直到操作完成。

> 📘 **教材批注：** 实训评分标准明确要求"成功/失败有明确反馈（Toast/页面跳转/提示信息）"。Loading 态（禁用按钮 + 显示进度）是实现"交互流畅"的基本手段。教材虽然没直接说要用 ProgressBar，但"按钮提交后无任何反馈"会在"界面设计"和"功能完整性"两个维度同时扣分。→ ✅ **教材要求**

### 1.5 AI Tells（taste-skill §9）

| Tell | 状态 | 说明 |
|------|------|------|
| Inter default font | N/A | Android 系统字体，不适用 |
| AI Purple | PASS | 使用 green 主色系，合理 |
| 3 equal feature cards | **WARN** | Welcome 页面三个等宽等样式按钮堆叠（§9.C 的 Android 等价情况） |
| No em-dash | PASS | 无此问题 |
| No generic names | PASS | 代码中无 Jane Doe 类占位名 |
| No hand-rolled SVG | PASS | 使用 drawable 资源 |
| No fake screenshots | PASS | 原生 App，无此问题 |
| No scroll cues | PASS | 无此问题 |
| No "Quietly in use at" | PASS | 无此问题 |
| Over-cardbox-ing | **WARN** | 详情页整页一个卡片，嵌套色块，三级容器层次偏重（§4.4） |

> 📘 **教材批注 — AI Tells 教材符合性：**
> 
> | Tell | 判定 | 说明 |
> |------|------|------|
> | 3 equal feature cards (Welcome 按钮) | ✅ 教材要求 | 对应课程设计评分标准"按钮布局合理" |
> | Over-cardbox-ing | ➕ 品质加分 | 简化容器嵌套改善间距一致性，不是违反教材 |
> | AI Purple / Generic names / Em-dash / 等 | N/A | 教材未涉及这些 web 前端概念，项目中也未出现这些问题 |

---

## 2. 逐页审查

### 2.1 登录页 (`activity_main.xml`)

**违反 taste-skill 的章节：**
- §4.7 Layout Discipline — dimen token 未使用
- §4.5 Interactive UI States — 无 loading/disabled 状态

**具体问题：**

| # | 位置 | 问题 | 改进建议 |
|---|------|------|---------|
| L9 | `android:padding="16dp"` | 硬编码，应引用 dimen | `@dimen/page_padding` |
| L22 | `android:layout_marginTop="20dp"` | 表单字段间距 20dp，与 `form_field_spacing=16dp` 不一致 | `@dimen/form_field_spacing` |
| L50 | `android:layout_marginTop="20dp"` | 同上 | `@dimen/form_field_spacing` |
| L23 | `app:cardCornerRadius="12dp"` | 硬编码，主题中有 ShapeMedium(12dp) | `style="@style/ShapeMedium"` 替换 |
| L92-101 | `btn_login` | 登录按钮无 loading 态 | 在布局中准备 ProgressBar 或按钮内 loading indicator |
| L23-24 | ContentPadding内硬编码16dp | 应使用 cardContentPadding dimen | `app:contentPadding="@dimen/card_content_padding"` 失效，此类 attr 只接受 dimen 格式 |

**额外观察：** Toolbar（L11-17）无返回按钮（navigationIcon），符合登录页定位——这没问题。但 Toolbar 的 background 绑定了 `?attr/colorPrimary`，与 statusBarColor 相同，视觉上融为一体，这在某些设备上看起来像是标题栏消失了。

> 📘 **教材批注：**
> - 间距硬编码问题（L9/L22/L50/L23）→ ✅ **教材要求**：教材要求"避免硬编码像素"、"不同屏幕尺寸下能正常显示"
> - Loading 态缺失（L92-101）→ ✅ **教材要求**：教材要求"交互流畅"、"明确的反馈"
> - cardCornerRadius 硬编码 → ➕ **品质加分**：符合 Material Design 规范的统一 shape 体系
> - Toolbar 背景色融合 → ➕ **品质加分**：小细节打磨，教材未要求但做了更好

---

### 2.2 注册页 (`activity_register.xml`)

**违反 taste-skill 的章节：**
- §4.7 Layout Discipline — dimen token 未使用，margin 值不一致
- §4.6 Data & Form Patterns — 输入间距标准化不足

**具体问题：**

| # | 位置 | 问题 | 改进建议 |
|---|------|------|---------|
| L27 | `android:layout_marginTop="4dp"` | Magic number 4dp，无语义意义 | 统一用 `@dimen/form_field_spacing` 或删除此多余 margin |
| L43, 56, 74, 91 | 各 `android:layout_marginTop="16dp"` | 表单字段间距，但前一字段 marginTop=4dp，不一致 | 统一用 `@dimen/form_field_spacing` |
| L108-114 | 注册按钮 | 无 loading 态 | 添加 ProgressBar 或按钮内 loading indicator |
| L123 | `android:layout_marginTop="8dp"` | 8dp 与其余 16dp 不统一 | 用 `@dimen/form_field_spacing` 或定义 8dp 专用 token |

**额外观察：** 注册页有四个输入字段（账号/密码/确认密码/邮箱），是最长的表单。`TextInputLayout` 的 counter/error 仅出现在编辑页，注册页没有任何字段级验证 UI。如果邮箱格式错误或密码不匹配，当前实现只靠 Toast 通知——违反 §4.5 Interactive UI States 的 inline error 要求。

> 📘 **教材批注：**
> - 间距不一致（L27 4dp / L43-91 16dp / L123 8dp）→ ✅ **教材要求**：硬编码间距不统一，影响"布局合理"
> - 加载态缺失（L108-114）→ ✅ **教材要求**：教材要求"成功/失败有明确反馈"
> - 字段级验证 UI → ✅ **教材要求**：教材明确要求"输入不合法时的红色/Toast 错误提示"。当前已用 Toast 满足基本要求，增加 inline error 是加分项

---

### 2.3 欢迎页 (`activity_welcome.xml`)

**违反 taste-skill 的章节：**
- §4.3 Anti-Center Bias — 三个等大等样按钮堆叠（Android 版"3 equal feature cards"）
- §4.4 Materiality — 仪表盘卡片的 elevation 合理性存疑
- §9.C — 3 equal 组件
- §4.7 — dimen 失效

**具体问题：**

| # | 位置 | 问题 | 改进建议 |
|---|------|------|---------|
| L114-121 | `btn_quick_add` | full-width MaterialButton | 区分优先级：快速创建用 FilledButton + icon，列表查看用 OutlinedButton |
| L123-128 | `btn_todo_list` | 同上，与 btn_quick_add 几乎完全相同的样式 | 与 btn_quick_add 区分层级（Filled vs Outlined vs Text） |
| L130-140 | `btn_logout` | 红色文字 + 红色描边，明确语义，这是正确的 | 但颜色代码 `?attr/colorError` 正确使用主题属性 — PASS |
| L127 | `android:layout_marginTop="12dp"` | 与 dimen `button_margin_top=24dp` 不一致 | 使用 `@dimen/button_margin_top` 或定义新的间距 token |
| L136 | `android:layout_marginTop="12dp"` | 同上 | 同上 |
| L42-53 | 仪表盘数字（tv_done_count/tv_pending_count）| fontSize 32sp bold — 只有数字，缺少视觉上下文 | 考虑用 M3 的数字卡片风格（大数字 + 微标签）+ 进度环动画 |
| L23 | `android:padding="16dp"` | 硬编码 | `@dimen/page_padding` |

**额外观察：** 三个按钮堆叠（快速创建 / 待办列表 / 退出登录）实际上对应两个功能入口 + 一个安全操作。退出按钮放在这里可以理解，但视觉上三个等宽按钮占满屏幕，用户视线从左上角扫码式掠过时看不出优先级。

> 📘 **教材批注：**
> - 三按钮等样式无层级（L114-128）→ ✅ **教材要求**：课程设计评分标准要求"按钮布局合理"、"操作路径短"
> - 按钮间距 12dp 与 token 24dp 不一致（L127/L136）→ ✅ **教材要求**：影响"适当的间距"评分项
> - 仪表盘数字 32sp 过大（L42-53）→ ✅ **教材要求**：统计数字与周围 14sp 标签的比例鸿沟，影响"简洁美观"评分
> - dimen 引用 → ✅ **教材要求**：避免硬编码像素

---

### 2.4 待办列表页 (`activity_todo_list.xml`)

**违反 taste-skill 的章节：**
- §4.7 Layout Discipline — padding 不一致
- §4.5 Interactive UI States — 无 loading 态

**具体问题：**

| # | 位置 | 问题 | 改进建议 |
|---|------|------|---------|
| L23-27 | `android:padding="8dp"` + `android:paddingStart="16dp"` + `android:paddingEnd="16dp"` | 三种 padding 叠加为 start=16dp, end=16dp, top=8dp, bottom=8dp，但语法上 `padding="8dp"` 被后续覆盖 start/end——实际生效的是 top/bottom=8dp + start/end=16dp。这是 bug 还是意图？ | 删掉 `android:padding="8dp"`，改用 `android:paddingTop="8dp" android:paddingBottom="8dp"` 显式声明 |
| L51-52 | `layout_margin="16dp"` | FAB 边距，应引用 dimen | `@dimen/page_padding` |
| L33 | `empty_state_container` | 空状态存在且视觉完整 — PASS | — |
| L52 | iv_empty 120dp x 120dp | 这个尺寸是以 layout_width/layout_height 设置的，但父容器是 wrap_content，除非图片资源本身是 120dp 否则会缩放 | 检查 `ic_empty` drawable 的尺寸是否匹配 |

**额外观察：** CoordinatorLayout + AppBarLayout 的组合在 M3 中常用，但这里的 Toolbar 直接放在 CoordinatorLayout 内，没有用 AppBarLayout 包裹——这意味着 Toolbar 不参与滚动行为。RecyclerView 使用 `layout_marginTop="?attr/actionBarSize"` 手动留出 Toolbar 空间，这绕过了 CoordinatorLayout 的滚动协调机制。

> 📘 **教材批注：**
> - RecyclerView padding 冲突（L23-27）→ ✅ **教材要求**：代码中的 padding 残留 (8dp + 16dp start/end) 是明显的实现疏忽，影响"布局合理"
> - FAB margin 硬编码（L51-52）→ ✅ **教材要求**：应引用 dimen
> - 空态插图 alpha 和居中（L33/L52）→ ➕ **品质加分**：空态已满足课程设计 F2 的基本要求，细节打磨是加分项
> - Toolbar 不参与滚动协调 → ➕ **品质加分**：当前实现功能正常，可改可不改。要改则需注意用 AppBarLayout 包裹并测试滚动行为

---

### 2.5 待办编辑页 (`activity_todo_edit.xml`)

**违反 taste-skill 的章节：**
- §4.7 Layout Discipline — dimen 失效
- §4.6 Data & Form Patterns — 基础较好

**具体问题：**

| # | 位置 | 问题 | 改进建议 |
|---|------|------|---------|
| L25, 44 | `android:layout_marginTop="16dp"` | 硬编码 | `@dimen/form_field_spacing` |
| L28 | `app:errorEnabled="true" app:counterEnabled="true" app:counterMaxLength="50"` | 标题字段的验证支持 — PASS | — |
| L65 | `android:layout_marginTop="12dp"` | 硬编码 | `@dimen/form_field_spacing` |
| L9 | `android:padding="16dp"` | 硬编码 | `@dimen/page_padding` |

**额外观察：** 编辑页是所有页面中结构最干净的——无冗余卡片、无嵌套层级、字段间间距一致。内容区域 `minLines="5"` + `gravity="top|start"` 是正确实践。Cancel 和 Save 按钮使用 `gravity="end"` + `layout_marginStart="16dp"` 组合——这是 M3 推荐的排列方式。

> 📘 **教材批注：**
> - 间距硬编码（L25/L44/L65/L9）→ ✅ **教材要求**：避免硬编码像素
> - 标题 counter 和 error（L28）→ ✅ **教材要求**：此为**正确实践**，符合"输入不合法时的错误提示"要求
> - 无 ScrollView 包裹 → ➕ **品质加分**：键盘弹出遮挡按钮会降低"交互流畅"评分，加上更稳妥
> - 总体评价：**编辑页是 all 7 页面中最符合教材要求的**——简洁、操作路径短、按钮布局合理

---

### 2.6 待办详情页 (`activity_todo_detail.xml`)

**违反 taste-skill 的章节：**
- §4.4 Materiality, Shadows, Cards — 卡片嵌套过多
- §4.7 Layout Discipline — 间距体系失效
- 三级容器：ScrollView(8dp) > CardView(16dp) > tinted background block

**具体问题：**

| # | 位置 | 问题 | 改进建议 |
|---|------|------|---------|
| L28-147 | 整页 MaterialCardView | 将整个内容区域包裹在卡片中是冗余的。卡片内又套了一个 `colorSurfaceVariant` 背景块（L80-108）。三级容器嵌套（ScrollView > CardView > tintBackground）视觉过重。 | 去掉外部 CardView，用 `colorSurfaceVariant` 作为内容区域背景；或者去掉内部色块，直接使用 CardView 作为唯一容器。不需要两个。 |
| L26 | `android:padding="8dp"` | ScrollView 内 padding 8dp，而 CardView 内又是 16dp padding，栈叠成 24dp | 统一用一个容器管理间距 |
| L80-82 | `android:background="?attr/colorSurfaceVariant" android:padding="12dp"` | 内容块单独使用 surfaceVariant 背景，在卡片内再创造一层新的视觉容器 | 去掉内部色块，或改用 `shape="@style/ShapeSmall"` 的分组背景 |
| L68-74 | DividerView 使用 `colorOutlineVariant` | 合理使用主题色属性 — PASS | — |
| L46 | `android:textSize="22sp"` | 标题字体大小硬编码，未使用 textAppearance 系统 | 用 `?attr/textAppearanceHeadlineSmall` 或 `?attr/textAppearanceTitleLarge` |
| L9 | `android:padding="16dp"` | 硬编码 | `@dimen/page_padding` |

**额外观察：** 这是全 App 中布局层最多的页面，但数据展示却最简单——只有标题 + 状态 + 内容 + 时间。大量嵌套容器服务于少量数据，§4.4 的核心原则被违反。

> 📘 **教材批注：**
> - 容器嵌套过深（L28-147）→ ➕ **品质加分**：容器净化可以解决 padding 叠加（8dp+16dp=24dp）导致的间距错乱，属于"布局合理"范畴，不是违反教材
> - 间距硬编码（L26/L80-82/L9）→ ✅ **教材要求**：避免硬编码像素
> - 标题 22sp 硬编码未用 textAppearance（L46）→ ➕ **品质加分**：目前 `android:textSize="22sp"` + `android:textStyle="bold"` 功能等价于 TitleLarge，用 `textAppearance` 是精进
> - DividerView 使用 colorOutlineVariant → ✅ **教材要求**：正确使用了主题色属性，是"符合 Material Design 规范"的正面案例
> - **整体评价：** 详情页功能完全满足 F2 要求。嵌套层级不是扣分点，但 padding 叠加导致的间距不统一应修复

---

### 2.7 待办列表项 (`item_todo.xml`)

**违反 taste-skill 的章节：**
- §4.7 Layout Discipline — dimen 失效
- §4.4 Shape Consistency — cardCornerRadius 硬编码

**具体问题：**

| # | 位置 | 问题 | 改进建议 |
|---|------|------|---------|
| L7 | `app:cardCornerRadius="12dp"` | 硬编码，应引用 shape token | `style="@style/ShapeMedium"` |
| L11 | `android:layout_margin="4dp"` | 硬编码，应引用 dimen | `@dimen/list_card_margin` |
| L20 | `android:padding="12dp"` | 硬编码 | `@dimen/list_item_padding_vertical`（注意 padding 是水平+垂直，而 token 仅定义 vertical） |
| L50-57 | `btn_delete` | IconButton 36dp x 36dp — 尺寸合理但硬编码 | 使用 `@dimen/icon_button_size` 或其他预定义尺寸 |

**额外观察：** 列表项整体结构合理。CheckBox + 标题 + 时间 + 删除按钮的标准四元素布局。`selectableItemBackground` foreground（L13）提供了良好的点击反馈。stroke（L9-10）增加了卡片的边界定义，视觉上清醒。

> 📘 **教材批注：**
> - cardCornerRadius 硬编码（L7）→ ➕ **品质加分**：符合 Material Design 规范的统一 shape 体系
> - margin/padding 硬编码（L11/L20）→ ✅ **教材要求**：避免硬编码像素
> - 删除按钮 36dp → ➕ **品质加分**：教材未要求 44dp 触摸目标。36dp 在 Android 中常见且可用，放大更好但不是必须
> - 删除图标颜色 `@color/danger` 硬编码 → ➕ **品质加分**：功能上等效，语义化引用是精进建议

---

## 3. 优先级改进清单

### P0 — Pre-Flight Fail（必须修）

| # | 问题 | 涉及文件 | 对应 taste-skill | 教材判定 |
|---|------|---------|-----------------|---------|
| 1 | **dimen token 零引用** — `dimens.xml` 定义的 8 个 token 无一被布局文件引用，等于是死代码 | 全部 7 个 layout | §4.7 | ✅ 教材要求 |
| 2 | **Loading 态缺失** — 所有 Activity 在子线程操作期间 UI 层无任何反馈 | 全部 7 个 layout | §4.5 | ✅ 教材要求 |
| 3 | **Shape token 未使用** — 5 个 shape style 已定义(`ShapeExtraSmall`-`ShapeExtraLarge`)，所有 cardCornerRadius 仍是硬编码 12dp | `activity_main.xml:23`, `activity_register.xml:31`, `activity_welcome.xml:42`, `item_todo.xml:7` | §4.4 | ➕ 品质加分 |
| 4 | **详情页嵌套过深** — ScrollView > CardView > tinted block 三级容器，且 ScrollView 内 padding 8dp + CardView 内 padding 16dp 叠加 | `activity_todo_detail.xml:26,35` | §4.4 | ➕ 品质加分 |

> 📘 **P0 优先级建议：** 4 项均可做。#1 #2 是教材要求项，先做。#3 #4 是加分项，顺手做了更好。

### P1 — 设计债务（应修）

| # | 问题 | 涉及文件 | 对应 taste-skill | 教材判定 |
|---|------|---------|-----------------|---------|
| 5 | **Dark 主题缺失** — 主题 parent 为 `DayNight` 但实际未提供 `values-night/themes.xml` | `themes.xml` | §4.11 | ➕ 品质加分 |
| 6 | **Welcome 页三按钮等样式堆叠** — 快速创建、待办列表、退出登录使用几乎相同的 full-width MaterialButton，缺乏优先级区分 | `activity_welcome.xml:114-140` | §4.3, §9.C | ✅ 教材要求 |
| 7 | **硬编码间距值不一致** — 页面间混合使用 8dp/12dp/16dp/20dp/24dp 未统一 | 全部 layout | §4.7 | ✅ 教材要求 |
| 8 | **登录/注册按钮无 disabled 态** — 提交过程中（网络/数据库操作未完成）按钮应禁用 | `activity_main.xml`, `activity_register.xml` | §4.5 | ✅ 教材要求 |
| 9 | **内联错误验证 UI 不足** — 除编辑页 title 外，所有表单验证失败只通过 Toast 通知 | `activity_main.xml`, `activity_register.xml` | §4.5, §4.6 | ➕ 品质加分 |
| 10 | **Register 页 4dp magic margin** — `android:layout_marginTop="4dp"` 无语义意义 | `activity_register.xml:28` | §4.7 | ✅ 教材要求 |

> 📘 **P1 优先级建议：** #6 #7 #8 #10 是教材要求项，优先处理。#5（Dark 主题）成本极低，顺便加上。#9（inline error）当前 Toast 已满足教材要求，增加字段级 error 是加分项。

### P2 — 精进（建议改）

| # | 问题 | 涉及文件 | 对应 taste-skill | 教材判定 |
|---|------|---------|-----------------|---------|
| 11 | Dashboard 单向数字展示（32sp bold）缺乏进度可视化 | `activity_welcome.xml:64-107` | §4.5 | ➕ 品质加分 |
| 12 | Todo 列表 RecyclerView 的 padding 组合不清晰（8dp 被局部覆盖） | `activity_todo_list.xml:24-27` | §4.7 | ✅ 教材要求 |
| 13 | 详情页标题 22sp 硬编码，应使用 `?attr/textAppearanceTitleLarge` | `activity_todo_detail.xml:46` | §4.1 | ➕ 品质加分 |
| 14 | Toolbar 直接放在 CoordinatorLayout 中，未使用 AppBarLayout，导致不参与滚动协调 | `activity_todo_list.xml:9-17` | §4.7 | ➕ 品质加分 |
| 15 | 欢迎页仪表盘 elevation 2dp 的必要性存疑（统计卡片无需和列表卡片同样深度） | `activity_welcome.xml:42` | §4.4 | ➕ 品质加分 |

> 📘 **P2 优先级建议：** #12 是代码残留清理，顺手做。#11 #13 #14 #15 都是加分项，有时间就做，不做也不影响评分。

---

## 总结

MyAndroidPT 的布局在 Material 3 组件迁移上完成了基础工作（主题系统、颜色 token、shape token 均已定义），但 **token 系统在布局层完全没有被消费**。设计系统的定义层（themes.xml / colors.xml / dimens.xml / styles.xml）和实现层（layout XML）之间存在断裂——定义了完美的标准，然后每一处都在硬编码。

最大的结构性问题：**Loading 态全面缺失** 和 **Detail 页过度嵌套**。

如果只修 3 件事（按投入产出比）：
1. 将 `@dimen/xxx` 和 shape token 引用到所有布局文件中（消除硬编码）— ✅ 教材要求：避免硬编码像素
2. 为登录/注册/列表/编辑添加至少一个加载指示器 — ✅ 教材要求：交互流畅，明确的反馈
3. Welcome 页按钮层级区分（主/次/安全操作）— ✅ 教材要求：按钮布局合理，操作路径短

> 📘 **总批注：** 本报告 15 项改进建议经教材 `>=` 原则过滤后：
> - ✅ **教材要求：6 项** — 直接拿分点，优先做
> - ➕ **品质加分：9 项** — 放心做，老师不会嫌学生太优秀
> - ❌ **不宜采用：0 项** — 没有与教材冲突或明显会出 bug 的建议
>
> 换句话说：**这 15 项全部可以做。没有一项是"不能做"的。**