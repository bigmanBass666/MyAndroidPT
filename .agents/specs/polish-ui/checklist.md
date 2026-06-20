# Checklist — UI 精进实施

## 密码可见性切换
- [x] 登录页密码框出现眼睛图标
- [x] 注册页密码框出现眼睛图标
- [x] 注册页确认密码框出现眼睛图标
- [x] 点击眼睛图标切换明文/密文

## 登录页表单卡片
- [x] 输入框 + CheckBox 被 MaterialCardView 包裹
- [x] 卡片有圆角 + 阴影效果
- [x] 卡片与按钮有明确视觉分层

## 注册页改进
- [x] Toolbar 返回箭头可正常返回
- [x] 邮箱输入框键盘显示 @ 快捷键

## 空状态插画
- [x] 空待办列表时显示插画（非仅文字）
- [x] 引导文字提示用户点击 + 创建

## 列表项圆角
- [x] 列表 CardView 圆角明显（≥12dp）

## 编辑页 TextInputLayout
- [x] 标题/内容输入框使用 OutlinedBox 风格
- [x] 没有裸露的 EditText
- [x] 没有 `label_title` / `label_content` 冗余标签
- [x] 内容输入框有足够的垂直空间（minLines=5）
- [x] 内容输入框内容从顶部排列

## 编辑页内联错误校验
- [x] 标题 TextInputLayout 启用 `errorEnabled`
- [x] 空标题提交后输入框变红 + 显示错误文字

## 详情页状态颜色
- [x] 已完成状态标签为绿色
- [x] 未完成状态标签为灰色/橙色
- [x] 内容区域被 CardView 包裹

## 欢迎页退出按钮
- [x] 退出登录按钮使用红色描边/文字

## 编译验证
- [x] 所有改动 `assembleDebug` 通过
- [x] 无新增 lint 警告
