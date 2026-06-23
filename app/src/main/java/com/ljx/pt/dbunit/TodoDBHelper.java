package com.ljx.pt.dbunit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ljx.pt.bean.Todo;

import java.util.ArrayList;
import java.util.List;

/** SQLiteOpenHelper，管理 todo 表（建表/升级/CRUD） */
public class TodoDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "todo.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "todo";

    public TodoDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** 建表：todo 表包含 _id（主键）、user_id（用户标识）、title（标题）、content（内容）、is_done（完成状态）、create_time（创建时间）六个字段 */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "title TEXT NOT NULL, "
                + "content TEXT, "
                + "is_done INTEGER NOT NULL DEFAULT 0, "
                + "create_time INTEGER NOT NULL)";
        db.execSQL(sql);
    }

    /** 数据库升级：删除旧表并重建——会导致数据丢失，仅教学用途 */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /** 插入一条新待办，返回自增主键 */
    public long insert(Todo todo, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", todo.getTitle());
        values.put("content", todo.getContent());
        values.put("is_done", todo.isDone() ? 1 : 0);
        values.put("create_time", System.currentTimeMillis());
        return db.insert(TABLE_NAME, null, values);
    }

    /** 按 _id 和 user_id 更新待办的标题和内容——双条件 WHERE 确保用户隔离 */
    public int update(Todo todo, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", todo.getTitle());
        values.put("content", todo.getContent());
        return db.update(TABLE_NAME, values, "_id=? AND user_id=?", new String[]{String.valueOf(todo.getId()), String.valueOf(userId)});
    }

    /** 更新 is_done 字段切换待办完成状态，WHERE 条件包含 user_id 确保用户隔离 */
    public int updateStatus(long id, boolean isDone, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_done", isDone ? 1 : 0);
        return db.update(TABLE_NAME, values, "_id=? AND user_id=?", new String[]{String.valueOf(id), String.valueOf(userId)});
    }

    /** 按 _id 和 user_id 双重条件删除待办，防止越权操作 */
    public int delete(long id, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, "_id=? AND user_id=?", new String[]{String.valueOf(id), String.valueOf(userId)});
    }

    /** 按 _id 和 user_id 精确查询单条待办 */
    public Todo queryById(long id, long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "_id=? AND user_id=?", new String[]{String.valueOf(id), String.valueOf(userId)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursorToTodo(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /** 按 user_id 查询所有待办，按 create_time DESC 排序——最近的在最前 */
    public List<Todo> queryAll(long userId) {
        List<Todo> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "user_id=?", new String[]{String.valueOf(userId)}, null, null, "create_time DESC");
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToTodo(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    /** 将 Cursor 行数据映射为 Todo 对象，处理 is_done 的 int 到 boolean 转换 */
    private Todo cursorToTodo(Cursor cursor) {
        Todo todo = new Todo();
        todo.setId(cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
        todo.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        todo.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        todo.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
        todo.setDone(cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1);
        todo.setCreateTime(cursor.getLong(cursor.getColumnIndexOrThrow("create_time")));
        return todo;
    }
}
