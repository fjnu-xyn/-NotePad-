/*
这是一个关于Note Pad内容提供者的合约定义。这个合约定义了内容提供者客户端需要访问的数据表的信息。下面是对代码的详细解释：

1. **包声明和导入**: 代码开始于包声明，表明这个类属于`com.example.android.notepad`包。然后，它导入了一些必要的类和方法。
2. **NotePad类定义**: `NotePad`是一个公共的最终类，表示这个类不能被继承或修改。它有一个常量`AUTHORITY`，用于定义内容提供者的名字，这里是`"com.google.provider.NotePad"`。还有一个私有构造函数，表明这个类不能被实例化。
3. **Notes内部类**: `Notes`是`NotePad`中的一个内部类，实现了`BaseColumns`接口，该接口定义了列名常量`_ID`, `_COUNT`, `_DATA`, `_DIR_FILTERED`, `_DATA_SET`等，用于数据库表的常见操作。这个内部类定义了关于笔记表的一些常量。

以下是`Notes`类中定义的常量及其解释：

* `TABLE_NAME`: 笔记表的名称，即`"notes"`。
* `SCHEME`: URI的模式部分，对于内容提供者来说通常是`"content://"`。
* `PATH_NOTES`, `PATH_NOTE_ID`, `PATH_LIVE_FOLDER`: 这些是路径部分，用于构建URI。
* `CONTENT_URI`: 笔记表的基础URI。
* `CONTENT_ID_URI_BASE`: 用于访问单个笔记的URI基础。
* `CONTENT_ID_URI_PATTERN`: 用于匹配传入URI或构建意图的单个笔记URI模式。
* `LIVE_FOLDER_URI`: 用于笔记列表在Live文件夹中的URI。
* `CONTENT_TYPE`和`CONTENT_ITEM_TYPE`: 分别表示笔记目录和单个笔记的MIME类型。
* `DEFAULT_SORT_ORDER`: 默认的排序顺序，这里按照修改时间降序排序。
* `COLUMN_NAME_*`: 这些是列名常量，定义了笔记表中的各个字段，如标题、内容、创建时间和修改时间等。

总的来说，这个Java文件定义了一个关于Note Pad内容提供者的合约，包括其URI结构、MIME类型和数据库表的列名等，为客户端应用程序提供了访问这些数据的方式。
*/


package com.example.android.notepad;
import android.net.Uri;
import android.provider.BaseColumns;
/**
 * Defines a contract between the Note Pad content provider and its clients. A contract defines the
 * information that a client needs to access the provider as one or more data tables. A contract
 * is a public, non-extendable (final) class that contains constants defining column names and
 * URIs. A well-written client depends only on the constants in the contract.
 */
public final class NotePad {
    public static final String AUTHORITY = "com.google.provider.NotePad";
    private NotePad() {
    }
    /**
     * Notes table contract
     */
    public static final class Notes implements BaseColumns {
        private Notes() {}
        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "notes";
        /*
         * URI definitions
         */
        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";
        /**
         * Path parts for the URIs
         */
        /**
         * Path part for the Notes URI
         */
        private static final String PATH_NOTES = "/notes";
        /**
         * Path part for the Note ID URI
         */
        private static final String PATH_NOTE_ID = "/notes/";
        /**
         * 0-relative position of a note ID segment in the path part of a note ID URI
         */
        public static final int NOTE_ID_PATH_POSITION = 1;
        /**
         * Path part for the Live Folder URI
         */
        private static final String PATH_LIVE_FOLDER = "/live_folders/notes";
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        /**
         * The content URI base for a single note. Callers must
         * append a numeric note id to this Uri to retrieve a note
         */
        public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        /**
         * The content URI match pattern for a single note, specified by its ID. Use this to match
         * incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN
            = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");
        /**
         * The content Uri pattern for a notes listing for live folders
         */
        public static final Uri LIVE_FOLDER_URI
            = Uri.parse(SCHEME + AUTHORITY + PATH_LIVE_FOLDER);
        /*
         * MIME type definitions
         */
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        /*
         * Column definitions
         */
        /**
         * Column name for the title of the note
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_TITLE = "title";
        /**
         * Column name of the note content
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_NOTE = "note";
        /**
         * Column name for the creation timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_CREATE_DATE = "created";
        /**
         * Column name for the modification timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";

        public static final String COLUMN_NAME_BACK_COLOR = "color";

        public static final int DEFAULT_COLOR = 0; //白
        public static final int YELLOW_COLOR = 1; //黄
        public static final int BLUE_COLOR = 2; //蓝
        public static final int GREEN_COLOR = 3; //绿
        public static final int RED_COLOR = 4; //红
        public static final int PURPLE_COLOR = 5;//紫
    }
}
