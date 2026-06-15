# Android 自动化测试最佳实践

> 面向 MyAndroidPT 项目维护者的实操指南。目标：**照着这篇能跑通全流程，不踩我们踩过的坑。**

---

## 1. 30 秒快速启动

```bash
# 构建 & 安装
cd D:/Working/Code/Android/MyAndroidPT && ./gradlew assembleDebug && adb install -r -d app/build/outputs/apk/debug/app-debug.apk

# 清数据重来
adb shell pm clear com.ljx.pt

# 启动应用
adb shell monkey -p com.ljx.pt 1
```

---

## 2. 两套 UI 探测工具

### 方案 A: `android layout`（推荐，有坐标/中心点）

```bash
android layout --device emulator-5554 -p
```

输出示例：
```json
{"text":"登录","center":"[540,942]","resource-id":"btn_login"}
```

直接带 `center`，不用算坐标。**首选**。

### 方案 B: `uiautomator dump`（兜底，信息更全）

```bash
adb shell uiautomator dump --force-system-ui /sdcard/ui.xml
adb shell cat /sdcard/ui.xml
```

XML 格式，含 `bounds`、`class`、`resource-id`。**当 android layout 拿不到数据时用**。

> ⚠ 如果 android layout 报 `Failed to retrieve UI dump`，换 uiautomator dump。
> ⚠ 两个工具同时只能用其一（AccessibilityService 冲突），串行使用。

---

## 3. 输入方案决策树（最重要的章节）

```
要输入什么？
├── 纯 ASCII（a-z, 0-9, 不含 @# 等特殊字符）
│   └── adb shell input text "xxx"
│       ├── 对 text/textPassword inputType ✅ 有效
│       └── 对 numberPassword inputType ❌ 无效（字符被过滤）
│
├── 含 @ 的邮箱 / 特殊符号
│   ├── 方法1：adb shell input text "test" && 跳过 @ 只输入 "test.com"
│   └── 方法2：ADBKeyBoard 广播（见下方）
│
├── 中文 / Unicode
│   └── ADBKeyBoard Base64 模式
│
└── 密码字段自动化
    ├── 最佳：intent extra 传参（完全绕过键盘）
    │   └── 需要在 Activity onCreate 中 getIntent().getStringExtra()
    └── 次选：ADBKeyBoard ADB_INPUT_TEXT 广播
```

### 3.1 `adb shell input text` 的隐藏规则

| inputType | 能否输入 | 备注 |
|-----------|---------|------|
| `text` | ✅ 正常 | 首选 |
| `textPassword` | ✅ 但掩码显示 | 输入本身可用，不可见但你不在乎 |
| `numberPassword` | ❌ 字符被过滤 | **不要用**，除非只输数字 |
| `textEmailAddress` | ✅ 正常 | @ 会报 NPE，需要绕行 |

### 3.2 ADBKeyBoard 广播方案

```bash
# 安装（一次性）
adb install ADBKeyBoard.apk
adb shell ime enable com.android.adbkeyboard.AdbIME
adb shell ime set com.android.adbkeyboard.AdbIME

# 四种模式
adb shell am broadcast -a ADB_INPUT_TEXT --es msg 'test123'     # ASCII
adb shell am broadcast -a ADB_INPUT_B64 --es msg 'dGVzdDEyMw=='  # Base64（中文用）
adb shell am broadcast -a ADB_INPUT_CHARS --eia chars '116,101,115'  # Unicode 码点列表
adb shell am broadcast -a ADB_INPUT_CODE --ei code 67             # 按键（67=退格）

# 清空当前输入框
adb shell am broadcast -a ADB_CLEAR_TEXT
```

### 3.3 `inputType` 修改原则

**`inputType` 决定了 adb 能不能输入，务必为自动化留一条路：**

- 密码字段使用 `textPassword`（不要用 `numberPassword`）
- 如果业务要求纯数字，可通过代码层面加 `InputFilter` 而非改 inputType

在 `activity_main.xml` / `activity_register.xml` 中：

```xml
<!-- ✅ 自动化友好 -->
android:inputType="textPassword"

<!-- ❌ 自动化噩梦 -->
android:inputType="numberPassword"
```

### 3.4 清空字段的正确方式

```bash
# 方法1（推荐）：三击全选后覆盖
adb shell input tap <x> <y>    # 点击字段
adb shell input tap <x> <y>    # 再点一次（两击 = 选中）
adb shell input tap <x> <y>    # 三击 = 全选
adb shell input text 'newvalue'  # 覆盖

# 方法2：ADBKeyBoard 清空
adb shell am broadcast -a ADB_CLEAR_TEXT

# 方法3：退格键（不推荐，慢且不可靠）
for i in $(seq 1 20); do adb shell input keyevent 67; done
```

---

## 4. 坐标点击最佳实践

### 4.1 从 `android layout` 提取点击坐标

```bash
# 方法1：直接读取 center（推荐）
android layout --device emulator-5554 -p | python -c "
import sys, json
for line in sys.stdin:
    el = json.loads(line.strip())
    rid = el.get('resource-id', '')
    if rid == 'btn_login':
        cx, cy = [int(x) for x in el['center'].strip('[]').split(',')]
        print(f'tap {cx} {cy}')
"

# 方法2：从 uiautomator XML 解析
adb shell cat /sdcard/ui.xml | python -c "
import sys, re
content = sys.stdin.read()
m = re.search(r'btn_login[^>]*bounds=\"\[(\d+),(\d+)\]\[(\d+),(\d+)\]\"', content)
if m:
    cx = (int(m.group(1)) + int(m.group(3))) // 2
    cy = (int(m.group(2)) + int(m.group(4))) // 2
    print(f'tap {cx} {cy}')
"
```

### 4.2 常见控件参考坐标（1080×2400 模拟器）

| 页面 | 控件 | 中心坐标 |
|------|------|---------|
| 登录页 | 账号输入框 | (540, ~260) |
| 登录页 | 密码输入框 | (540, ~460) |
| 登录页 | 登录按钮 | (540, ~942) |
| 登录页 | 去注册链接 | (~861, ~1100) |
| 注册页 | 账号输入框 | (540, ~402) |
| 注册页 | 密码输入框 | (540, ~605) |
| 注册页 | 确认密码框 | (540, ~808) |
| 注册页 | 邮箱输入框 | (540, ~1011) |
| 注册页 | 注册按钮 | (540, ~2127) |
| 注册页 | 同意协议复选框 | (~155, ~2274) |
| 欢迎页 | 进入待办列表 | (540, ~463) |
| 欢迎页 | 退出登录 | (540, ~621) |
| 待办列表 | FAB 新增 | (~964, ~2284) |

> 注意：M3 TextInputLayout 的 EditText 在 uiautomator dump 中可能不暴露 `bounds`，正确方案是使用 `android layout` 获取 center 坐标。

### 4.3 基本点击节奏

```python
import time

def tap(x, y):
    subprocess.run(['adb', 'shell', 'input', 'tap', str(x), str(y)])

def type_text(text):
    subprocess.run(['adb', 'shell', 'input', 'text', text])

# 每个操作后等界面渲染完毕
tap(540, 402)      # 点击输入框
time.sleep(0.5)
type_text('test')  # 输入文字
time.sleep(0.5)
tap(540, 942)      # 点击按钮
time.sleep(2)      # 等 Activity 跳转完成
```

---

## 5. 数据库操作的常见坑

### 5.1 ❌ 错误：手动关闭 SQLiteDatabase

```java
// 错的！Cursor 还在迭代中，db 就已经 closed 了
public List<Todo> queryAll() {
    SQLiteDatabase db = getReadableDatabase();
    try {
        Cursor cursor = db.query(...);
        // ... 遍历 cursor
        cursor.close();
    } finally {
        db.close();  // ← 这一行会导致 IllegalStateException
    }
}
```

### 5.2 ✅ 正确：只关 Cursor，不关 Database

```java
// SQLiteOpenHelper 管理的 db 引用计数自动管理
public List<Todo> queryAll() {
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.query(...);
    try {
        while (cursor.moveToNext()) { ... }
    } finally {
        cursor.close();  // ✅ 只关 cursor
    }
    // ❌ 不调 db.close()
}
```

---

## 6. 双设备的坑

当同时运行两个模拟器时：

```bash
# 所有命令必须加 -s serial
adb -s emulator-5554 shell ...
adb -s emulator-5556 shell ...

# dumpsys / uiautomator 也必须指定设备
# 否则读到的是最后一个连接的设备，导致：
```

| 现象 | 原因 | 修复 |
|------|------|------|
| 明明点击了 FAB 但页面没变 | 连的是 emulator-5556，操作的是另一个应用 | 所有命令加 `-s emulator-5554` |
| 应用崩溃但 logcat 没日志 | 读错了设备的 logcat | `adb -s emulator-5554 logcat` |
| 自动登录不生效 | 数据在另一个设备上 | 确认 serial 一致 |

---

## 7. 完整的自动化测试骨架

```python
import subprocess, time, json

ADB = ['adb', '-s', 'emulator-5554']

def shell(cmd):
    subprocess.run(ADB + ['shell'] + cmd.split())

def tap(x, y):
    subprocess.run(ADB + ['shell', 'input', 'tap', str(x), str(y)])

def type_text(text):
    subprocess.run(ADB + ['shell', 'input', 'text', str(text)])

def layout():
    r = subprocess.run(['android', 'layout', '--device', 'emulator-5554', '-p'],
                       capture_output=True, text=True)
    items = [json.loads(l) for l in r.stdout.strip().split('\n') if l.strip()]
    return items

def find(rid):
    """按 resource-id 查找元素"""
    items = layout()
    for el in items:
        if el.get('resource-id') == rid:
            return el
    return None

# ===== 注册 =====
def register(name, psw, email):
    tap(861, 1100)  # 去注册
    time.sleep(2)
    tap(540, 402)   # 账号
    time.sleep(0.5)
    type_text(name)
    time.sleep(0.3)
    tap(540, 605)   # 密码
    time.sleep(0.5)
    type_text(psw)
    time.sleep(0.3)
    tap(540, 808)   # 确认密码
    time.sleep(0.5)
    type_text(psw)
    time.sleep(0.3)
    tap(540, 1011)  # 邮箱
    time.sleep(0.5)
    type_text(email)
    time.sleep(0.3)
    tap(155, 2274)  # 同意协议
    time.sleep(0.5)
    tap(540, 2127)  # 注册按钮
    time.sleep(3)

# ===== 登录 =====
def login(name, psw):
    tap(540, 402)
    time.sleep(0.5)
    type_text(name)
    time.sleep(0.3)
    tap(540, 605)
    time.sleep(0.5)
    type_text(psw)
    time.sleep(0.3)
    tap(540, 942)
    time.sleep(2)
    items = layout()
    return any(i.get('resource-id') == 'tv_welcome' for i in items)

# ===== 新增待办 =====
def create_todo(title, content):
    btn = find('fab_add')
    if btn:
        cx, cy = [int(x) for x in btn['center'].strip('[]').split(',')]
        tap(cx, cy)
    time.sleep(2)
    tap(540, 360)   # 标题
    time.sleep(0.5)
    type_text(title)
    time.sleep(0.3)
    tap(540, 1361)  # 内容
    time.sleep(0.5)
    type_text(content)
    time.sleep(0.3)
    tap(964, 2274)  # 保存
    time.sleep(2)
```

---

## 8. 核查清单

每次修改后跑全流程前，先检查：

- [ ] `pm clear` 清数据了吗？（不清可能导致注册冲突）
- [ ] 坐标是哪个设备/分辨率的？（1080×2400 模拟器还是真机？）
- [ ] `inputType` 是 `textPassword` 还是 `numberPassword`？（后者输不了字母）
- [ ] `adb shell` 命令加了 `-s serial` 吗？（双设备必加）
- [ ] 数据库操作不在 UI 线程吧？（`new Thread()` + `runOnUiThread()`）
- [ ] `Cursor` 关了吗？`db.close()` 关了吗？（只关前者，不关后者）
- [ ] 新 Activity 在 `AndroidManifest.xml` 声明了吗？
- [ ] 坐标系是 android layout 查的还是手算的？（不要手算，用工具）

---

## 9. 一句话原则总结

> **`inputType` 决定能不能输入，`android layout` 决定点在哪儿，`adb -s serial` 决定打哪个设备，`Cursor` 要关 `db` 不要关。**

---

*版本: 1.0 · 最后更新: 2026-06-15 · 基于 MyAndroidPT 项目实战经验*
