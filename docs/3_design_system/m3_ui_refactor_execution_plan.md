# M3 UI 全面重构 — 执行编排

> 角色分工：编排层（本文）+ 执行层（开发者按步骤操作）
> 每个 Phase 有明确的 **输入 → 操作 → 验收标准 → 输出**，不通过不进下一 Phase。

---

## 前置准备

### 分支策略

```
main       — 稳定分支（重构前先确保此处可编译）
  └─ feat/m3-ui-refactor  ← 当前工作分支
        ├─ Phase 1 commit
        ├─ Phase 2 commit
        ├─ Phase 3 commit
        └─ Phase 4 commit
```

### 验证基线

重构正式开始前，先拍一排基线截图（存在 `docs/screenshots/baseline/`）：

| # | 页面 | ADB 导航路径 | 文件名 |
|---|------|-------------|--------|
| 1 | 登录页 | 直接启动 MainActivity | `01-login.png` |
| 2 | 注册页 | 登录页点"去注册" | `02-register.png` |
| 3 | 欢迎页 | 登录成功后 | `03-welcome.png` |
| 4 | 待办列表 | 欢迎页点"我的待办" | `04-todo-list.png` |
| 5 | 待办详情 | 列表页点任意项 | `05-todo-detail.png` |
| 6 | 编辑待办 | 详情页点"编辑" | `06-todo-edit.png` |
| 7 | 新增待办 | 列表页点 FAB | `07-todo-create.png` |

对应截图脚本片段（PowerShell，执行前确保模拟器已启动）：

```powershell
# 先清旧截图
adb shell "rm -f /sdcard/screenshot_*.png"

# 每截一张后拉取
adb shell screencap -p /sdcard/screenshot_01.png
adb pull /sdcard/screenshot_01.png docs/screenshots/baseline/01-login.png
```

---

## Phase 1：主题基础设施

**目标**：把 M3 theme 和 token 体系搭好，此阶段不碰任何布局。

### 步骤

| 步骤 | 操作 | 文件 |
|------|------|------|
| 1.1 | 确认 `app/build.gradle` 中 `material` 依赖已存在且为较新版本 | `app/build.gradle` |
| 1.2 | 扩展 `colors.xml`，新增 M3 21 色角色所需颜色 | `res/values/colors.xml` |
| 1.3 | 重写 `themes.xml`，父主题改为 `Theme.Material3.DayNight.NoActionBar`，定义全部 21 色角色 | `res/values/themes.xml` |
| 1.4 | 在 `styles.xml` 中新增 shape token 样式和 typography token 样式 | `res/values/styles.xml` |

### colors.xml 新增颜色清单

```
green_50, green_100, green_200        # 主色容器层（浅绿）
green_500                              # 主色（不变，沿用教材指定值）
green_700, green_900                    # 主色深色变体
teal_50, teal_100, teal_200            # 辅色容器层
teal_900                               # 辅色深色（替代原有 teal_700）
red_50, red_100, red_600               # 错误色
surface_light, on_surface_light        # 浅色表面
surface_dark, on_surface_dark          # 深色表面
surface_variant, on_surface_variant    # 表面变体
outline_light, outline_dark             # 描边色
white, black                            # 基础色（已有则复用）
```

### themes.xml 应设置的颜色角色（检查清单）

```
[ ] colorPrimary / colorOnPrimary / colorPrimaryContainer / colorOnPrimaryContainer
[ ] colorSecondary / colorOnSecondary / colorSecondaryContainer / colorOnSecondaryContainer
[ ] colorTertiary                          # 可选，暂不填也行
[ ] colorError / colorOnError / colorErrorContainer / colorOnErrorContainer
[ ] colorSurface / colorOnSurface / colorSurfaceVariant / colorOnSurfaceVariant
[ ] colorOutline / colorOutlineVariant
[ ] statusBarColor （用 ?attr/colorPrimaryVariant 保持联动）
```

### styles.xml 应新增的 token 样式

```
shape/:
  Shape.ExtraSmall  (cornerSize=4dp)
  Shape.Small       (cornerSize=8dp)
  Shape.Medium      (cornerSize=12dp)
  Shape.Large       (cornerSize=16dp)
  Shape.ExtraLarge  (cornerSize=28dp)

text/:
  TextAppearance.DisplaySmall
  TextAppearance.HeadlineSmall/Medium/Large
  TextAppearance.TitleMedium
  TextAppearance.BodyMedium
  TextAppearance.LabelMedium
```

> MyBtnStyle 和 MyEditStyle **保留但不修改**，Phase 2 中再在其上叠加 M3 组件样式。

### 验收标准

| 检查项 | 通过条件 | 检查方式 |
|--------|---------|---------|
| 编译 | `gradlew assembleDebug` 返回 BUILD SUCCESSFUL | 命令行 |
| 启动不崩溃 | 6 个 Activity 都能正常打开 | ADB 逐一启动 |
| 颜色角色完整 | 21 个角色在 themes.xml 中均有定义 | 人工审核 XML |
| Shape 令牌可用 | 在任一布局中引用 `?attr/shapeAppearanceCornerMedium` 不报错 | 测试布局 |
| MyBtnStyle/MyEditStyle 仍存在 | grep 能搜到定义 | 命令行 |

### 输出

- commit: `feat: Phase 1 — M3 theme system, color tokens, shape tokens`
- 截图: `docs/screenshots/phase1/alignment.png`（证明各页面仍正常渲染）

---

## Phase 2：登录 + 注册页

**目标**：登录和注册页的组件从原生升级到 M3 Material 组件，是改动量最大的 Phase。

### activity_main.xml 改造点

| 原来 | 改为 | 注意 |
|------|------|------|
| 根 LinearLayout + 固定 padding | 保留结构，将 `android:background` 从硬编码改为 `?attr/colorSurface` | |
| Toolbar | MaterialToolbar（已有，确认 `app:theme` 引用白色文字主题） | |
| 账号行 TextView+EditText | TextInputLayout + TextInputEditText，style="?attr/textFieldOutlinedStyle"，hint="账号" | 保留 `MyEditStyle` 作为基础，叠加 `?attr/textFieldOutlinedStyle` |
| 密码行 TextView+EditText | 同上，inputType="textPassword" | |
| CheckBox 记住密码 | MaterialCheckBox，style="?attr/materialCheckBoxStyle" | |
| CheckBox 自动登录 | MaterialCheckBox | |
| Button 登录 | MaterialButton，style="?attr/materialButtonOutlinedStyle"，独立保留 MyBtnStyle 定义 | MyBtnStyle 中 background 改为透明，让 MaterialButton 接管 |
| TextView 去注册 | MaterialButton，style="?attr/materialButtonTextButtonStyle" | |

### activity_register.xml 改造点

| 原来 | 改为 | 注意 |
|------|------|------|
| 账号/密码/确认密码 三行 | 全部改为 TextInputLayout + TextInputEditText | 确认密码框增加 `errorEnabled="true"` |
| Button 注册 | MaterialButton，filled style，`?attr/materialButtonStyle` | |
| RadioButton 同意协议 | MaterialRadioButton，style="?attr/materialRadioButtonStyle" | |
| 提示文本 | 若注册失败，用 `TextInputLayout.setError()` 替代 Toast 显示账号已存在 | 不改 Java 逻辑，只改错误展示方式 |

### 验收标准

| 检查项 | 通过条件 | 检查方式 |
|--------|---------|---------|
| 编译 | assembleDebug 通过 | 命令行 |
| 登录流程正常 | 输入账号密码 → 登录 → 进入欢迎页 | 手动 / 脚本 |
| 注册流程正常 | 填写 → 勾选协议 → 注册成功 → 自动回填登录页 | 手动 / 脚本 |
| 记住密码 | 勾选 → 退出 → 重新打开 → 密码已填充 | 手动 |
| 自动登录 | 勾选 → 退出 → 重新打开 → 直接进欢迎页 | 手动 |
| 空输入提示 | 不填账号点击登录 → 有红色错误提示 | 手动 |
| 未勾协议提示 | 不勾协议点击注册 → 拦截（已有 rbAgree 逻辑） | 手动 |
| 截图对比 | Phase2 截图 vs 基线截图，视觉明显更现代 | 人工对比 |

### 输出

- commit: `feat: Phase 2 — login & register M3 Material components`
- 截图: `docs/screenshots/phase2/{login,register,welcome}.png`

---

## Phase 3：待办模块（4 个文件）

**目标**：列表、详情、编辑三个页面 + 列表项全部升级为 M3 组件。

### item_todo.xml

| 改动 | 具体操作 |
|------|---------|
| CardView 包名 | `androidx.cardview.widget.CardView` → `com.google.android.material.card.MaterialCardView` |
| 形状属性 | `app:cardCornerRadius="8dp"` → `app:shapeAppearanceOverlay="@style/Shape.Small"` |
|  elevation | `app:cardElevation="2dp"` 保留 |
| CheckBox | 平台 `CheckBox` → `com.google.android.material.checkbox.MaterialCheckBox` |
| 删除按钮 | `ImageButton` → `MaterialButton`（icon + iconTint），保持 `background="?attr/selectableItemBackground"` |
| ripple | CardView 的 `android:foreground="?attr/selectableItemBackground"` 保留 |

### activity_todo_list.xml

| 改动 | 具体操作 |
|------|---------|
| Toolbar | 确认已使用 MaterialToolbar + `app:theme`（白色标题） |
| FAB | 现有 `FloatingActionButton`，增加 `app:shapeAppearanceOverlay="@style/Shape.Full"` 确保为圆形 |
| RecyclerView | 确认 padding 单位是 dp 而非 px |

### activity_todo_detail.xml

| 改动 | 具体操作 |
|------|---------|
| 布局扁平化 | 移除嵌套 ScrollView，只保留一个 ScrollView 包裹内容 |
| 结构重组 | 内容区用 `MaterialCardView` 包裹，shape="?attr/shapeAppearanceCornerMedium" |
| CheckBox | → `MaterialCheckBox`，`buttonTint="?attr/colorPrimary"` |
| 按钮组 | 编辑/删除 → `MaterialButton`，style 分别为 text / outlined |
| 时间标签 | 保持 TextView，使用 `?attr/textAppearanceBodySmall` |

### activity_todo_edit.xml

| 改动 | 具体操作 |
|------|---------|
| 标题输入 | → `TextInputLayout` + `TextInputEditText`，outlined style |
| 内容输入 | → `TextInputLayout`（multiline），`layout_weight` 保留 |
| 取消按钮 | → `MaterialButton`，text style |
| 保存按钮 | → `MaterialButton`，filled style |

### 验收标准

| 检查项 | 通过条件 | 检查方式 |
|--------|---------|---------|
| 编译 | assembleDebug 通过 | 命令行 |
| 列表渲染 | 启动列表页，现有待办正常显示 | 手动 |
| FAB 点击 | 点击 FAB → 进入新增待办页 | 手动 |
| 列表项勾选 | 勾选 CheckBox → 待办状态变更（Java 逻辑未改，需确认仍正常） | 手动 |
| 长按删除 | 长按列表项 → 删除确认对话框 → 确认后删除 | 手动 |
| 查看详情 | 点击列表项 → 进入详情页 | 手动 |
| 编辑保存 | 详情页点编辑 → 修改 → 保存 → 返回列表 | 手动 |
| 取消操作 | 编辑页点取消 → 返回上一页 | 手动 |
| 截图对比 | Phase3 截图 vs 基线截图 | 人工对比 |

### 输出

- commit: `feat: Phase 3 — todo module M3 Material components`
- 截图: `docs/screenshots/phase3/{list,detail,edit,create}.png`

---

## Phase 4：欢迎页 + 全局收尾

**目标**：最后一块拼图 + 全局一致性检查。

### activity_welcome.xml

| 改动 | 具体操作 |
|------|---------|
| 根背景 | 移除硬编码 `@color/background_light_blue`，改为 `?attr/colorSurface` |
| 欢迎标题 | TextView → M3 `textAppearanceHeadlineMedium`，`?attr/colorOnSurface` |
| 待办按钮 | → `MaterialButton`，filled style |
| 退出按钮 | → `MaterialButton`，outlined style |

### 全局一致性检查清单

执行层完成后，逐项 tick：

```
[ ] 所有布局的 android:background 不再硬编码颜色值
[ ] 所有 Button 已替换为 MaterialButton（grep "<Button" 应无结果）
[ ] 所有 EditText 已替换为 TextInputLayout（grep "<EditText" 应无结果，TextInputEditText 保留）
[ ] 所有 CheckBox 已替换为 MaterialCheckBox
[ ] 所有 RadioButton 已替换为 MaterialRadioButton
[ ] item_todo.xml 导入 com.google.android.material.card（非 androidx）
[ ] themes.xml 21 色角色无遗漏
[ ] MyBtnStyle 和 MyEditStyle 定义仍存在于 styles.xml
[ ] btn_bg_selector 和 edit_text_bg 未被删除（可保留或标注为 M3 兼容）
[ ] build.gradle 无新增依赖冲突
[ ] 6 个 Activity 全部可启动，功能完整（F1-F5）
[ ] 深浅主题切换正常（如设备支持）
```

### 最终验收

| 检查项 | 通过条件 | 检查方式 |
|--------|---------|---------|
| 全量编译 | `gradlew assembleDebug` 通过 | 命令行 |
| 全链路功能 | 登录 → 欢迎 → 列表 → 查看 → 编辑 → 新增 → 删除，全通 | 手动 / 导航脚本 |
| 基线对比 | Phase4 全量截图 vs 基线截图 | 并排对比 |
| 报告就绪 | `docs/screenshots/phase4/{all}.png` 至少 5 张功能截图 | 文件存在 |
| 代码审查 | 执行层对照上方检查清单逐项确认 | 人工 tick |

### 输出

- commit: `feat: Phase 4 — welcome page + global M3 consistency`
- 截图: `docs/screenshots/phase4/{login,register,list,detail,edit,create}.png`（6 张全量）

---

## 验证脚本要求

执行层应提供一条一次走完 6 个页面的自动化导航脚本。

### 脚本输入

- 已知账号密码（或等待输入）
- 模拟器已启动，ADB 已连接

### 脚本输出

- 每个页面一张截图，存到指定目录
- 控制台打印每步操作日志

### 脚本验收标准

| 标准 | 说明 |
|------|------|
| 单条命令运行 | `powershell -File scripts/navigate_and_capture.ps1` |
| 自动导航 | 无需人工干预，从登录到编辑全流程自动完成 |
| 截图命名规范 | `{phase}-{page-name}.png`，如 `phase4-list.png` |
| 可重跑 | 清空目标目录后重新运行不报错 |

> 脚本本身不计入重构工作量，但必须在 Phase 2 完成后提供可用版本，Phase 3/4 复用。

---

## 与报告的对齐

课程设计报告（`02-grading-rubric.md`）对 UI 的评分点：

| 评分维度 | Phase 贡献 |
|---------|-----------|
| Simple and beautiful | Phase 2/3/4 的组件升级直接提升 |
| Reasonable button layout | Phase 2 中布局重排 |
| Smooth interaction | MDC ripple + M3 动效系统 |
| Conforms to Android design specs | **Phase 1 主题升级即满足** |
| Screenshots (5+) | Phase 4 产出 6 张，直接填入报告 §六 |

**报告 §三（涉及技术）应新增条目：**
- Material Design 3 Design System（theme, color roles, shape tokens, typography）
- MaterialButton / TextInputLayout / MaterialCardView / MaterialCheckBox
- MDC-Android 组件库集成

---

## 各 Phase 依赖关系

```
Phase 1（主题）
  │
  ├──▶ Phase 2（登录/注册）
  │
  ├──▶ Phase 3（待办模块）
  │
  └──▶ Phase 4（欢迎页 + 收尾）
        │
        └──▶ 报告更新（截图填入 + 技术点新增）
```

**可以并行：** Phase 2 和 Phase 3 互不依赖，可以两个执行者并行。
**必须串行：** Phase 1 → Phase 2/3 → Phase 4。

---

## 回退策略

| 阶段 | 回退方式 |
|------|---------|
| Phase 1 出问题 | `git revert <phase1-commit>` — 主题回退到 M2 |
| Phase 2 出问题 | `git revert <phase2-commit>` — 登录注册页回退 |
| Phase 3 出问题 | `git revert <phase3-commit>` — 待办模块回退 |
| Phase 4 出问题 | `git revert <phase4-commit>` — 全局回退 |
| 全部毁掉 | `git revert <phase1> <phase2> <phase3> <phase4>` — 回到基线 |

每个 commit 都是独立的原子改动，回退零风险。

---

## 预估工作量

| Phase | 预估改动行数 | 文件数 | 预估时间 |
|-------|------------|--------|---------|
| Phase 1 | ~80 行 | 3 | 30 min |
| Phase 2 | ~140 行 | 2 | 45 min |
| Phase 3 | ~160 行 | 4 | 60 min |
| Phase 4 | ~50 行 | 1 | 20 min |
| **合计** | **~430 行** | **10** | **~2.5 hrs** |

不含脚本编写和截图时间。
