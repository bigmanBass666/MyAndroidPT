## 图1
登录
账号: lrq
密码: ……
记住密码
自动登录
登录
还没有账号？


## 图2
```java
// (1)当勾选"自动登录"时，会自动勾选"记住密码"
cb_auto_login.setCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) cb_pass_remember.setChecked(true);
    }
});
// (2)当取消"记住密码"时，会自动取消"自动登录"
cb_pass_remember.setCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!isChecked) cb_auto_login.setChecked(false);
    }
});
```

## 图3
```java
Log.i( tag: "MSG", msg: "登录成功！");

//勾选记住密码
if(cb_pass_remember.isChecked()){
    SharedPreferences spf=getSharedPreferences( name: "spfRecord",MODE_PRIVATE);
    SharedPreferences.Editor editor=
            spf.edit();
    editor.putString("userName",name);
    editor.putString("password",PSW);
    editor.putBoolean("isRemember",true);
    // //勾选自动登录
    if(cb_auto_login.isChecked()){
        editor.putBoolean("isAutoLogin",true);
    }else{
        editor.putBoolean("isAutoLogin",false);
    }
    editor.apply();
}else{ //没有勾选记住密码
    SharedPreferences spf=getSharedPreferences( name: "spfRecord",MODE_PRIVATE);
    SharedPreferences.Editor editor=
            spf.edit();
    editor.putBoolean("isRemember",false);
    editor.apply();
}
```

## 图4
```java
private void initData() {
    SharedPreferences spf=getSharedPreferences( name: "spfRecord",MODE_PRIVATE);

    //取出是否勾选记住密码
    Boolean isRemember= spf.getBoolean( key: "isRemember", defaultValue: false);

    //取出是否勾选自动登录
    Boolean isAutoLogin= spf.getBoolean( key: "isAutoLogin", defaultValue: false);

    //取出用户与密码
    String  userName= spf.getString( key: "UserName", defValue: "");
    String  password= spf.getString( key: "password", defValue: "");

    //勾选自动登录
    if(isAutoLogin){
        //跳转到欢迎页面
        Intent intent = new Intent( packageContext: MainActivity.this, WelcomeActivity.class);
        intent.putExtra( name: "userName",userName);
        startActivity(intent);
        MainActivity.this.finish(); //跳到欢迎页面后，当前的登录页面就销毁吧
    }

    //勾选记住密码
    if(isRemember){

## 图5
public void logout(View view) {
  //退出登录时，要将登录的状态设置为false;
  SharedPreferences spf=getSharedPreferences("spfRecord",MODE_PRIVATE);
  SharedPreferences.Editor editor= spf.edit();
  editor.putBoolean("isAutoLogin",false);
  editor.apply();
  Intent intent=new Intent(packageContext: this,MainActivity.class);
  startActivity(intent);
  this.finish();
}

## 图6
User ruser = new User(name, psw);
int rows = userDao.insertUser(ruser);
if (rows > 0) {
  Toast.makeText(context: RegisterActivity.this,text:"注册成功", Toast.LENGTH_SHORT).show();
  Log.i(tag:"MSG", msg:"注册成功");
}
// 注册成功后自动跳到登录页面
Intent intent =new Intent();
Bundle bundle=new Bundle();
//以打包的形式返回数据
bundle.putString("userName", name);
bundle.putString("password", psw);
intent.putExtras(bundle);
setResult(1,intent); //其中1为上一个Activity返回处理结果
RegisterActivity.this.finish();
} else {
  Toast.makeText(context: RegisterActivity.this,text:"注册失败", Toast.LENGTH_SHORT).show();
  Log.i(tag:"MSG", msg:"注册失败");
}

## 图7
//初始化组件，
//用于监听点击事件，如按钮、控件等，并获取CharSequence对象中的字符串值。
tv_register.setOnClickListener(new View.OnClickListener() {
  @Override
  public void onClick(View v) {
    Intent intent = new Intent(packageContext: MainActivity.this, RegisterActivity.class);
    //startActivity(intent);
    //数据的回传
    startActivityForResult(intent, 1);
    //其中的1为请求码，用于判断数据的来源。
  }
});

## 图8
//数据回传后，设置登录页面
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  if(requestCode==1 && resultCode==1 && data!=null){
    Bundle bundle = data.getExtras();
    String userName = bundle.getString("userName", "");
    String password = bundle.getString("password", "");
    etAccount.setText(userName);
    etPassword.setText(password);
  }
}
```