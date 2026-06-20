# MyAndroidPT UI 精进建议汇总

> 生成时间：2026-06-17
> 基于 6 页面截图 + 布局 XML + 源码审阅

---

## 评级体系

| 优先级 | 含义 | 示例 |
|--------|------|------|
| **P0** | 强烈建议，直接影响用户体验或代码评分 | 缺少密码切换、空态无引导、输入框非 M3 |
| **P1** | 建议，提升视觉质感或交互流畅度 | 分组卡片、Loading 态、间距调整 |
| **P2** | 加分项，完善细节 | 排序菜单、动画过渡、宽屏适配 |
| **P3** | 锦上添花 | 草稿保存、相对时间、富文本 |

---

## 跨页面总量统计

| 优先级 | 数量 | 涉及页面 |
|--------|------|----------|
| P0 | 12 项 | 全部 6 页 |
| P1 | 10 项 | 全部 6 页 |
| P2 | 8 项 | 登录/注册/欢迎/待办列表/详情 |
| P3 | 4 项 | 欢迎/编辑/详情 |

---

## P0 必修项清单（按页面排序）

| # | 页面 | 问题 | 文件影响 | 难度 |
|---|------|------|----------|------|
| 1 | 登录 | 密码框无 `password_toggle` | `activity_main.xml` +1 attr | 极低 |
| 2 | 登录 | 表单无卡片分组，层次扁平 | `activity_main.xml` +CardView | 低 |
| 3 | 注册 | 密码框/确认密码无 `password_toggle` | `activity_register.xml` +2 attr | 极低 |
| 4 | 注册 | 密码无实时强度指示 | `RegisterActivity.java` + 布局 | 中 |
| 5 | 欢迎 | 页面内容单薄，占不满一屏 | 重新设计布局 | 高 |
| 6 | 待办列表 | 空态无插画/引导，仅文字 | `activity_todo_list.xml` + VectorDrawable | 低 |
| 7 | 待办列表 | 列表项 CardView 圆角/阴影不够 | `item_todo.xml` 增属性 | 极低 |
| 8 | 待办编辑 | 裸露 EditText → TextInputLayout | `activity_todo_edit.xml` 重写 | 中 |
| 9 | 待办编辑 | 内容框 minLines=1 不够用 | `activity_todo_edit.xml` +1 attr | 极低 |
| 10 | 待办编辑 | 缺少内联错误校验 | `TodoEditActivity.java` + 布局 | 中 |
| 11 | 待办详情 | 状态标签无色（完成应绿/未完成应灰） | `activity_todo_detail.xml` + 代码 | 极低 |
| 12 | 待办详情 | 内容区域无 CardView 包裹 | `activity_todo_detail.xml` | 低 |

---

## 最佳性价比：3 个改动拿最大提升

如果时间有限，以下 3 个改动覆盖最多页面、提升最明显：

### 1. 密码可见性切换（登录 + 注册）

```xml
<!-- TextInputLayout 添加这一行 -->
app:endIconMode="password_toggle"
```

**影响**：登录页 + 注册页的 3 个密码框  
**难度**：3 行 xml，零代码改动  
**收益**：密码输入体验明显提升

### 2. 待办编辑页 TextInputLayout 化

移除 `label_title` / `label_content` 两个冗余 TextView，用 OutlinedBox 的 hint 替代。

**影响**：编辑页输入框质感提升到跟登录/注册同一水平  
**难度**：中等，需重构布局

### 3. 空态插画

在 `res/drawable/` 添加一个 VectorDrawable（如一个打开的笔记本或空盒子），在 `tv_empty_hint` 上方显示。

**影响**：用户首次进入 App 的第一印象  
**难度**：低（插画 + xml 引用）

---

## 全局改进方向

| 方向 | 涉及页面 | 说明 |
|------|----------|------|
| 统一输入框风格 | 编辑页 | 登录/注册使用 TextInputLayout，编辑页使用裸露 EditText，不一致 |
| 危险操作红色标示 | 欢迎页退出 + 详情页删除 | 删除类操作无红色警示 |
| 交互反馈 | 全部 | 缺少 Loading 态、按钮按压反馈不明显 |
| 内容排版 | 编辑 + 详情 | 多行内容间距和行高缺少优化 |
| 品牌一致性 | 全部 | 缺乏 App Logo/图标 |

---

## 文件清单

```
docs/ui-review/
├── README.md                  ← 本文件（汇总）
├── screenshots/               ← 页面截图
│   ├── 01_login.png
│   ├── 02_register.png
│   ├── 03_welcome.png
│   ├── 04_todolist.png
│   ├── 04_todolist_empty.png
│   ├── 05_todo_edit.png
│   └── 06_todo_detail.png
├── page-01-login.md           ← 登录页分析
├── page-02-register.md        ← 注册页分析
├── page-03-welcome.md         ← 欢迎页分析
├── page-04-todolist.md        ← 待办列表分析
├── page-05-todo-edit.md       ← 编辑/新增分析
└── page-06-todo-detail.md     ← 详情页分析
```

---

## 下一步

UI 分析阶段完成。接下来可通过 `feature/ui-polish` 分支逐步实施以上 P0/P1 改进。