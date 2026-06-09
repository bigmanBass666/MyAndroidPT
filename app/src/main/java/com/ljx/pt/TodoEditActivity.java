package com.ljx.pt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.ljx.pt.R;
import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

public class TodoEditActivity extends AppCompatActivity {

    public static final String EXTRA_TODO_ID = "extra_todo_id";

    private EditText etTitle;
    private EditText etContent;
    private Button btnCancel;
    private Button btnSave;
    private MaterialToolbar toolbar;

    private TodoDBHelper dbHelper;
    private int todoId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_edit);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);

        dbHelper = new TodoDBHelper(this);

        todoId = getIntent().getIntExtra(EXTRA_TODO_ID, -1);
        isEditMode = todoId != -1;

        if (isEditMode) {
            toolbar.setTitle("编辑待办");
            loadTodo();
        }

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                Todo todo = new Todo();
                todo.setTitle(title);
                todo.setContent(content);
                if (isEditMode) {
                    todo.setId(todoId);
                    dbHelper.update(todo);
                } else {
                    dbHelper.insert(todo);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }).start();
        });
    }

    private void loadTodo() {
        new Thread(() -> {
            Todo todo = dbHelper.queryById(todoId);
            runOnUiThread(() -> {
                if (todo != null) {
                    etTitle.setText(todo.getTitle());
                    etContent.setText(todo.getContent());
                } else {
                    Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
