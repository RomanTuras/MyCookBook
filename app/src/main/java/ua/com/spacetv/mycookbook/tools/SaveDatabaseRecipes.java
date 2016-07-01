package ua.com.spacetv.mycookbook.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import java.io.IOException;

import ua.com.spacetv.mycookbook.MainActivity;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.google_services.Analytics;

/**
 * Checking if SD card is available - showing save dialog
 * Else - showing error dialog with one "cancel" button
 *
 * {@link MainActivity}
 */
public class SaveDatabaseRecipes implements Constants {
    private static boolean isOkButtonShow = true; //show or hidden OK button


    /**
     * Checking if SD card is available - showing save dialog
     * Else - showing error dialog with one "cancel" button
     *
     * @param context
     */
    public static void dialogSaveDatabase(Context context) {
        String pathFolder = getPath();
        String title = context.getResources().getString(R.string.dlg_save_file);
        String message;
        if (pathFolder != null) {
            message = context.getResources().getString(R.string.dlg_path_to_file);
            message += " " + pathFolder;
            message += '\n' + context.getResources().getString(R.string.dlg_name_of_file);
        } else {
            isOkButtonShow = false;//hide OK button
            message = context.getResources().getString(R.string.dlg_sdcard_disabled);
            message += '\n' + context.getResources().getString(R.string.dlg_sdcard_check_mount);
        }
        showDialog(context, title, message, pathFolder);
    }

    /**
     * Getting path to external storage directory
     *
     * @return path, if SD card available, else - null
     */
    public static String getPath() {
        String sdState = Environment.getExternalStorageState();
        String path = null;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += "/" + FOLDER_NAME + "/";
        }
        return path;
    }

    /**
     * Showing Alert dialog
     *
     * @param title
     * @param message
     * @param pathFolder - path to folder in to SD card
     */
    private static void showDialog(Context context, String title,
                                   String message, final String pathFolder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false);
        if (isOkButtonShow) {
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                new MainActivity().saveDatabaseFile(pathFolder);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            new Analytics(context).sendAnalytics("myCookBook",
                    "Main Activity", "Save db", "nop");
        }
        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
