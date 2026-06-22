package com.ljx.pt;

import com.ljx.pt.bean.Todo;
import com.ljx.pt.bean.User;
import com.ljx.pt.dao.TodoDao;
import com.ljx.pt.dao.UserDao;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

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
    private TextInputLayout tilPassword;

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
        tilPassword = findViewById(R.id.til_password);

        // 密码强度实时检测
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String psw = s.toString();
                if (psw.isEmpty()) {
                    tilPassword.setHelperText(null);
                    tilPassword.setHelperTextEnabled(false);
                    tilPassword.setError(null);
                    tilPassword.setErrorEnabled(false);
                    return;
                }

                int strength = checkPasswordStrength(psw);
                switch (strength) {
                    case 0:
                        tilPassword.setError(getString(R.string.password_weak));
                        break;
                    case 1:
                        tilPassword.setErrorEnabled(false);
                        tilPassword.setHelperText(getString(R.string.password_medium));
                        break;
                    case 2:
                        tilPassword.setErrorEnabled(false);
                        tilPassword.setHelperText(getString(R.string.password_strong));
                        break;
                }
            }
        });

        btnRegister.setOnClickListener(v -> {
            String name = etAccount.getText().toString().trim();
            String psw = etPassword.getText().toString().trim();
            String pswConfirm = etPasswordConfirm.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (name.isEmpty() || psw.isEmpty() || pswConfirm.isEmpty()) {
                Toast.makeText(this, R.string.toast_incomplete_input, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!psw.equals(pswConfirm)) {
                Toast.makeText(this, R.string.toast_password_mismatch, Toast.LENGTH_SHORT).show();
                return;
            }

            // 邮箱格式校验
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                Toast.makeText(this, R.string.toast_invalid_email, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!rbAgree.isChecked()) {
                Toast.makeText(this, R.string.toast_agree_protocol, Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);
            btnRegister.setText(R.string.btn_registering);

            new Thread(() -> {
                userDao = new UserDao(RegisterActivity.this);
                if (userDao.findByName(name) != null) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, R.string.toast_user_exists, Toast.LENGTH_SHORT).show();
                        btnRegister.setEnabled(true);
                        btnRegister.setText(R.string.btn_register);
                    });
                    return;
                }

                int rows = userDao.insert(new User(name, psw, email));
                if (rows > 0) {
                    // 为新用户创建 3 条示例待办
                    User newUser = userDao.findByName(name);
                    if (newUser != null) {
                        TodoDao todoDao = new TodoDao(RegisterActivity.this, newUser.getId());
                        long now = System.currentTimeMillis();

                        Todo t1 = new Todo();
                        t1.setTitle("欢迎使用 MyAndroid！");
                        t1.setContent("试试编辑、标记完成、删除 — 所有操作都可以在这里完成。");
                        t1.setDone(false);
                        t1.setCreateTime(now);
                        todoDao.insert(t1);

                        Todo t2 = new Todo();
                        t2.setTitle("试着标记我为已完成");
                        t2.setContent("点一下详情页的复选框，可以切换待办状态。");
                        t2.setDone(false);
                        t2.setCreateTime(now + 1000);
                        todoDao.insert(t2);

                        Todo t3 = new Todo();
                        t3.setTitle("查看待办列表");
                        t3.setContent("从右上角菜单进入完整待办列表，一目了然。");
                        t3.setDone(true);
                        t3.setCreateTime(now + 2000);
                        todoDao.insert(t3);

                        todoDao.close();
                    }
                }
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
                        Toast.makeText(this, R.string.toast_register_failed, Toast.LENGTH_SHORT).show();
                    }
                    btnRegister.setEnabled(true);
                    btnRegister.setText(R.string.btn_register);
                });
            }).start();
        });
    }

    /**
     * 检测密码强度
     * @param psw 密码字符串
     * @return 0=弱, 1=中, 2=强
     */
    private int checkPasswordStrength(String psw) {
        if (psw.length() < 6) return 0; // 弱
        boolean hasLetter = false, hasDigit = false;
        boolean hasUpper = false, hasLower = false;
        for (char c : psw.toCharArray()) {
            if (Character.isUpperCase(c)) { hasUpper = true; hasLetter = true; }
            else if (Character.isLowerCase(c)) { hasLower = true; hasLetter = true; }
            else if (Character.isDigit(c)) hasDigit = true;
        }
        // 必须同时包含字母和数字，否则为弱
        if (!hasLetter || !hasDigit) return 0; // 弱
        // 包含大小写字母和数字且长度≥8 → 强
        if (psw.length() >= 8 && hasUpper && hasLower && hasDigit) return 2; // 强
        return 1; // 中
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDao != null) {
            userDao.close();
        }
    }


}
