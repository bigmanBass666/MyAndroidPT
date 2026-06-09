package com.ljx.pt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
            startActivity(new Intent(this, TodoListActivity.class));
            return;
        }
        SharedPreferences spf = getSharedPreferences("spfRecord", MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.putBoolean("isAutoLogin", false);
        editor.apply();

        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
