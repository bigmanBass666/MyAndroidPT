# Android 自动化测试最佳实践

> 面向 MyAndroidPT 项目维护者的实操指南。目标：**照着这篇能跑通全流程，不踩我们踩过的坑。**

涵盖两套方案：
- **方案 A：纯 ADB 方案** — 只用 `adb shell` + `android layout`，零依赖
- **方案 B：uiautomator2 Python SDK** — 高级 API，适合复杂交互场景

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

## 2. 方案选择指南

| 场景 | 推荐方案 | 理由 |
|------|---------|------|
| 快速验证、手动点几下 | **方案 A：ADB** | 零依赖，一条命令搞定 |
| CI/CD 全流程回归 | **方案 B：uiautomator2** | 结果稳定，可复用 |
| 需要截图/录屏 | **方案 B：uiautomator2** | 原生 API 支持 |
| 密码框输入 | **方案 A：ADB**（`input text`） | uiautomator2 `set_text()` 在 SDK 36 崩溃 |
| M3 TextInputLayout 定位 | **方案 B：uiautomator2** | 通过 `instance` 索引定位 |
| 脚本运行环境受限（无 Python） | **方案 A：ADB** | 纯 shell 就能跑 |
| 需要元素级等待/重试 | **方案 B：uiautomator2** | 内置 wait 机制 |
| 复杂的多步用户流程 | **方案 B：uiautomator2** | 代码可读性和可维护性更好 |

---

## 方案 A：纯 ADB 方案

### 3A. 两套 UI 探测工具

#### 3A.1 `android layout`（推荐，有坐标/中心点）

```bash
android layout --device emulator-5554 -p
```

输出示例：
```json
{"text":"登录","center":"[540,942]","resource-id":"btn_login"}
```

直接带 `center`，不用算坐标。**首选**。

#### 3A.2 `uiautomator dump`（兜底，信息更全）

```bash
adb shell uiautomator dump --force-system-ui /sdcard/ui.xml
adb shell cat /sdcard/ui.xml
```

XML 格式，含 `bounds`、`class`、`resource-id`。**当 android layout 拿不到数据时用**。

> ⚠ 如果 android layout 报 `Failed to retrieve UI dump`，换 uiautomator dump。
> ⚠ 两个工具同时只能用其一（AccessibilityService 冲突），串行使用。

---

### 4A. 输入方案决策树（最重要的章节）

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

#### 4A.1 `adb shell input text` 的隐藏规则

| inputType | 能否输入 | 备注 |
|-----------|---------|------|
| `text` | ✅ 正常 | 首选 |
| `textPassword` | ✅ 但掩码显示 | 输入本身可用，不可见但你不在乎 |
| `numberPassword` | ❌ 字符被过滤 | **不要用**，除非只输数字 |
| `textEmailAddress` | ✅ 正常 | @ 会报 NPE，需要绕行 |

#### 4A.2 ADBKeyBoard 广播方案

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

#### 4A.3 `inputType` 修改原则

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

#### 4A.4 清空字段的正确方式

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

### 5A. 坐标点击最佳实践

#### 5A.1 从 `android layout` 提取点击坐标

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

#### 5A.2 常见控件参考坐标（1080×2400 模拟器）

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

---

### 6A. 完整的自动化测试骨架（ADB 方案）

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

## 方案 B：uiautomator2 Python SDK 方案

### 3B. 环境配置

#### 3B.1 MCP（Claude Code 内使用）

```json
{
  "mcpServers": {
    "u2": {
      "type": "stdio",
      "command": "uvx",
      "args": ["uiautomator2-mcp-server", "stdio"],
      "env": {
        "PYTHONUNBUFFERED": "1"
      }
    }
  }
}
```

关键点：
- **必须加 `stdio` 子命令**（v0.2.0+ 要求）
- **Windows 必须加 `PYTHONUNBUFFERED=1`**，否则 MCP init 超时
- 不要用 HTTP 模式，需要手动启 server 且容易端口冲突

#### 3B.2 Python SDK（脚本内使用）

```bash
pip install uiautomator2
```

连接：
```python
import uiautomator2 as u2

d = u2.connect('emulator-5556')  # 指定设备序列号
```

---

### 4B. 设备管理

#### 4B.1 模拟器隔离

| 场景 | 做法 |
|------|------|
| 共享模拟器（另一个 AI 在用） | **必须另起一个**，避免 UIA 服务被锁 |
| 新模拟器 UIA 服务锁死 | 杀掉 `com.github.uiautomator` 进程不一定有用，重启模拟器最可靠 |
| `uiautomator dump` 不返回数据 | 大概率 UIA 服务被占用，换模拟器或重启 |

#### 4B.2 自启动新模拟器

```bash
# 创建独立 AVD 数据目录，避免与已有实例数据冲突
emulator -avd Pixel_6_API_36 -port 5556 -no-snapshot -wipe-data
```

---

### 5B. 文本输入（核心痛点）

#### 5B.1 决策树

```
需要输入文本？
├── 是非密码框 (EditText with password="true")？
│   ├── 是 → click() + send_keys()
│   └── 否 → set_text() 或 click() + send_keys()
│
├── 元素有 resource-id？
│   ├── 是 → d(resourceId='com.ljx.pt:id/et_xxx').set_text('xxx')
│   └── 否 → d(className='android.widget.EditText', instance=N).xxx
│
└── 有 hint 文本？
    └── d(text='请输入xxx').click() + send_keys('xxx')
```

#### 5B.2 SDK 36 上的限制

| 方法 | 普通 EditText | 密码框 |
|------|-------------|--------|
| `set_text()` | ✅ 可用 | ❌ 崩溃（`InputManager.getInstance()` 已移除）|
| `send_keys()` | ✅ 可用 | ✅ 可用 |
| `ADBKeyBoard` | ❌ TextInputLayout 阻断 | ❌ TextInputLayout 阻断 |
| `clipboard + paste` | ❌ 不稳定 | ❌ 不稳定 |

> `send_keys()` 背后的原理：uiautomator2 会安装 ATX Agent APK，通过无障碍服务模拟按键，不依赖 `InputManager`。

#### 5B.3 resouce-id + instance 定位

M3 TextInputLayout 包裹的 EditText 在布局中不会暴露 resource-id，只能用 instance 索引：

```python
# 注册页 4 个字段（按出现顺序）
d(className='android.widget.EditText', instance=0).click()  # 账号
d(className='android.widget.EditText', instance=1).click()  # 密码
d(className='android.widget.EditText', instance=2).click()  # 确认密码
d(className='android.widget.EditText', instance=3).click()  # 邮箱
```

#### 5B.4 自动回填陷阱

```python
# ❌ 错误：注册成功后回到登录页，密码已被自动填充
d(resourceId='com.ljx.pt:id/et_password').click()
d.send_keys('Test123456')
# 结果：密码变成 "已填充的值" + "Test123456" → 登录失败

# ✅ 正确：先 clear_text()
d(resourceId='com.ljx.pt:id/et_password').click()
d.clear_text()  # 或 send_keys 前手动清空
d.send_keys('Test123456')
```

> 注意：`clear_text()` 底层调用 `set_text('')`，在 SDK 36 密码框上也会崩溃。安全做法：使用 `d(resourceId='xxx').set_text('Test123456')` 直接覆盖，或者 `click()` 后 `send_keys()`（如果无预填内容）。

---

### 6B. 密码强度校验

```python
# 注册页校验规则：
# - 长度 ≥ 6
# - 必须字母 + 数字混合
password = 'Test123456'   # ✅
password = '123456'       # ❌ 纯数字，校验不通过
password = 'abcdef'       # ❌ 纯字母，校验不通过
```

若注册失败（页面不跳转），优先排查密码强度。

---

### 7B. 全流程测试模式

```python
import uiautomator2 as u2
import time

d = u2.connect('emulator-5554')

def log(msg): print(f'  [{time.strftime("%H:%M:%S")}] {msg}')
def shot(): d.screenshot(f'test_{int(time.time())}.jpg')

# 1. 重启 app（确保干净状态）
d.app_stop('com.ljx.pt')
time.sleep(1)
d.app_start('com.ljx.pt', wait=True)
time.sleep(2)

# 2. 注册
d(text='暂无账号？立即注册').click()
d(className='android.widget.EditText', instance=0).click(); d.send_keys('testuser')
d(className='android.widget.EditText', instance=1).click(); d.send_keys('Test123456')
d(className='android.widget.EditText', instance=2).click(); d.send_keys('Test123456')
d(className='android.widget.EditText', instance=3).click(); d.send_keys('test@test.com')
d(text='同意用户协议').click()
d(text='注册').click()

# 3. 登录
d(text='请输入用户名或手机号').click(); d.send_keys('testuser')
d(resourceId='com.ljx.pt:id/et_password').click(); d.send_keys('Test123456')
d(text='登录').click()

# 4. 进入待办列表
d(textContains='待办').click()

# 5. 新增待办（FAB）
d(description='新增待办').click()
d(className='android.widget.EditText', instance=0).click(); d.send_keys('标题')
d(className='android.widget.EditText', instance=1).click(); d.send_keys('内容')
d(text='保存').click()
```

### 关键等待策略

| 操作 | 等待方式 |
|------|---------|
| app 启动 | `d.app_start('pkg', wait=True)` + `time.sleep(2)` |
| Activity 跳转 | `btn.wait(timeout=5)` + `time.sleep(2)` |
| 列表加载 | `RecyclerView.child(instance=0).wait(timeout=3)` |
| 保存/删除 | `time.sleep(2)` — 数据库操作异步 |

- 能用 `.wait(timeout=N)` 的优先用（比 sleep 快）
- 翻页/跳转后至少 `sleep(1)`，模拟器有动画延迟
- 增删改后至少 `sleep(2)`，等数据库写入

---

## 7. 数据库操作的常见坑

### 7.1 ❌ 错误：手动关闭 SQLiteDatabase

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

### 7.2 ✅ 正确：只关 Cursor，不关 Database

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

## 8. 双设备的坑

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

## 9. 常见问题快速诊断

| 现象 | 最可能原因 | 解决 |
|------|-----------|------|
| 点击后页面无反应 | 连错了模拟器 | 检查 adb 命令是否加 `-s serial` |
| 应用崩溃闪退 | `db.close()` 关闭过早 | SQLiteOpenHelper 只关 cursor，不关 db |
| `adb shell input text` 输不了字母 | `inputType="numberPassword"` | 改为 `textPassword` |
| `set_text()` 抛 NoSuchMethodError | SDK 36 `InputManager.getInstance()` 被移除 | 改用 `send_keys()` |
| 密码框输入后显示为空 | SDK 36 密码框无视觉反馈 | 抓屏确认 |
| 登录失败，账号不存在 | 注册时密码强度校验未通过 | 检查密码 ≥6 位 + 字母数字混合 |
| 登录失败，密码错误 | 自动回填导致密码被追加 | 用 `set_text()` 覆盖而非 `send_keys()` |
| MCP uiautomator2 连接超时 | Windows stdout 缓冲 | 加 `PYTHONUNBUFFERED=1` |
| UI dump 为空 | UIA 服务被占用 | 检查有无其他 uiautomator 进程 |
| 元素找不到 | TextInputLayout 隐藏了 resource-id | 用 instance 索引定位 |
| 复选框点不了 | 坐标被 TextInputLayout 遮挡 | 用 `d(text='文本').click()` |
| 数据库 IllegalStateException | Cursor 迭代中 db 被关闭 | 删除 `finally { db.close() }` |
| 新 Activity 启动崩溃 | 未在 AndroidManifest.xml 声明 | 检查 manifest |

---

## 10. 核查清单

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

## 11. 一句话原则总结

> **`inputType` 决定能不能输入，设备 serial 决定打哪个设备，`Cursor` 要关 `db` 不要关。**

ADB 方案：**`adb -s serial shell input text`** — 三个要素缺一不可。
uiautomator2 方案：**密码框用 `send_keys()`，普通框用 `set_text()`，TextInputLayout 用 instance 索引。**

---

*版本: 2.0 · 合并自 ADB 方案 + uiautomator2 方案 · 基于 MyAndroidPT 项目实战经验*