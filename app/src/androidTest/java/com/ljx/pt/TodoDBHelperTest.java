package com.ljx.pt;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TodoDBHelperTest {
    private TodoDBHelper dbHelper;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = new TodoDBHelper(context);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM todo");
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }

    @Test
    public void insert_returnsValidRowId() {
        Todo todo = new Todo("测试标题", "测试内容");
        long rowId = dbHelper.insert(todo);
        assertTrue("插入应返回有效ID", rowId > 0);
    }

    @Test
    public void queryById_returnsCorrectTodo() {
        Todo inserted = new Todo("标题A", "内容A");
        long rowId = dbHelper.insert(inserted);
        Todo queried = dbHelper.queryById((int) rowId);
        assertNotNull("查询应返回数据", queried);
        assertEquals("标题应一致", "标题A", queried.getTitle());
        assertEquals("内容应一致", "内容A", queried.getContent());
        assertFalse("新建应为未完成", queried.isDone());
        assertTrue("创建时间应 > 0", queried.getCreateTime() > 0);
    }

    @Test
    public void update_modifiesTitleAndContent() {
        Todo todo = new Todo("原标题", "原内容");
        long rowId = dbHelper.insert(todo);
        Todo toUpdate = new Todo("新标题", "新内容");
        toUpdate.setId((int) rowId);
        int rows = dbHelper.update(toUpdate);
        assertEquals("应更新1行", 1, rows);
        Todo updated = dbHelper.queryById((int) rowId);
        assertEquals("标题应更新", "新标题", updated.getTitle());
        assertEquals("内容应更新", "新内容", updated.getContent());
    }

    @Test
    public void updateStatus_changesDoneFlag() {
        Todo todo = new Todo("任务", "内容");
        long rowId = dbHelper.insert(todo);
        dbHelper.updateStatus((int) rowId, true);
        Todo after = dbHelper.queryById((int) rowId);
        assertTrue("应标记为完成", after.isDone());
        dbHelper.updateStatus((int) rowId, false);
        Todo undone = dbHelper.queryById((int) rowId);
        assertFalse("应标记为未完成", undone.isDone());
    }

    @Test
    public void delete_removesRecord() {
        Todo todo = new Todo("待删", "内容");
        long rowId = dbHelper.insert(todo);
        int rows = dbHelper.delete((int) rowId);
        assertEquals("应删除1行", 1, rows);
        Todo after = dbHelper.queryById((int) rowId);
        assertNull("删除后查询应返回null", after);
    }

    @Test
    public void queryAll_returnsOrderedByTimeDesc() {
        dbHelper.insert(new Todo("第一条", ""));
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        dbHelper.insert(new Todo("第二条", ""));
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        dbHelper.insert(new Todo("第三条", ""));

        List<Todo> list = dbHelper.queryAll();
        assertEquals("应返回3条", 3, list.size());
        assertEquals("最新应在最前", "第三条", list.get(0).getTitle());
        assertEquals("最旧应在最后", "第一条", list.get(2).getTitle());
    }

    @Test
    public void queryById_nonExistent_returnsNull() {
        Todo result = dbHelper.queryById(99999);
        assertNull("不存在的ID应返回null", result);
    }

    @Test
    public void delete_nonExistent_returnsZero() {
        int rows = dbHelper.delete(99999);
        assertEquals("删除不存在的行应返回0", 0, rows);
    }
}
