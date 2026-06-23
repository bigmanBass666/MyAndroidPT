package com.ljx.pt.dao;

import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

import android.content.Context;

import java.util.List;

/** 待办数据访问对象，封装 TodoDBHelper 的 CRUD 操作 */
public class TodoDao {

    private final TodoDBHelper dbHelper;
    private final long userId;

    public TodoDao(Context context, long userId) {
        this.dbHelper = new TodoDBHelper(context);
        this.userId = userId;
    }

    /** 委托给 TodoDBHelper，关闭底层数据库连接，防止资源泄漏 */
    public void close() {
        dbHelper.close();
    }

    /** 委托给 TodoDBHelper，插入新待办并返回自增 id */
    public long insert(Todo todo) {
        return dbHelper.insert(todo, userId);
    }

    /** 委托给 TodoDBHelper，更新待办的标题和内容 */
    public int update(Todo todo) {
        return dbHelper.update(todo, userId);
    }

    /** 委托给 TodoDBHelper，切换待办的完成状态（done/undone） */
    public int updateStatus(long id, boolean isDone) {
        return dbHelper.updateStatus(id, isDone, userId);
    }

    /** 委托给 TodoDBHelper，按 id 删除指定待办 */
    public int delete(long id) {
        return dbHelper.delete(id, userId);
    }

    /** 委托给 TodoDBHelper，按 id 查询单条待办详情 */
    public Todo queryById(long id) {
        return dbHelper.queryById(id, userId);
    }

    /** 委托给 TodoDBHelper，查询当前用户所有待办，按创建时间倒序排列 */
    public List<Todo> queryAll() {
        return dbHelper.queryAll(userId);
    }
}
