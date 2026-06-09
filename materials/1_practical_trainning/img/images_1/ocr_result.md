## 图1
登录
账号：
请输入用户名或手机号
密码：
请输入密码
□记住密码 □自动登录
登录
还没有账号？

## 图2
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginLeft="20dp"
    android:layout_marginTop="40dp"
    android:layout_marginRight="20dp"
    android:gravity="center_vertical"
    android:orientation="horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="密码："
        android:textSize="25sp" />

    <EditText
        android:id="@+id/et_password"
        style="@style/MyEditStyle"
        android:layout_width="match_parent"
        android:layout_marginLeft="10dp"
        android:hint="请输入密码"
        android:inputType="numberPassword" />

</LinearLayout>

## 图3
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginLeft="20dp"
    android:layout_marginTop="20dp"
    android:layout_marginRight="20dp"
    android:gravity="center_vertical"
    android:orientation="horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="确认密码："
        android:textSize="25sp" />

    <EditText
        android:id="@+id/et_password_confirm"
        style="@style/MyEditStyle"
        android:layout_width="match_parent"
        android:layout_marginLeft="10dp"
        android:hint="再次输入密码"
        android:inputType="numberPassword" />

</LinearLayout>

## 图4
<Button
    android:id="@+id/btn_register"
    style="@style/MyBtnStyle"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="注册" />

<RadioButton
    android:id="@+id/rb_agree"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="left"
    android:layout_marginTop="5dp"
    android:text="同意用户协议"
    android:textColor="@color/colorPrimary"
    app:layout_constraintStart_toStartOf="@id/btn_register"
    app:layout_constraintTop_toBottomOf="@id/btn_register" />

## 图5
注册
账 号：请输入用户名或手机号
密 码：请输入密码
确认密码：再次输入密码
[注册]
同意用户协议（单选框）

## 图6
Android XML 布局结构视图（IDE 截图，5 个顶层子节点依次编号 1-4）：
- 根节点 <LinearLayout>（vertical, match_parent）
- ① <LinearLayout>（账号区，水平布局）
- ② <LinearLayout>（密码区，水平布局）
- ③ <LinearLayout>（确认密码区，水平布局）
- ④ <Button>（注册按钮）+ <TextView>（旁侧文字节点）


## 图7
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:layout_marginLeft="20dp"
    android:layout_marginRight="20dp"
    android:layout_marginTop="40dp"
    android:gravity="center_vertical"
    >

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="账号："
        android:textSize="25sp"
        />

    <EditText
        android:id="@+id/et_account"
        android:layout_width="match_parent"
        android:hint="请输入用户名或手机号"
        android:layout_marginLeft="10dp"
        style="@style/MyEditStyle"
        android:inputType="text"
        />
</LinearLayout>

## 图8
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:layout_marginLeft="20dp"
    android:layout_marginRight="20dp"
    android:layout_marginTop="20dp"
    android:gravity="center_vertical"
    >

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="密码："
        android:textSize="25sp"
        />

    <EditText
        android:id="@+id/et_password"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:hint="请输入密码"
        android:textSize="18sp"
android:layout_marginLeft="10dp"
    android:paddingLeft="5dp"
    android:inputType="numberPassword"
    android:background="@drawable/edit_text_bg"
    />
</LinearLayout>

## 图9
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:layout_marginLeft="20dp"
    android:layout_marginTop="20dp"
    android:layout_marginRight="20dp"
    android:gravity="center"
    >

    <CheckBox
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="记住密码"
        />

    <CheckBox
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="自动登录"
        android:layout_marginLeft="40dp"
        />

</LinearLayout>

## 图10
<Button
    android:id="@+id/btn_login"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="登录"
    style="@style/MyBtnStyle"
    />

<TextView
    android:id="@+id/tv_register"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textColor="@color/colorPrimary"
    android:text="还没有账号？"
    android:layout_gravity="right"
    android:layout_marginRight="20dp"
    android:layout_marginTop="10dp"
    />

## 图11
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".RegisterActivity">
    <LinearLayout ...>
    </LinearLayout>
    <LinearLayout ...>
    </LinearLayout>
    <LinearLayout ...>
    </LinearLayout>
    <Button ...>
    </Button>
    <RadioButton ...>
    </RadioButton>
</LinearLayout>
```

## 图12
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginLeft="20dp"
    android:layout_marginTop="40dp"
    android:layout_marginRight="20dp"
    android:gravity="center_vertical"
    android:orientation="horizontal">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="账号　|　"
        android:textSize="25sp" />
    <EditText
        android:id="@+id/et_account"
        style="@style/MyEditStyle"
        android:layout_width="match_parent"
        android:layout_marginLeft="10dp"
        android:hint="请输入用户名或手机号"
        android:inputType="text"/>
</LinearLayout>
```