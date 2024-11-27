/*
这段代码是一个针对 Android 系统中内容提供者（Content Provider）的测试用例。该测试用例的目的是测试名为 "NotePad" 的应用中的 "NotePadProvider" 类。它包含各种测试方法，以验证内容提供者的各种功能，包括 URI 识别、MIME 类型处理、数据流处理、查询、插入、删除和更新操作。

具体来说，这个类包含以下主要测试方法：

1. `testUriAndGetType()`：测试内容提供者的 URI 和 getType() 方法，以确保它能正确识别并返回不同类型数据的 MIME 类型。

2. `testGetStreamTypes()`：测试内容提供者返回的流 MIME 类型，确保它能正确处理并返回数据的 MIME 类型。

3. `testOpenTypedAssetFile()`：测试内容提供者打开数据流的方法，包括验证文件描述符是否有效以及数据流是否正确。

4. `testWriteDataToPipe()`：测试内容提供者写入数据到数据流的方法，确保数据能被正确写入并读取。

5. `testQueriesOnNotesUri()`：测试内容提供者基于 URI 的查询功能，包括空表查询、全表查询、投影查询、条件查询和排序查询。

6. `testQueriesOnNoteIdUri()`：测试内容提供者基于单条记录 ID 的查询功能，包括空表查询和单条记录查询。

7. `testInserts()`：测试内容提供者的插入功能，包括插入新记录并验证记录是否成功插入。

8. `testDeletes()`：测试内容提供者的删除功能，包括删除空表记录和删除存在记录。

9. `testUpdates()`：测试内容提供者的更新功能，包括更新空表记录和更新存在记录。

每个测试方法都包含具体的数据和验证步骤，以确保内容提供者的各种操作都能正常工作。
*/


package com.example.android.notepad;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
/*
 */
/**
 * This class tests the content provider for the Note Pad sample application.
 *
 * To learn how to run an entire test package or one of its classes, please see
 * "Testing in Eclipse, with ADT" or "Testing in Other IDEs" in the Developer Guide.
 */
public class NotePadProviderTest extends ProviderTestCase2<NotePadProvider> {
    private static final Uri INVALID_URI =
        Uri.withAppendedPath(NotePad.Notes.CONTENT_URI, "invalid");
    private MockContentResolver mMockResolver;
    private SQLiteDatabase mDb;
    private final NoteInfo[] TEST_NOTES = {
        new NoteInfo("Note0", "This is note 0"),
        new NoteInfo("Note1", "This is note 1"),
        new NoteInfo("Note2", "This is note 2"),
        new NoteInfo("Note3", "This is note 3"),
        new NoteInfo("Note4", "This is note 4"),
        new NoteInfo("Note5", "This is note 5"),
        new NoteInfo("Note6", "This is note 6"),
        new NoteInfo("Note7", "This is note 7"),
        new NoteInfo("Note8", "This is note 8"),
        new NoteInfo("Note9", "This is note 9") };
    private static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;
    private static final long ONE_WEEK_MILLIS = ONE_DAY_MILLIS * 7;
    private static final GregorianCalendar TEST_CALENDAR =
        new GregorianCalendar(2010, Calendar.JANUARY, 1, 0, 0, 0);
    private final static long START_DATE = TEST_CALENDAR.getTimeInMillis();
    private final static String MIME_TYPES_ALL = "*/*";
    private final static String MIME_TYPES_NONE = "qwer/qwer";
    private final static String MIME_TYPE_TEXT = "text/plain";
    /*
     * Constructor for the test case class.
     * Calls the super constructor with the class name of the provider under test and the
     * authority name of the provider.
     */
    public NotePadProviderTest() {
        super();
    }
    /*
     * Sets up the test environment before each test method. Creates a mock content resolver,
     * gets the provider under test, and creates a new database for the provider.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMockResolver = getMockContentResolver();
        /*
         * Gets a handle to the database underlying the provider. Gets the provider instance
         * created in super.setUp(), gets the DatabaseOpenHelper for the provider, and gets
         * a database object from the helper.
         */
        mDb = getProvider().getOpenHelperForTest().getWritableDatabase();
    }
    /*
     *  This method is called after each test method, to clean up the current fixture. Since
     *  this sample test case runs in an isolated context, no cleanup is necessary.
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    /*
     * Sets up test data.
     * The test data is in an SQL database. It is created in setUp() without any data,
     * and populated in insertData if necessary.
     */
    private void insertData() {
        ContentValues values = new ContentValues();
        for (int index = 0; index < TEST_NOTES.length; index++) {
            TEST_NOTES[index].setCreationDate(START_DATE + (index * ONE_DAY_MILLIS));
            TEST_NOTES[index].setModificationDate(START_DATE + (index * ONE_WEEK_MILLIS));
            mDb.insertOrThrow(
                NotePad.Notes.TABLE_NAME,             // the table name for the insert
                NotePad.Notes.COLUMN_NAME_TITLE,      // column set to null if empty values map
                TEST_NOTES[index].getContentValues()  // the values map to insert
            );
        }
    }
    /*
     * Tests the provider's publicly available URIs. If the URI is not one that the provider
     * understands, the provider should throw an exception. It also tests the provider's getType()
     * method for each URI, which should return the MIME type associated with the URI.
     */
    public void testUriAndGetType() {
        String mimeType = mMockResolver.getType(NotePad.Notes.CONTENT_URI);
        assertEquals(NotePad.Notes.CONTENT_TYPE, mimeType);
        mimeType = mMockResolver.getType(NotePad.Notes.LIVE_FOLDER_URI);
        assertEquals(NotePad.Notes.CONTENT_TYPE, mimeType);
        Uri noteIdUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, 1);
        mimeType = mMockResolver.getType(noteIdUri);
        assertEquals(NotePad.Notes.CONTENT_ITEM_TYPE, mimeType);
        mimeType = mMockResolver.getType(INVALID_URI);
    }
    /*
     * Tests the provider's stream MIME types returned by getStreamTypes(). If the provider supports
     * stream data for the URI, the MIME type is returned. Otherwise, the provider returns null.
     */
    public void testGetStreamTypes() {
        assertNull(mMockResolver.getStreamTypes(NotePad.Notes.CONTENT_URI, MIME_TYPES_ALL));
        assertNull(mMockResolver.getStreamTypes(NotePad.Notes.LIVE_FOLDER_URI, MIME_TYPES_ALL));
        /*
         * Tests the note id URI for a single note, using _ID value "1" which is a valid ID. Uses a
         * valid MIME type filter that will return all the supported MIME types for a content URI.
         * The result should be "text/plain".
         */
        Uri testUri = Uri.withAppendedPath(NotePad.Notes.CONTENT_ID_URI_BASE, "1");
        String mimeType[] = mMockResolver.getStreamTypes(testUri, MIME_TYPES_ALL);
        assertNotNull(mimeType);
        assertEquals(mimeType[0],"text/plain");
        assertEquals(mimeType.length,1);
        /*
         * Tests with the same URI but with a filter that should not return any URIs.
         */
        mimeType = mMockResolver.getStreamTypes(testUri, MIME_TYPES_NONE);
        assertNull(mimeType);
        /*
         * Tests with a URI that should not have any associated stream MIME types, but with a
         * filter that returns all types. The result should still be null.
         */
        mimeType = mMockResolver.getStreamTypes(NotePad.Notes.CONTENT_URI, MIME_TYPES_ALL);
        assertNull(mimeType);
    }
    /*
     * Tests the provider's public API for opening a read-only pipe of data for a note ID URI
     * and MIME type filter matching "text/plain".
     * This method throws a FileNotFoundException if the URI isn't for a note ID or the MIME type
     * filter isn't "text/plain". It throws an IOException if it can't close a file descriptor.
     */
    public void testOpenTypedAssetFile() throws FileNotFoundException, IOException {
        Uri testNoteIdUri;
        AssetFileDescriptor testAssetDescriptor;
        insertData();
        testNoteIdUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, 1);
        testAssetDescriptor = mMockResolver.openTypedAssetFileDescriptor(
                testNoteIdUri,         // the URI for a single note. The pipe points to this
                MIME_TYPE_TEXT,        // a MIME type of "text/plain"
                null                   // the "opts" argument
        );
        ParcelFileDescriptor testParcelDescriptor = testAssetDescriptor.getParcelFileDescriptor();
        FileDescriptor testDescriptor = testAssetDescriptor.getFileDescriptor();
        assertNotNull(testAssetDescriptor);
        assertNotNull(testParcelDescriptor);
        assertNotNull(testDescriptor);
        assertTrue(testDescriptor.valid());
        testParcelDescriptor.close();
        testAssetDescriptor.close();
        /*
         * Changes the URI to a notes URI for multiple notes, and re-test. This should fail, since
         * the provider does not support this type of URI. A FileNotFound exception is expected,
         * so call fail() if it does *not* occur.
         */
        try {
            testAssetDescriptor = mMockResolver.openTypedAssetFileDescriptor(
                    NotePad.Notes.CONTENT_URI,
                    MIME_TYPE_TEXT,
                    null
            );
            fail();
        } catch (FileNotFoundException e) {
        }
        /*
         * Changes back to the note ID URI, but changes the MIME type filter to one that is not
         * supported by the provider. This should also fail, since the provider will only open a
         * pipe for MIME type "text/plain". A FileNotFound exception is expected, so calls
         * fail() if it does *not* occur.
         */
        try {
            testAssetDescriptor = mMockResolver.openTypedAssetFileDescriptor(
                    testNoteIdUri,
                    MIME_TYPES_NONE,
                    null
            );
            fail();
        } catch (FileNotFoundException e) {
        }
    }
    /*
     * Tests the provider's method for actually returning writing data into a pipe. The method is
     * writeDataToPipe, but this method is not called directly. Instead, a caller invokes
     * openTypedAssetFile(). That method uses ContentProvider.openPipeHelper(), which has as one of
     * its arguments a ContentProvider.PipeDataWriter object that must actually put the data into
     * the pipe. PipeDataWriter is an interface, not a class, so it must be implemented.
     *
     * The NotePadProvider class itself implements the "ContentProvider.PipeDataWriter, which means
     * that it supplies the interface's only method, writeDataToPipe(). In effect, a call to
     * openTypedAssetFile() calls writeDataToPipe().
     *
     *  The test of writeDataToPipe() is separate from other tests of openTypedAssetFile() for the
     *  sake of clarity.
     */
    public void testWriteDataToPipe() throws FileNotFoundException {
        String[] inputData = {"","",""};
        Uri noteIdUri;
        Cursor noteIdCursor;
        AssetFileDescriptor noteIdAssetDescriptor;
        ParcelFileDescriptor noteIdParcelDescriptor;
        insertData();
        noteIdUri = ContentUris.withAppendedId(
                NotePad.Notes.CONTENT_ID_URI_BASE,  // The base pattern for a note ID URI
                1                                   // Sets the URI to point to record ID 1 in the
        );
        noteIdCursor = mMockResolver.query(
                noteIdUri,  // the URI for the note ID we want to retrieve
                null,       // no projection, retrieve all the columns
                null,       // no WHERE clause
                null,       // no WHERE arguments
                null        // default sort order
        );
        assertNotNull(noteIdCursor);
        assertEquals(1,noteIdCursor.getCount());
        noteIdAssetDescriptor = mMockResolver.openTypedAssetFileDescriptor(
                noteIdUri,        // the URI of the note that will provide the data
                MIME_TYPE_TEXT,   // the "text/plain" MIME type
                null              // no other options
        );
        assertNotNull(noteIdAssetDescriptor);
        noteIdParcelDescriptor = noteIdAssetDescriptor.getParcelFileDescriptor();
        assertNotNull(noteIdParcelDescriptor);
        FileReader fIn = new FileReader(noteIdParcelDescriptor.getFileDescriptor());
        BufferedReader bIn = new BufferedReader(fIn);
        /*
         * The pipe should contain three lines: The note's title, an empty line, and the note's
         * contents. The following code reads and stores these three lines.
         */
        for (int index = 0; index < inputData.length; index++) {
            try {
                inputData[index] = bIn.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        }
        assertEquals(TEST_NOTES[0].title, inputData[0]);
        assertEquals(TEST_NOTES[0].note, inputData[2]);
    }
    /*
     * Tests the provider's public API for querying data in the table, using the URI for
     * a dataset of records.
     */
    public void testQueriesOnNotesUri() {
        final String[] TEST_PROJECTION = {
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
        };
        final String TITLE_SELECTION = NotePad.Notes.COLUMN_NAME_TITLE + " = " + "?";
        final String SELECTION_COLUMNS =
            TITLE_SELECTION + " OR " + TITLE_SELECTION + " OR " + TITLE_SELECTION;
        final String[] SELECTION_ARGS = { "Note0", "Note1", "Note5" };
        final String SORT_ORDER = NotePad.Notes.COLUMN_NAME_TITLE + " ASC";
        Cursor cursor = mMockResolver.query(
            NotePad.Notes.CONTENT_URI,  // the URI for the main data table
            null,                       // no projection, get all columns
            null,                       // no selection criteria, get all records
            null,                       // no selection arguments
            null                        // use default sort order
        );
        assertEquals(0, cursor.getCount());
        insertData();
        cursor = mMockResolver.query(
            NotePad.Notes.CONTENT_URI,  // the URI for the main data table
            null,                       // no projection, get all columns
            null,                       // no selection criteria, get all records
            null,                       // no selection arguments
            null                        // use default sort order
        );
        assertEquals(TEST_NOTES.length, cursor.getCount());
        Cursor projectionCursor = mMockResolver.query(
              NotePad.Notes.CONTENT_URI,  // the URI for the main data table
              TEST_PROJECTION,            // get the title, note, and mod date columns
              null,                       // no selection columns, get all the records
              null,                       // no selection criteria
              null                        // use default the sort order
        );
        assertEquals(TEST_PROJECTION.length, projectionCursor.getColumnCount());
        assertEquals(TEST_PROJECTION[0], projectionCursor.getColumnName(0));
        assertEquals(TEST_PROJECTION[1], projectionCursor.getColumnName(1));
        assertEquals(TEST_PROJECTION[2], projectionCursor.getColumnName(2));
        projectionCursor = mMockResolver.query(
            NotePad.Notes.CONTENT_URI, // the URI for the main data table
            TEST_PROJECTION,           // get the title, note, and mod date columns
            SELECTION_COLUMNS,         // select on the title column
            SELECTION_ARGS,            // select titles "Note0", "Note1", or "Note5"
            SORT_ORDER                 // sort ascending on the title column
        );
        assertEquals(SELECTION_ARGS.length, projectionCursor.getCount());
        int index = 0;
        while (projectionCursor.moveToNext()) {
            assertEquals(SELECTION_ARGS[index], projectionCursor.getString(0));
            index++;
        }
        assertEquals(SELECTION_ARGS.length, index);
    }
    /*
     * Tests queries against the provider, using the note id URI. This URI encodes a single
     * record ID. The provider should only return 0 or 1 record.
     */
    public void testQueriesOnNoteIdUri() {
      final String SELECTION_COLUMNS = NotePad.Notes.COLUMN_NAME_TITLE + " = " + "?";
      final String[] SELECTION_ARGS = { "Note1" };
      final String SORT_ORDER = NotePad.Notes.COLUMN_NAME_TITLE + " ASC";
      final String[] NOTE_ID_PROJECTION = {
           NotePad.Notes._ID,                 // The Notes class extends BaseColumns,
           NotePad.Notes.COLUMN_NAME_TITLE};  // The note's title
      Uri noteIdUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, 1);
      Cursor cursor = mMockResolver.query(
          noteIdUri, // URI pointing to a single record
          null,      // no projection, get all the columns for each record
          null,      // no selection criteria, get all the records in the table
          null,      // no need for selection arguments
          null       // default sort, by ascending title
      );
      assertEquals(0,cursor.getCount());
      insertData();
      cursor = mMockResolver.query(
          NotePad.Notes.CONTENT_URI, // the base URI for the table
          NOTE_ID_PROJECTION,        // returns the ID and title columns of rows
          SELECTION_COLUMNS,         // select based on the title column
          SELECTION_ARGS,            // select title of "Note1"
          SORT_ORDER                 // sort order returned is by title, ascending
      );
      assertEquals(1, cursor.getCount());
      assertTrue(cursor.moveToFirst());
      int inputNoteId = cursor.getInt(0);
      noteIdUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, inputNoteId);
      cursor = mMockResolver.query(noteIdUri, // the URI for a single note
          NOTE_ID_PROJECTION,                 // same projection, get ID and title columns
          SELECTION_COLUMNS,                  // same selection, based on title column
          SELECTION_ARGS,                     // same selection arguments, title = "Note1"
          SORT_ORDER                          // same sort order returned, by title, ascending
      );
      assertEquals(1, cursor.getCount());
      assertTrue(cursor.moveToFirst());
      assertEquals(inputNoteId, cursor.getInt(0));
    }
    /*
     *  Tests inserts into the data model.
     */
    public void testInserts() {
        NoteInfo note = new NoteInfo(
            "Note30", // the note's title
            "Test inserting a note" // the note's content
        );
        note.setCreationDate(START_DATE + (10 * ONE_DAY_MILLIS));
        note.setModificationDate(START_DATE + (2 * ONE_WEEK_MILLIS));
        Uri rowUri = mMockResolver.insert(
            NotePad.Notes.CONTENT_URI,  // the main table URI
            note.getContentValues()     // the map of values to insert as a new record
        );
        long noteId = ContentUris.parseId(rowUri);
        Cursor cursor = mMockResolver.query(
            NotePad.Notes.CONTENT_URI, // the main table URI
            null,                      // no projection, return all the columns
            null,                      // no selection criteria, return all the rows in the model
            null,                      // no selection arguments
            null                       // default sort order
        );
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        int titleIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
        int noteIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
        int crdateIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CREATE_DATE);
        int moddateIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);
        assertEquals(note.title, cursor.getString(titleIndex));
        assertEquals(note.note, cursor.getString(noteIndex));
        assertEquals(note.createDate, cursor.getLong(crdateIndex));
        assertEquals(note.modDate, cursor.getLong(moddateIndex));
        ContentValues values = note.getContentValues();
        values.put(NotePad.Notes._ID, (int) noteId);
        try {
            rowUri = mMockResolver.insert(NotePad.Notes.CONTENT_URI, values);
            fail("Expected insert failure for existing record but insert succeeded.");
        } catch (Exception e) {
        }
    }
    /*
     * Tests deletions from the data model.
     */
    public void testDeletes() {
        final String SELECTION_COLUMNS = NotePad.Notes.COLUMN_NAME_TITLE + " = " + "?";
        final String[] SELECTION_ARGS = { "Note0" };
        int rowsDeleted = mMockResolver.delete(
            NotePad.Notes.CONTENT_URI, // the base URI of the table
            SELECTION_COLUMNS,         // select based on the title column
            SELECTION_ARGS             // select title = "Note0"
        );
        assertEquals(0, rowsDeleted);
        insertData();
        rowsDeleted = mMockResolver.delete(
            NotePad.Notes.CONTENT_URI, // the base URI of the table
            SELECTION_COLUMNS,         // same selection column, "title"
            SELECTION_ARGS             // same selection arguments, title = "Note0"
        );
        assertEquals(1, rowsDeleted);
        Cursor cursor = mMockResolver.query(
            NotePad.Notes.CONTENT_URI, // the base URI of the table
            null,                      // no projection, return all columns
            SELECTION_COLUMNS,         // select based on the title column
            SELECTION_ARGS,            // select title = "Note0"
            null                       // use the default sort order
        );
        assertEquals(0, cursor.getCount());
    }
    /*
     * Tests updates to the data model.
     */
    public void testUpdates() {
        final String SELECTION_COLUMNS = NotePad.Notes.COLUMN_NAME_TITLE + " = " + "?";
        final String[] selectionArgs = { "Note1" };
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, "Testing an update with this string");
        int rowsUpdated = mMockResolver.update(
            NotePad.Notes.CONTENT_URI,  // the URI of the data table
            values,                     // a map of the updates to do (column title and value)
            SELECTION_COLUMNS,           // select based on the title column
            selectionArgs               // select "title = Note1"
        );
        assertEquals(0, rowsUpdated);
        insertData();
        rowsUpdated = mMockResolver.update(
            NotePad.Notes.CONTENT_URI,   // The URI of the data table
            values,                      // the same map of updates
            SELECTION_COLUMNS,            // same selection, based on the title column
            selectionArgs                // same selection argument, to select "title = Note1"
        );
        assertEquals(1, rowsUpdated);
    }
    private static class NoteInfo {
        String title;
        String note;
        long createDate;
        long modDate;
        /*
         * Constructor for a NoteInfo instance. This class helps create a note and
         * return its values in a ContentValues map expected by data model methods.
         * The note's id is created automatically when it is inserted into the data model.
         */
        public NoteInfo(String t, String n) {
            title = t;
            note = n;
            createDate = 0;
            modDate = 0;
        }
        public void setCreationDate(long c) {
            createDate = c;
        }
        public void setModificationDate(long m) {
            modDate = m;
        }
        /*
         * Returns a ContentValues instance (a map) for this NoteInfo instance. This is useful for
         * inserting a NoteInfo into a database.
         */
        public ContentValues getContentValues() {
            ContentValues v = new ContentValues();
            v.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
            v.put(NotePad.Notes.COLUMN_NAME_NOTE, note);
            v.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, createDate);
            v.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, modDate);
            return v;
        }
    }
}
