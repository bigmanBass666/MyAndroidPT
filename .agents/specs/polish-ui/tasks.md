# Tasks — UI 精进实施

## 任务清单

### 任务 1: 密码可见性切换（登录 + 注册）
改动 3 个 TextInputLayout，一行属性 `app:endIconMode="password_toggle"`：
- `activity_main.xml` — `et_password` 的父 TextInputLayout
- `activity_register.xml` — `et_password` 和 `et_password_confirm` 的父 TextInputLayout
- 验证：构建通过，模拟器看到眼睛图标

### 任务 2: 登录页表单卡片容器
- 在 `activity_main.xml` 中，将账号输入框到 CheckBox 所在区域包裹进 MaterialCardView
- 验证：构建通过，卡片圆角阴影可见

### 任务 3: 注册页 Toolbar 返回键 + 邮箱键盘类型
- `RegisterActivity.java` — 添加 `toolbar.setNavigationOnClickListener(v -> finish())`
- `activity_register.xml` — `et_email` 添加 `android:inputType="textEmailAddress"`
- 验证：构建通过

### 任务 4: 空状态插画
- 创建一个 VectorDrawable（空列表/笔记本图标）放在 `res/drawable/ic_empty.xml`
- 修改 `activity_todo_list.xml`，在 `tv_empty_hint` 上方添加 ImageView 引用
- 验证：构建通过，空态时插画显示

### 任务 5: 列表项 CardView 圆角提升
- `item_todo.xml` — 改 `app:cardCornerRadius="12dp"`，确认阴影合适
- 验证：构建通过，列表项圆角变大

### 任务 6: 待办编辑页 TextInputLayout 改造
- 重构 `activity_todo_edit.xml`：
  - 移除 `label_title` 和 `label_content` 两个 TextView
  - 将 `et_title` 和 `et_content` 分别包裹进 TextInputLayout（OutlinedBox）
  - 用 `android:hint` 替代原有标签
  - `et_content` 设置 `minLines="5"` + `gravity="top|start"`
- 验证：构建通过，编辑页输入框风格与登录页一致

### 任务 7: 待办编辑页内联错误校验
- `TodoEditActivity.java` — 在空标题校验失败时，调用 TextInputLayout 的 `setError()` 显示红色边框和错误文字
- `activity_todo_edit.xml` — 在标题 TextInputLayout 上启用 `app:errorEnabled="true"`
- 验证：构建通过，空标题提交后输入框变红

### 任务 8: 待办详情页颜色优化
- `activity_todo_detail.xml` — 内容区域包裹 CardView
- `TodoDetailActivity.java` — `tv_detail_status` 根据状态颜色变化
  - 已完成 → 绿色（`?attr/colorPrimary`）
  - 未完成 → 灰色（`?attr/colorOnSurfaceVariant`）
- 验证：构建通过，详情页颜色语义正确

### 任务 9: 欢迎页退出按钮红色
- `activity_welcome.xml` — `btn_logout` 添加红色描边 + 红色文字
- 验证：构建通过，退出按钮红色显眼

## 依赖关系

```
任务 1 ──┐
任务 2 ──┤
任务 3 ──┤
任务 4 ──┤── 全部并行（无依赖）
任务 5 ──┤
任务 6 ──┤
任务 8 ──┤
任务 9 ──┘
任务 7 ── 依赖任务 6（编辑页 TextInputLayout 化）
```

## 执行说明

- 所有任务的代码改动均在 `feature/ui-polish` 分支上进行
- 每个任务完成后需运行 `cd app && ../gradlew.bat assembleDebug` 验证编译通过
- 每任务一个原子提交
- 最后运行完整构建确认无回归
