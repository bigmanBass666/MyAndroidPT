# Tasks — Verdant Design System

Task execution order is designed for safety: infrastructure first (colors, dimens, type), then styles, then layouts, then Java logic, then verification. Tasks with no dependencies can run in parallel.

---

## Phase 1: Design Tokens (Infrastructure)

- [ ] Task 1: 更新 colors.xml 为 Verdant 色值
  - [ ] 更新所有绿色系色值（green_200/500/700/800/900）
  - [ ] 更新 teal 色系为暖灰石板色（teal_50/200/700/900 → 改用新色系但保留原名）
  - [ ] 更新 amber 色值微调（amber_500/700）
  - [ ] 更新红色 error 色值（red_600 #E53935 → #C62828）
  - [ ] 更新 surface 为暖白（surface_light #FFFBFE → #FFFDF5）
  - [ ] 更新 surface_variant 为暖灰（#F0EDE8 → #F5F0EB）
  - [ ] 更新 outline 为暖棕（#79747E → #A79B8E）
  - [ ] 更新 status_done/status_undone 对齐色板
  - [ ] 更新 danger 对齐 red_600 新值

- [ ] Task 2: 创建 dimens.xml 间距系统（与 Task 1 无依赖，可并行）
  - [ ] 定义 page_padding = 16dp
  - [ ] 定义 form_field_spacing = 16dp
  - [ ] 定义 card_content_padding = 16dp
  - [ ] 定义 button_margin_top = 24dp
  - [ ] 定义 list_item_padding_vertical = 12dp
  - [ ] 定义 list_card_margin = 4dp

- [ ] Task 3: 创建 type.xml 排版系统（与 Task 1 无依赖，可并行）
  - [ ] 定义 TextAppearance.MyAndroidPT.HeadlineSmall (22sp Bold)
  - [ ] 定义 TextAppearance.MyAndroidPT.TitleLarge (22sp Bold)
  - [ ] 定义 TextAppearance.MyAndroidPT.TitleMedium (18sp Medium)
  - [ ] 定义 TextAppearance.MyAndroidPT.BodyLarge (16sp Regular)
  - [ ] 定义 TextAppearance.MyAndroidPT.BodyMedium (14sp Regular)
  - [ ] 定义 TextAppearance.MyAndroidPT.LabelLarge (14sp Medium)
  - [ ] 定义 TextAppearance.MyAndroidPT.LabelSmall (12sp Regular)

## Phase 2: Styles & Theme

- [ ] Task 4: 增强 MyBtnStyle — 继承 Widget.Material3.Button（依赖 Task 1, 2, 3 完成）
  - [ ] 修改 MyBtnStyle parent 为 Widget.Material3.Button
  - [ ] 移除 background 属性（转交 M3 按钮控制）
  - [ ] 移除 cornerRadius（转交 M3 shape 控制）
  - [ ] 保留 android:textColor 和 android:textSize
  - [ ] 保留 btn_bg_selector.xml 文件（不删除，只不再引用）

- [ ] Task 5: 更新 themes.xml（依赖 Task 1, 3 完成）
  - [ ] 更新 values/themes.xml 中所有颜色引用以匹配新的 colors.xml
  - [ ] 在 Theme.MyAndroidPT 中添加 typeAppearance 引用
  - [ ] 更新 values-night/themes.xml 中所有颜色引用
  - [ ] 在 Base.Theme.MyAndroidPT 中添加 typeAppearance 引用

## Phase 3: Layout Updates

- [ ] Task 6: 改造注册页（activity_register.xml）（依赖 Task 1, 2, 4, 5）
  - [ ] 添加 ScrollView 包裹整个表单
  - [ ] 添加 MaterialCardView 容器（与登录页一致：12dp 圆角、2dp 阴影、16dp 内边距）
  - [ ] 添加密码强度指示器 TextInputLayout 辅助文本
  - [ ] 调整间距使用 @dimen/ 资源

- [ ] Task 7: 改造欢迎页（activity_welcome.xml）（依赖 Task 1, 2, 4, 5）
  - [ ] 标题改为 ?attr/colorOnSurface（去掉绿色）
  - [ ] 添加待办速览卡片区域（已完成/待完成计数）
  - [ ] 添加快速创建待办入口按钮
  - [ ] 调整间距使用 @dimen/ 资源

- [ ] Task 8: 更新待办列表页（activity_todo_list.xml）（依赖 Task 1, 2, 5）
  - [ ] 清理 RecyclerView padding 分散问题
  - [ ] FAB 图标 tint 改为 ?attr/colorOnPrimary

- [ ] Task 9: 修复 item_todo.xml（依赖 Task 1, 2, 5）
  - [ ] 移除冗余 app:cardCornerRadius（与 @style/ShapeSmall 冲突）
  - [ ] 或移除 @style/ShapeSmall 保留内联属性（选一种，不要并存）

- [ ] Task 10: 更新待办编辑页（activity_todo_edit.xml）（依赖 Task 1, 2, 5）
  - [ ] 标题输入框开启 counterEnabled + counterMaxLength=50

- [ ] Task 11: 更新待办详情页（activity_todo_detail.xml）（依赖 Task 1, 2, 5）
  - [ ] 删除按钮文字色改为 ?attr/colorError
  - [ ] 内层 MaterialCardView 改为带背景的 LinearLayout（减少嵌套）

- [ ] Task 12: 更新登录页（activity_main.xml）（依赖 Task 1, 2, 5）
  - [ ] 注册链接 tv_register 改为 layout_gravity="center_horizontal"
  - [ ] 调整颜色引用以匹配新的颜色体系

## Phase 4: Java Logic

- [ ] Task 13: 实现密码强度检测（RegisterActivity.java）（与 Task 14 无依赖）
  - [ ] 监听密码输入框文本变化（TextWatcher）
  - [ ] 实现三段强度逻辑：弱(<6字符) / 中(>=6单一类型) / 强(>=8混合)
  - [ ] 使用 TextInputLayout.setHelperText() 或 setError() 显示强度

- [ ] Task 14: 实现登录按钮 loading 状态（MainActivity.java）
  - [ ] 点击登录按钮 → disabled + "登录中..."
  - [ ] 登录完成 → enabled + "登录"

- [ ] Task 15: 实现保存按钮 loading 状态（TodoEditActivity.java）
  - [ ] 点击保存按钮 → disabled + "保存中..."
  - [ ] 保存完成 → enabled + "保存"

- [ ] Task 16: 实现欢迎页仪表盘（WelcomeActivity.java）（依赖 Task 7 布局完成）
  - [ ] 子线程查询 TodoDBHelper 获取待办统计
  - [ ] 更新 UI 显示已完成/待完成计数
  - [ ] 空数据处理

## Phase 5: Verification

- [ ] Task 17: 编译验证 — gradlew assembleDebug 通过
- [ ] Task 18: Lint 验证 — gradlew lint 无新增 warning
- [ ] Task 19: Checklist 验证 — 逐项检查 checklist.md

---

## Task Dependencies

```
Task 1 (colors.xml) ──────┐
Task 2 (dimens.xml) ──────┤──→ Task 4 (MyBtnStyle) → Task 5 (themes.xml)
Task 3 (type.xml) ────────┘          │
                                      ▼
                          ┌─── Task 6 (注册页) ─── Task 13 (密码强度)
                          ├─── Task 7 (欢迎页) ─── Task 16 (仪表盘 Java)
                          ├─── Task 8 (todo list)
                    Task 5─┼─── Task 9 (item_todo)
                          ├─── Task 10 (编辑页) ── Task 15 (保存按钮 Java)
                          ├─── Task 11 (详情页)
                          └─── Task 12 (登录页) ── Task 14 (登录按钮 Java)

Task 13/14/15/16 ──→ Task 17 (编译) → Task 18 (lint) → Task 19 (checklist)
```

Phase 1 (Task 1, 2, 3) can all run in parallel.
Phase 3 layouts (Task 6-12) can run in parallel after Task 5 completes.
Phase 4 Java (Task 13, 14) can run in parallel after their respective layouts.
Task 16 depends on Task 7's layout.
