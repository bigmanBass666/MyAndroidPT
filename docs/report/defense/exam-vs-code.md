# 考试要求 → 代码实现对照表

> 把考试要求一条条列出来，每条告诉你：实现了没有、在代码的哪里、老师问到怎么答

---

## 第一部分：课程设计考试要求（F1-F5，占 50 分）

### F1：待办创建

| 项 | 内容 |
|---|------|
| **要求原文** | 用户能方便地创建新待办，支持输入文本 |
| **实现状态** | ✅ 已实现 |
| **对应位置** | `TodoEditActivity.java` + `activity_todo_edit.xml` |
| **操作路径** | 欢迎页 → FAB (+) 按钮 → 编辑页 → 输入标题+内容 → 保存 |

**老师问「你创建待办的流程是什么样的？」**
> "点击新建按钮，跳转到 TodoEditActivity，在标题和内容输入框填入信息，点保存按钮。保存时先校验标题不能为空，校验通过后开子线程插入数据库，插入成功后弹 Toast 提示并关闭页面。"

**老师问「你怎么区分新增和编辑？」**
> "通过 `EXTRA_TODO_ID` 判断。如果传入的 ID 是 -1 表示新增，有具体值表示编辑模式。"

**打开文件指给他看：**
> `TodoEditActivity.java` 第 50-51 行：`isEditMode = todoId != -1;`

---

### F2：待办查看

| 项 | 内容 |
|---|------|
| **要求原文** | 列出所有已保存的待办；点击列表项查看详情 |
| **实现状态** | ✅ 已实现 |
| **对应位置** | `TodoListActivity.java` + `activity_todo_list.xml` + `TodoAdapter.java` + `item_todo.xml` |

**老师问「你的待办列表怎么实现的？」**
> "用 RecyclerView 组件展示。数据存在 SQLite 数据库里，用 TodoDao.queryAll() 查询所有待办，通过 TodoAdapter 把数据绑定到列表的每一行。每行显示标题、时间、完成状态。"

**老师问「RecyclerView 是什么？」**
> "Android 的列表组件，只创建屏幕上能看到的行，滚动时复用已创建的行，性能好。"

**打开文件指给他看：**
> `TodoListActivity.java`：`todoDao.queryAll()` → `adapter.setTodos(list)`

---

### F3：待办编辑

| 项 | 内容 |
|---|------|
| **要求原文** | 对已有待办进行修改编辑 |
| **实现状态** | ✅ 已实现 |
| **对应位置** | `TodoDetailActivity.java`（点编辑按钮）→ `TodoEditActivity.java`（编辑模式） |

**老师问「编辑功能怎么做的？」**
> "在详情页 Toolbar 点击编辑按钮，跳转到 TodoEditActivity，并传入当前待办的 ID。TodoEditActivity 收到 ID 后先加载原有数据回填到输入框，用户修改后保存。保存时调用 update() 方法更新数据库。"

---

### F4：待办删除

| 项 | 内容 |
|---|------|
| **要求原文** | 删除不再需要的待办 |
| **实现状态** | ✅ 已实现 |
| **对应位置** | `TodoDetailActivity.java` 底部删除按钮 / `TodoAdapter.java` 长按删除 |

**老师问「删除功能怎么做的？」**
> "两个入口：列表页长按待办项触发删除，详情页点删除按钮触发删除。删除前会弹出 AlertDialog 确认框，防止误删。确认后开子线程删数据库，弹 Toast 提示，延迟 800ms 才关闭页面。"

**老师问「为什么 delay 800ms 才关页面？」**
> "如果直接 finish()，Toast 刚弹出来页面就关了，用户看不到提示。延迟 800ms 确保 Toast 显示完整。"

**打开文件指给他看：**
> `TodoDetailActivity.java` 第 81-96 行（AlertDialog）
> `TodoDetailActivity.java` 第 90 行（`postDelayed(() -> finish(), 800)`）

---

### F5：待办状态切换

| 项 | 内容 |
|---|------|
| **要求原文** | 切换"已完成 / 未完成"两种状态 |
| **实现状态** | ✅ 已实现 |
| **对应位置** | `TodoAdapter.java`（列表页 CheckBox）+ `TodoDetailActivity.java`（详情页 CheckBox + Chip） |

**老师问「状态切换怎么做的？」**
> "两个地方可以切换：列表页每行前面的 CheckBox，打勾就是已完成，取消就是未完成。详情页也有一个 CheckBox 和 Chip 标签，切换后 Chip 显示'已完成'绿色或'未完成'灰色。状态改变后开子线程更新数据库。"

**打开文件指给他看：**
> `TodoAdapter.java` 第 77-78 行：CheckBox 切换
> `TodoDetailActivity.java` 第 138-141 行：`updateChipStatus()` 方法

---

## 第二部分：界面设计要求（20 分）

| 要求 | 状态 | 对应位置 |
|------|------|---------|
| 简洁直观，按钮布局合理 | ✅ | 6 个布局文件 |
| 操作路径短 | ✅ | 欢迎页 FAB → 新建；列表页点击 → 详情 |
| 符合安卓设计规范（Material） | ✅ | `themes.xml` 使用 MaterialDesign 3 |
| 输入框 hint 提示 | ✅ | `strings.xml` 中 `hint_account`、`hint_password` 等 |
| 错误提示（红色/Toast） | ✅ | TextInputLayout 的 setError() + Toast 提示 |
| 不同屏幕尺寸适配 | ✅ | 使用 dp/sp 单位，不在 XML 写死 px |
| 适当的图标、间距、配色 | ✅ | 12 个矢量图标 + colors.xml 色值体系 + dimens.xml 间距体系 |

**老师问「界面设计你觉得有什么亮点？」**
> "用了 Material Design 3 的主题，支持 Light 和 Dark 双模式自动切换。颜色全部定义在 colors.xml 里，间距在 dimens.xml 里，整个项目的界面风格统一。"

**打开文件指给他看：**
> `res/values/themes.xml`：`Theme.Material3.DayNight.NoActionBar`
> `res/values/colors.xml`：21 个语义色值
> `res/values/dimens.xml`：间距/尺寸体系

---

## 第三部分：代码规范要求（20 分）

| 要求 | 状态 | 对应位置 |
|------|------|---------|
| 包结构合理 | ✅ | `bean/`、`dao/`、`dbunit/`、`adapter/` 四包 |
| 命名规范 | ✅ | 类名 PascalCase，变量 camelCase，资源 snake_case |
| 注释详细 | ✅ | 13 个 Java 文件，共 66 处 javadoc 注释 |
| 子线程执行数据库操作 | ✅ | 所有 DB 操作都在 `new Thread()` 中 |
| UI 更新切回主线程 | ✅ | 全部使用 `runOnUiThread()` |
| Intent 跳转 | ✅ | `startActivity()` + `registerForActivityResult()` |

**老师问「你怎么保证数据库操作不在主线程？」**
> "所有数据库操作都用 `new Thread(() -> { ... }).start()` 包起来，更新 UI 的时候用 `runOnUiThread()` 切回主线程。"

**打开文件指给他看：**
> `WelcomeActivity.java` 第 126-160 行：完整示范了子线程 + try-finally + runOnUiThread 模式

---

## 第四部分：实训阶段要求（阶段一）

### 注册功能

| 检查项 | 状态 | 代码位置 |
|--------|------|---------|
| 输入用户名、密码、邮箱 | ✅ | `activity_register.xml` |
| 用户名判重 | ✅ | `RegisterActivity.java` → `userDao.findByName(name)` |
| 密码强度检测 | ✅ | `RegisterActivity.java` → `checkPasswordStrength()` |
| 邮箱格式校验 | ✅ | `RegisterActivity.java` 第 107 行 regex |
| 注册成功/失败反馈 | ✅ | `RegisterActivity.java` Toast 提示 |

### 登录功能

| 检查项 | 状态 | 代码位置 |
|--------|------|---------|
| 校验用户名+密码 | ✅ | `MainActivity.java` → `userDao.login()` |
| 登录成功跳转欢迎页 | ✅ | `Intent → WelcomeActivity` |
| 失败提示（不存在/密码错误/为空） | ✅ | 三个独立 Toast 提示 |

### 记住密码 / 自动登录（可选加分项）

| 检查项 | 状态 | 代码位置 |
|--------|------|---------|
| 记住密码自动回填 | ✅ | `SharedPreferences("spfRecord")` |
| 自动登录跳过登录页 | ✅ | `WelcomeActivity` → SP 检查 |
| CheckBox 联动 | ✅ | `MainActivity.java` OnCheckedChangeListener |

---

## 第五部分：提交资料要求（10 分）

| 要求 | 状态 |
|------|------|
| 代码 zip 包（`学号_姓名_实训代码.zip`） | ⏳ **需要你打包** |
| 课程设计报告 docx（`学号_姓名_简易待办课程设计报告.docx`） | ⏳ **需要你导出** |
| 展示视频（3 分钟以内，本人声音） | ⏳ **需要你录制** |

---

## 总结：如果老师问「考试要求的都实现了没有？」

> "全部实现了。F1-F5 待办创建查看编辑删除状态切换全部完整实现，注册登录功能含判重/密码强度/邮箱校验，记住密码和自动登录也实现了。数据库用的 SQLite，语言 Java，组件用的 Activity，完全符合技术要求。"

然后无缝切换到亮点：
> "在这些基础要求之上，我们还做了几个超出要求的功能提升，比如 M3 双主题、密码强度实时检测、欢迎页仪表盘……"
