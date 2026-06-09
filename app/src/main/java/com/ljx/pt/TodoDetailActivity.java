package com.ljx.pt;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ljx.pt.R;
import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TodoDetailActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvStatus;
    private CheckBox cbDone;
    private TextView tvContent;
    private TextView tvTime;
    private Button btnEdit;
    private Button btnDelete;

    private TodoDBHelper dbHelper;
    private int todoId = -1;
    private Todo currentTodo;

    private static final SimpleDateFormat DATE_FMT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        tvTitle = findViewById(R.id.tv_detail_title);
        tvStatus = findViewById(R.id.tv_detail_status);
        cbDone = findViewById(R.id.cb_detail_done);
        tvContent = findViewById(R.id.tv_detail_content);
        tvTime = findViewById(R.id.tv_detail_time);
        btnEdit = findViewById(R.id.btn_detail_edit);
        btnDelete = findViewById(R.id.btn_detail_delete);

        todoId = getIntent().getIntExtra("todo_id", -1);
        if (todoId == -1) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new TodoDBHelper(this);
        loadTodo();

        cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Thread(() -> {
                dbHelper.updateStatus(todoId, isChecked);
            }).start();
            tvStatus.setText(isChecked ? "已完成" : "未完成");
            tvStatus.setTextColor(isChecked ?
                getColor(R.color.status_done) : getColor(R.color.status_undone));
        });

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(TodoDetailActivity.this, TodoEditActivity.class);
            intent.putExtra(TodoEditActivity.EXTRA_TODO_ID, todoId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, currentTodo != null ? currentTodo.getTitle() : ""))
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    new Thread(() -> {
                        dbHelper.delete(todoId);
                        runOnUiThread(() -> {
                            Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }).start();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
        });
    }

    private void loadTodo() {
        new Thread(() -> {
            Todo todo = dbHelper.queryById(todoId);
            runOnUiThread(() -> {
                if (todo == null) {
                    Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentTodo = todo;
                tvTitle.setText(todo.getTitle());
                tvStatus.setText(todo.isDone() ? "已完成" : "未完成");
                tvStatus.setTextColor(todo.isDone() ?
                    getColor(R.color.status_done) : getColor(R.color.status_undone));
                cbDone.setChecked(todo.isDone());
                tvContent.setText(TextUtils.isEmpty(todo.getContent()) ? "无内容" : todo.getContent());
                tvTime.setText(DATE_FMT.format(new Date(todo.getCreateTime())));
            });
        }).start();
    }
}
