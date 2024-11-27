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
