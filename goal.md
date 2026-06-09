/goal 6 个 Activity 界面视觉验证全部通过、代码层面遗留问题全部关闭、Debug APK 可成功构建。

约束：
- 所有工作以 materials/ 中的实训教材和设计文档为北极星
- 只修复布局/代码问题，不处理报告和视频
- 代码规范遵守 AGENTS.md（线程模型、包结构、命名约定）
- 不得修改功能逻辑，仅修复视觉和代码质量问题

---

## 背景

项目处于"功能基本完成、视觉和代码质量待修"的阶段。上一阶段完成了两份报告和截图资产积累，但：
- MainActivity 对齐修复从未在修复后重新截图验证
- 6 个 Activity 的视觉验证未在本会话完成完整闭环
- 6月9日代码审查发现 30 个问题尚未处理

## 先决条件

Debug APK 构建退出码为 0（`cd app && gradlew.bat assembleDebug`）

## 工作阶段

### 第一阶段：已知问题修复

逐条修复以下列出类型的问题（不全重复 30 个，按类别处理）：

**布局/视觉类（需截图验证）**：
- activity_main.xml 对齐问题修复 —— 账号/密码标签与输入框对齐
- TodoAdapter 消息条宽度计算异常（62/113dp 固定值）
- TodoListActivity RecyclerView 顶部 padding 硬编码

**逻辑/运行时类**：
- WelcomeActivity 用户名显示为 null（初始化时序问题）
- 待办状态切换后的列表刷新时序

### 第二阶段：6 个 Activity 截图验证

使用 /android-cli skill 对每个 Activity 按以下流程截图：

1. 构建并安装 Debug APK（如已安装则跳过安装，或使用 `android run` 启动）
2. 用 `android screen capture` 截图
3. 自动锁定应用前台：`adb shell monkey -p com.ljx.pt -c android.intent.category.LAUNCHER 1`
4. 再次截图，确保为目标 Activity 界面
5. 保存截图到 reports/screenshots/（覆盖旧截图或命名新版本）
6. 逐项视觉检查：控件对齐、文字可读、按钮可点击、无重叠/截断

Activity 顺序：
1. MainActivity（登录页）
2. RegisterActivity（注册页）
3. WelcomeActivity（欢迎页）
4. TodoListActivity（待办列表，需确保 seedIfEmpty 有数据）
5. TodoEditActivity（编辑页）
6. TodoDetailActivity（详情页）

### 第三阶段：功能验证

在视觉验证通过的页面上验证以下功能路径：

- [ ] F1 创建：登录 → 欢迎页 → 待办列表 → FAB → 填写标题/内容 → 保存 → 列表可见新条目
- [ ] F2 查看：点击列表条目 → 进入详情页 → 所有字段正确展示（标题、内容、状态、时间）
- [ ] F3 编辑：详情页 → 编辑按钮 → 修改标题/内容 → 保存 → 列表更新
- [ ] F4 删除：详情页 → 删除按钮 → 确认弹窗 → 删除 → 列表刷新
- [ ] F5 状态切换：列表页 CheckBox 勾选 → 状态变更 → 详情页确认

### 第四阶段：构建验证

最终确认 `cd app && gradlew.bat assembleDebug` 退出码为 0。

## 完成标准

所有以下条件均为 true 时目标完成：
- 视觉验证截图无布局/样式问题（6 个页面）
- F1-F5 功能路径在最终构建上验证通过
- Debug APK 构建退出码 0
- 上一阶段已知问题的修复代码已提交
