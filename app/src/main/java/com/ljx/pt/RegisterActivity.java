package com.ljx.pt;

import com.ljx.pt.bean.User;
import com.ljx.pt.dao.UserDao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import androidx.appcompat.app.AppCompatActivity;

/** 注册页面，提供用户注册功能（账号/密码/确认密码/邮箱） */
public class RegisterActivity extends AppCompatActivity {

    public static final String EXTRA_USER_NAME = "userName";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_EMAIL = "email";

    private EditText etAccount;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private EditText etEmail;
    private UserDao userDao;
    private Button btnRegister;
    private CheckBox rbAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        etEmail = findViewById(R.id.et_email);
        btnRegister = findViewById(R.id.btn_register);
        rbAgree = findViewById(R.id.rb_agree);

        btnRegister.setOnClickListener(v -> {
            String name = etAccount.getText().toString().trim();
            String psw = etPassword.getText().toString().trim();
            String pswConfirm = etPasswordConfirm.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (name.isEmpty() || psw.isEmpty() || pswConfirm.isEmpty()) {
                Toast.makeText(this, "输入信息不完整，请重新输入！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 密码强度校验：长度至少6位
            if (psw.length() < 6) {
                Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
                return;
            }

            // 密码强度校验：必须包含字母和数字
            boolean hasLetter = false;
            boolean hasDigit = false;
            for (char c : psw.toCharArray()) {
                if (Character.isLetter(c)) {
                    hasLetter = true;
                } else if (Character.isDigit(c)) {
                    hasDigit = true;
                }

                if (hasLetter && hasDigit) {
                    break;
                }

            }

            if (!hasLetter || !hasDigit) {
                Toast.makeText(this, "密码必须包含字母和数字", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!psw.equals(pswConfirm)) {
                Toast.makeText(this, "两次输入的密码不一致，请重新输入！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 邮箱格式校验
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                Toast.makeText(this, "请输入正确的邮箱地址", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!rbAgree.isChecked()) {
                Toast.makeText(this, "请勾选同意用户协议", Toast.LENGTH_SHORT).show();
                return;
            }


            new Thread(() -> {
                userDao = new UserDao(RegisterActivity.this);
                if (userDao.findByName(name) != null) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "该用户名已被注册", Toast.LENGTH_SHORT).show());
                    return;
                }

                int rows = userDao.insert(new User(name, psw, email));
                runOnUiThread(() -> {
                    if (rows > 0) {
                        Toast.makeText(this, R.string.toast_register_success, Toast.LENGTH_SHORT).show();
                        // 注册成功后将用户信息回传给登录页
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_USER_NAME, name);
                        intent.putExtra(EXTRA_PASSWORD, psw);
                        intent.putExtra(EXTRA_EMAIL, email);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Toast.makeText(this, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDao != null) {
            userDao.close();
        }
    }


}
