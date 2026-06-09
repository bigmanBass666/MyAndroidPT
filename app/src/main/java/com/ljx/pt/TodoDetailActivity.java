package com.ljx.pt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

public class TodoDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvContent, tvTime, tvStatus;
    private CheckBox cbDone;
    private TodoDBHelper dbHelper;
    private int todoId = -1;
    private Todo todo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvTitle = findViewById(R.id.tv_detail_title);
        tvContent = findViewById(R.id.tv_detail_content);
        tvTime = findViewById(R.id.tv_detail_time);
        tvStatus = findViewById(R.id.tv_detail_status);
        cbDone = findViewById(R.id.cb_detail_done);

        dbHelper = new TodoDBHelper(this);

        todoId = getIntent().getIntExtra(TodoEditActivity.EXTRA_TODO_ID, -1);
        if (todoId == -1) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTodo();
    }

    private void loadTodo() {
        new Thread(() -> {
            todo = dbHelper.queryById(todoId);
            if (todo == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "待办不存在", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
            runOnUiThread(this::bindData);
        }).start();
    }

    private void bindData() {
        tvTitle.setText(todo.getTitle());
        tvContent.setText(todo.getContent() != null && !todo.getContent().isEmpty()
                ? todo.getContent() : "（无内容）");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        tvTime.setText("创建时间：" + sdf.format(new java.util.Date(todo.getCreateTime())));
        cbDone.setChecked(todo.isDone());
        updateStatusText();

        findViewById(R.id.btn_detail_edit).setOnClickListener(v -> {
            if (todo == null) return;
            Intent intent = new Intent(this, TodoEditActivity.class);
            intent.putExtra(TodoEditActivity.EXTRA_TODO_ID, todoId);
            startActivity(intent);
        });

        findViewById(R.id.btn_detail_delete).setOnClickListener(v -> {
            if (todo == null) return;
            new AlertDialog.Builder(this)
                    .setTitle("确认删除")
                    .setMessage("确定要删除\"" + todo.getTitle() + "\"吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        new Thread(() -> {
                            dbHelper.delete(todoId);
                            runOnUiThread(() -> {
                                setResult(RESULT_OK);
                                finish();
                            });
                        }).start();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        cbDone.setOnCheckedChangeListener(null);
        cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Thread(() -> {
                dbHelper.updateStatus(todoId, isChecked);
                runOnUiThread(() -> {
                    if (todo != null) todo.setDone(isChecked);
                    updateStatusText();
                });
            }).start();
        });
    }

    private void updateStatusText() {
        tvStatus.setText(todo.isDone() ? "状态：已完成" : "状态：未完成");
        tvStatus.setTextColor(todo.isDone() ? 0xFFFF9800 : 0xFF4CAF50);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
