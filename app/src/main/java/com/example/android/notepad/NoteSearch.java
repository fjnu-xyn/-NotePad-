/*
这是一个名为`NoteSearch`的Android活动（Activity），主要用于搜索记事本（NotePad）中的笔记。以下是关于该代码的详细解释：

1. **导入必要的库和类**: 导入了Android开发中常用的库和类，包括数据库操作相关的类。
2. **ListView和数据库变量声明**: 在类中声明了用于显示搜索结果的ListView以及用于访问数据库的SQLiteDatabase对象。
3. **onCreate方法**: 在活动创建时执行。设置布局，初始化ListView和搜索框（SearchView）。同时获取可读数据库实例。
4. **onQueryTextSubmit方法**: 当用户在搜索框提交查询时触发。这里只是显示了一个Toast提示，实际并未执行真正的查询操作。
5. **onQueryTextChange方法**: 当用户在搜索框中输入文本时触发。这里是实现搜索功能的核心部分。


	* 根据用户输入的查询字符串构建查询条件（使用LIKE语句）。
	* 使用SQLite数据库查询符合条件的笔记。
	* 使用SimpleCursorAdapter将查询结果适配到ListView中显示。
6. **相关常量与数组定义**:


	* `PROJECTION`：定义了从数据库中查询需要的列。
	* `dataColumns`：定义了在ListView中要显示的列。
	* `viewIDs`：定义了ListView中每个列的视图ID。

总结：这是一个简单的搜索功能，允许用户输入关键字搜索记事本中的笔记，并将搜索结果展示在一个ListView中。
*/

package com.example.android.notepad;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
public class NoteSearch extends Activity implements SearchView.OnQueryTextListener
{
    ListView listView;
    SQLiteDatabase sqLiteDatabase;
    /**
     * The columns needed by the cursor adapter
     */
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE//时间
    };
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "您选择的是："+query, Toast.LENGTH_SHORT).show();
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);
        SearchView searchView = findViewById(R.id.search_view);
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        listView = findViewById(R.id.list_view);
        sqLiteDatabase = new NotePadProvider.DatabaseHelper(this).getReadableDatabase();
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("查找");
        searchView.setOnQueryTextListener(this);
    }
    public boolean onQueryTextChange(String string) {
        String selection1 = NotePad.Notes.COLUMN_NAME_TITLE+" like ? or "+NotePad.Notes.COLUMN_NAME_NOTE+" like ?";
        String[] selection2 = {"%"+string+"%","%"+string+"%"};
        Cursor cursor = sqLiteDatabase.query(
                NotePad.Notes.TABLE_NAME,
                PROJECTION, // The columns to return from the query
                selection1, // The columns for the where clause
                selection2, // The values for the where clause
                null,          // don't group the rows
                null,          // don't filter by row groups
                NotePad.Notes.DEFAULT_SORT_ORDER // The sort order
        );
        String[] dataColumns = {
                NotePad.Notes.COLUMN_NAME_TITLE,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
        } ;
        int[] viewIDs = {
                android.R.id.text1,
                android.R.id.text2
        };
        SimpleCursorAdapter adapter
                = new SimpleCursorAdapter(
                this,                             // The Context for the ListView
                R.layout.noteslist_item,         // Points to the XML for a list item
                cursor,                           // The cursor to get items from
                dataColumns,
                viewIDs
        );
        listView.setAdapter(adapter);
        return true;
    }
}
