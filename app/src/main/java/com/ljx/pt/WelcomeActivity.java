package com.ljx.pt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ljx.pt.bean.Todo;
import com.ljx.pt.dao.TodoDao;

import java.util.List;

/**
 * 登录后欢迎页面，显示用户名，提供进入待办列表和退出登录功能
 */
public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogout;
    private Button btnTodoList;
    private TextView tvWelcome;
    private String userName;
    private long userId;
    private Button btnQuickAdd;
    private TextView tvDoneCount;
    private TextView tvPendingCount;
    private View dashboardContainer;

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

        userId = getIntent().getLongExtra("user_id", -1L);
        btnQuickAdd = findViewById(R.id.btn_quick_add);
        tvDoneCount = findViewById(R.id.tv_done_count);
        tvPendingCount = findViewById(R.id.tv_pending_count);
        dashboardContainer = findViewById(R.id.dashboard_container);
        btnQuickAdd.setOnClickListener(this);
        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_todo_list) {
            Intent intent = new Intent(this, TodoListActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            return;
        }
        if (v.getId() == R.id.btn_quick_add) {
            Intent intent = new Intent(this, TodoEditActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            return;
        }
        SharedPreferences spf = getSharedPreferences("user_info", MODE_PRIVATE);
        spf.edit().putBoolean("isAutoLogin", false).apply();

        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadDashboardData() {
        new Thread(() -> {
            TodoDao todoDao = new TodoDao(this, userId);
            try {
                List<Todo> allTodos = todoDao.queryAll();
                int done = 0, pending = 0;
                for (Todo t : allTodos) {
                    if (t.isDone()) done++;
                    else pending++;
                }
                final int doneFinal = done;
                final int pendingFinal = pending;
                runOnUiThread(() -> {
                    if (doneFinal == 0 && pendingFinal == 0) {
                        tvDoneCount.setText("—");
                        tvPendingCount.setText("—");
                    } else {
                        tvDoneCount.setText(String.valueOf(doneFinal));
                        tvPendingCount.setText(String.valueOf(pendingFinal));
                    }
                });
            } finally {
                todoDao.close();
            }
        }).start();
    }
}
