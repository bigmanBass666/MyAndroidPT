package com.ljx.pt.dbunit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ljx.pt.bean.User;

/** 用户数据库辅助类，管理 user.db 的建表和数据操作 */
public class UserDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "user.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "userinfo";

    public UserDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** 首次创建数据库时执行建表语句，创建 userinfo 表 */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户信息表：_id 自增主键、name 唯一用户名、psw 密码、email 邮箱
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE, "
                + "psw TEXT NOT NULL, "
                + "email TEXT NOT NULL)";
        db.execSQL(sql);
    }

    /** 数据库升级：删除旧表并重建（数据会丢失，仅教学演示） */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /** 将 User 对象属性写入 ContentValues，插入 userinfo 表，返回插入行 id */
    public long insert(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("psw", user.getPsw());
        values.put("email", user.getEmail());
        return db.insert(TABLE_NAME, null, values);
    }

    /** 用参数化查询（WHERE name=?）防止 SQL 注入，按用户名查询用户信息 */
    public User findByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "name=?", new String[]{name}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                user.setPsw(cursor.getString(cursor.getColumnIndexOrThrow("psw")));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                return user;
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
