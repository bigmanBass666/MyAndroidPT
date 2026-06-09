package com.ljx.pt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

/** 待办编辑 Activity，从列表 FAB 新增（EXTRA_TODO_ID=-1）或从列表项点击编辑（携带 id） */
public class TodoEditActivity extends AppCompatActivity {

    public static final String EXTRA_TODO_ID = "extra_todo_id";
    private static final int MODE_NEW = -1;
    private EditText etTitle, etContent;
    private Button btnSave, btnCancel;
    private TodoDBHelper dbHelper;
    private int todoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        dbHelper = new TodoDBHelper(this);

        todoId = getIntent().getIntExtra(EXTRA_TODO_ID, -1);
        if (todoId != -1) {
            getSupportActionBar().setTitle("编辑待办");
            loadTodo(todoId);
        } else {
            getSupportActionBar().setTitle("新增待办");
        }

        btnSave.setOnClickListener(v -> saveTodo());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadTodo(int id) {
        new Thread(() -> {
            Todo todo = dbHelper.queryById(id);
            runOnUiThread(() -> {
                if (todo != null) {
                    etTitle.setText(todo.getTitle());
                    etContent.setText(todo.getContent());
                }
            });
        }).start();
    }

    private void saveTodo() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = etContent.getText().toString().trim();
        new Thread(() -> {
            if (todoId != -1) {
                Todo todo = new Todo(title, content);
                todo.setId(todoId);
                dbHelper.update(todo);
            } else {
                Todo todo = new Todo(title, content);
                dbHelper.insert(todo);
            }
            runOnUiThread(() -> {
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
