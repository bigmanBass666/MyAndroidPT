package com.ljx.pt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ljx.pt.adapter.TodoAdapter;
import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

import java.util.ArrayList;
import java.util.List;

public class TodoListActivity extends AppCompatActivity implements TodoAdapter.OnTodoActionListener {

    private RecyclerView rvTodo;
    private TodoAdapter adapter;
    private TodoDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        rvTodo = findViewById(R.id.rv_todo);
        rvTodo.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new TodoDBHelper(this);
        adapter = new TodoAdapter(this);
        rvTodo.setAdapter(adapter);

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
            List<Todo> list = dbHelper.queryAll();
            runOnUiThread(() -> adapter.setTodos(list));
        }).start();
    }

    @Override
    public void onToggleDone(int todoId, boolean isDone) {
        new Thread(() -> {
            dbHelper.updateStatus(todoId, isDone);
            runOnUiThread(this::loadTodos);
        }).start();
    }

    @Override
    public void onDelete(int todoId) {
        new Thread(() -> {
            dbHelper.delete(todoId);
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                loadTodos();
            });
        }).start();
    }

    @Override
    public void onItemClick(int todoId) {
        Intent intent = new Intent(TodoListActivity.this, TodoDetailActivity.class);
        intent.putExtra("todo_id", todoId);
        startActivity(intent);
    }
}
