移动应用开发(android)实训

实训3 android与mysql的连接

**项目描述：**

项目要求 Android Studio 通过 MySQL 数据库对数据库的增、删、查、改的相关操作。

**目标：**

熟练mysql与android 应用的开发的相关步骤与设置

熟练Android在mysql中对数据库的增、删、查、改的相关操作。

### 实践步骤：

1.  连接到 MySQL 数据库服务器

> 打开 Navicat。
>
> 单击"连接"菜单，然后选择"MySQL"。
>
> 在"连接"对话框中，输入以下信息：

![截图](media/image1.png){width="4.84375in" height="2.9895833333333335in"}

2.  创建数据库

创建一个新的数据库 androidPT，如下图所示

![截图](media/image2.png){width="4.041666666666667in" height="3.6243055555555554in"}

创建一个Userinfo表，各字段如下：（其中id 为主键，并设置自动递增)

![截图](media/image3.png){width="5.760416666666667in" height="2.988888888888889in"}

3.  Android Studio 配置

> 在前面项目的基础上做如下的操作。

A.  在项目根目录下的 build.gradle 文件中添加 MySQL 依赖项：

implementation(\"mysql:mysql-connector-java:5.1.49\")

implementation \'mysql:mysql-connector-java:5.1.49\'

> ![截图](media/image4.png){width="5.764583333333333in" height="2.5680555555555555in"}

B.  在 AndroidManifest.xml 文件中添加 Internet 权限：

\<uses-permission android:name=\"android.permission.INTERNET\" /\>

![截图](media/image5.png){width="5.764583333333333in" height="1.8430555555555554in"}

C.  在 java目录下，在项目所在的包下，创建一个dbunit、bean、dao三个包

![截图](media/image6.png){width="4.402775590551181in" height="2.994270559930009in"}

在dbunit包中创建一个数据库连接的辅助类JdbcHelper.java

![截图](media/image7.png){width="5.768055555555556in" height="4.375in"}

注意：android的连接mysql要求要用IP，不用IP就会出错。

为了保证能够用IP连接成功。要求先在navicat 用IP尝试是否能连接成功

![截图](media/image8.png){width="4.74589457567804in" height="4.140801618547681in"}

如果测试连接不成功，则要打开下面的命令行

![截图](media/image9.png){width="3.3225010936132984in" height="1.853934820647419in"}

在下面的命令行中输入下面的代码

GRANT ALL PRIVILEGES ON \*.\* TO \'root\'@\'%\' IDENTIFIED BY \'123456\' WITH GRANT OPTION;

FLUSH PRIVILEGES;

**MySQL 8.0 及以上版 要分为三步：**

CREATE USER \'root\'@\'%\' IDENTIFIED BY \'123456\';

GRANT ALL PRIVILEGES ON \*.\* TO \'root\'@\'%\' WITH GRANT OPTION;

FLUSH PRIVILEGES;

![截图](media/image10.png){width="5.768055555555556in" height="3.0166666666666666in"}

在bean包中创建一个数据库连接的辅助类User.java

![截图](media/image11.png){width="3.6180424321959754in" height="5.348408792650918in"}

在dao包中创建一个数据库连接的辅助类UserDao.java

![截图](media/image12.png){width="5.59248687664042in" height="5.643559711286089in"}

上面的代码，用于测试数据库的连接

在mysql的userInfo表中插入一行数据。

如下图所示：

![截图](media/image13.png){width="5.277731846019248in" height="4.189955161854768in"}

![截图](media/image14.png){width="5.677933070866142in" height="3.7565037182852143in"}

![截图](media/image15.png){width="4.695024059492564in" height="4.331681977252844in"}

有关数据库的连接操作必须在线程当中实现

![截图](media/image16.png){width="6.638624234470691in" height="2.170924103237095in"}

![截图](media/image17.png){width="5.768055555555556in" height="2.56875in"}

要求：

1、请同学们按上面的操作，完成数据库的连接

2、请同学们尝试向数据库插入一行数据？如何实现
