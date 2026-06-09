package com.ljx.pt.dbunit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ljx.pt.bean.User;

public class UserDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "user.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "userinfo";

    public UserDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE, "
                + "psw TEXT NOT NULL)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insert(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("psw", user.getPsw());
        return db.insert(TABLE_NAME, null, values);
    }

    public User findByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "name=?", new String[]{name}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                user.setPsw(cursor.getString(cursor.getColumnIndexOrThrow("psw")));
                return user;
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
