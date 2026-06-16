package com.ljx.pt.dao;

import com.ljx.pt.bean.User;
import com.ljx.pt.dbunit.UserDBHelper;

import android.content.Context;

/** 用户数据访问对象，封装 UserDBHelper 的登录/注册/查重操作 */
public class UserDao {
    private UserDBHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new UserDBHelper(context);
    }

    // 登录结果枚举：区分不同失败原因
    public enum LoginResult {
        OK,
        USER_NOT_FOUND,
        WRONG_PASSWORD
    }

    // 根据用户名登录
    public LoginResult login(String name, String psw) {
        User user = dbHelper.findByName(name);
        if (user == null) {
            return LoginResult.USER_NOT_FOUND;
        }
        if (!user.getPsw().equals(psw)) {
            return LoginResult.WRONG_PASSWORD;
        }
        return LoginResult.OK;
    }

    // 根据用户名查询
    public User findByName(String name) {
        return dbHelper.findByName(name);
    }

    public void close() {
        dbHelper.close();
    }

// 插入新用户（注册）
    public int insert(User user) {
        long rowId = dbHelper.insert(user);
        return rowId > 0 ? 1 : 0;
    }
}
