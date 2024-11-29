# -NotePad-
本项目是基于源码：https://github.com/llfjfz/NotePad 的项目拓展。<br>把源代码打开后，首先需要配置合适的gradle和sdk，下面是我的配置代码：<br>
```
classpath 'com.android.tools.build:gradle:8.6.0'    //build.gradle(project)
```
```
plugins {                                       //build.gradle(app)
    id 'com.android.application'
}
android {
    compileSdk 33 // 更新到较新的编译 SDK 版本

    defaultConfig {
        applicationId "com.example.android.notepad"
        minSdkVersion 21 // 推荐 minSdkVersion 至少为 21，以支持较新的 API
        targetSdkVersion 33 // 更新到较新的目标 SDK 版本

        testApplicationId "com.example.android.notepad.tests"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner" // 使用 androidx 的测试运行器
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro' // 更新 ProGuard 配置文件名
        }
    }
    namespace 'com.example.android.notepad'
```
```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip        //gradle-wrapper.properties
```
说明：源代码打开后是一个简单的记事本项目，可以实现新建笔记，删除笔记，修改笔记，修改标题以及复制粘贴的功能，界面如下图所示<br> ![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/origin.jpg)
<br>打开的菜单主页太黑了，把它换成白色主题，方便后续美化UI。在AndroidManifest的NoteList活动里面添加下面代码<br>
```
android:theme="@android:style/Theme.Holo.Light"
```
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/a.png)
<br>接下来是项目的具体功能的实现<br>基础功能实现：1.增加时间戳  2.增添搜索功能<br>附加功能实现：1.UI美化（包括更换图标，更改背景颜色） 2.笔记排序  3.笔记导出<br>
## 基础功能   
#### 1.时间戳实现
初始的项目只会显示笔记标题，我在这个基础上加了一张图片作笔记图标，在标题下方增加了时间戳。首先在notelist_item.xml文件中增加下面代码
```
<ImageView
            android:id="@+id/image1"                    //图标，drawable里面的图片换成自己找的图标
            android:layout_width="53dp"
            android:layout_height="64dp"
            android:layout_marginRight="8dp"
            android:src="@drawable/notebook" />
<TextView
        android:id="@android:id/text2"                    //时间戳
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:gravity="center_vertical"
        android:paddingLeft="5dp"
        android:singleLine="true" />
```
因为原项目的数据库已经加入了COLUMN_NAME_MODIFICATION_DATE这一数据项，所以不需要手动添加；下一步是在NoteList的dataColumns和viewIDs增添时间戳相关数据
```Java
String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE  } ;
        int[] viewIDs = { android.R.id.text1 ,android.R.id.text2};
```
又因为这里只会显示时间戳的数字而不是具体的时间，所以我们还需要在NoteEditor的updateNote方法下转换时间格式
```Java
ContentValues values = new ContentValues();
        Long now = Long.valueOf(System.currentTimeMillis());
        SimpleDateFormat sf = new SimpleDateFormat("yy/MM/dd HH:mm");
        sf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));  // 设置时区为上海时间（东八区）
```
我创建了一个名为TimeLine的笔记，时间戳在下方正确显示<br>
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/b.png)<br>
#### 2.搜索功能实现
要实现这个功能，首先我们需要在list_options_menu.xml里面增加一个搜索的图标跳转（要在string.xml里面定义menu_search，同理下面的其它@String里面也需要自己定义）
```
<item
        android:id="@+id/menu_search"
        android:icon="@android:drawable/ic_menu_search"
        android:title="@string/menu_search"
        android:showAsAction="always" />
```
然后在NoteList的onOptionsItemSelected中增加跳转查询的选项。这里源码是用的switch语句，因为我这里会报错，更改为了if-else语句。
```Java
else if (item.getItemId() == R.id.menu_search) {
            Intent intent = new Intent(this, NoteSearch.class);
            startActivity(intent);
            return true;
        }
```
这里的if语句跳转到了NoteSearch，用这个活动来实现查询
```Java
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
        searchView.setOnQueryTextListener(this);                //实现setOnQueryTextListener接口
    }
    public boolean onQueryTextChange(String string) {
        String selection1 = NotePad.Notes.COLUMN_NAME_TITLE+" like ? or "+NotePad.Notes.COLUMN_NAME_NOTE+" like ?";             //动态搜索
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
```
实现搜索功能我们还需要一个新的界面布局，在NoteSearch的setContentView(R.layout.note_search)这句代码，意味着我们在点击搜索后会跳转到note_search的界面，所以我们新建一个note_search.xml
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <SearchView
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:iconifiedByDefault="false"
        />
    <ListView
        android:id="@+id/list_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        />
</LinearLayout>
```
最后我们需要在AndroidManifest里面增加NoteSearch的activity
```
<activity android:name="NoteSearch"
            android:exported="true"
            android:label="@string/menu_search"
            android:theme="@android:style/Theme.Holo.Light">
            <intent-filter>
                <action android:name="android.intent.action.SEARCH" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
```
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/c.png)    ![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/d.png)
## 附加功能
#### 1.UI美化
UI美化要做的是更换一些图标和设置编辑界面和主界面的背景色。前者直接在对应的图标的@drawable更改图片即可，要实现后者，我们需要在数据库增加关于颜色的列。<br>
首先在NotePad类里添加颜色的字段，并定义好颜色对应的Int值，方便后续查找。
```Java
public static final String COLUMN_NAME_BACK_COLOR = "color";

        public static final int DEFAULT_COLOR = 0; //白
        public static final int YELLOW_COLOR = 1; //黄
        public static final int BLUE_COLOR = 2; //蓝
        public static final int GREEN_COLOR = 3; //绿
        public static final int RED_COLOR = 4; //红
        public static final int PURPLE_COLOR = 5;//紫
```
然后找到NotePadProvider中找到oncreate()方法，这是创建数据库的地方，在这里添加下面的代码：
```Java
+ NotePad.Notes.COLUMN_NAME_BACK_COLOR + " INTEGER" //颜色
```
##### 注意：oncreate()只会在数据库第一次被创建时调用，在后续不会被调用，直接加入颜色字段后，是不会新增颜色字段的，直接运行程序会导致项目闪退，并且在logcat里面提示数据库找不到相应的color的id。
##### 解决方法：在Device Explore中，打开data/data/com.自己对应的项目名/databases，里面后缀为.db的是数据库的字段，将其全部删除，这样在运行项目时会重新创建一张表
数据库添加字段后，要在NotePadProvider的static里面增加
```Java
sNotesProjectionMap.put(
                NotePad.Notes.COLUMN_NAME_BACK_COLOR,
                NotePad.Notes.COLUMN_NAME_BACK_COLOR);
```
在insert()方法增加默认背景的跳转
```Java
if (values.containsKey(NotePad.Notes.COLUMN_NAME_BACK_COLOR) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.DEFAULT_COLOR);
        }
```
接下来需要实现颜色的实际填充，可以自定义一个cursoradapter继承SimpleCursorAdapter
```Java
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.SimpleCursorAdapter;

public class MyCursorAdapter extends SimpleCursorAdapter {
    public MyCursorAdapter(Context context, int layout, Cursor c,
                           String[] from, int[] to) {
        super(context, layout, c, from, to);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor){
        super.bindView(view, context, cursor);
        //从数据库中读取的cursor中获取笔记列表对应的颜色数据，并设置笔记颜色
        int x = cursor.getInt(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_BACK_COLOR));           //不要使用getColumnIndex，可能识别不出来返回的values值
        switch (x){
            case NotePad.Notes.DEFAULT_COLOR:
                view.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
            case NotePad.Notes.YELLOW_COLOR:
                view.setBackgroundColor(Color.rgb(247, 216, 133));
                break;
            case NotePad.Notes.BLUE_COLOR:
                view.setBackgroundColor(Color.rgb(165, 202, 237));
                break;
            case NotePad.Notes.GREEN_COLOR:
                view.setBackgroundColor(Color.rgb(161, 214, 174));
                break;
            case NotePad.Notes.RED_COLOR:
                view.setBackgroundColor(Color.rgb(244, 149, 133));
                break;
            case NotePad.Notes.PURPLE_COLOR:
                view.setBackgroundColor(Color.rgb(230, 190, 255));
                break;
            default:
                view.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
        }
    }
}
```
然后在NoteList的projection增加一行
```Java
NotePad.Notes.COLUMN_NAME_BACK_COLOR,  //在这里增加颜色的显示
```
再将adapter改成自定义的adapater
```Java
MyCursorAdapter adapter = new MyCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                dataColumns,
                viewIDs
        );
```
到这里，数据库以及颜色的跳转的部分已经完成，接下来我们还需要将背景的颜色进行更换，首先需要对笔记编辑界面，也就是NoteEditor活动里增加背景颜色的数据
```Java
private static final String[] PROJECTION =
        new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
                NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    };
```
然后我们需要在onResume()方法中添加从数据库读取颜色，并设置为背景的代码
```Java
int x = mCursor.getInt(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
        
        switch (x){
            case NotePad.Notes.DEFAULT_COLOR:
                mText.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
            case NotePad.Notes.YELLOW_COLOR:
                mText.setBackgroundColor(Color.rgb(247, 216, 133));
                break;
            case NotePad.Notes.BLUE_COLOR:
                mText.setBackgroundColor(Color.rgb(165, 202, 237));
                break;
            case NotePad.Notes.GREEN_COLOR:
                mText.setBackgroundColor(Color.rgb(161, 214, 174));
                break;
            case NotePad.Notes.RED_COLOR:
                mText.setBackgroundColor(Color.rgb(244, 149, 133));
                break;
            case NotePad.Notes.PURPLE_COLOR:
                mText.setBackgroundColor(Color.rgb(230, 190, 255));
                break;
            default:
                mText.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
        }
```
接下来需要在editors_options_menu.xml中添加一个改变颜色的图标
```
<item android:id="@+id/menu_color"
        android:title="@string/menu_color"
        android:icon="@drawable/art"
        android:showAsAction="always"/>
```
又因为在点击该图标后需要一个改变颜色的具体布局，所以创建一个垂直的线性布局，里面的颜色需要在String.xml中自己定义，和前面设置一样的rgb即可
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal" android:layout_width="match_parent"
    android:layout_height="match_parent">
    <ImageButton
        android:id="@+id/color_white"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorWhite"
        android:onClick="white"/>
    <ImageButton
        android:id="@+id/color_yellow"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorYellow"
        android:onClick="yellow"/>
    <ImageButton
        android:id="@+id/color_blue"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorBlue"
        android:onClick="blue"/>
    <ImageButton
        android:id="@+id/color_green"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorGreen"
        android:onClick="green"/>
    <ImageButton
        android:id="@+id/color_red"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorRed"
        android:onClick="red"/>
    <ImageButton
        android:id="@+id/color_purple"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorPurple"
        android:onClick="purple"/>
</LinearLayout>
```
然后在NoteEditor中增加相应的跳转
```Java
else if(item.getItemId() == R.id.menu_color){
            changeColor();
        }
```
会跳转到changeColor()方法，该方法将uri信息传递到NoteColor的activity里面
```Java
private final void changeColor() {
        Intent intent = new Intent(null,mUri);
        intent.setClass(NoteEditor.this,NoteColor.class);
        NoteEditor.this.startActivity(intent);
    }
```
NoteColor()的代码如下
```Java
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class NoteColor extends Activity {
    private Cursor mCursor;
    private Uri mUri;
    private int color;
    private static final int COLUMN_INDEX_TITLE = 1;
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    };
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_color);
        //从NoteEditor传入的uri
        mUri = getIntent().getData();
        mCursor = managedQuery(
                mUri,        // The URI for the note that is to be retrieved.
                PROJECTION,  // The columns to retrieve
                null,        // No selection criteria are used, so no where columns are needed.
                null,        // No where columns are used, so no where values are needed.
                null         // No sort order is needed.
        );
    }
    @Override
    protected void onResume(){
        //执行顺序在onCreate之后
        if (mCursor != null) {
            mCursor.moveToFirst();
            color = mCursor.getInt(COLUMN_INDEX_TITLE);
        }
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, color);
        getContentResolver().update(mUri, values, null, null);
    }
    public void white(View view){
        color = NotePad.Notes.DEFAULT_COLOR;
        finish();
    }
    public void yellow(View view){
        color = NotePad.Notes.YELLOW_COLOR;
        finish();
    }
    public void blue(View view){
        color = NotePad.Notes.BLUE_COLOR;
        finish();
    }
    public void green(View view){
        color = NotePad.Notes.GREEN_COLOR;
        finish();
    }
    public void red(View view){
        color = NotePad.Notes.RED_COLOR;
        finish();
    }
    public void purple(View view){
        color = NotePad.Notes.PURPLE_COLOR;
        finish();
    }
}
```
最后在AndroidManifest里面添加这个activity
```
<activity android:name="NoteColor"
            android:theme="@android:style/Theme.Holo.Light.Dialog"
            android:label="ChangeColor"
            android:windowSoftInputMode="stateVisible"/>
```
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/e.png)
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/f.png)
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/g.png)
#### 2.笔记排序
第一步还是在菜单，也就是list_options_menu.xml里面添加排序
```
<item
        android:id="@+id/menu_sort"
        android:title="@string/menu_sort"
        android:icon="@android:drawable/ic_menu_sort_by_size"
        android:showAsAction="always" >
        <menu>
            <item
                android:id="@+id/menu_sort1"
                android:title="@string/menu_sort1"/>
            <item
                android:id="@+id/menu_sort2"
                android:title="@string/menu_sort2"/>
            <item
                android:id="@+id/menu_sort3"
                android:title="@string/menu_sort3"/>
        </menu>
    </item>
```
然后在NoteList下增加相应的跳转,三种情况对应三种cursor的排序方式
```Java
else if (item.getItemId() == R.id.menu_sort1){
            cursor = managedQuery(
                    getIntent().getData(),
                    PROJECTION,
                    null,
                    null,
                    NotePad.Notes._ID
            );
            adapter = new MyCursorAdapter(
                    this,
                    R.layout.noteslist_item,
                    cursor,
                    dataColumns,
                    viewIDs
            );
            setListAdapter(adapter);
            return true;
        }else if (item.getItemId() == R.id.menu_sort2){
            cursor = managedQuery(
                    getIntent().getData(),
                    PROJECTION,
                    null,
                    null,
                    NotePad.Notes.DEFAULT_SORT_ORDER
            );
            adapter = new MyCursorAdapter(
                    this,
                    R.layout.noteslist_item,
                    cursor,
                    dataColumns,
                    viewIDs
            );
            setListAdapter(adapter);
            return true;
        }else if (item.getItemId() == R.id.menu_sort3){
            cursor = managedQuery(
                    getIntent().getData(),
                    PROJECTION,
                    null,
                    null,
                    NotePad.Notes.COLUMN_NAME_BACK_COLOR
            );
            adapter = new MyCursorAdapter(
                    this,
                    R.layout.noteslist_item,
                    cursor,
                    dataColumns,
                    viewIDs
            );
            setListAdapter(adapter);
            return true;
        }
```
下面的截图分别是按创建时间排序，按修改时间排序和按颜色排序<br>
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/h.png)
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/i.png)
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/j.png)
#### 3.笔记导出
同样的，在editor_options_menu.xml中添加导出选项
```
<item android:id="@+id/menu_output"
        android:title="@string/menu_output" />
```
在NoteEditor的选择方法中加入跳转
```Java
else if(item.getItemId() == R.id.menu_output){
            outputNote();
        }
```
然后定义一个outputNote()方法，将uri信息传到新活动中
```Java
private final void outputNote() {
        Intent intent = new Intent(null,mUri);
        intent.setClass(NoteEditor.this,OutputText.class);
        NoteEditor.this.startActivity(intent);
    }
```
这个新活动命名为OutputText.java，里面的write()方法是实现具体导出的
```Java
public class OutputText extends Activity {
    //要使用的数据库中笔记的信息
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_NOTE, // 2
            NotePad.Notes.COLUMN_NAME_CREATE_DATE, // 3
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 4
    };
    //读取出的值放入这些变量
    private String TITLE;
    private String NOTE;
    private String CREATE_DATE;
    private String MODIFICATION_DATE;
    //读取该笔记信息
    private Cursor mCursor;
    //导出文件的名字
    private EditText mName;
    //NoteEditor传入的uri，用于从数据库查出该笔记
    private Uri mUri;
    //关于返回与保存按钮的一个特殊标记，返回的话不执行导出，点击按钮才导出
    private boolean flag = false;
    private static final int COLUMN_INDEX_TITLE = 1;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output_text);
        mUri = getIntent().getData();
        mCursor = managedQuery(
                mUri,        // The URI for the note that is to be retrieved.
                PROJECTION,  // The columns to retrieve
                null,        // No selection criteria are used, so no where columns are needed.
                null,        // No where columns are used, so no where values are needed.
                null         // No sort order is needed.
        );
        mName = (EditText) findViewById(R.id.output_name);
    }
    @Override
    protected void onResume(){
        super.onResume();
        if (mCursor != null) {
            // The Cursor was just retrieved, so its index is set to one record *before* the first
            // record retrieved. This moves it to the first record.
            mCursor.moveToFirst();
            //编辑框默认的文件名为标题，可自行更改
            mName.setText(mCursor.getString(COLUMN_INDEX_TITLE));
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mCursor != null) {
            //从mCursor读取对应值
            TITLE = mCursor.getString(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_TITLE));
            NOTE = mCursor.getString(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_NOTE));
            CREATE_DATE = mCursor.getString(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CREATE_DATE));
            MODIFICATION_DATE = mCursor.getString(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
            //flag在点击导出按钮时会设置为true，执行写文件
            if (flag == true) {
                write();
            }
            flag = false;
        }
    }
    public void OutputOk(View v){
        flag = true;
        finish();
    }
    private void write() {
        // 检查 SD 卡是否挂载
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                // 获取文件名，使用笔记名称作为文件名
                String fileName = mName.getText().toString() + ".txt";

                // 创建 MediaStore 文件内容
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // 文件名
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain"); // 文件类型
                contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS); // 保存路径为文档文件夹

                // 获取 ContentResolver，并插入新文件
                Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);

                // 获取文件输出流
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        printWriter.println(TITLE);
                        printWriter.println(NOTE);
                        printWriter.println("创建时间：" + CREATE_DATE);
                        printWriter.println("最后一次修改时间：" + MODIFICATION_DATE);
                        printWriter.close();
                        Toast.makeText(this, "保存成功, 保存位置: " + uri.toString(), Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```
##### 注意：如果你的应用在 Android 10（API 29）及以上版本上运行，并且通过 MediaStore API 进行文件存储，则无需再在 AndroidManifest.xml 中声明 WRITE_EXTERNAL_STORAGE 或 READ_EXTERNAL_STORAGE 权限。由于我的Android项目版本是15，可以使用 MediaStore 来操作共享存储，而不用在Android Manifest里面直接访问文件权限
下一步是定义一个导出的布局界面
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="6dip"
    android:paddingRight="6dip"
    android:paddingBottom="3dip">

    <EditText android:id="@+id/output_name"
        android:maxLines="1"
        android:layout_marginTop="2dp"
        android:layout_marginBottom="15dp"
        android:layout_width="wrap_content"
        android:ems="25"
        android:layout_height="wrap_content"
        android:autoText="true"
        android:capitalize="sentences"
        android:scrollHorizontally="true" />

    <Button android:id="@+id/output_ok"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="right"
        android:text="@string/output_ok"
        android:onClick="OutputOk" />
</LinearLayout>
```
最后在AndroidManifest声明一下，就可以了
```
<activity android:name="OutputText"
            android:label="@string/output_name"
            android:theme="@android:style/Theme.Holo.Dialog"
            android:windowSoftInputMode="stateVisible">

        </activity>
```
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/k.png)
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/l.png)
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/m.png)
![image](https://github.com/fjnu-xyn/-NotePad-/blob/main/app/src/main/res/drawable/n.png)
