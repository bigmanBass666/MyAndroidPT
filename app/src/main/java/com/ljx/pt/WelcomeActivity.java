package com.ljx.pt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ljx.pt.bean.Todo;
import com.ljx.pt.dao.TodoDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 登录后欢迎页面，显示用户信息和待办统计概览，提供快速操作入口
 */
public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private TextView tvWelcome;
    private TextView tvDoneCount;
    private TextView tvPendingCount;
    private LinearLayout recentTodosContainer;
    private TextView tvEmptyRecent;
    private long userId;

    /**
     * 初始化欢迎页 UI，设置 Toolbar 为 ActionBar，加载用户名欢迎语，
     * 绑定快速操作按钮和 FAB 点击事件，最后异步加载仪表盘数据。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        String userName = getIntent().getStringExtra("userName");
        userId = getIntent().getLongExtra("user_id", -1L);

        // Toolbar setup as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Welcome text
        tvWelcome = findViewById(R.id.tv_welcome);
        tvWelcome.setText(getString(R.string.welcome_back, userName != null ? userName : "用户"));

        // Stats
        tvDoneCount = findViewById(R.id.tv_done_count);
        tvPendingCount = findViewById(R.id.tv_pending_count);

        // Recent todos
        recentTodosContainer = findViewById(R.id.recent_todos_container);
        tvEmptyRecent = findViewById(R.id.tv_empty_recent);

        // Quick add action in stats card
        findViewById(R.id.action_new).setOnClickListener(this);

        // FAB
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(this);

        loadDashboardData();
    }

    /**
     * 每次页面重新可见时刷新仪表盘数据，确保待办统计与最新数据同步。
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    /**
     * 加载欢迎页的菜单布局文件
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    /**
     * 处理菜单项点击：跳转到待办列表、退出登录（清除自动登录状态）、设置（占位）
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_todo_list) {
            Intent intent = new Intent(this, TodoListActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            return true;
        }
        if (itemId == R.id.action_logout) {
            SharedPreferences spf = getSharedPreferences("user_info", MODE_PRIVATE);
            spf.edit().putBoolean("isAutoLogin", false).apply();
            Toast.makeText(this, R.string.toast_logout_success, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            new Handler(getMainLooper()).postDelayed(() -> finish(), 800);
            return true;
        }
        if (itemId == R.id.action_settings) {
            Toast.makeText(this, R.string.settings_placeholder, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * FAB 和快速新建按钮点击处理：跳转到待办编辑页
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.action_new || id == R.id.fab_add) {
            Intent intent = new Intent(this, TodoEditActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        }
    }

    /**
     * 子线程查询所有待办，统计已完成/待办数量，
     * 取最近 3 条展示在仪表盘，通过 runOnUiThread 更新 UI。
     */
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

                // Recent 3 todos (queryAll already ordered by create_time DESC)
                final int recentCount = Math.min(allTodos.size(), 3);
                final List<Todo> recentList = allTodos.subList(0, recentCount);

                runOnUiThread(() -> {
                    // Update stats
                    if (doneFinal == 0 && pendingFinal == 0) {
                        tvDoneCount.setText("0");
                        tvPendingCount.setText("0");
                    } else {
                        tvDoneCount.setText(String.valueOf(doneFinal));
                        tvPendingCount.setText(String.valueOf(pendingFinal));
                    }

                    // Update recent todos preview
                    updateRecentTodos(recentList);
                });
            } finally {
                todoDao.close();
            }
        }).start();
    }

    /**
     * 纯代码动态构建最近待办列表：遍历 todos 创建 LinearLayout + 状态指示器 + 分割线，
     * 不使用 XML 布局，通过 TypedValue 解析主题色适配深色模式。
     */
    private void updateRecentTodos(List<Todo> todos) {
        recentTodosContainer.removeAllViews();
        if (todos.isEmpty()) {
            tvEmptyRecent.setVisibility(View.VISIBLE);
            return;
        }
        tvEmptyRecent.setVisibility(View.GONE);

        // Resolve theme colors for programmatic views
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorOnSurface, typedValue, true);
        int onSurfaceColor = typedValue.data;
        getTheme().resolveAttribute(R.attr.colorOnSurfaceVariant, typedValue, true);
        int onSurfaceVariantColor = typedValue.data;

        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < todos.size(); i++) {
            final Todo todo = todos.get(i);

            // Add divider between items
            if (i > 0) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * density)));
                divider.setBackgroundResource(R.color.surface_variant);
                recentTodosContainer.addView(divider);
            }

            // Item row
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, (int) (12 * density), 0, (int) (12 * density));
            row.setClickable(true);
            row.setFocusable(true);

            // Resolve selectableItemBackground for ripple effect
            TypedValue backgroundTv = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, backgroundTv, true);
            row.setBackgroundResource(backgroundTv.resourceId);

            final long todoId = todo.getId();
            row.setOnClickListener(v -> {
                Intent intent = new Intent(WelcomeActivity.this, TodoDetailActivity.class);
                intent.putExtra("todo_id", todoId);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            });

            // Status indicator
            int indicatorColor = todo.isDone()
                    ? getColor(R.color.green_500)
                    : getColor(R.color.grey_500);
            TextView tvStatus = new TextView(this);
            tvStatus.setText(todo.isDone() ? "✓" : "☐");
            tvStatus.setTextSize(18);
            tvStatus.setTextColor(indicatorColor);
            LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            statusLp.gravity = Gravity.CENTER_VERTICAL;
            tvStatus.setLayoutParams(statusLp);
            row.addView(tvStatus);

            // Title + time column
            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            textLayout.setPadding((int) (12 * density), 0, 0, 0);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(todo.getTitle());
            tvTitle.setTextSize(16);
            tvTitle.setMaxLines(1);
            tvTitle.setEllipsize(TextUtils.TruncateAt.END);
            tvTitle.setTextColor(onSurfaceColor);
            textLayout.addView(tvTitle);

            TextView tvTime = new TextView(this);
            tvTime.setText(DATE_FMT.format(new Date(todo.getCreateTime())));
            tvTime.setTextSize(12);
            tvTime.setTextColor(onSurfaceVariantColor);
            textLayout.addView(tvTime);

            row.addView(textLayout);
            recentTodosContainer.addView(row);
        }
    }
}
