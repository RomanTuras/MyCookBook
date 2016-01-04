package ua.com.spacetv.mycookbook.helpers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.TextView;

import ua.com.spacetv.mycookbook.FragSubCategory;
import ua.com.spacetv.mycookbook.FragTopCategory;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.tools.StaticFields;


public class FragDialog extends DialogFragment implements StaticFields, DialogInterface.OnClickListener {

    private FragTopCategory fragTopCategory;
    private FragSubCategory fragSubCategory;
    private EditText input;
    private String nameForAction = "";
    private int idDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.idDialog = getArguments().getInt(ID_DIALOG);
        this.nameForAction = getArguments().getString(NAME_FOR_ACTION);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        input = new EditText(getActivity());
        final TextView msg = new TextView(getActivity());

        int title = 0;
        int message = 0;
        switch (idDialog) {
            case DIALOG_ADD_CATEGORY:
                title = R.string.dlg_add_category;
                adb.setMessage(R.string.dlg_space);
                input.setHint(R.string.dlg_hint);
                adb.setView(input);
                break;

            case DIALOG_REN_CATEGORY:
                title = R.string.dlg_rename_category;
                adb.setView(input);
                input.setText(nameForAction);
                break;

            case DIALOG_DEL_CATEGORY:
          /* TODO make check "Folder empty or Not" before call Dialog **/
                title = R.string.dlg_confirm_delete;
                adb.setMessage(nameForAction + "?");
                break;

            case DIALOG_ADD_RECIPE:
                title = R.string.dlg_add_recipe;
                adb.setMessage(R.string.dlg_space);
                input.setHint(R.string.dlg_hint);
                adb.setView(input);
                break;

            case DIALOG_REN_RECIPE:
                title = R.string.dlg_rename_recipe;
                input.setText(nameForAction);
                adb.setView(input);
                break;

            case DIALOG_DEL_RECIPE:
                title = R.string.dlg_confirm_del_recipe;
                adb.setMessage(nameForAction + "?");
                break;
        }
        adb.setTitle(title);
        adb.setPositiveButton(android.R.string.ok, this);
        adb.setNegativeButton(android.R.string.cancel, null);
        return adb.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        /** Dialog Category */
        if (idDialog == DIALOG_ADD_CATEGORY) {
            if (input.getText().toString().length() != 0) {
                new FragTopCategory().onDialogClick(idDialog, input.getText().toString());
            }
        }else if (idDialog == DIALOG_REN_CATEGORY) {
            if (input.getText().toString().length() != 0) {
                new FragTopCategory().onDialogClick(idDialog, input.getText().toString());
            }
        }else if (idDialog == DIALOG_DEL_CATEGORY) {
            new FragTopCategory().onDialogClick(idDialog, null);
        }
        /** Dialog Recipe */
        else if (idDialog == DIALOG_ADD_RECIPE) {

        }else if (idDialog == DIALOG_REN_RECIPE) {

        }else if (idDialog == DIALOG_DEL_RECIPE) {

        }
    }

    @Override
    public void onDismiss(DialogInterface unused) {
        super.onDismiss(unused);
    }

    @Override
    public void onCancel(DialogInterface unused) {
        super.onCancel(unused);
    }
}