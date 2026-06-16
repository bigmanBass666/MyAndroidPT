package com.ljx.pt;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ljx.pt.bean.Todo;
import com.ljx.pt.dbunit.TodoDBHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.Intent;
import android.widget.CheckBox;

import androidx.test.espresso.Espresso;

import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class TodoDetailActivityTest {
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TodoDBHelper helper = new TodoDBHelper(context);
        helper.getWritableDatabase().execSQL("DELETE FROM todo");
        helper.close();
    }

    @After
    public void tearDown() {
        TodoDBHelper helper = new TodoDBHelper(context);
        helper.getWritableDatabase().execSQL("DELETE FROM todo");
        helper.close();
    }

    private void launchWithId(long id) {
        Intent intent = new Intent(context, TodoDetailActivity.class);
        intent.putExtra(TodoEditActivity.EXTRA_TODO_ID, id);
        ActivityScenario.launch(intent);
    }

    @Test
    public void detail_showsTitleAndContent() {
        TodoDBHelper helper = new TodoDBHelper(context);
        long id = helper.insert(Todo.of("展示标题", "展示内容"));
        helper.close();

        launchWithId(id);

        Espresso.onView(withId(R.id.tv_detail_title)).check(matches(withText("展示标题")));
        Espresso.onView(withId(R.id.tv_detail_content)).check(matches(withText("展示内容")));
    }

    @Test
    public void detail_emptyContent_showsPlaceholder() {
        TodoDBHelper helper = new TodoDBHelper(context);
        long id = helper.insert(Todo.of("只有标题", ""));
        helper.close();

        launchWithId(id);

        Espresso.onView(withId(R.id.tv_detail_content)).check(matches(withText(containsString("无内容"))));
    }

    @Test
    public void detail_statusChanged_togglesCheckbox() {
        TodoDBHelper helper = new TodoDBHelper(context);
        long id = helper.insert(Todo.of("任务", ""));
        helper.close();

        launchWithId(id);

        Espresso.onView(withId(R.id.cb_detail_done)).perform(click());

        Todo updated = new TodoDBHelper(context).queryById(id);
        assertThat(updated.isDone(), is(true));
    }

    @Test
    public void deleteButton_opensConfirmDialog() {
        TodoDBHelper helper = new TodoDBHelper(context);
        long id = helper.insert(Todo.of("待删任务", ""));
        helper.close();

        launchWithId(id);

        Espresso.onView(withId(R.id.btn_detail_delete)).perform(click());
        Espresso.onView(withText("确认删除")).inRoot(isDialog()).check(matches(isDisplayed()));
        Espresso.onView(withText("确定要删除\"待删任务\"吗？")).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test
    public void deleteConfirm_deletesTodo() {
        TodoDBHelper helper = new TodoDBHelper(context);
        long id = helper.insert(Todo.of("待删任务", ""));
        helper.close();

        launchWithId(id);

        Espresso.onView(withId(R.id.btn_detail_delete)).perform(click());
        Espresso.onView(withText("删除")).inRoot(isDialog()).perform(click());

        Todo result = new TodoDBHelper(context).queryById(id);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void editButton_launchesEditActivity() {
        TodoDBHelper helper = new TodoDBHelper(context);
        long id = helper.insert(Todo.of("测试编辑", ""));
        helper.close();

        launchWithId(id);

        Espresso.onView(withId(R.id.btn_detail_edit)).perform(click());
        Espresso.onView(withId(R.id.et_title)).check(matches(isDisplayed()));
    }

    @Test
    public void invalidId_finishesActivity() {
        Intent intent = new Intent(context, TodoDetailActivity.class);
        intent.putExtra(TodoEditActivity.EXTRA_TODO_ID, 99999);
        ActivityScenario.launch(intent);
    }
}
