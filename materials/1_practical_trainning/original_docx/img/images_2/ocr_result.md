## 图1
登录
账号：请输入用户名或手机号
密码：请输入密码
记住密码
自动登录
登录
还没有账号?

## 图2
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

private Button btnRegister;
private EditText etAccount,etPass,etPassConfirm;
private RadioButton rbAgree;

@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_register);
getSupportActionBar().setTitle("注册");

etAccount = findViewById(R.id.et_account);
etPass = findViewById(R.id.et_password);
etPassConfirm = findViewById(R.id.et_password_confirm);
rbAgree = findViewById(R.id.rb_agree);
btnRegister = findViewById(R.id.btn_register);

btnRegister.setOnClickListener(this);
}

}

## 图3
账号：请输入用户名或手机号
密码：请输入密码
确认密码：再次输入密码
注册
同意用户协议
```java
@Override
public void onClick(View v) {
String name = etAccount.getText().toString();
String pass = etPass.getText().toString();
String passConfirm = etPassConfirm.getText().toString();
if(TextUtils.isEmpty(name)){
Toast.makeText(
context: RegisterActivity.this,
text: "用户名不能为空",
length: Toast.LENGTH_LONG).show();
return;
}
if(TextUtils.isEmpty(pass)){
Toast.makeText(
context: RegisterActivity.this,
text: "密码不能为空",
length: Toast.LENGTH_LONG).show();
return;
}
if(!TextUtils.equals(pass,passConfirm)){
Toast.makeText(
context: RegisterActivity.this,
text: "密码不一致",
length: Toast.LENGTH_LONG).show();
return;
}
if(!rbAgree.isChecked()){
Toast.makeText(
context: RegisterActivity.this,
text: "请同意用户协议",
length: Toast.LENGTH_LONG).show();
return;
}
Toast.makeText(
context: RegisterActivity.this,
text: "注册成功!",
length: Toast.LENGTH_LONG).show();
}
```

## 图4
注册
账号：请输入用户名或手机号
密码：请输入密码
确认密码：再次输入密码
注册
同意用户协议

## 图5
还没有账号?

## 图6
```
public class MainActivity extends AppCompatActivity {

private static final String TAG = "tag";
private Button btnLogin;
private EditText etAccount,etPassword;

private String userName = "lrq";
private String pass = "123";

private TextView tv_register;
}
```

## 图7
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_main);
getSupportActionBar().setTitle("登录");

btnLogin = findViewById(R.id.btn_login);
etAccount = findViewById(R.id.et_account);
etPassword = findViewById(R.id.et_password);
tv_register=findViewById(R.id.tv_register);

tv_register.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
startActivity(intent);
}
});
}
```

## 图8
```java
btnLogin.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
String account = etAccount.getText().toString();
String password = etPassword.getText().toString();
Log.d(TAG, "onClick: ----------------" + account);
Log.d(TAG, "password: ---------------" + password);
if(account.equals(userName)){
if(password.equals(pass)){
Toast.makeText(context: MainActivity.this, text:"恭喜你，登陆成功！", Toast.LENGTH_LONG).show();
}else{
Toast.makeText(context:MainActivity.this, text:"密码错误！", Toast.LENGTH_LONG).show();
}
}else{
Toast.makeText(context:MainActivity.this, text:"用户名错误！", Toast.LENGTH_LONG).show();
}
}
});
```

## 图9
账号：请输入用户名或手机号
密码：请输入密码
确认密码：再次输入密码
注册
同意用户协议
用户名不能为空

## 图10
密码不一致

## 图11
请同意用户协议