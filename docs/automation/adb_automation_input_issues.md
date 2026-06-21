# Android 自动化输入问题汇总

> 本文档汇总了 MyAndroidPT 项目中所有 ADB/UiAutomator 自动化测试过程中遇到的输入相关问题。数据来源：75 条历史观测记录（50 observations + 25 sessions），时间跨度 2026-05-09 至 2026-06-15。

---

## 一、问题分类总览

| 分类 | 严重度 | 频率 | 状态 |
|------|--------|------|------|
| adb input text 无法写入 EditText | ���️ | 高频 | 部分解决（需换方案） |
| 密码字段不可写（Android 安全限制） | 🔴 | 高频 | 部分解决（ADBKeyBoard/am start --es） |
| 中文字符 NPE 崩溃 | 🔴 | 中频 | 已绕过（使用 Base64/chars 模式） |
| ADB 输入追加而非覆盖 | 🟡 | 中频 | 部分解决 |
| ADB 清空文本字段异常 | 🟡 | 中频 | 未完全解决 |
| 特殊字符问题（@、空格等） | 🟡 | 低频 | 已绕过 |
| 超长文本截断 | 🟡 | 低频 | 已绕过 |
| 坐标点击无响应 | 🟡 | 中频 | 已解决（精确坐标定位） |
| Activity 前台化困难 | 🟡 | 中频 | 已解决 |
| ADB 设备连接不稳定 | 🟢 | 低频 | 偶发 |

---

## 二、各问题详细记录

### 2.1 adb input text 无法写入 EditText（核心根因问题）

- **发生时间**: 2026-06-08 ~ 06-10
- **现象**: 使用 `adb shell input text "test"` 后，目标 EditText 内容为空
- **根因分析**: `adb shell input text` 不直接设置文本内容，而是通过系统 IME 模拟按键事件逐字发送。在某些应用实现中：
  - EditText 的 `InputType` 配置可能导致不接收按键事件形式的文本
  - 模拟器环境下与默认输入法兼容性不佳
  - 部分 ROM 存在额外的安全限制
- **影响范围**: 所有 EditText 输入框（登录、注册、待办标题、待办内容）
- **具体案例**:
  - TodoEditActivity 中，`adb shell input text` 写入 et_title 和 et_content 均失败，导致数据库插入 NOT NULL 约束违例，保存静默失败，列表始终为空
  - 视觉 API 验证时发现输入框始终为空
- **影响链**: 文本未写入 → 数据库保存失败 → TodoList 空列表 → 列表项点击无效（无 ViewHolder）
- **历史观测**: #11877（根因确认，75 条历史记录交叉验证）、#13349（向 et_title 输入中文标题）
- **当前状态**: 需要切换到 ADBKeyBoard 方案或 `am start --es` 传参方案

### 2.2 密码字段哑化（Android 安全限制）

- **发生时间**: 2026-06-09 ~ 06-15
- **现象**: `adb shell input text` 对 `inputType="textPassword"` 或 `numberPassword` 的字段输入成功但不可见（掩码显示），部分 ROM 下完全静默失败
- **根因**: Android 系统安全机制禁止非输入法应用向密码字段写入内容。`adb shell input text` 属于此类，被系统拦截
- **历史观测**:
  - #14686：密码输入框内容始终为空
  - #14738：密码输入验证失败
  - #14643/#14644：确认 Android 系统级限制
  - #14709/#14711：尝试 ADBKeyBoard 密码输入方案
- **当前状态**:
  - ADBKeyBoard 已安装并设为默认输入法，可以通过广播 `ADB_INPUT_TEXT` 输入密码（#14711 确认 result=0 执行成功）
  - 最佳方案：`adb shell am start -n "com.ljx.pt/.MainActivity" --es "userName" "xxx" --es "password" "xxx"` 通过 Intent Extra 直接传参

### 2.3 中文字符导致 NullPointerException

- **发生时间**: 2026-06-09
- **现象**: 使用 `adb shell input text "学习Android开发"` 或 `adb shell input text "完成实训报告和课程设计报告"` 时，Android 系统输入管理服务崩溃
- **根因分析**: 异常发生在 `com.android.server.input.InputShellCommand.sendText` 第 393 行，堆栈为 `sendText → runText → onCommand → exec → onShellCommand`，错误信息为 `java.lang.NullPointerException: Attempt to get length of null array`。`adb shell input text` 将字符串拆分为单个字符逐个发送 KeyEvent，在处理多字节 UTF-8/Unicode 字符时，设备端 `InputShellCommand` 未能正确处理数组，导致空指针异常
- **影响**: 中文字符输入完全不可用，包括标题和内容字段
- **历史观测**: #12491
- **解决方案**: 使用 ADBKeyBoard 的 `ADB_INPUT_B64` 模式（Base64 编码后传入）或 `ADB_INPUT_CHARS` 模式（直接传 Unicode 码点列表）。示例：`adb shell am broadcast -a ADB_INPUT_B64 --es msg "<base64编码>"`

### 2.4 ADB 清空文本字段异常

- **发生时间**: 2026-06-09
- **现象**: 尝试用 `adb shell input text ""` 或 `adb shell input text " "` 清空输入框失败
- **根因**: `adb shell input text ""` 抛出 `IllegalArgumentException: 要求提供非空文本`，空字符串和纯空格均不能作为清空手段。连续使用退格键删除后，字段仍残留内容（如 `student01dent01`）
- **影响**: 无法可靠地在重复自动化测试前清空已有文本
- **历史观测**: #12458
- **尝试的方案**:
  - 空字符串输入 → IllegalArgumentException
  - 连续退格键删除 → 残留内容（时序不可靠）
  - 滑动全选后删除 → 未生效（#12459）
- **当前状态**: 推荐使用 ADBKeyBoard 的 `ADB_CLEAR_TEXT` 广播（v2.0+），或通过 `am start --es` 传参覆盖

### 2.5 ADB 输入追加而非覆盖

- **发生时间**: 2026-06-09
- **现象**: 在已有文本的字段中输入新内容时，新文本追加到末尾而非替换原有内容。如原内容为 `testuser02`，输入 `pass123` 后变为 `testuser02pass123`
- **根因**: `adb shell input text` 发送按键事件时，如果 EditText 未处于「选中全部」状态，文本会被追加到光标位置。且 ADB 的 `swipe` 命令无法可靠触发全选高亮
- **具体案例**: #12459 中，尝试通过 `adb shell input swipe` 在账号字段内全选文本后输入 `student01`，结果字段内容变为 `student01dent01student01`（说明选择未生效 + 输入仍是追加模式）
- **历史观测**: #12459、#14602
- **解决方案**: 
  - 使用 ADBKeyBoard 的 `ADB_CLEAR_TEXT` 广播先清空，再输入
  - 或通过 `am start --es` 直接传完整的新值

### 2.6 特殊字符问题

- **发生时间**: 2026-05 ~ 2026-06
- **现象**: 
  - `@` 字符导致 NPE
  - 空格可能被截断
  - `&` 等特殊字符需要转义处理
  - 超长字符串有约 500~1024 字符限制
- **根因**: `adb shell input text` 内部将字符串拆分为单个字符逐字节处理，非 ASCII 字符需要特殊编码，超长字符串超出 IME 事件缓冲区
- **历史观测**: #14644、#14645
- **解决方案**: 
  - 特殊字符使用 `ADB_INPUT_B64` 模式（Base64 编码）
  - Emoji 等特殊字符使用 `ADB_INPUT_CHARS` 模式（传 Unicode 码点）
  - 超长文本使用 `ADB_INPUT_B64` 模式或写到本地 shell 文件后 `adb push` 执行

### 2.7 坐标点击无响应

- **发生时间**: 2026-05 ~ 2026-06
- **现象**: `adb shell input tap x y` 在预期坐标处点击无响应，界面不变
- **根因**: 
  - 坐标精度不足：偏差 20~30 像素即可导致点击区域偏移
  - 真机上坐标与模拟器不同
  - 部分视图（如 RecyclerView 子项）位置动态计算
- **具体案例**:
  - #11884：坐标 (540,131) 点击 RecyclerView 区域无响应
  - #12395：初始坐标 (630, 720) 未命中 tvRegister，正确坐标 [596,742] 直接命中后成功
- **解决方案**: 
  - 使用布局 dump JSON（`layout_main.json` 等）获取精确坐标
  - 真机坐标需单独标定（与模拟器不同）
  - RecyclerView 子项坐标需要通过 `dumpsys ui.decorview` 动态获取

### 2.8 Activity 前台化困难

- **发生时间**: 2026-06-09
- **现象**: 使用 `adb shell am start` 启动非 launcher Activity 时，系统将其路由到顶层实例，无法正确前台化
- **根因**: 
  - launchMode=singleTop 与非 MAIN/LAUNCHER intent category 冲突
  - `android run --activity` CLI 命令底层未正确传递 Activity 启动标志（FLAG_ACTIVITY_SINGLE_TOP）
  - 不带 category LAUNCHER 的 `am start` 更可靠
- **历史观测**:
  - #12408：`adb shell am start --activity-single-top -n` 成功，与 `android run --activity` 失败形成对比
  - #12395：通过精确坐标点击实现应用内导航（startActivity），比外部强制启动更可靠
  - #12424：移除 launchMode=singleTop 后，最简单的 `adb shell am start -n` 即可稳定前台化
- **解决方案**: 
  - 非 Launcher Activity 使用 `adb shell am start -n "com.ljx.pt/.ActivityName"`（无 flag、无 category）
  - 或用 `adb shell input tap` 触发应用内 startActivity
  - 统一使用此模式无需区分 LAUNCHER 和非 LAUNCHER Activity

### 2.9 ADBKeyBoard 方案

- **发生时间**: 2026-06-15
- **状态**: 已在模拟器上安装并设为默认输入法，四种输入模式全部可用
- **安装配置**:
  ```bash
  # 安装 ADBKeyBoard APK（一次性）
  adb install ADBKeyBoard.apk
  # 启用并设为默认输入法
  adb shell ime enable com.android.adbkeyboard.AdbIME
  adb shell ime set com.android.adbkeyboard.AdbIME
  ```
- **四种输入模式**:
  | 模式 | 命令 | 适用场景 |
  |------|------|----------|
  | ADB_INPUT_TEXT | `adb shell am broadcast -a ADB_INPUT_TEXT --es msg '文本'` | 纯 ASCII 文本 |
  | ADB_INPUT_B64 | `adb shell am broadcast -a ADB_INPUT_B64 --es msg '<base64>'` | Unicode/中文/特殊字符 |
  | ADB_INPUT_CHARS | `adb shell am broadcast -a ADB_INPUT_CHARS --eia chars '48,49,50,51,52'` | emoji 等（传 Unicode 码点） |
  | ADB_INPUT_CODE | `adb shell am broadcast -a ADB_INPUT_CODE --ei code 67` | 按键事件（67=退格, 66=回车） |
- **额外命令**:
  | 命令 | 用途 |
  |------|------|
  | `adb shell am broadcast -a ADB_CLEAR_TEXT` | 清空输入框（v2.0+） |
  | `adb shell am broadcast -a ADB_INPUT_MCODE --eia mcode '4096,62'` | 元键组合（Ctrl+Space 等） |
- **已知限制**: 
  - 模拟器更稳定，真机部分 ROM 可能拦截
  - 需要安装 APK（一次性成本）

### 2.10 剪贴板 + Paste 方案

- **状态**: 可行但有时限限制
- **命令**:
  ```bash
  adb shell cmd clipboard set "your_text"
  adb shell input keyevent 279  # 粘贴键
  ```
- **优点**: 无需安装 APK，对密码字段有效
- **缺点**: Android 10+ 剪贴板 1 分钟自动清除
- **历史观测**: #14645 提及 Clipper/am broadcast 剪贴板方案

### 2.11 am start --es 传参方案

- **状态**: 最有效方案，推荐首选
- **原理**: 在 Activity onCreate 中读取 Intent extra 并直接设置 EditText
- **命令**:
  ```bash
  adb shell am start -n "com.ljx.pt/.MainActivity" \
    --es "userName" "testuser" \
    --es "password" "testpass"
  ```
- **优点**: 完全绕过键盘输入，最快最可靠
- **缺点**: 需要修改 Activity 代码（`getIntent().getStringExtra()`），仅限测试 build 启用
- **历史观测**: #14643、#14647 提及 `am start-activity` 传参时带空格的字符串需引号包裹

---

## 三、已验证有效的方案汇总

### 方案对比矩阵

| 场景 | 推荐方案 | 可靠性 | 备注 |
|------|----------|--------|------|
| 普通文本输入（ASCII） | ADB_KEYBOARD BROADCAST | 高 | 需安装 APK |
| 中文/Unicode 输入 | ADB_INPUT_B64 | 高 | Base64 编码后传入 |
| Emoji/特殊字符 | ADB_INPUT_CHARS | 高 | 传 Unicode 码点 |
| 密码输入 | am start --es | 最高 | 需改代码；备选 ADBKeyBoard |
| 快速登录测试 | am start --es | 最高 | 完全绕过键盘 |
| 无 root 兜底 | 剪贴板+Paste | 中 | Android 10+ 1分钟过期 |
| 数字键盘输入 | input keyevent | 中 | 仅纯数字，慢速 |
| 清空输入框 | ADB_CLEAR_TEXT | 高 | v2.0+，备选 ADB_INPUT_CODE退格 |
| 非 Launcher Activity 前台化 | am start -n | 高 | 不带 flag/category |
| 精确 UI 操作 | 布局 dump + tap | 高 | 需标定精确坐标 |

### ADBKeyBoard 完整命令速查

已确认在模拟器（emulator-5554）上安装并设为默认输入法（#14702）。

```bash
# 检查当前输入法
adb shell ime list -a
adb shell settings get secure default_input_method

# 切换回默认输入法
adb shell ime set com.google.android.inputmethod.latin.AdbIME

# 清空输入框
adb shell am broadcast -a ADB_CLEAR_TEXT

# 输入退格（连续使用可实现删除）
adb shell am broadcast -a ADB_INPUT_CODE --ei code 67
```

---

## 四、仍待解决的问题

| 问题 | 原因 | 建议 |
|------|------|------|
| 真机上 ADBKeyBoard 稳定性 | 部分 ROM 拦截第三方 IME | 在真机上逐一验证各方案 |
| 真机坐标标定 | 分辨率/布局与模拟器不同 | 建立真机专用坐标映射 |
| RecyclerView 动态坐标 | 子项位置随滚动变化 | 结合视觉 API 或 dump 动态获取 |
| SharedPreferences 同步修改 | "spfRecord" 四字段散落两文件 | 提取为常量类统一管理 |
| 密码字段安全性 | 明文存储（教学用途） | 生产环境需加哈希 |
| `input keyboard text`（SDK 23+） | 系统原生但兼容性不明 | 可在 SDK 23+ 设备上测试 |

---

## 五、建议的研究方向

### 5.1 真机适配研究
- 真机上逐项验证 ADBKeyBoard 各模式的兼容性
- 建立真机坐标自动标定工具（通过 dump 实时计算）
- 测试 ADBKeyBoard 在不同 ROM（华为 EMUI、小米 MIUI、OPPO ColorOS）上的行为差异

### 5.2 自动化框架选型
- 评估 **Appium**：基于 UiAutomator2/Espresso Driver，原生支持 EditText setText，绕过 `adb shell input text` 限制
- 评估 **Maestro**：自带输入命令，基于 AccessibilityService，绕过密码字段限制
- 评估 **UIAutomator2 (Python)**：`driver(text="用户名").set_text("test")` 直接写入，无需键盘
- 评估 **AndroidX Test + ActivityScenarioRule**：代码级注入测试数据

### 5.3 ADB 增强方案
- 研究 **Rtiming/android-adb-automation-kit**：提供 `set_text()` + ADBKeyboard fallback + OCR 元素定位
- 研究 **Bob8259/Shell-Clipboard-Android**：自定义 IME 绕过剪贴板限制
- 探索 **AccessibilityService**：可编程控制输入法的无障碍服务方案

### 5.4 Google IssueTracker 追踪
- [#234737628](https://issuetracker.google.com/issues/234737628)：Android 12/13 中 `adb shell input text` 对国际字符支持退化
- [#207386157](https://issuetracker.google.com/issues/207386157)：请求为 `input` 命令提供 Unicode 原生支持
- 持续关注系统级修复进展

---

## 附录：术语表

| 术语 | 说明 |
|------|------|
| IME | Input Method Editor，输入法编辑器 |
| ADBKeyBoard | 虚拟键盘 APK，通过广播 Intent 发送文本 |
| `adb shell input text` | 系统内置文本输入命令，逐字发送 KeyEvent |
| `adb shell input tap` | 系统内置坐标点击命令 |
| `adb shell input swipe` | 系统内置滑动命令 |
| `adb shell input keyevent` | 系统内置按键事件命令 |
| `am broadcast` | 向 Android 系统发送广播，用于 ADBKeyBoard 通信 |
| `am start -n` | 直接指定组件名称启动 Activity |
| `am start --es` | 带 String extra 参数启动 Activity |
| `dumpsys window` | 查看窗口状态，含 mCurrentFocus |
| layout JSON | 应用布局层级结构的 JSON 导出文件 |

---

> **文档版本**: v1.0  
> **最后更新**: 2026-06-15  
> **数据来源**: 75 条历史记录（50 observations + 25 sessions），覆盖 50 个观测 ID
