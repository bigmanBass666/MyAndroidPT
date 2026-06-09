# 实训 2　Activity 的跳转与 Toast 的使用

> 课程：移动应用开发（Android）实训
> 主题：Activity 跳转 + Toast 表单校验
> 前置：[实训 1 登录与注册页面](./实训1-登录与注册页面.md)
> 后续：[实训 3 Android 与 MySQL 的连接](./实训3-Android与MySQL的连接.md)

> ⚠️ 原文标题写作"Ativity的跳转"，明显是"Activity"的拼写错误，本文档统一为 **Activity**。

## 项目描述

编写 Activity 的跳转与 Toast 的使用。

## 项目目标

- 熟练 Android Activity 的跳转
- 熟练 Android 的 Toast 的使用

## 实践步骤

### 步骤 1：实现页面跳转

打开工程项目下的 `app/src/main/java/com/ljx/pt1/MainActivity.java` 文件，编写代码，使点击登录按钮时跳转到注册页面。

> 📌 原文使用包名 `com.ljx.pt1`（源文档中写作 `com.lrq.pt1`，此处已统一为 `ljx`）。常见跳转写法：
>
> ```java
> Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
> startActivity(intent);
> ```

### 步骤 2：编写登录校验逻辑

要求：

- 账号栏输入**自己名字的简写**
- 密码为 `123`
- 满足上述条件 → 登录成功
- 否则 → 登录失败

### 步骤 3：用 Toast 做输入检查

使用 `Toast` 实现以下检查：

- 输入非空校验（账号、密码不能为空）
- 密码与预期值不一致的提示

> 📌 常见 Toast 用法：
>
> ```java
> Toast.makeText(MainActivity.this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
> ```

## 本实训小结

| 涉及文件 | 关键能力 |
|---------|---------|
| `MainActivity.java` | `Intent` 跳转、`EditText` 取值、字符串判断 |
| `RegisterActivity.java`（若新建） | 注册页接收方，可在后续实训中补充功能 |
| `Toast` | 轻量级用户反馈，不打断操作 |

## 衔接说明

- 本实训的"账号+密码=固定值"只是教学占位。真正接入 MySQL 校验要到 [实训 4 注册与登录功能实现](./实训4-注册与登录功能实现.md) 才完成。
- 跳转使用的 `startActivity(intent)` 在 [实训 6 自动登录](./实训6-自动登录.md) 中会被替换为带结果回传的 `startActivityForResult`，届时做对应修改。
