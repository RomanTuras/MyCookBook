package ua.com.spacetv.mycookbook.tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.TextView;

import ua.com.spacetv.mycookbook.R;

/**
 * The static utility class
 */
public class Utilities {
    //The server timeout. After this time the Server error message would be shown
    private static final long SERVER_TIMEOUT_MS = 600000;

    public interface IYesNoCallback{
        void onYes();
//        void onNo();
    }

    /**
     * Shows the dialog with the single OK button where the text is centered
     */
    public static void showOkDialog(Context context, String string, final IYesNoCallback listener){
        if (context == null) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context)
                .setPositiveButton(context.getResources()
                        .getString(R.string.common_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) listener.onYes();
                    }
                });

        TextView textView = new TextView(context);
        textView.setText(string);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(20);
        textView.setPadding(10, 30, 10, 30);
        builder.setView(textView);

        builder.show();
    }

    /**
     * Starts the progress dialog.
     *
     * @param activity
     * @return
     */
    public static ProgressDialog startProgressDialog(final Activity activity){
        if (activity == null) return null;

        final ProgressDialog resultDialog = new ProgressDialog(activity);
        resultDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        resultDialog.setMessage(activity.getResources()
                .getString(R.string.common_progress_message));
        resultDialog.setCancelable(false);
        resultDialog.show();

        //Closes the dialog after a timeout
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(SERVER_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    //
                }
                if (resultDialog.isShowing()){
                    //means that connection is enabled as no response during timeout
                    resultDialog.dismiss();

                    //Shows the error message dependably by the type
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            showOkDialog(activity, activity.getResources()
                                    .getString(R.string.common_server_error));
                        }
                    });

                }
            }
        }).start();

        return resultDialog;
    }

    /**
     * Shows the dialog with the single OK button where the text is centered
     */
    public static void showOkDialog(Context context, String string){
        if (context == null) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context)
                .setPositiveButton(context.getResources()
                        .getString(R.string.common_dialog_ok), null);

        TextView textView = new TextView(context);
        textView.setText(string);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        //textView.setTextColor(Color.WHITE);
        textView.setTextSize(20);
        textView.setPadding(10, 30, 10, 30);
        builder.setView(textView);

        builder.show();
    }
}
