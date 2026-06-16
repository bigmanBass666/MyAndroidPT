# XML 层面的视觉改进计划

> 基于 InstallerX-Revived 的对比分析，提取可在 Classic Views (XML) 项目中直接应用的改进

## 核心问题诊断

| 问题 | 影响 | 严重程度 |
|------|------|---------|
| colors.xml 有 6 个 legacy 硬编码颜色 | 夜间模式下文字不可见 | P0 |
| btn_bg_selector.xml 硬编码 `@color/colorPrimary` | M3 主题无法驱动按钮颜色 | P1 |
| edit_text_bg.xml 硬编码白色背景 | 夜间模式下输入框变成白底黑字 | P1 |
| MyBtnStyle 硬编码 `@color/white` | 文字颜色不跟随主题 | P1 |
| shapeAppearance 只在 2 个卡片上使用 | 90% 的 UI 没有圆角层次 | P2 |
| 没有 dimens.xml | 间距是魔法数字，不一致 | P2 |
| 图标 tint 硬编码 | 图标不跟随主题 | P2 |
| 没有 elevation 层次 | UI 看起来扁平 | P2 |

## 改进方案

### 1. 修复 colors.xml — 移除硬编码，统一命名

**当前问题**:
```xml
<!-- 硬编码，夜间模式不生效 -->
<color name="text_primary">#FF212121</color>
<color name="text_secondary">#FF757575</color>
<color name="text_hint">#FFBDBDBD</color>
<color name="divider">#FFE0E0E0</color>
<color name="btn_disabled">#FFBDBDBD</color>
<color name="background_light_blue">#FFE3F2FD</color>
```

**改进方案**:
- 删除以上 6 个 legacy 别名
- 改用 M3 标准 token: `?attr/colorOnSurface`, `?attr/colorOnSurfaceVariant`, `?attr/colorOutline`, `?attr/colorSurfaceVariant`
- 按 InstallerX 的做法，用 `light_`/`dark_` 前缀命名主色调

### 2. 修复 btn_bg_selector.xml — 使用主题属性

**当前问题**:
```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true" android:drawable="@color/colorPrimary" />
    <item android:state_enabled="false" android:drawable="@color/btn_disabled" />
    <item android:drawable="@color/colorPrimary" />
</selector>
```

**改进方案**: 改用 ripple + M3 主题属性，或使用 `Widget.Material3.Button.*` 内置状态

### 3. 修复 edit_text_bg.xml — 使用主题属性

**当前问题**:
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@android:color/white" />  <!-- 硬编码白 -->
    <stroke
        android:width="2dp"
        android:color="@color/colorPrimary" />
    <corners android:radius="8dp" />
</shape>
```

**改进方案**: 改用 `?attr/colorSurface` 和 `?attr/colorOutline`

### 4. 创建 dimens.xml — 统一间距系统

参考 InstallerX 的做法（虽然没有 dimens，但这是 Android 最佳实践）:

```xml
<!-- values/dimens.xml -->
<dimen name="margin_xsmall">4dp</dimen>
<dimen name="margin_small">8dp</dimen>
<dimen name="margin_medium">16dp</dimen>
<dimen name="margin_large">24dp</dimen>
<dimen name="margin_xlarge">32dp</dimen>
<dimen name="elevation_small">2dp</dimen>
<dimen name="elevation_medium">4dp</dimen>
<dimen name="elevation_large">8dp</dimen>
```

### 5. 统一图标 tint — 使用主题属性

参考 InstallerX 的做法:
```xml
android:tint="?attr/colorControlNormal"
```

### 6. 增强 shape 系统应用

当前只在 `item_todo.xml` 和 `activity_todo_detail.xml` 中使用了 shape tokens。

需要应用到:
- MaterialCardView 卡片
- MaterialButton 按钮
- TextInputLayout 输入框
- FAB

---

## 具体改动清单

| 文件 | 改动 | 预计行数 |
|------|------|---------|
| `colors.xml` | 删除 6 个 legacy 别名 | -6 行 |
| `btn_bg_selector.xml` | 改用主题属性 | 改 3 行 |
| `edit_text_bg.xml` | 改用主题属性 | 改 3 行 |
| `styles.xml` | MyBtnStyle 改用 `?attr/colorOnPrimary` | 改 1 行 |
| `dimens.xml` | **新建** 统一间距系统 | ~20 行 |
| `ic_launcher_background.xml` | 加 `tint="?attr/colorControlNormal"` | 改 1 行 |
| 所有布局 XML | 替换 `@color/text_primary` → `?attr/colorOnSurface` | ~15 处 |
| 所有布局 XML | 替换 `@color/text_secondary` → `?attr/colorOnSurfaceVariant` | ~10 处 |
| 所有布局 XML | 替换 `@color/text_hint` → `?attr/colorOnSurfaceVariant` | ~8 处 |
| 所有布局 XML | 替换 `@color/divider` → `?attr/colorOutline` | ~5 处 |
| 所有布局 XML | 替换 `@color/btn_disabled` → `?attr/colorOnSurface` + alpha | ~3 处 |
| 所有布局 XML | 替换 `@color/background_light_blue` → `?attr/colorSurfaceVariant` | ~2 处 |
| `themes.xml` | 添加 shapeAppearanceMediumComponent 等主题属性 | ~3 行 |

总计: 约 73 行改动，分布在 10+ 文件中

---

## 参考对照

### InstallerX 的颜色命名 vs 我们的

| InstallerX | 当前项目 | 改进后 |
|------------|---------|--------|
| `light_primary` (#4A672D) | `green_500` | 重命名为 `light_primary` |
| `light_on_primary` (#0E2000) | `white` | 保持不变 |
| `light_primary_container` (#CBEEA5) | `green_200` | 重命名为 `light_primary_container` |
| `dark_primary` (#AFD18C) | N/A | 夜间模式颜色 |
| `dark_primary_container` (#334E17) | N/A | 夜间模式颜色 |

### InstallerX 的图标 tint 模式 vs 我们的

| InstallerX | 当前项目 |
|-----------|---------|
| `android:tint="?attr/colorControlNormal"` | 无 tint 属性（使用系统默认） |

---

## 不在本次改进范围

以下改进需要较大改动或 Compose 依赖，不在本次 XML 层面改进中:

1. material-kolor 动态色彩引擎（需要 Compose + Kotlin）
2. 颜色动画过渡（AnimateColorAsState 是 Compose API）
3. 毛玻璃模糊效果（需要 Miuix Shader 或 RenderScript）
4. M3 Expressive motion（需要 Compose MotionScheme）
5. 分段卡片形状（Segmented shapes，需要自定义 Shape 类）

如果未来想加入，需要单独评估。
