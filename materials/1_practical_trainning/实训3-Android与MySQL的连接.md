# 实训 3　Android 与 MySQL 的连接

> 课程：移动应用开发（Android）实训
> 主题：搭建 MySQL 后端 + Android JDBC 连接
> 前置：[实训 2 Activity 跳转与 Toast](./实训2-Activity跳转与Toast.md)
> 后续：[实训 4 注册与登录功能实现](./实训4-注册与登录功能实现.md)

## 项目描述

项目要求 Android Studio 通过 MySQL 数据库对数据库的**增、删、查、改**的相关操作。

## 项目目标

- 熟练 MySQL 与 Android 应用的开发的相关步骤与设置
- 熟练 Android 在 MySQL 中对数据库的增、删、查、改的相关操作

## 实践步骤

### 一、连接 MySQL 数据库服务器

1. 打开 **Navicat**。
2. 单击"连接"菜单，然后选择"MySQL"。
3. 在"连接"对话框中，输入连接信息（主机、端口、用户名、密码）。

> ⚠️ 原文中此处应有一张连接配置截图（原文为"如下图所示"），但 docx 内仅含占位文字，未提供具体参数。可用示例：主机 `localhost` / 端口 `3306` / 用户 `root` / 密码 `123456`（与后文授权语句一致）。

### 二、创建数据库与数据表

1. 创建一个新的数据库，库名 `androidPT`。
2. 在 `androidPT` 中创建 `Userinfo` 表，字段如下：

| 字段名 | 类型 | 说明 |
|-------|------|------|
| `id` | INT | **主键**，自动递增 |
| （其他字段） | — | 原文未列出，由学生在后续实训中按需补充（如 username、password 等） |

> ⚠️ 原文中"如下图所示"对应的表结构截图在原 docx 中缺失，请按上述最小结构先行建表，剩余字段在实训 4 补齐。

### 三、Android Studio 配置

#### 1. 添加 MySQL 驱动依赖

在项目根目录的 `build.gradle` 文件中添加：

```gradle
implementation("mysql:mysql-connector-java:5.1.49")
implementation 'mysql:mysql-connector-java:5.1.49'
```

> 原文同时给了两行相同的依赖（带括号和不带括号两种写法），实际项目中**保留其中一行**即可。

#### 2. 添加网络权限

在 `AndroidManifest.xml` 中添加：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

#### 3. 规划代码包结构

在 `java` 目录下，在项目所在的包下创建三个子包：

| 包名 | 职责 |
|------|------|
| `dbunit` | 数据库连接工具（如 `JdbcHelper.java`） |
| `bean`   | 实体类（如 `User.java`） |
| `dao`    | 数据访问对象（如 `UserDao.java`） |

### 四、解决 MySQL 远程/外部连接问题

> ⚠️ **关键坑点**：Android 连接 MySQL 必须使用 **IP** 形式，不能用 `localhost`，否则会出错。

为了保证能用 IP 连接成功，要求先在 Navicat 用 IP 尝试连接。如果测试连接不成功，则打开命令行执行以下授权语句。

**MySQL 5.x 一步到位版本：**

```sql
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123456' WITH GRANT OPTION;

FLUSH PRIVILEGES;
```

**MySQL 8.0 及以上必须分三步：**

```sql
CREATE USER 'root'@'%' IDENTIFIED BY '123456';

GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

FLUSH PRIVILEGES;
```

### 五、编写核心类（参考结构）

按下面的目录结构创建类，类内具体代码"自行实现"——这是本实训主要让学生练习的部分。

```
java/<your.package>/
├── dbunit/
│   └── JdbcHelper.java   # 数据库连接辅助类
├── bean/
│   └── User.java         # 用户实体
└── dao/
    └── UserDao.java      # 增删查改示例
```

> 📌 `UserDao` 中的连接代码是测试连接的关键路径，连接成功才能继续后面的注册/登录。

### 六、首次插入测试数据

在 MySQL 的 `userInfo` 表中插入一行数据（用于验证连接是否通畅）。

> ⚠️ 原文中"如下图所示"对应的截图在原 docx 中缺失，请自行用 Navicat 插入一条测试记录。

### 七、线程模型要求

> ⚠️ **Android 平台硬性约束**：有关数据库的连接操作**必须放在子线程中实现**，否则会触发 `NetworkOnMainThreadException`。

参考写法：

```java
new Thread(new Runnable() {
    @Override
    public void run() {
        // 在这里执行 JDBC 连接和 SQL
    }
}).start();
```

## 作业要求

1. 按上面的操作完成数据库的连接。
2. 尝试向数据库插入一行数据，并简述实现思路。

## 本实训小结

| 产出物 | 位置 |
|-------|------|
| MySQL 库 `androidPT` 与表 `Userinfo` | Navicat |
| MySQL JDBC 依赖 | `app/build.gradle` |
| INTERNET 权限 | `AndroidManifest.xml` |
| `JdbcHelper` / `User` / `UserDao` 三个类 | `java/<pkg>/dbunit`、`bean`、`dao` |
| MySQL 用户授权 | Navicat 或 MySQL 命令行 |

## 衔接说明

- 本实训只解决"连得上"。真正完成"注册 / 登录"业务请到 [实训 4 注册与登录功能实现](./实训4-注册与登录功能实现.md)。
- 线程中拿到结果后需要切回主线程更新 UI，常见做法是 `runOnUiThread(...)` 或 `Handler`。
