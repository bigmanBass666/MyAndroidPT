package com.ljx.pt.dao;

import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

import android.content.Context;

import java.util.List;

/** 待办数据访问对象，封装 TodoDBHelper 的 CRUD 操作 */
public class TodoDao {

    private final TodoDBHelper dbHelper;

    public TodoDao(Context context) {
        this.dbHelper = new TodoDBHelper(context);
    }

    public void close() {
        dbHelper.close();
    }

    public long insert(Todo todo) {
        return dbHelper.insert(todo);
    }

    public int update(Todo todo) {
        return dbHelper.update(todo);
    }

    public int updateStatus(long id, boolean isDone) {
        return dbHelper.updateStatus(id, isDone);
    }

    public int delete(long id) {
        return dbHelper.delete(id);
    }

    public Todo queryById(long id) {
        return dbHelper.queryById(id);
    }

    public List<Todo> queryAll() {
        return dbHelper.queryAll();
    }
}
