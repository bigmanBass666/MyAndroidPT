package com.ljx.pt;

import com.ljx.pt.bean.User;
import com.ljx.pt.dao.UserDao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

	private EditText etAccount;
	private EditText etPassword;
	private EditText etPasswordConfirm;
	private Button btnRegister;
	private RadioButton rbAgree;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		etAccount = findViewById(R.id.et_account);
		etPassword = findViewById(R.id.et_password);
		etPasswordConfirm = findViewById(R.id.et_password_confirm);
		btnRegister = findViewById(R.id.btn_register);
		rbAgree = findViewById(R.id.rb_agree);

		btnRegister.setOnClickListener(v -> {
			String name = etAccount.getText().toString().trim();
			String psw = etPassword.getText().toString().trim();
			String pswConfirm = etPasswordConfirm.getText().toString().trim();

			if (name.isEmpty() || psw.isEmpty() || pswConfirm.isEmpty()) {
				Toast.makeText(this, "请完整填写注册信息", Toast.LENGTH_SHORT).show();
				return;
			}
			if (!psw.equals(pswConfirm)) {
				Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
				return;
			}
			if (!rbAgree.isChecked()) {
				Toast.makeText(this, "请先同意用户协议", Toast.LENGTH_SHORT).show();
				return;
			}

			new Thread(() -> {
				UserDao dao = new UserDao(RegisterActivity.this);
				if (dao.findByName(name) != null) {
					runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "该用户名已被注册", Toast.LENGTH_SHORT).show());
					return;
				}
				int rows = dao.insert(new User(name, psw));
				runOnUiThread(() -> {
					if (rows > 0) {
						Toast.makeText(this, R.string.toast_register_success, Toast.LENGTH_SHORT).show();
						Intent intent = new Intent();
						intent.putExtra("userName", name);
						intent.putExtra("password", psw);
						setResult(RESULT_OK, intent);
						finish();
					} else {
						Toast.makeText(this, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
					}
				});
			}).start();
		});
	}
}
