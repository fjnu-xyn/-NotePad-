/*
这是一个关于Android应用的测试类，名为`NotePadActivityTest`，它继承自`ActivityInstrumentationTestCase2`，用于测试`NotesList`活动（Activity）。

代码功能解释：

1. 导入必要的测试类和活动类。
2. 定义`NotePadActivityTest`类，继承自`ActivityInstrumentationTestCase2<NotesList>`，表示这是一个针对`NotesList`活动的测试类。
3. 在构造函数中，使用`super(NotesList.class)`指定要测试的活动的类。
4. 定义了一个测试方法`testActivityTestCaseSetUpProperly()`，用于验证`NotesList`活动能否成功启动。
	* `assertNotNull("activity should be launched successfully", getActivity());`：这一行代码检查活动是否成功启动。如果活动启动失败，`getActivity()`方法返回null，`assertNotNull`方法将抛出一个异常，表示测试失败。反之，如果活动成功启动，测试通过。

总的来说，这个测试类的主要目的是验证`NotesList`活动能否正常启动。
*/


package com.example.android.notepad;
import static android.app.PendingIntent.getActivity;

import android.app.PendingIntent;
import android.test.ActivityInstrumentationTestCase2;
import com.example.android.notepad.NotesList;
/**
 * Make sure that the main launcher activity opens up properly, which will be
 * verified by {@link #testActivityTestCaseSetUpProperly}.
 */
public class NotePadActivityTest extends ActivityInstrumentationTestCase2<NotesList> {
    /**
     * Creates an {@link ActivityInstrumentationTestCase2} for the {@link NotesList} activity.
     */
    public NotePadActivityTest() {
        super();
    }
    /**
     * Verifies that the activity under test can be launched.
     */
    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }

    private Object getActivity() {
        return null;
    }


}
