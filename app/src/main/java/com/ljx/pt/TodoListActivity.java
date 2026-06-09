package com.ljx.pt;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ljx.pt.adapter.TodoAdapter;
import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

import java.util.List;

public class TodoListActivity extends AppCompatActivity implements TodoAdapter.OnItemListener {

 private RecyclerView rvTodo;
 private TodoAdapter adapter;
 private TodoDBHelper dbHelper;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.activity_todo_list);

 getSupportActionBar().setTitle("我的待办");
 getSupportActionBar().setDisplayHomeAsUpEnabled(true);

 rvTodo = findViewById(R.id.rv_todo);
 rvTodo.setLayoutManager(new LinearLayoutManager(this));
 rvTodo.setClipToPadding(false);
 int toolbarH = getSupportActionBar() != null ? getSupportActionBar().getHeight() : (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
 rvTodo.setPadding(0, toolbarH, 0, 0);
 adapter = new TodoAdapter();
 adapter.setOnItemListener(this);
 rvTodo.setAdapter(adapter);

 dbHelper = new TodoDBHelper(this);

 findViewById(R.id.fab_add).setOnClickListener(v -> {
 Intent intent = new Intent(this, TodoEditActivity.class);
 startActivity(intent);
 });
 }

 @Override
 protected void onResume() {
 super.onResume();
 loadTodos();
 }

 private void loadTodos() {
 new Thread(() -> {
 seedIfEmpty();
 List<Todo> list = dbHelper.queryAll();
 runOnUiThread(() -> adapter.setTodos(list));
 }).start();
 }

 private void seedIfEmpty() {
 if (dbHelper.queryAll().isEmpty()) {
 Todo t1 = new Todo(); t1.setTitle("学习Android开发"); t1.setContent("完成实训报告和课程设计报告"); dbHelper.insert(t1);
 Todo t2 = new Todo(); t2.setTitle("提交项目代码"); t2.setContent("确保所有功能正常运行"); dbHelper.insert(t2);
 }
 }

 @Override
 public void onItemClick(Todo todo) {
 Intent intent = new Intent(this, TodoDetailActivity.class);
 intent.putExtra(TodoEditActivity.EXTRA_TODO_ID, todo.getId());
 startActivity(intent);
 }

 @Override
 public void onStatusChanged(Todo todo, boolean isDone) {
 new Thread(() -> {
 dbHelper.updateStatus(todo.getId(), isDone);
 runOnUiThread(() -> loadTodos());
 }).start();
 }

 @Override
 public void onDelete(Todo todo) {
 new AlertDialog.Builder(this)
 .setTitle("确认删除")
 .setMessage("确定要删除\"" + todo.getTitle() + "\"吗？")
 .setPositiveButton("删除", (dialog, which) -> {
 new Thread(() -> {
 dbHelper.delete(todo.getId());
 runOnUiThread(() -> {
 loadTodos();
 Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
 });
 }).start();
 })
 .setNegativeButton("取消", null)
 .show();
 }

 @Override
 public boolean onSupportNavigateUp() {
 finish();
 return true;
 }
}
