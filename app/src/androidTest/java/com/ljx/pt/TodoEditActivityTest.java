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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class TodoEditActivityTest {
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

    private void launchForNew() {
        Intent intent = new Intent(context, TodoEditActivity.class);
        intent.putExtra(TodoEditActivity.EXTRA_TODO_ID, -1);
        ActivityScenario.launch(intent);
    }

    @Test
    public void newTodo_emptyTitle_staysOnScreen() {
        launchForNew();
        onView(withId(R.id.btn_save)).perform(click());
        onView(withId(R.id.et_title)).check(matches(isDisplayed()));
    }

    @Test
    public void newTodo_validInput_createsTodo() {
        launchForNew();
        onView(withId(R.id.et_title)).perform(replaceText("新任务标题"));
        onView(withId(R.id.et_content)).perform(replaceText("详细内容"), closeSoftKeyboard());
        onView(withId(R.id.btn_save)).perform(click());
    }

    @Test
    public void newTodo_emptyContent_createsTodo() {
        launchForNew();
        onView(withId(R.id.et_title)).perform(replaceText("仅有标题"), closeSoftKeyboard());
        onView(withId(R.id.btn_save)).perform(click());
    }

    @Test
    public void cancelButton_finishesActivity() {
        launchForNew();
        onView(withId(R.id.btn_cancel)).perform(click());
    }

    @Test
    public void editMode_titlePreFilled() {
        TodoDBHelper helper = new TodoDBHelper(context);
        long id = helper.insert(new Todo("原标题", "原内容"));
        helper.close();

        Intent intent = new Intent(context, TodoEditActivity.class);
        intent.putExtra(TodoEditActivity.EXTRA_TODO_ID, (int) id);
        ActivityScenario.launch(intent);

        onView(withId(R.id.et_title)).check(matches(withText("原标题")));
        onView(withId(R.id.et_content)).check(matches(withText("原内容")));
    }

    @Test
    public void editMode_save_updatesTitle() {
        TodoDBHelper helper = new TodoDBHelper(context);
        long id = helper.insert(new Todo("原标题", "原内容"));
        helper.close();

        Intent intent = new Intent(context, TodoEditActivity.class);
        intent.putExtra(TodoEditActivity.EXTRA_TODO_ID, (int) id);
        ActivityScenario.launch(intent);

        onView(withId(R.id.et_title)).perform(replaceText("更新后的标题"));
        onView(withId(R.id.btn_save)).perform(click());

        Todo updated = new TodoDBHelper(context).queryById((int) id);
        assertThat(updated.getTitle(), is("更新后的标题"));
    }
}
