---
name: adb-input-automation-solutions
description: Android 自动化测试中 adb 输入问题的解决方案汇总，避免重复踩坑
metadata:
  type: reference
---

## adb shell input text 已知限制

1. **不支持 Unicode 字符**（中文、日文、韩文等）：字符会被忽略
2. **textPassword inputType 下**：输入成功但不可见（掩码显示），某些 ROM 有额外安全限制
3. **特殊字符**：`@` 会导致 NPE，空格可能被截断，超长字符串有 ~1024 字符限制
4. **Oreo/Android P 之后**：UTF-8 文本字符串可能不被接受

## 解决方案（按推荐度排序）

### 方案 1：ADBKeyBoard（最推荐）

虚拟键盘 IME，通过广播 Intent 发送文本，兼容所有 inputType。

**安装**（一次性）：
```bash
# 下载 ADBKeyboard.apk（GitHub: senzhk/ADBKeyBoard），安装到设备
adb install ADBKeyboard.apk
# 启用并设为输入法
adb shell ime enable com.android.adbkeyboard.AdbIME
adb shell ime set com.android.adbkeyboard.AdbIME
```

**使用**：
```bash
# 发送纯文本
adb shell am broadcast -a ADB_INPUT_TEXT --es msg 'password123'
# 发送 Base64 编码（Android Oreo+ 推荐）
adb shell am broadcast -a ADB_INPUT_B64 --es msg '<base64编码>'
# 发送 Unicode 码点（emoji 等）
adb shell am broadcast -a ADB_INPUT_CHARS --eia chars '48,49,50,51,52'
# 发送按键事件（67=BACKSPACE, 66=ENTER）
adb shell am broadcast -a ADB_INPUT_CODE --ei code 67
# 清空当前输入框
adb shell am broadcast -a ADB_CLEAR_TEXT
```

**优点**：支持所有字符、密码字段有效、被 Appium/Maestro 广泛采用
**缺点**：需要安装 APK（一次性）

### 方案 2：Clipboard + Paste（纯 adb）

```bash
# 写入剪贴板
adb shell cmd clipboard set "your_password"
# 发送粘贴键
adb shell input keyevent 279
```

**优点**：无需安装 APK，对密码字段有效
**缺点**：Android 10+ 剪贴板 1 分钟自动清除

### 方案 3：am start --es 传参（跳过键盘）

在 Activity 的 onCreate 中读取 extra 并填充 EditText：

```java
// MainActivity.java onCreate 中
String userName = getIntent().getStringExtra("userName");
String password = getIntent().getStringExtra("password");
if (userName != null) etUsername.setText(userName);
if (password != null) etPassword.setText(password);
```

测试时：
```bash
adb shell am start -n "com.ljx.pt/.MainActivity" \
  --es "userName" "testuser" \
  --es "password" "testpass"
```

**优点**：完全绕过键盘输入，最快最可靠
**缺点**：需要修改 Activity 代码（仅限测试 build 启用）

### 方案 4：逐个 keyevent 输入数字

```bash
# KEYCODE_0=7, KEYCODE_1=8, ..., KEYCODE_9=16
adb shell input keyevent 8 8 8 8 8 8  # 输入 111111
adb shell input keyevent 66  # ENTER 确认
```

**优点**：系统原生，零依赖
**缺点**：仅适用于纯数字，速度慢

## 推荐组合方案

| 场景 | 方案 |
|------|------|
| 普通文本输入（用户名、标题） | 方案 1 ADBKeyBoard |
| 密码输入 | 方案 3 am start --es（最佳）或 方案 1 |
| 中文/emoji 输入 | 方案 1 ADB_INPUT_CHARS |
| 快速测试登录 | 方案 3 am start --es |
| 无 root 兜底 | 方案 2 Clipboard + Paste |

## 模拟器 vs 真机差异

- **模拟器**：默认硬件物理键盘，需关闭 `Settings -> Language & Input -> Physical Keyboard -> 关闭`
- **真机**：预装第三方输入法（搜狗、Gboard），需先切换输入法
- **ADBKeyBoard 可用性**：模拟器更稳定，真机部分 ROM 可能拦截

## 相关项目

- ADBKeyBoard: https://github.com/senzhk/ADBKeyBoard
- AdbClipboard: https://github.com/PRosenb/AdbClipboard
- adbeasykey: https://github.com/hansalemaos/adbeasykey
