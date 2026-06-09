## 图1

- 登录
- 账号：Irq
- 密码：
- 记住密码
- 自动登录
- 登录
- 还没有账号？

## 图2

```java
Log.i(tag, msg, throwable);
//勾选记住密码
if(cb_pass_remember.isChecked()){
    SharedPreferences spf=getSharedPreferences("spfRecord", MODE_PRIVATE);
    SharedPreferences.Editor editor= spf.edit();
    editor.putString("userName", name);
    editor.putBoolean("password", psW);
    editor.putBoolean("isRemember", true);
    editor.apply();
} else{
    SharedPreferences spf=getSharedPreferences("spfRecord", MODE_PRIVATE);
    SharedPreferences.Editor editor= spf.edit();
    editor.putBoolean("isRemember", false);
    editor.apply();
}
Intent intent = new Intent(packageContext: MainActiviy.this, WelcomeActivity.class);
intent.putExtra(name, "userName", name);
startActivity(intent);
MainActiviy.this.finish();
//跳到欢迎页面后，当前的登录页面就销毁吧
```

## 图3

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getSupportActionBar().setTitle("登录");
    btnLogin = findViewById(R.id.btn_login);
    etAccount = findViewById(R.id.et_account);
    etPassword = findViewById(R.id.et_password);
    tv_register = findViewById(R.id.tv_register);
    cb_pass_remember = findViewById(R.id.cb_pass_remember);
    initDate();  //用于勾选了记住密码后，初始化界面时，并从SharedPreferences里拿到用户名和密码。
    tv_register.setOnClickListener(new View.OnClickListener() {
```

## 图4

```java
private void initData() {
    SharedPreferences spf=getSharedPreferences("spfRecord", MODE_PRIVATE);
    Boolean isRemember= spf.getBoolean(key: "isRemember", defValue: false);
    String userName = spf.getString(key: "userName", defValue: "");
    String password = spf.getString(key: "password", defValue: "");
    if(isRemember){
        etAccount.setText(userName);
        etPassword.setText(password);
        cb_pass_remember.setChecked(true);
    }
}
```
