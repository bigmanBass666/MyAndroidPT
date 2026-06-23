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

    /** 登录结果枚举：区分不同失败原因 */
    public enum LoginResult {
        OK,
        USER_NOT_FOUND,
        WRONG_PASSWORD
    }

    /** 根据用户名和密码校验登录，通过 LoginResult 区分成功/用户不存在/密码错误三种结果 */
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

    /** 按用户名精确查找用户，用于登录校验和注册判重 */
    public User findByName(String name) {
        return dbHelper.findByName(name);
    }

    /** 关闭数据库连接，防止资源泄漏 */
    public void close() {
        dbHelper.close();
    }

/** 插入新用户到数据库（注册时调用），返回 1 表示成功、0 表示失败 */
    public int insert(User user) {
        long rowId = dbHelper.insert(user);
        return rowId > 0 ? 1 : 0;
    }
}
