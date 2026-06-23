package com.ljx.pt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.ljx.pt.bean.Todo;
import com.ljx.pt.dao.TodoDao;

/** 新增/编辑待办页面，根据 EXTRA_TODO_ID 区分新增或编辑模式 */
public class TodoEditActivity extends AppCompatActivity {

    public static final String EXTRA_TODO_ID = "extra_todo_id";

    private EditText etTitle;
    private EditText etContent;
    private MaterialToolbar toolbar;
    private TextInputLayout tilTitle;

    private TodoDao todoDao;
    private long todoId = -1;
    private boolean isEditMode = false;

    /** 初始化 UI，区分新增/编辑模式——EXTRA_TODO_ID=-1 为新增，否则为编辑 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_edit);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilTitle = findViewById(R.id.til_title);

        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);

        long userId = getIntent().getLongExtra("user_id", -1L);
        todoDao = new TodoDao(this, userId);

        todoId = getIntent().getLongExtra(EXTRA_TODO_ID, -1);
        isEditMode = todoId != -1;

        if (isEditMode) {
            getSupportActionBar().setTitle(R.string.title_todo_edit);
            loadTodo();
        } else {
            getSupportActionBar().setTitle(R.string.title_todo_add);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_todo_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveTodo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveTodo() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            tilTitle.setError("标题不能为空");
            return;
        } else {
            tilTitle.setError(null);
        }
        new Thread(() -> {
            Todo todo = new Todo();
            todo.setTitle(title);
            todo.setContent(content);
            if (isEditMode) {
                todo.setId(todoId);
                todoDao.update(todo);
            } else {
                todoDao.insert(todo);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                new Handler(getMainLooper()).postDelayed(() -> finish(), 800);
            });
        }).start();
    }

    private void loadTodo() {
        if (!isEditMode) return;
        new Thread(() -> {
            Todo todo = todoDao.queryById(todoId);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (todoDao != null) {
            todoDao.close();
        }
    }
}
