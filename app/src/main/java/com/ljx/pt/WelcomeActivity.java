package com.ljx.pt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 登录后欢迎页面，显示用户名，提供进入待办列表和退出登录功能
 */
public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogout;
    private Button btnTodoList;
    private TextView tvWelcome;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        userName = getIntent().getStringExtra("userName");

        tvWelcome = findViewById(R.id.tv_welcome);
        btnLogout = findViewById(R.id.btn_logout);
        btnTodoList = findViewById(R.id.btn_todo_list);

        tvWelcome.setText(getString(R.string.welcome_back, userName != null ? userName : "用户"));
        btnLogout.setOnClickListener(this);
        btnTodoList.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_todo_list) {
            Intent intent = new Intent(this, TodoListActivity.class);
            intent.putExtra("user_id", getIntent().getLongExtra("user_id", -1L));
            startActivity(intent);
            return;
        }
        SharedPreferences spf = getSharedPreferences("user_info", MODE_PRIVATE);
        spf.edit().clear().apply();

        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
