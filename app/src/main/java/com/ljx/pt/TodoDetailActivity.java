package com.ljx.pt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.ljx.pt.R;
import com.ljx.pt.bean.Todo;
import com.ljx.pt.dao.TodoDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 待办详情页面，展示待办完整信息，支持状态切换、编辑和删除操作 */
public class TodoDetailActivity extends AppCompatActivity {

    private TextView tvTitle;
    private Chip chipStatus;
    private CheckBox cbDone;
    private TextView tvContent;
    private TextView tvTime;
    private Button btnDelete;
    private MaterialToolbar toolbar;
    private TodoDao todoDao;
    private long todoId = -1;
    private Todo currentTodo;
    private CompoundButton.OnCheckedChangeListener doneListener;

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        tvTitle = findViewById(R.id.tv_detail_title);
        chipStatus = findViewById(R.id.tv_detail_status);
        cbDone = findViewById(R.id.cb_detail_done);
        tvContent = findViewById(R.id.tv_detail_content);
        tvTime = findViewById(R.id.tv_detail_time);
        btnDelete = findViewById(R.id.btn_detail_delete);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 设置 Toolbar 右侧菜单（编辑按钮）
        toolbar.inflateMenu(R.menu.menu_todo_detail);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                Intent intent = new Intent(TodoDetailActivity.this, TodoEditActivity.class);
                intent.putExtra(TodoEditActivity.EXTRA_TODO_ID, todoId);
                intent.putExtra("user_id", getIntent().getLongExtra("user_id", -1L));
                startActivity(intent);
                return true;
            }
            return false;
        });

        todoId = getIntent().getLongExtra("todo_id", -1);
        if (todoId == -1) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        todoDao = new TodoDao(this, getIntent().getLongExtra("user_id", -1L));

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(getString(R.string.dialog_delete_message, currentTodo != null ? currentTodo.getTitle() : ""))
                    .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                        new Thread(() -> {
                            todoDao.delete(todoId);
                            runOnUiThread(() -> {
                                Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                                new Handler(getMainLooper()).postDelayed(() -> finish(), 800);
                            });
                        }).start();
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        });

        doneListener = (buttonView, isChecked) -> {
            new Thread(() -> {
                todoDao.updateStatus(todoId, isChecked);
                runOnUiThread(() -> {
                    if (currentTodo != null) {
                        currentTodo.setDone(isChecked);
                        updateChipStatus(isChecked);
                    }
                });
            }).start();
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodo();
    }

    private void loadTodo() {
        new Thread(() -> {
            Todo todo = todoDao.queryById(todoId);
            runOnUiThread(() -> {
                if (todo == null) {
                    Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentTodo = todo;
                tvTitle.setText(todo.getTitle());
                updateChipStatus(todo.isDone());
                cbDone.setOnCheckedChangeListener(null);
                cbDone.setChecked(todo.isDone());
                cbDone.setOnCheckedChangeListener(doneListener);
                tvContent.setText(TextUtils.isEmpty(todo.getContent()) ? "无内容" : todo.getContent());
                tvTime.setText(DATE_FMT.format(new Date(todo.getCreateTime())));
            });
        }).start();
    }

    private void updateChipStatus(boolean isDone) {
        chipStatus.setText(isDone ? "已完成" : "未完成");
        chipStatus.setChipBackgroundColorResource(isDone ? R.color.green_500 : R.color.surface_variant);
        chipStatus.setTextColor(getColor(isDone ? R.color.white : R.color.on_surface_variant));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (todoDao != null) {
            todoDao.close();
        }
    }
}
