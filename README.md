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
到这里，数据库以及颜色的跳转的部分已经完成，接下来我们还需要在界面中加具体的颜色按钮
