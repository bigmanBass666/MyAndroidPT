package com.ljx.pt;

import com.ljx.pt.bean.User;
import com.ljx.pt.dao.UserDao;

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

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private EditText etAccount;
    private EditText etPassword;
    private Button btnLogin;
    private CheckBox cbRemember;
    private CheckBox cbAutoLogin;
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
                Toast.makeText(MainActivity.this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                UserDao dao = new UserDao(MainActivity.this);
                User user = dao.findByName(name);
                runOnUiThread(() -> {
                    if (user == null) {
                        Toast.makeText(MainActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                    } else if (!user.getPsw().equals(psw)) {
                        Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.toast_login_success, Toast.LENGTH_SHORT).show();
                        saveLoginState(name, psw);
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                        intent.putExtra("userName", name);
                        startActivity(intent);
                        finish();
                    }
                });
            }).start();
        });

        registerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String userName = result.getData().getStringExtra("userName");
                        String password = result.getData().getStringExtra("password");
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

    private void saveLoginState(String name, String psw) {
        SharedPreferences spf = getSharedPreferences("spfRecord", MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        if (cbRemember.isChecked()) {
            editor.putString("userName", name);
            editor.putString("password", psw);
            editor.putBoolean("isRemember", true);
            editor.putBoolean("isAutoLogin", cbAutoLogin.isChecked());
        } else {
            editor.putBoolean("isRemember", false);
            editor.putBoolean("isAutoLogin", false);
        }
        editor.apply();
    }

    private void initData() {
        SharedPreferences spf = getSharedPreferences("spfRecord", MODE_PRIVATE);
        boolean isRemember = spf.getBoolean("isRemember", false);
        boolean isAutoLogin = spf.getBoolean("isAutoLogin", false);
        String userName = spf.getString("userName", "");
        String password = spf.getString("password", "");
        if (isAutoLogin && !userName.isEmpty() && !password.isEmpty()) {
            performLogin(userName, password);
        } else if (isRemember) {
            etAccount.setText(userName);
            etPassword.setText(password);
            cbRemember.setChecked(true);
        }
    }

    private void performLogin(String name, String psw) {
        new Thread(() -> {
            UserDao dao = new UserDao(MainActivity.this);
            User user = dao.findByName(name);
            runOnUiThread(() -> {
                if (user != null && user.getPsw().equals(psw)) {
                    Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                    intent.putExtra("userName", name);
                    startActivity(intent);
                    finish();
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
}
