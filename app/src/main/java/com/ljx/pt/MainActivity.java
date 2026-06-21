package com.ljx.pt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        cbRemember = findViewById(R.id.cb_pass_remember);
        cbAutoLogin = findViewById(R.id.cb_auto_login);
        tvRegister = findViewById(R.id.tv_register);

        cbAutoLogin.setOnCheckedChangeListener(this);
        cbRemember.setOnCheckedChangeListener(this);

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
                        finish();
                    }
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.btn_login);
                });
            }).start();
        });

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
        editor.apply();
    }

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
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "自动登录失败，请手动登录", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == cbAutoLogin) {
            if (isChecked) cbRemember.setChecked(true);
        } else if (buttonView == cbRemember) {
            if (!isChecked) cbAutoLogin.setChecked(false);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDao != null) {
            userDao.close();
        }
    }
}
