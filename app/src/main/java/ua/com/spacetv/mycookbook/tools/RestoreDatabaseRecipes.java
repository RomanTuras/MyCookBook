package ua.com.spacetv.mycookbook.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ua.com.spacetv.mycookbook.MainActivity;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.google_services.Analytics;
import ua.com.spacetv.mycookbook.interfaces.Constants;

/**
 * Checking if SD card is available - showing restore dialog
 * Else - showing error dialog with one "cancel" button
 * <p/>
 * {@link MainActivity}
 */
public class RestoreDatabaseRecipes implements Constants {
    private static boolean isOkButtonShow = true; //show or hidden OK button

    /**
     * Checking if SD card is available - showing save dialog
     * Else - showing error dialog with one "cancel" button
     *
     * @param context - Context
     */
    public static void dialogRestoreDatabase(Context context) {
        isOkButtonShow = true;
        String pathFolder = getPath();
        String title = context.getResources().getString(R.string.dlg_restore_file);
        String message;
        if (pathFolder != null) {//SD card mounted, folder found
            String filename = checkFile();
            if (filename != null) {//file founded - restore available
                message = context.getResources().getString(R.string.dlg_file_found);
                message += '\n' + filename + '\n';
                message += '\n' + context.getResources().getString(R.string.dlg_warning);
            } else {//file not found
                isOkButtonShow = false;
                message = context.getResources().getString(R.string.dlg_file_not_found);
                message += '\n' + context.getResources().getString(R.string.dlg_sdcard_not_prepared);
            }
        } else {//SD card disabled
            isOkButtonShow = false;
            message = context.getResources().getString(R.string.dlg_folder_not_found);
            message += '\n' + context.getResources().getString(R.string.dlg_sdcard_not_prepared);
        }
        showDialog(context, title, message, pathFolder);
    }

    /**
     * Checking file of backup mDatabase
     *
     * @return date of last modified file if it exist, else return null
     */
    public static String checkFile() {
        Locale locale = Locale.getDefault();
        Log.d("TG", "Locale = " + locale.toString());

        String dateLastModeFile = null;
        File backupDatabase = new File(getPath(), BACKUP_FILENAME);
        if (backupDatabase.exists()) {
            Date lastModDate = new Date(backupDatabase.lastModified());
            DateFormat sdf = new SimpleDateFormat("d.MM.yyyy kk:mm:ss", locale);
            dateLastModeFile = sdf.format(lastModDate);

            Log.d("TG","date - "+
                    android.text.format.DateFormat.format("yyyy-MM-dd kk:mm:ss", lastModDate));

        }
        return dateLastModeFile;
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
     * @param title - caption of dialog
     * @param message - text of dialog
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
                                new MainActivity().restoreDatabase(pathFolder);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            new Analytics(context).sendAnalytics("myCookBook",
                    "Main Activity", "Restore db", "nop");
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
