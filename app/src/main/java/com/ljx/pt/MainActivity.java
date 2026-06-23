package com.ljx.pt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.ljx.pt.bean.User;
import com.ljx.pt.dao.UserDao;

import com.ljx.pt.RegisterActivity;

/** 登录页面，提供用户登录功能（账号/密码 + 记住密码 + 自动登录） */
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private EditText etAccount;
    private EditText etPassword;
    private Button btnLogin;
    private CheckBox cbRemember;
    private CheckBox cbAutoLogin;
    private UserDao userDao;
    private TextView tvRegister;

    private ActivityResultLauncher<Intent> registerLauncher;

    /**
     * 初始化登录页面 UI，绑定 CheckBox 联动监听器，
     * 注册 registerForActivityResult 以接收注册页回传的用户名密码，
     * 最后检查 SharedPreferences 判断是否自动登录或记住密码。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password); // 注：inputType 已由 numberPassword 修复为 textPassword，支持字母输入
        btnLogin = findViewById(R.id.btn_login);
        cbRemember = findViewById(R.id.cb_pass_remember);
        cbAutoLogin = findViewById(R.id.cb_auto_login);
        tvRegister = findViewById(R.id.tv_register);

        cbAutoLogin.setOnCheckedChangeListener(this);
        cbRemember.setOnCheckedChangeListener(this);

        // 登录按钮：子线程校验账号密码，区分「用户不存在」/「密码错误」/「成功」三种结果
        btnLogin.setOnClickListener(v -> {
            String name = etAccount.getText().toString().trim();
            String psw = etPassword.getText().toString().trim();
            if (name.isEmpty() || psw.isEmpty()) {
                Toast.makeText(MainActivity.this, "输入信息不完整，请重新输入！", Toast.LENGTH_SHORT).show();
                return;
            }
            btnLogin.setEnabled(false);
            btnLogin.setText(R.string.btn_logging_in);
            new Thread(() -> {
                userDao = new UserDao(MainActivity.this);
                UserDao.LoginResult result = userDao.login(name, psw);
                runOnUiThread(() -> {
                    if (result == UserDao.LoginResult.USER_NOT_FOUND) {
                        Toast.makeText(MainActivity.this, "该用户不存在", Toast.LENGTH_SHORT).show();
                    } else if (result == UserDao.LoginResult.WRONG_PASSWORD) {
                        Toast.makeText(MainActivity.this, "密码错误，请重新输入！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
                        User user = userDao.findByName(name);
                        long userId = user != null ? user.getId() : 0;
                        saveLoginState(name, psw, userId);
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                        intent.putExtra("userName", name);
                        intent.putExtra("user_id", userId);
                        startActivity(intent);
                        new Handler(getMainLooper()).postDelayed(() -> finish(), 800);
                    }
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.btn_login);
                });
            }).start();
        });

        // 注册页面回调：接收注册成功后回传的用户名密码，自动填入登录输入框
        registerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String userName = result.getData().getStringExtra(RegisterActivity.EXTRA_USER_NAME);
                    String password = result.getData().getStringExtra(RegisterActivity.EXTRA_PASSWORD);
                    if (userName != null && password != null) {
                        etAccount.setText(userName);
                        etPassword.setText(password);
                    }
                }
            }
        );

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            registerLauncher.launch(intent);
        });

        initData();
    }

    /**
     * 保存登录状态到 SharedPreferences，仅在「记住密码」勾选时才持久化账号密码，
     * 否则清除记住状态。
     */
    private void saveLoginState(String name, String psw, long userId) {
        SharedPreferences spf = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        if (cbRemember.isChecked()) {
            editor.putString("userName", name);
            editor.putString("password", psw);
            editor.putLong("userId", userId);
            editor.putBoolean("isRemember", true);
            editor.putBoolean("isAutoLogin", cbAutoLogin.isChecked());
        } else {
            editor.putBoolean("isRemember", false);
            editor.putBoolean("isAutoLogin", false);
        }
        editor.commit();
    }

    /** 读取 SharedPreferences，根据记住密码/自动登录状态恢复输入框或执行自动登录 */
    private void initData() {
        SharedPreferences spf = getSharedPreferences("user_info", MODE_PRIVATE);
        boolean isRemember = spf.getBoolean("isRemember", false);
        boolean isAutoLogin = spf.getBoolean("isAutoLogin", false);
        String userName = spf.getString("userName", "");
        String password = spf.getString("password", "");
        if (isAutoLogin && !userName.isEmpty() && !password.isEmpty()) {
            performAutoLogin(userName, password);
        } else if (isRemember) {
            etAccount.setText(userName);
            etPassword.setText(password);
            cbRemember.setChecked(true);
            cbAutoLogin.setChecked(isAutoLogin);
        }
    }

    /** 子线程执行自动登录校验，成功则直接跳转到欢迎页，失败提示手动登录 */
    private void performAutoLogin(String name, String psw) {
        new Thread(() -> {
            userDao = new UserDao(MainActivity.this);
            User user = userDao.findByName(name);
            long userId = user != null ? user.getId() : 0;
            runOnUiThread(() -> {
                if (user != null && user.getPsw().equals(psw)) {
                    Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                    intent.putExtra("userName", name);
                    intent.putExtra("user_id", userId);
                    startActivity(intent);
                    new Handler(getMainLooper()).postDelayed(() -> finish(), 800);
                } else {
                    Toast.makeText(MainActivity.this, "自动登录失败，请手动登录", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * CheckBox 联动逻辑：勾选「自动登录」→ 自动勾选「记住密码」；
     * 取消「记住密码」→ 自动取消「自动登录」。
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == cbAutoLogin) {
            if (isChecked) cbRemember.setChecked(true);
        } else if (buttonView == cbRemember) {
            if (!isChecked) cbAutoLogin.setChecked(false);
        }
    }
    /** 关闭数据库连接，释放资源 */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDao != null) {
            userDao.close();
        }
    }
}
