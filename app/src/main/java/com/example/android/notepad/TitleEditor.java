/*
这段代码是一个名为 `TitleEditor` 的 Android Activity，其主要功能是允许用户编辑笔记的标题。以下是代码的详细功能解释：

1. **包和导入**: 该代码位于 `com.example.android.notepad` 包中，并导入了必要的 Android 类和接口。
2. **类定义**: `TitleEditor` 是一个继承自 `Activity` 的类，用于显示一个浮动窗口，包含一个 `EditText` 用于编辑笔记的标题。
3. **常量定义**: 定义了一些常量，如用于操作的 URI、查询投影（用于从数据库检索数据）、列索引等。
4. **onCreate 方法**: 当 Activity 被首次创建时调用此方法。它设置 Activity 的视图，从触发此 Activity 的 Intent 获取 URI，并使用该 URI 从数据库中检索笔记。
5. **onResume 方法**: 当 Activity 即将进入前台时调用此方法。它检查查询是否成功，如果成功，则显示当前笔记的标题。
6. **onPause 方法**: 当 Activity 失去焦点时调用此方法。它更新数据库中笔记的标题，使用 `EditText` 中的当前文本内容。这是通过创建一个 `ContentValues` 对象，设置标题为 `EditText` 的当前文本，然后使用 `getContentResolver().update()` 方法更新数据库。
7. **onClickOk 方法**: 当点击“OK”按钮时调用此方法。它结束（finish）此 Activity。

需要注意的是，这个代码示例中存在一些需要改进的地方。例如，数据库操作（如查询和更新）在 UI 线程上执行，这可能导致界面冻结，尤其是在处理大量数据或复杂查询时。在实际应用中，应该使用 `AsyncQueryHandler` 或 `AsyncTask` 在单独的线程上执行这些操作，以避免界面冻结并改善用户体验。
*/


package com.example.android.notepad;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
/**
 * This Activity allows the user to edit a note's title. It displays a floating window
 * containing an EditText.
 *
 * NOTE: Notice that the provider operations in this Activity are taking place on the UI thread.
 * This is not a good practice. It is only done here to make the code more readable. A real
 * application should use the {@link android.content.AsyncQueryHandler}
 * or {@link android.os.AsyncTask} object to perform operations asynchronously on a separate thread.
 */
public class TitleEditor extends Activity {
    /**
     * This is a special intent action that means "edit the title of a note".
     */
    public static final String EDIT_TITLE_ACTION = "com.android.notepad.action.EDIT_TITLE";
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
    };
    private static final int COLUMN_INDEX_TITLE = 1;
    private Cursor mCursor;
    private EditText mText;
    private Uri mUri;
    /**
     * This method is called by Android when the Activity is first started. From the incoming
     * Intent, it determines what kind of editing is desired, and then does it.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title_editor);
        mUri = getIntent().getData();
        /*
         * Using the URI passed in with the triggering Intent, gets the note.
         *
         * Note: This is being done on the UI thread. It will block the thread until the query
         * completes. In a sample app, going against a simple provider based on a local database,
         * the block will be momentary, but in a real app you should use
         * android.content.AsyncQueryHandler or android.os.AsyncTask.
         */
        mCursor = managedQuery(
            mUri,        // The URI for the note that is to be retrieved.
            PROJECTION,  // The columns to retrieve
            null,        // No selection criteria are used, so no where columns are needed.
            null,        // No where columns are used, so no where values are needed.
            null         // No sort order is needed.
        );
        mText = (EditText) this.findViewById(R.id.title);
    }
    /**
     * This method is called when the Activity is about to come to the foreground. This happens
     * when the Activity comes to the top of the task stack, OR when it is first starting.
     *
     * Displays the current title for the selected note.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mCursor != null) {
            mCursor.moveToFirst();
            mText.setText(mCursor.getString(COLUMN_INDEX_TITLE));
        }
    }
    /**
     * This method is called when the Activity loses focus.
     *
     * For Activity objects that edit information, onPause() may be the one place where changes are
     * saved. The Android application model is predicated on the idea that "save" and "exit" aren't
     * required actions. When users navigate away from an Activity, they shouldn't have to go back
     * to it to complete their work. The act of going away should save everything and leave the
     * Activity in a state where Android can destroy it if necessary.
     *
     * Updates the note with the text currently in the text box.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mCursor != null) {
            ContentValues values = new ContentValues();
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, mText.getText().toString());
            /*
             * Updates the provider with the note's new title.
             *
             * Note: This is being done on the UI thread. It will block the thread until the
             * update completes. In a sample app, going against a simple provider based on a
             * local database, the block will be momentary, but in a real app you should use
             * android.content.AsyncQueryHandler or android.os.AsyncTask.
             */
            getContentResolver().update(
                mUri,    // The URI for the note to update.
                values,  // The values map containing the columns to update and the values to use.
                null,    // No selection criteria is used, so no "where" columns are needed.
                null     // No "where" columns are used, so no "where" values are needed.
            );
        }
    }
    public void onClickOk(View v) {
        finish();
    }
}
