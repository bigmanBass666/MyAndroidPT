# 构建环境与测试相关问题与解决办法

## 问题 1：gradlew.bat 在 bash 环境中无法执行（跨平台兼容性）

- **现象**：在 Windows 的 Git Bash 环境中执行 `gradlew.bat` 返回 `command not found`；直接运行 .bat 文件导致 bash 无法解析批处理语法（如 `@rem` 注释），Gradle Wrapper 无法启动。
- **原因**：`gradlew.bat` 是 Windows 批处理脚本（cmd.exe 语法），bash shell（Git Bash / WSL）无法正确解析。项目根目录存在两个版本的 Gradle Wrapper 启动脚本：`gradlew.bat`（Windows 批处理）和 `gradlew`（Unix shell 脚本），不同 shell 环境需要选择对应的版本。
- **解决办法**：在 bash 环境（Git Bash、WSL、Cygwin）中执行 `./gradlew`（Unix shell 脚本版本），而非 `gradlew.bat`。在 Windows 原生 cmd 或 PowerShell 中使用 `gradlew.bat`。
- **涉及情景**：项目构建初始化、跨平台开发环境切换
- **代码/命令片段**：
  ```bash
  # bash 环境（Git Bash / WSL）— 使用 Unix 版本
  ./gradlew assembleDebug

  # Windows cmd / PowerShell — 使用批处理版本
  gradlew.bat assembleDebug
  ```

---

## 问题 2：JAVA_HOME 配置不当导致 Gradle 构建失败（JVM 版本不兼容）

- **现象**：`gradle assembleDebug` 构建失败，提示 JVM 版本不兼容错误。Gradle 守护进程无法启动。
- **原因**：Gradle 8.7.0 要求最低 JVM 11，但系统默认 PATH 指向 Java 8（JRE 1.8.0_481）。开发环境中虽有 JDK 17 和 JDK 21 可用，但在 Git Bash 中使用 bash 语法设置 PATH（如 `export PATH=...`）对 Windows 系统无效，导致 `java -version` 仍返回 Java 8。
- **解决办法**：在 PowerShell 中使用 `$env:` 语法正确设置环境变量；使用 `java -jar` 直接调用 Gradle Wrapper JAR 绕过 PATH 问题。
  - 最终构建命令（PowerShell）：
    ```powershell
    $env:JAVA_HOME = "D:\apps\Java\jdk-17"
    $env:PATH = "D:\apps\Java\jdk-17\bin;$env:PATH"
    java -jar gradle/wrapper/gradle-wrapper.jar assembleDebug
    ```
  - 构建成功输出：BUILD SUCCESSFUL in 16s，30 actionable tasks: 8 executed, 22 up-to-date
- **涉及情景**：项目首次构建、迁移到新开发机后构建失败

---

## 问题 3：gradle-wrapper.jar 被误当作可执行文件直接运行

- **现象**：尝试通过 bash 直接运行 `gradle/wrapper/gradle-wrapper.jar` 失败，系统报错无法执行。
- **原因**：`.jar` 文件是 Java 归档（ZIP 格式），本质上是需要由 JVM 解释的二进制包，不是 shell 可以执行的脚本。Gradle Wrapper 的标准入口是项目根目录的 `./gradlew`（shell 脚本）或 `gradlew.bat`（批处理），它们内部会正确处理 classpath 和 JVM 参数。
- **解决办法**：
  ```bash
  # 正确方式 1：使用 gradlew 脚本（推荐）
  ./gradlew assembleDebug

  # 正确方式 2：直接调用 java -jar（需已设置 JAVA_HOME）
  java -jar gradle/wrapper/gradle-wrapper.jar assembleDebug
  ```
- **涉及情景**：Gradle Wrapper 使用方式踩坑

---

## 问题 4：登录触发 ANR（应用无响应）

- **现象**：点击登录按钮后，应用立刻弹出 "Process system isn't responding" 系统 ANR 对话框，对话框包含 "Close app" 和 "Wait" 两个选项。应用窗口焦点丢失，系统进程连锁触发 ANR——在 30 秒内连续产生 6 次重复 ANR，间隔约 5 秒。ANR 日志原因均为 "Input dispatching timed out (Application does not have a focused window)"。ANR 临时文件写入 `/data/anr/` 目录。
- **原因**：登录按钮的点击事件（OnClickListener）中直接执行了同步网络请求（可能是 HTTPURLConnection 的同步调用或 OkHttp 的 execute() 而非 enqueue()），阻塞了主线程（UI 线程）。主线程阻塞超过 5 秒即触发 ANR。更复杂的是，由于应用无窗口焦点，系统进程在尝试弹出 ANR 对话框时也陷入无焦点等待状态，导致 ANR 被归因为 `system` 进程而非 `com.ljx.pt` 进程。
- **解决办法**：
  1. 将登录的网络请求移到后台线程执行（`new Thread()`、`AsyncTask` 或 HandlerThread）
  2. UI 更新通过 `runOnUiThread()` 或 `Handler()` 切回主线程
  3. 设置网络请求超时机制（如 OkHttp 的 connectTimeout / readTimeout）
  4. 在关键路径添加 Log 日志便于定位问题
- **涉及情景**：登录功能调试、Android 主线程阻塞排查

---

## 问题 5：应用无自定义日志输出，调试困难

- **现象**：过滤 `com.ljx.pt` 包名后，logcat 中无任何应用自定义日志输出（`Log.d` / `Log.e` / `Log.i`）。登录按钮点击后（时间戳 11:34:19）未触发任何应用日志记录。所有过滤到的日志均为系统框架级别（WindowManager、ActivityTaskManager）。对比之下，WelcomeActivity 已有正确的后台线程实现（`loadDashboardData` 使用 `new Thread()` 执行 SQLite 查询并通过 `runOnUiThread` 更新 UI），但同样无日志输出。
- **原因**：应用未在关键路径实现 Android `Log` 日志输出。具体缺失日志的位置包括：
  - 登录按钮点击事件
  - 网络请求发出（URL、请求参数）
  - API 响应回调（成功/失败状态码）
  - 页面跳转判断逻辑
- **解决办法**：在以下位置添加 `Log.d(TAG, ...)` / `Log.e(TAG, ...)`：
  ```java
  private static final String TAG = "LoginActivity";

  // 按钮点击
  Log.d(TAG, "登录按钮被点击");
  // 网络请求
  Log.d(TAG, "发起登录请求，用户名: " + username);
  // 请求结果
  Log.d(TAG, "登录成功，准备跳转到欢迎页");
  Log.e(TAG, "登录失败: " + errorMessage);
  ```
- **涉及情景**：全流程调试、问题定位能力不足

---

## 问题 6：ADB 自动化测试坐标偏移导致错误打开其他应用

- **现象**：自动化测试脚本通过 `adb shell tap 607 981` 点击底部 dock 栏的 MyAndroidPT 图标时，实际打开的是 Google Personal Safety 应用而非目标应用。
- **原因**：模拟器在不同状态下桌面布局可能变化（如启动器版本、分辨率、dpi、前台 Activity 状态），固定硬编码坐标 (607,981) 未命中目标图标中心点。基于固定坐标的 ADB 测试方案本质上是脆弱的——任何 UI 布局变化都会导致坐标失效。
- **解决办法**：改用 Intent 方式启动应用，完全避免依赖 UI 坐标：
  ```bash
  # 直接通过 Intent 启动，不依赖 UI 点击坐标
  adb shell am start -n com.ljx.pt/.MainActivity

  # 或先启动再通过 monkey 保持前台
  adb shell monkey -p com.ljx.pt -c android.intent.category.LAUNCHER 1
  ```
- **涉及情景**：ADB 自动化测试脚本编写、自动化回归测试

---

## 问题 7：ADB 自动化测试误用 keyevent 错误码

- **现象**：Python 自动化脚本使用 `adb shell input keyevent 53` 发送 6 次按键来输入密码 "111111"，但密码字段未被正确填充。
- **原因**：keyevent 53 对应的是 `KEYCODE_ENTER`（回车键），而非数字键 1（`KEYCODE_1` 的代码是 8）。实际执行的操作是向密码输入框发送了 6 次回车键，而不是输入数字 "111111"。
- **解决办法**：使用 `adb shell input text` 直接输入文本更可靠；如需逐个按键需核对 keyevent 映射表。
  ```bash
  # 推荐方式：直接输入文本
  adb shell input text 111111

  # 如需逐个按键（正确 keycode）
  adb shell input keyevent 8   # KEYCODE_1
  adb shell input keyevent 8   # KEYCODE_1
  adb shell input keyevent 8   # KEYCODE_1
  adb shell input keyevent 8   # KEYCODE_1
  adb shell input keyevent 8   # KEYCODE_1
  adb shell input keyevent 8   # KEYCODE_1
  ```
- **涉及情景**：ADB 自动化测试文本输入、keyevent 映射知识

---

## 问题 8：`android run` 和 `am start` 无法让应用保持前台焦点

- **现象**：使用 `am start -n com.ljx.pt/.MainActivity` 或 `android run --activity` 启动 Activity 后，应用立即让出前台焦点回到系统桌面。`dumpsys window displays` 中 `mCurrentFocus` 无法持续指向应用 Activity，短暂显示 Activity 后回落到系统启动器。
- **原因**：单纯使用 `am start` 启动 Activity 时，应用可能因为任务栈标志位、启动器配置或代码中 `onCreate` 的异常流程而无法稳固占据前台焦点。模拟器环境下，系统启动器和 Activity 之间的焦点切换机制也增加了不稳定性。
- **解决办法**：组合方案——先发送 KEYCODE_HOME 回到桌面，再用 monkey LAUNCHER category 重新启动：
  ```bash
  # Step 1: 回到桌面
  adb shell input keyevent KEYCODE_HOME

  # Step 2: 通过 LAUNCHER category 重新启动，创建新任务栈
  adb shell monkey -p com.ljx.pt -c android.intent.category.LAUNCHER 1
  ```
  monkey LAUNCHER 方案会创建一个新的任务栈，确保应用稳定占据前台。该方案对 5/6 个 Activity 有效（MainActivity、RegisterActivity、WelcomeActivity、TodoListActivity、TodoEditActivity）。TodoDetailActivity 因需要携带 `extra_todo_id` 数据才能正常启动，是已知例外。
- **涉及情景**：Android 自动化测试 Activity 启动与前台保持

---

## 问题 9：git add 父级目录污染（多项目混入）

- **现象**：项目根目录 `D:\Working\Code\Android\MyAndroidPT` 没有自己的 `.git` 目录，而是由父级目录 `D:\Working\.git` 统一管理。执行 `git add -A` 会将整个工作区（包含 `D:\Working` 下其他完全不相关的项目）的变更全部纳入暂存区，导致提交内容混杂。
- **原因**：Git 仓库在父级目录初始化，子目录项目未通过 `git init` 或 `git submodule` 隔离。`git add -A` 的作用范围是整个 Git 工作区，而非当前目录。
- **解决办法**：显式逐文件 `git add`，精准控制纳入版本控制的文件：
  ```bash
  # 分批显式添加，不依赖 -A 通配
  git add .gitignore AGENTS.md build.gradle.kts settings.gradle.kts gradle.properties gradle/wrapper/
  git add app/build.gradle.kts app/proguard-rules.pro app/src/main/ app/src/test/ app/src/androidTest/
  ```
  这种方式虽然繁琐，但能实现对暂存区的精确控制，完全隔离无关项目文件。
- **涉及情景**：项目初始化、版本控制配置

---

## 问题 10：copy-to-clipboard.ps1 中文文件名路径解析失败

- **现象**：copy-to-clipboard.ps1 脚本在连续复制多个文件时，英文文件名的 `goal.md` 和 `AGENTS.md` 复制成功，但中文文件名 "教材大纲审查-正式计划.md" 报告 "File not found"，复制失败。
- **原因**：PowerShell 子进程（通过 `-File` 参数启动）的工作目录可能与预期不同，导致 `Resolve-Path` 对混合风格路径（反斜杠 + 正斜杠）解析不一致；同时中文文件名在跨进程命令行参数传递过程中可能存在编码问题（PowerShell 控制台编码默认为 GBK，而脚本可能使用 UTF-8）。
- **解决办法**：
  1. 为包含中文的文件路径添加双引号包裹
  2. 使用绝对路径而非依赖相对路径解析
  3. 在自动化脚本中优先使用英文文件名，避免跨平台编码转换
  4. 检查并统一 PowerShell 控制台的输出编码（`[Console]::OutputEncoding` 和 `$OutputEncoding`）
- **涉及情景**：自动化脚本开发、中文路径处理
