# Layout Polish — 布局精进 Spec

## Why
上一轮 UI 改进仅完成了颜色语义化和排版系统，布局本身的结构性问题（间距 token 零引用、Loading 态缺失、嵌套过深、按钮层级混乱）未解决。本 spec 针对 docs/review-v5.md 中 taste-skill 审查发现的 15 项问题进行系统修复。

## What Changes
- **P0-1**: dimen token 引用到全部 7 个布局文件，消除硬编码 dp
- **P0-2**: 为所有异步操作 Activity 添加 Loading 态（禁用按钮 + 进度指示）
- **P0-3**: Shape token 替换硬编码 cardCornerRadius，统一引用 ShapeMedium
- **P0-4**: 简化详情页三层容器嵌套，修复 padding 叠加问题
- **P1-5**: 添加 Dark 主题 support（values-night/themes.xml）
- **P1-6**: Welcome 页按钮层级区分（主 Filled / 次级 Outlined / 安全操作）
- **P1-7**: 统一跨页面间距值，全部引用 dimen token
- **P1-8**: 登录/注册按钮在提交过程中禁用（disabled 态）
- **P1-9**: 内联表单验证 UI（字段级 error）
- **P1-10**: Register 页 4dp magic margin 清理
- **P2-11**: 仪表盘数字 32sp 降为 24sp-28sp
- **P2-12**: RecyclerView padding 冲突清理
- **P2-13**: 详情页标题 22sp 替换为 textAppearanceTitleLarge
- **P2-14**: Toolbar 用 AppBarLayout 包裹
- **P2-15**: 仪表盘 elevation 调整

## Impact
- Affected specs: 全部 7 个布局 XML、themes.xml、dimens.xml、colors.xml、values-night/
- Affected code: MainActivity.java, RegisterActivity.java, TodoEditActivity.java, TodoDetailActivity.java
- 无 Breaking Change（所有功能保持向后兼容）