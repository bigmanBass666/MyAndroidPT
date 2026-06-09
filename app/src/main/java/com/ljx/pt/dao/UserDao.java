package com.ljx.pt.dao;

import com.ljx.pt.bean.User;
import com.ljx.pt.dbunit.UserDBHelper;

import android.content.Context;

public class UserDao {
    private UserDBHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new UserDBHelper(context);
    }

    public User findByName(String name) {
        return dbHelper.findByName(name);
    }

    public int insert(User user) {
        long rowId = dbHelper.insert(user);
        return rowId > 0 ? 1 : 0;
    }
}
