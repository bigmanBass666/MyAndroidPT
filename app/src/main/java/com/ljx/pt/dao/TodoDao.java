package com.ljx.pt.dao;

import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

import android.content.Context;

import java.util.List;

/** 待办数据访问对象，封装 TodoDBHelper 的 CRUD 操作 */
public class TodoDao {

    private final TodoDBHelper dbHelper;
    private final long userId;

    /** @deprecated 仅用于未接 userId 的旧调用方，将 userId 默认设为 0；后续由 Commits 4-5 替换为带 userId 的构造 */
    @Deprecated
    public TodoDao(Context context) {
        this(context, 0L);
    }

    public TodoDao(Context context, long userId) {
        this.dbHelper = new TodoDBHelper(context);
        this.userId = userId;
    }

    public void close() {
        dbHelper.close();
    }

    public long insert(Todo todo) {
        return dbHelper.insert(todo, userId);
    }

    public int update(Todo todo) {
        return dbHelper.update(todo, userId);
    }

    public int updateStatus(long id, boolean isDone) {
        return dbHelper.updateStatus(id, isDone, userId);
    }

    public int delete(long id) {
        return dbHelper.delete(id, userId);
    }

    public Todo queryById(long id) {
        return dbHelper.queryById(id, userId);
    }

    public List<Todo> queryAll() {
        return dbHelper.queryAll(userId);
    }
}
