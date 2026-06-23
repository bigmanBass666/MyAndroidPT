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
    private View emptyStateContainer;

    /**
     * 初始化 RecyclerView 及 Adapter、FAB 新增按钮、空状态提示视图，
     * 创建 TodoDao 实例并加载待办列表。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        rvTodo = findViewById(R.id.rv_todo);
        rvTodo.setLayoutManager(new LinearLayoutManager(this));

        todoDao = new TodoDao(this, getIntent().getLongExtra("user_id", -1L));
        adapter = new TodoAdapter(this);
        rvTodo.setAdapter(adapter);

        tvEmptyHint = findViewById(R.id.tv_empty_hint);
        emptyStateContainer = findViewById(R.id.empty_state_container);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(TodoListActivity.this, TodoEditActivity.class);
            intent.putExtra("user_id", getIntent().getLongExtra("user_id", -1L));
            startActivity(intent);
        });

        loadTodos();
    }

    /**
     * 每次回到列表页时重新加载数据，确保列表与数据库同步。
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadTodos();
    }

    /**
     * 子线程查询所有待办，更新 Adapter 数据并切换空状态视图的可见性。
     */
    private void loadTodos() {
        new Thread(() -> {
            List<Todo> list = todoDao.queryAll();
            runOnUiThread(() -> {
                adapter.setTodos(list);
                if (emptyStateContainer != null) {
                    emptyStateContainer.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    rvTodo.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                }
            });
        }).start();
    }

    /**
     * 待办状态切换回调：子线程更新数据库中的完成状态，刷新列表。
     */
    @Override
    public void onToggleDone(long todoId, boolean isDone) {
        new Thread(() -> {
            todoDao.updateStatus(todoId, isDone);
            runOnUiThread(this::loadTodos);
        }).start();
    }

    /**
     * 弹出删除确认对话框，用户确认后在子线程执行删除操作并刷新列表。
     */
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

    /**
     * 列表项点击跳转到待办详情页
     */
    @Override
    public void onItemClick(long todoId) {
        Intent intent = new Intent(TodoListActivity.this, TodoDetailActivity.class);
        intent.putExtra("todo_id", todoId);
        intent.putExtra("user_id", getIntent().getLongExtra("user_id", -1L));
        startActivity(intent);
    }

    /** 关闭数据库连接，释放资源 */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (todoDao != null) {
            todoDao.close();
        }
    }
}
