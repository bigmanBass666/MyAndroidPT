# Checklist — Verdant Design System

## Color Palette
- [ ] colors.xml green_500 从 #8BC34A 改为 #2E7D32
- [ ] colors.xml green_700 从 #689F38 改为 #1B5E20
- [ ] colors.xml teal_700 从 #00796B 改为 #546E7A
- [ ] colors.xml amber_500 从 #FFC107 改为 #F9A825
- [ ] colors.xml red_600 从 #E53935 改为 #C62828
- [ ] colors.xml surface_light 从 #FFFBFE 改为 #FFFDF5
- [ ] colors.xml surface_variant 从 #F0EDE8 改为 #F5F0EB
- [ ] colors.xml outline_light 从 #79747E 改为 #A79B8E
- [ ] colors.xml status_done/status_undone 对齐新色板
- [ ] colors.xml danger 与 red_600 新值一致
- [ ] colors.xml green_500/green_700 等名字保留（只改 hex）
- [ ] values-night/themes.xml 暗色色值与新色板一致

## Typography
- [ ] type.xml 文件已创建
- [ ] 定义了 7 个 textAppearance 样式
- [ ] themes.xml 引用了 typeAppearance 属性
- [ ] MyBtnStyle 使用 M3 标准字体尺寸（继承 Widget.Material3.Button 自动获取）

## Spacing
- [ ] dimens.xml 文件已创建
- [ ] 定义了 6 个 @dimen/ 资源
- [ ] 布局中不再使用硬编码 16dp（改用 @dimen/page_padding 等）

## Button Enhancement
- [ ] MyBtnStyle parent 改为 Widget.Material3.Button
- [ ] MyBtnStyle 移除了 background 属性
- [ ] MyBtnStyle 移除了 cornerRadius 属性
- [ ] btn_bg_selector.xml 文件保留但不再被 MyBtnStyle 引用
- [ ] 登录页按钮和注册页按钮引用 MyBtnStyle 编译通过

## Register Page
- [ ] ScrollView 包裹整个表单区域
- [ ] MaterialCardView 容器（与登录页一致）
- [ ] 密码强度指示器 UI 可见
- [ ] 密码强度 Java 逻辑工作

## Welcome Dashboard
- [ ] 布局中包含速览卡片区域
- [ ] 布局中包含快速创建入口
- [ ] WelcomeActivity 子线程查询 TotoDBHelper 统计
- [ ] 空数据时显示 "暂无待办"
- [ ] 标题使用 colorOnSurface 而非 colorPrimary

## Login Page
- [ ] tv_register 居中
- [ ] 登录按钮 loading 状态（disabled + "登录中..."）

## Todo Edit Page
- [ ] 标题输入框 counterEnabled
- [ ] 保存按钮 loading 状态

## Todo Detail Page
- [ ] 删除按钮文字色为 ?attr/colorError
- [ ] 内层卡片替换为简单容器

## Item Todo
- [ ] ShapeSmall 不再被 cardCornerRadius 覆盖
- [ ] 或选择了其中一种方案（style 或 inline）

## Todo List
- [ ] RecyclerView padding 统一
- [ ] FAB tint 使用 ?attr/colorOnPrimary

## Compilation & Lint
- [ ] gradlew assembleDebug 编译通过
- [ ] gradlew lint 无新增 warning
