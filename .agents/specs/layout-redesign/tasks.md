# Tasks

## 全局准备
- [ ] Task 0: 创建 feature/layout-redesign 分支
  - `git checkout -b feature/layout-redesign master`
  - 在单独分支上开发，与之前的 layout-polish 隔离

## Welcome 页重组
- [ ] Task 1: Welcome 页布局重写（activity_welcome.xml）
  - Toolbar: 背景改为 `?attr/colorSurface`，标题文字 `?attr/colorOnSurface`
  - 右侧添加 overflow 菜单（退出登录 + 设置预留）
  - 三个全宽按钮 → 横向操作卡片（待办数/完成数/新建，weight=1，白色卡片+轻微阴影）
  - 下半屏添加最近待办预览区（标题+时间，hairline 间隔，最多 3 条）
  - 无待办时显示空态引导文案
  - FAB 右下角
- [ ] Task 2: WelcomeActivity.java 逻辑更新
  - `loadDashboardData()` → 同时加载最近待办列表（limit 3）
  - 点击最近待办预览 → 跳转 TodoDetailActivity
  - Toolbar 菜单：退出登录 + 设置（预留）
  - Toolbar surface 背景适配

## 登录页重构
- [ ] Task 3: 登录页布局重写（activity_main.xml）
  - 去掉 Toolbar
  - 顶部品牌区：App icon (64dp) + 应用名 (22sp bold) + tagline (14sp, colorOnSurfaceVariant)
  - 输入框添加 leading icon（ic_person / ic_lock）
  - 两个 Checkbox 从 Card 中移出，独立一行
  - Card 只包裹两个输入框
  - 注册链接在登录按钮下方居中
- [ ] Task 4: 登录页资源准备
  - 新增 drawable: `ic_person`, `ic_lock`（使用 Material Icons）
  - 新增 string: `app_name_display`="MyAndroid", `app_tagline`="记录每一个完成时刻"
  - dimens.xml 新增 `gap_large` (32dp) 用于区块间距

## 注册页统一
- [ ] Task 5: 注册页布局（activity_register.xml）
  - 输入框添加 leading icon（ic_person / ic_lock / ic_email）
  - Toolbar 标题改为 "创建账号"
  - 其余保持现有结构

## 列表页交互升级
- [ ] Task 6: 列表项布局重写（item_todo.xml）
  - 移除删除按钮
  - 右侧添加状态指示（未完成=灰色圆点，已完成=绿色勾号）
  - 已完成项背景色轻微变化
  - 行间使用 hairline divider，不再用 Card 包裹
- [ ] Task 7: TodoListActivity.java 删除逻辑修改
  - 原先的 `btn_delete` 点击删除 → 改为长按列表项弹出 AlertDialog 确认删除
  - 保留 F4 删除功能，只改触发方式
- [ ] Task 8: TodoAdapter.java 适配
  - 适配更新后的 item_todo.xml（删除按钮移除，状态指示新字段）
  - 注册长按事件监听器

## 详情页美化
- [ ] Task 9: 详情页布局重写（activity_todo_detail.xml）
  - 状态从 TextView 改为 Material Chip（`com.google.android.material.chip.Chip`）
  - 编辑按钮移入 Toolbar 右侧
  - 内容区使用 Card（elevation=0dp, stroke=1dp）包裹
  - 删除按钮独立沉底（OutlinedButton, colorError）
  - 使用 Divider 分隔各内容区块
- [ ] Task 10: TodoDetailActivity.java 适配
  - 状态 Chip 的文本和颜色在 `loadTodo()` 中同步
  - Toolbar 编辑按钮 → TodoEditActivity
  - 删除按钮保留 AlertDialog 确认模式

## 编辑页精简
- [ ] Task 11: 编辑页布局（activity_todo_edit.xml）
  - 保存按钮移到 Toolbar 右侧
  - 移除独立取消按钮（用 Toolbar 返回替代）
  - 输入框添加 leading icon（ic_note / ic_description）
- [ ] Task 12: TodoEditActivity.java 适配
  - Toolbar 保存按钮 action
  - 移除 btn_cancel 逻辑

## 验证
- [ ] Task 13: 编译验证
  - `assembleDebug` 编译通过
  - `lint` 无新增警告
- [ ] Task 14: 回归测试
  - 登录功能正常
  - 注册功能正常
  - 待办 CRUD 正常（创建/查看/编辑/长按删除/状态切换）
  - Welcome 页菜单、横向卡片、最近待办预览正常
  - 退出登录功能正常
  - logcat 无异常

# Task Dependencies
- [Task 0] 依赖无（分支创建优先）
- [Task 1, 2] 互相依赖（Welcome 布局 + Java 逻辑）
- [Task 3, 4] 互相依赖（登录布局 + 资源）
- [Task 5] 无依赖
- [Task 6, 7, 8] 互相依赖（列表项布局 + Java + Adapter）
- [Task 9, 10] 互相依赖（详情布局 + Java）
- [Task 11, 12] 互相依赖（编辑布局 + Java）
- [Task 13] 依赖 [Task 1-12]
- [Task 14] 依赖 [Task 13]