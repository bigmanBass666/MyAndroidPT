# Checklist — Layout Redesign Implementation

## Welcome 页
- [ ] Welcome 页 Toolbar 使用 surface 背景，非绿色
- [ ] Toolbar 右侧有 overflow 菜单（含退出登录）
- [ ] 三个全宽按钮 → 横向操作卡片（待办数/完成数/新建）
- [ ] 下半屏有最近待办预览（最多 3 条，hairline 间隔）
- [ ] 无待办时显示空态引导
- [ ] FAB 右下角
- [ ] Java: loadDashboardData() 加载最近待办列表
- [ ] 点击最近待办预览跳转详情页

## 登录页
- [ ] Toolbar 已移除
- [ ] 顶部品牌标识区（icon + 应用名 + tagline）
- [ ] 输入框有 leading icon（ic_person / ic_lock）
- [ ] 两个 Checkbox 在 Card 外单独一行
- [ ] Card 只包裹两个输入框

## 注册页
- [ ] 输入框有 leading icon
- [ ] Toolbar 标题改为 "创建账号"

## 列表页
- [ ] 列表项无删除按钮
- [ ] 右侧有状态指示（灰色圆点 / 绿色勾号）
- [ ] 已完成项背景色有变化
- [ ] 行间使用 hairline divider
- [ ] 长按列表项弹出删除确认对话框
- [ ] Adapter 适配新布局

## 详情页
- [ ] 状态使用 Chip 组件展示
- [ ] 编辑按钮在 Toolbar 右侧
- [ ] 内容区使用 Card（elevation=0dp）
- [ ] 删除按钮独立沉底（红色 OutlinedButton）
- [ ] 各内容区块使用 Divider 分隔
- [ ] Java: 状态 Chip 同步

## 编辑页
- [ ] 保存按钮在 Toolbar 右侧
- [ ] 无独立取消按钮
- [ ] 输入框有 leading icon
- [ ] Java: Toolbar 保存 action / 移除了 btn_cancel 逻辑

## 全局
- [ ] 新增 drawable: ic_person, ic_lock, ic_email, ic_note, ic_description
- [ ] 新增 string: app_name_display, app_tagline 等
- [ ] dimens.xml 有 gap_large(32dp) 等新增 token

## 验证
- [ ] assembleDebug 编译通过
- [ ] lint 无新增警告
- [ ] 登录功能正常
- [ ] 注册功能正常
- [ ] 创建待办正常
- [ ] 查看待办列表正常
- [ ] 长按删除待办正常
- [ ] 状态切换正常
- [ ] 退出登录功能正常
- [ ] logcat 无崩溃/ANR/异常