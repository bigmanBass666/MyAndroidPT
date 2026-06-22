# Checklist — Layout Polish Implementation

## P0 Items
- [x] P0-1: dimen token 在全部 7 个布局 XML 中被引用（每个布局至少替换 1-2 处硬编码 dp）
- [x] P0-2: 每个有异步操作的 Activity 都有对应的 loading 态 UI（按钮禁用 + 文字切换）
- [x] P0-3: 所有 cardCornerRadius="12dp" 被替换为 shape token 引用
- [x] P0-4: activity_todo_detail.xml 的容器嵌套从三级简化为二级

## P1 Items
- [x] P1-5: res/values-night/themes.xml 已创建，包含 dark theme 色值
- [x] P1-6: welcome 页三个按钮有明确的视觉层级区分
- [x] P1-7: 跨页面间距值统一通过 @dimen/* 引用（等价于 P0-1）
- [x] P1-8: 登录/注册按钮在提交过程中正确禁用
- [ ] P1-9: 注册页增加字段级错误提示 UI（密码字段已有，其余字段用 Toast —— 品质加分项）
- [x] P1-10: Register 页 4dp magic margin 被清理

## P2 Items
- [x] P2-11: 仪表盘数字从 32sp 降为 24sp
- [x] P2-12: RecyclerView padding="8dp" 被移除
- [x] P2-13: 详情页标题使用 textAppearanceTitleLarge
- [ ] P2-14: Toolbar 用 AppBarLayout 包裹（功能正常，可改可不改）
- [ ] P2-15: 仪表盘 elevation 调整（当前 2dp，可降为 1dp）

## Verification
- [x] assembleDebug 编译通过无错误
- [x] lint 无新增警告
- [x] 登录功能正常（成功/失败反馈）
- [x] 注册功能正常（成功/失败反馈 + 密码强度检测）
- [x] 待办 CRUD 正常（创建/查看/编辑/删除/状态切换）
- [x] 记住密码/自动登录功能正常
- [x] 退出登录功能正常
- [x] logcat 中无 ANR / 崩溃 / 数据库异常（仅一条 emulator HWUI 警告，非 app 问题）