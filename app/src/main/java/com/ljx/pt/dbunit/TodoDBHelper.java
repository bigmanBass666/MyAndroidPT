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
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "todo";

    public TodoDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "content TEXT, "
                + "is_done INTEGER NOT NULL DEFAULT 0, "
                + "create_time INTEGER NOT NULL)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /** 插入一条新待办，返回自增主键 */
    public long insert(Todo todo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", todo.getTitle());
        values.put("content", todo.getContent());
        values.put("is_done", 0);
        values.put("create_time", System.currentTimeMillis());
        return db.insert(TABLE_NAME, null, values);
    }

    public int update(Todo todo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", todo.getTitle());
        values.put("content", todo.getContent());
        return db.update(TABLE_NAME, values, "_id=?", new String[]{String.valueOf(todo.getId())});
    }

    public int updateStatus(int id, boolean isDone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_done", isDone ? 1 : 0);
        return db.update(TABLE_NAME, values, "_id=?", new String[]{String.valueOf(id)});
    }

    public int delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, "_id=?", new String[]{String.valueOf(id)});
    }

    public Todo queryById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "_id=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursorToTodo(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public List<Todo> queryAll() {
        List<Todo> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, "create_time DESC");
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToTodo(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private Todo cursorToTodo(Cursor cursor) {
        Todo todo = new Todo();
        todo.setId(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
        todo.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        todo.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
        todo.setDone(cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1);
        todo.setCreateTime(cursor.getLong(cursor.getColumnIndexOrThrow("create_time")));
        return todo;
    }
}
