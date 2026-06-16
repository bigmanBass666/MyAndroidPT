package com.ljx.pt;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ljx.pt.adapter.TodoAdapter;
import com.ljx.pt.bean.Todo;
import com.ljx.pt.dao.TodoDao;

import java.util.ArrayList;
import java.util.List;

/** 待办列表页面，展示所有待办事项，支持新增、删除和状态切换 */
public class TodoListActivity extends AppCompatActivity implements TodoAdapter.OnTodoActionListener {

    private RecyclerView rvTodo;
    private TodoAdapter adapter;
    private TodoDao todoDao;
    private TextView tvEmptyHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        rvTodo = findViewById(R.id.rv_todo);
        rvTodo.setLayoutManager(new LinearLayoutManager(this));

        todoDao = new TodoDao(this);
        adapter = new TodoAdapter(this);
        rvTodo.setAdapter(adapter);

        tvEmptyHint = findViewById(R.id.tv_empty_hint);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(TodoListActivity.this, TodoEditActivity.class);
            startActivity(intent);
        });

        loadTodos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodos();
    }

    private void loadTodos() {
        new Thread(() -> {
            List<Todo> list = todoDao.queryAll();
            runOnUiThread(() -> {
                adapter.setTodos(list);
                if (tvEmptyHint != null) {
                    tvEmptyHint.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    rvTodo.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                }
            });
        }).start();
    }

    @Override
    public void onToggleDone(long todoId, boolean isDone) {
        new Thread(() -> {
            todoDao.updateStatus(todoId, isDone);
            runOnUiThread(this::loadTodos);
        }).start();
    }

    @Override
    public void onDelete(long todoId, String todoTitle) {
        if (TextUtils.isEmpty(todoTitle)) {
            todoTitle = "";
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, todoTitle))
                .setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(() -> {
                            todoDao.delete(todoId);
                            runOnUiThread(() -> {
                                Toast.makeText(TodoListActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                                loadTodos();
                            });
                        }).start();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void onItemClick(long todoId) {
        Intent intent = new Intent(TodoListActivity.this, TodoDetailActivity.class);
        intent.putExtra("todo_id", todoId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (todoDao != null) {
            todoDao.close();
        }
    }
}
