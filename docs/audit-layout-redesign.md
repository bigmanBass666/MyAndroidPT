# Layout Redesign — 回归测试审计报告

> 审计日期：2026-06-22
> 分支：feature/layout-redesign
> 测试环境：Medium_Phone_API_36.1 (emulator-5554)
> 测试范围：全功能回归 + 视觉评估

---

## 截图证据

| 截图 | 文件 | 状态 |
|------|------|------|
| ① 注册页 | `01-login.png` → 实际为注册页 | ✅ |
| ② 注册页（带字段） | `02-register.png` | ✅ |
| ③ 注册后回登录页 | `03-after-register.png` | ✅ |
| ④ 欢迎页（v2） | `04-welcome-v2.png` | ✅ |
| ⑤ 创建待办后 | `07-after-create.png` | ✅ |
| ⑥ 已完成状态 | `10-todo-done.png` | ✅ |
| ⑦ 删除后空态 | `11-after-delete.png` | ✅ |
| ⑧ 退出登录后 | `12-after-logout.png` | ✅ |
| ⑨ 自动登录验证 | `14-auto-login-1.png` | ⚠️ 见下方说明 |

---

## 视觉评估（基于 free-vision 分析）

### 登录页 ✅
- **品牌标识区**："MyAndroid" + "记录每一个完成时刻" tagline ✅
- **无 Toolbar**，品牌区替代 ✅
- **Leading icon**：账号框有用户图标，密码框有锁图标 ✅
- **Checkbox 独立**：在 Card 外单独一行 ✅
- **视觉重心**：品牌区在上，输入区居中，登录按钮锚定，底部留白 ✅

### 欢迎页 ✅
- **Toolbar**：surface 背景（非绿色），标题 "MyAndroidPT"，右侧 overflow 菜单 ✅
- **横向操作卡片**："已完成"、"待完成"、"新建" 三个卡片水平排列 ✅
- **最近待办预览**：显示已创建的待办 "Test" + 时间 ✅
- **空态引导**：无待办时显示 "还没有待办，点击 + 新建一个" ✅
- **FAB**：右下角，绿色 ✅
- **退出登录**：在 Toolbar 菜单中（待办列表/设置/退出登录） ✅

### 详情页 ✅
- **状态 Chip**："已完成" 和 "未完成" 通过 Chip 组件展示 ✅
- **CheckBox**：状态切换正常工作 ✅
- **内容分隔**：使用 Divider 分隔各区块 ✅
- **删除按钮**：红色 OutlinedButton 独立沉底 ✅
- **编辑按钮**：在 Toolbar 右侧 ✅

### 编辑页 ⚠️
- **保存按钮**：在 Toolbar 右侧，可点击 ✅
- **无独立取消按钮**：通过 Toolbar 返回替代 ✅
- **Leading icon**：已添加到标题和内容输入框 ✅
- **Toolbar 标题**：显示为 "新增待办"（但在某些状态下显示 "MyAndroidPT"，存在不一致）⚠️

---

## 功能回归测试

| # | 功能 | 结果 | 详细 |
|---|------|------|------|
| 1 | 登录页品牌展示 | ✅ PASS | "MyAndroid" + tagline 正常显示，无 Toolbar |
| 2 | 注册 → 登录表单自动回填 | ✅ PASS | 注册成功后账号密码自动填入登录页 |
| 3 | 登录（账号+密码+勾选） | ✅ PASS | 成功跳转到欢迎页 |
| 4 | 欢迎页横向操作卡片 | ✅ PASS | 三个卡片正常显示 |
| 5 | 创建待办 | ✅ PASS | Test 待办标题+内容成功保存 |
| 6 | 最近待办预览 | ✅ PASS | 待办在欢迎页下半屏显示标题和时间 |
| 7 | 待办详情查看 | ✅ PASS | Chip 状态、内容、时间正常显示 |
| 8 | 状态切换（未完成↔已完成） | ✅ PASS | Chip 文本从 "未完成" → "已完成" |
| 9 | 删除待办 | ✅ PASS | AlertDialog 确认 → 删除成功 → 显示空态 |
| 10 | 欢迎页 Toolbar 菜单 | ✅ PASS | 待办列表/设置/退出登录 三个选项 |
| 11 | 退出登录 | ✅ PASS | 回到登录页，无已保存凭证 |
| 12 | 自动登录 | ⚠️ 部分通过 | 登录勾选记住密码+自动登录后可正常回访，但 force-stop 重启后自动登录未触发 |
| 13 | Leading icon 显示 | ✅ PASS | 所有输入框显示对应图标（person/lock/email/note/description） |
| 14 | 空态引导 | ✅ PASS | 无待办时显示引导文案 |

---

## 日志分析

| 检查项 | 结果 |
|--------|------|
| FATAL/CRASH | 0 |
| ANR | 0 |
| Exception | 0 |
| SQLite 错误 | 0 |
| HWUI 警告 | 1（`Failed to initialize 101010-2 format` — 模拟器 GPU 通用问题，非 app 相关） |

## 变更统计

```
12 个源文件修改，732 行新增，477 行删除
```

| 页面 | 文件 | 行数变化 |
|------|------|---------|
| Welcome | activity_welcome.xml + WelcomeActivity.java | +296/-116 |
| Login | activity_main.xml | +112/-59 |
| Register | activity_register.xml | +16/-6 |
| Todo List | item_todo.xml + TodoAdapter.java | +39/-15 |
| Detail | activity_todo_detail.xml + TodoDetailActivity.java | +293/-182 |
| Edit | activity_todo_edit.xml + TodoEditActivity.java | +52/-56 |
| Resources | strings.xml + 6 new drawables + menu files | +13 new files |

---

## 结论

**整体评估：PASS** ✅

核心功能（注册、登录、创建、查看、编辑（通过保存验证）、删除、状态切换、退出登录）全部正常工作。布局结构已按设计文档重组。

已确认的问题：
1. **自动登录**在 app force-stop 后未触发 — 可能涉及 SharedPreferences 的键名对齐问题，需要进一步排查
2. **编辑页 Toolbar 标题**在某些状态下显示 "MyAndroidPT" 而非 "新建待办" — 标题设置需追踪
3. **横向卡片缺少统计数字** — 卡片只显示了标签文字（已完成/待完成/新建），未显示实际计数

推荐在合并到 master 前至少修复 #1（自动登录是教材中的可选加分功能，不修复不影响核心评分）。
