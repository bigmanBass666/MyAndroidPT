## 图1 MySQL - 新建连接 常规 高级 数据库 SSL SSH 连接名： mysql 主机： localhost 端口： 3306 用户名： root 密码： ********** 保存密码 ## 图2 新建立数据库 常规 SQL 预览 数据库名：androidPT 字符集：utf8 排序规则： 确定 取消 ## 图3 MySQL 数据库表结构设计界面： 字段列表： - 字段名称：id、name、psw - 类型：int、varchar、varchar - 长度：11、20、20 - 小数点：0、0、0 - 是否非空：id字段为是，name和psw字段为否 - 主键：id字段为主键 - 注释：无 选项栏： - 默认值：自动递增（已勾选） - 无符号：未勾选 ## 图4 dependencies { implementation 'androidx.appcompat:appcompat:1.3.0' implementation 'com.google.android.material:material:1.4.0' implementation 'androidx.constraintlayout:constraintlayout:2.0.4' implementation("mysql:mysql-connector-java:5.1.47") testImplementation 'junit:junit:4.13.2' androidTestImplementation 'androidx.test.ext:junit:1.1.3' androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0' } ## 图5 <?xml version="1.0" encoding="utf-8"?> <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.lq.pt1"> <uses-permission android:name="android.permission.INTERNET"/> <application android:allowBackup="true" android:icon="@mipmap/ic_launcher" android:label=".pt1" android:roundIcon="@mipmap/ic_launcher_round" android:supportsRtl="true" android:theme="@style/Theme.AndroidPT2"> <activity android:name=".RegisterActivity"> </activity> <activity android:name=".MainActivity" android:exported="true"> <intent-filter> <action android:name="android.intent.action.MAIN" /> <category android:name="android.intent.category.LAUNCHER" /> </intent-filter> </activity> </application> </manifest> ## 图6 图片中展示了一个电脑屏幕截图，显示了一个编程界面和日志窗口。在左侧的项目资源管理器区域，可以看到多个文件和文件夹，包括"AndroidManifest.xml"、"MainActivity.java"等。这些通常是Android项目的一部分。此外，还有一些其他文件和文件夹，如"build.gradle（Project: chapter1）"，"build.gradle（Module: chapter1.app）"等。 在右侧的代码编辑区，有一段Java代码，这段代码是用于连接MySQL数据库的。它首先导入了必要的类，然后定义了一个公共静态方法getConnection()来获取数据库连接。该方法尝试加载MySQL JDBC驱动程序，并使用DriverManager.getConnection()方法建立与MySQL数据库的连接。如果发生异常，它会捕获这些异常并打印堆栈跟踪信息。 在底部，有一个日志窗口，显示了应用程序的运行信息和一些错误消息。这些信息对于调试应用程序非常有用。 ## 图7 public class Jdbchelper { // MySQL 数据库的MySQL_数据库的连接_URL_包括主机名_端口号和数据库名称 static String url = "jdbc:mysql://192.168.1.18:3306/andoridpt?useSSL=false"; // MySQL 数据库的用户名 static String name = "root"; // MySQL 数据库的密码 static String psd = "123456"; public static Connection getConn() { Connection conn = null; try { // 加载 MySQL JDBC 驱动程序 Class.forName("com.mysql.jdbc.Driver"); // 使用 DriverManager.getConnection 方法尝试建立与 MySQL 数据库的连接 conn = (Connection)DriverManager.getConnection(url, name, psd); } catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); } return conn; } } ## 图8 Navicat → 数据库 连接名称: mysql 主机: 192.168.1.18 端口: 3306 用户名: root 密码: •••••••• ☑ 保存密码 [按钮] 测试连接 | URI... | 上一步 | 确定 ## 图9 图中显示的是电脑屏幕上的一个窗口，其中包含以下中文文字： - Microsoft Whiteboard - MySQL 5.7 Command Line Client - 最近添加 - MySQL Installer - Community - 最近添加 - 最近添加 ## 图10 选择MySQL 5.7 Command Line Client Enter password: ********** Welcome to the MySQL monitor. Commands end with ; or \g. Your MySQL connection id is 9 Server version: 5.7.40-log MySQL Community Server (GPL) Copyright (c) 2000, 2022, Oracle and/or its affiliates. Oracle is a registered trademark of Oracle Corporation and/or its affiliates. Other names may be trademarks of their respective owners. Type 'help;' or '\h' for help. Type '\c' to clear the current input statement. mysql> GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123456' WITH GRANT OPTION; Query OK, 0 rows affected, 1 warning (0.00 sec) mysql> FLUSH PRIVILEGES; Query OK, 0 rows affected (0.00 sec) mysql> ## 图11 图片中展示的是一段Java代码，具体内容如下： package com.lrq.pti.bean; public class User { private int id; private String name; private String password; public User(String name, String password) { this.name = name; this.password = password; } public User() { } public int getId() { return id; } public void setId(int id) { this.id = id; } public String getName() { return name; } public void setName(String name) { this.name = name; } public String getPassword() { return password; } public void setPassWord(String password) { this.password = password; } } ## 图12 图片中的中文文字有"半简"和"Cn"。

## 图13
Navicat Premium Lite 数据库管理工具界面，显示 MySQL 数据库 androidpt 中的 userinfo 表结构和数据。

左侧导航树：
- 1.18 (服务器连接)
  - androidpt (数据库)
    - 表 (tables)
      - userinfo (当前选中表)

userinfo 表结构（表格视图）：

| 字段 | 类型 | 非空 | 说明 |
|------|------|------|------|
| id | int(11) | ✓ 是 | -- |
| name | varchar(20) | 为空 | -- |
| psw | varchar(20) | 为空 | -- |

数据行：
- id: 1, name: aaa, psw: 123456

底部状态栏：
- SELECT * FROM `androidpt`.`userinfo` LIMIT 0,1000
- 第 1 条记录（共 1 条）于第 1 页
- 上次刷新时间: 13m
- 窗口标题：实验3 android与mysql的连接.docx - 兼容性模式
- 工具栏：文件 开始 粘贴 剪切 复制 连接 保存 关闭

## 图14
Android Studio 项目界面，项目名：chapter1，正在编辑 activity_main.xml 布局文件（ConstraintLayout 布局）。

左侧项目结构树：
- chapter1 (Project)
  - Android
    - MainActivity
    - MyBroadcastReceiver
    - MyBroadcastReceiver2
    - MyOrderedBroadcastReceiver1
    - MyOrderedBroadcastReceiver2
    - MyOrderedBroadcastReceiver3
  - com.example.lab6 (androidTest)
  - com.example.lab6 (test)
  - java (generated)
  - res
    - drawable
    - layout
      - activity_main.xml (当前编辑)
    - mipmap
  - values
  - res (generated)

中部编辑区 - activity_main.xml 内容：
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/btn_select"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="158dp"
        android:layout_marginTop="254dp"
        android:layout_marginEnd="129dp"
        android:layout_marginBottom="429dp"
        android:text="测试查询数据"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.0"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>

底部状态栏 Logcat 输出：
I/ConfigStore: android:hardware:configstore:VI_0:ISurfaceFlingerConfigs::hasWideColorDisplay retrieved: 0
I/ConfigStore: android:hardware:configstore:VI_0:ISurfaceFlingerConfigs::hasHDRDisplay retrieved: 0
D/OpenGLRenderer: Initialized EGL, version 1.4
D/OpenGLRenderer: Swap behavior 1
E/GL_adreno: CreateContext rcMajorVersion:3, minorVersion:0

## 图15
package com.example.mysqltest;

import ...

public class MainActivity extends AppCompatActivity {

    private Button btn_query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_query = findViewById(R.id.btn_select);
        btn_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UserDao userDao = new UserDao();
                        userDao.test();
                    }
                }).start();
            }
        });
    }
}

## 图16
package com.example.mysqltest.dao;

import ...

public class UserDao {
    //测试数据库连接的
    public void test() {
        try {
            Connection connection = JdbcHelper.getConn();
            String sql = "select * from userinfo";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int userId = resultSet.getInt(columnLabel: "id");
                String usr = resultSet.getString(columnLabel: "name");
                String pwd = resultSet.getString(columnLabel: "psw");
                // 处理每一行数据，您可以根据需求进行相应的操作
                System.out.println("user: " + userId);
                System.out.println("username: " + usr);
                System.out.println("password: " + pwd);
                //在日志中输出
                Log.i(tag: "jdbc", msg: "user: " + userId);
                Log.i(tag: "jdbc", msg: "username: " + usr);
                Log.i(tag: "jdbc", msg: "password: " + pwd);
            }
            resultSet.close();
            preparedStatement.close();
        } catch (Exception e) {
            System.out.println(e);
            Log.e(tag: "jdbc", e.getMessage());
        }
    }
}

## 图17
2025-04-29 08:23:07.331 4019-5172/com.example.mysqltest I/jdbc:: user: 1
2025-04-29 08:23:07.331 4019-5172/com.example.mysqltest I/jdbc: username: lrq
2025-04-29 08:23:07.331 4019-5172/com.example.mysqltest I/jdbc: password: 123456