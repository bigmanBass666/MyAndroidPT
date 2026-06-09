package com.ljx.pt;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ljx.pt.bean.User;
import com.ljx.pt.dao.UserDao;
import com.ljx.pt.dbunit.UserDBHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UserDaoTest {
    private UserDao userDao;
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        UserDBHelper helper = new UserDBHelper(context);
        helper.getWritableDatabase().execSQL("DELETE FROM userinfo");
        helper.close();
        userDao = new UserDao(context);
    }

    @After
    public void tearDown() {
        UserDBHelper helper = new UserDBHelper(context);
        helper.getWritableDatabase().execSQL("DELETE FROM userinfo");
        helper.close();
    }

    @Test
    public void insert_returnsOne() {
        User user = new User("alice", "password123");
        int result = userDao.insert(user);
        assertEquals("插入应返回1", 1, result);
    }

    @Test
    public void findByName_existingUser_returnsUser() {
        User inserted = new User("bob", "mypwd");
        userDao.insert(inserted);
        User found = userDao.findByName("bob");
        assertNotNull("应能找到用户", found);
        assertEquals("用户名应一致", "bob", found.getName());
        assertEquals("密码应一致", "mypwd", found.getPsw());
        assertTrue("ID应 > 0", found.getId() > 0);
    }

    @Test
    public void findByName_nonExisting_returnsNull() {
        User found = userDao.findByName("nonexistent");
        assertNull("不存在的用户应返回null", found);
    }

    @Test
    public void insert_duplicateName_failsSilently() {
        User user1 = new User("charlie", "pwd1");
        User user2 = new User("charlie", "pwd2");
        userDao.insert(user1);
        int result2 = userDao.insert(user2);
        assertEquals("重复插入应返回0(UNIQUE约束)", 0, result2);
        User found = userDao.findByName("charlie");
        assertNotNull("用户应存在", found);
        assertEquals("密码应为第一条", "pwd1", found.getPsw());
    }
}
