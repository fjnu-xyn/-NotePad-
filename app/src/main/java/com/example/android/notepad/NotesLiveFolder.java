/*
这段代码定义了一个名为 `NotesLiveFolder` 的 Android 活动（Activity），其主要功能是创建一个“实时文件夹”（Live Folder）并返回给 HOME 应用。实时文件夹是 Android 系统中的一种特殊功能，允许用户通过点击图标来快速访问特定内容。在这个例子中，实时文件夹显示并管理来自 `NotePad` 应用中的笔记。以下是关于代码功能的详细解释：

1. **包声明和导入**: 代码首先声明了包名 `com.example.android.notepad` 并导入了一些必要的类和接口。
2. **活动创建**: 在 `onCreate()` 方法中，该活动检查接收到的 Intent 的动作是否是创建实时文件夹的动作（`LiveFolders.ACTION_CREATE_LIVE_FOLDER`）。
3. **创建实时文件夹 Intent**: 如果动作是创建实时文件夹，活动会创建一个新的 Intent，这个 Intent 包含用于设置实时文件夹的各种数据。


	* `setData()`：设置内容提供者（在本例中是 `NotePad.Notes.LIVE_FOLDER_URI`）的 URI 模式，该内容提供者将为文件夹提供数据。
	* `putExtra()`：添加显示名称、图标和显示模式作为额外数据到 Intent 中。其中，显示名称从字符串资源中获取，图标从应用的资源中获取。
	* 设置基础 Intent：当用户点击实时文件夹列表中的单个笔记时，这个基础 Intent 会被触发。在本例中，它的动作是编辑（`Intent.ACTION_EDIT`），并且数据是单个笔记的 URI 模式。这意味着当用户点击一个笔记时，会触发 Note Editor 活动并获取该笔记进行编辑。
4. **返回结果**: 根据操作是否成功创建实时文件夹，设置返回的结果。如果成功，则通过 `setResult(RESULT_OK, liveFolderIntent)` 返回包含实时文件夹数据的 Intent。否则，返回取消的结果（`RESULT_CANCELED`）。
5. **结束活动**: 最后，通过调用 `finish()` 方法结束这个活动。这会将活动的结果（一个包含实时文件夹数据的 Intent 或取消状态）返回给调用者（通常是 HOME 应用）。

总的来说，这个活动的目的是根据用户的操作创建一个显示和管理 `NotePad` 应用中笔记的实时文件夹，并将其实时文件夹的意图（Intent）返回给 HOME 应用进行显示和管理。
*/


package com.example.android.notepad;
import com.example.android.notepad.NotePad;
import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import android.provider.LiveFolders;
/**
 * This Activity creates a live folder Intent and
 * sends it back to HOME. From the data in the Intent, HOME creates a live folder and displays
 * its icon in the Home view.
 * When the user clicks the icon, Home uses the data it got from the Intent to retrieve information
 * from a content provider and display it in a View.
 *
 * The intent filter for this Activity is set to ACTION_CREATE_LIVE_FOLDER, which
 * HOME sends in response to a long press and selection of Live Folder.
 */
public class NotesLiveFolder extends Activity {
    /**
     * All of the work is done in onCreate(). The Activity doesn't actually display a UI.
     * Instead, it sets up an Intent and returns it to its caller (the HOME activity).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * Gets the incoming Intent and its action. If the incoming Intent was
         * ACTION_CREATE_LIVE_FOLDER, then create an outgoing Intent with the
         * necessary data and send back OK. Otherwise, send back CANCEL.
         */
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
            final Intent liveFolderIntent = new Intent();
            /*
             * The following statements put data into the outgoing Intent. Please see
             * {@link android.provider.LiveFolders for a detailed description of these
             * data values. From this data, HOME sets up a live folder.
             */
            liveFolderIntent.setData(NotePad.Notes.LIVE_FOLDER_URI);
            String foldername = getString(R.string.live_folder_name);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, foldername);
            ShortcutIconResource foldericon =
                Intent.ShortcutIconResource.fromContext(this, R.drawable.live_folder_notes);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON, foldericon);
            liveFolderIntent.putExtra(
                    LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                    LiveFolders.DISPLAY_MODE_LIST);
            /*
             * Adds a base action for items in the live folder list, as an Intent. When the
             * user clicks an individual note in the list, the live folder fires this Intent.
             *
             * Its action is ACTION_EDIT, so it triggers the Note Editor activity. Its
             * data is the URI pattern for a single note identified by its ID. The live folder
             * automatically adds the ID value of the selected item to the URI pattern.
             *
             * As a result, Note Editor is triggered and gets a single note to retrieve by ID.
             */
            Intent returnIntent
                    = new Intent(Intent.ACTION_EDIT, NotePad.Notes.CONTENT_ID_URI_PATTERN);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT, returnIntent);
            /* Creates an ActivityResult object to propagate back to HOME. Set its result indicator
             * to OK, and sets the returned Intent to the live folder Intent that was just
             * constructed.
             */
            setResult(RESULT_OK, liveFolderIntent);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}
