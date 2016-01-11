/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ua.com.spacetv.mycookbook.helpers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ua.com.spacetv.mycookbook.MainActivity;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/** Created by Roman Turas on 11/01/2016.
 * */

public class SaveRestoreDialog extends DialogFragment implements StaticFields,
        DialogInterface.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private Context context;
    private AlertDialog.Builder adb;
    private TextView textViewTop, textViewBottom, textTitle, textWarning;
    private LinearLayout layout;
    private RadioButton rButtonSdcard, rButtonPhone;
    private RadioGroup radioGroup;
    private int idDialog;
    private String pathFolder = null;
    private String backupFile = null;
    private String textTop = "";
    private String textBottom = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.idDialog = getArguments().getInt(ID_DIALOG);
        this.context = getContext();
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initAllViewsInDialog();
        pathFolder = getPath();
        backupFile = checkFile();
        selectDialog();
        textViewTop.setText(textTop);
        textViewBottom.setText(textBottom);

        adb.setCustomTitle(textTitle);
        layout.addView(textViewTop);
        layout.addView(textViewBottom);
        layout.addView(textWarning);
        adb.setView(layout);
        adb.setPositiveButton(android.R.string.ok, this);
        adb.setNegativeButton(android.R.string.cancel, null);
        //        rButtonSdcard.setText(R.string.dlg_sdcard);
        //        rButtonPhone.setText(R.string.dlg_memory_phone);
        //        layout.addView(radioGroup);
        return adb.create();
    }

    private void selectDialog(){
        switch (idDialog) {
            case DIALOG_FILE_SAVE:
                textTitle.setText(getResources().getString(R.string.dlg_save_file));
                textWarning.setVisibility(View.GONE);
                if(pathFolder != null) {
                    textTop = getResources().getString(R.string.dlg_path_to_file);
                    textTop += " "+pathFolder;
                    textBottom = getResources().getString(R.string.dlg_name_of_file);
                }else{
                    textTop = getResources().getString(R.string.dlg_sdcard_disabled);
                    textBottom = getResources().getString(R.string.dlg_sdcard_check_mount);
                }
                break;
            case DIALOG_FILE_RESTORE:
                textTitle.setText(getResources().getString(R.string.dlg_restore_file));
                if(pathFolder != null) {
                    Log.d("TG", "DIALOG_FILE_RESTORE pathFolder ="+pathFolder);
                    if(backupFile != null) {
                        Log.d("TG", "DIALOG_FILE_RESTORE checkFile() ="+checkFile());
                        textTop = getResources().getString(R.string.dlg_file_found);
                        textBottom = checkFile();
                        textWarning.setText(getResources().getString(R.string.dlg_warning));
                    }else{
                        Log.d("TG", "DIALOG_FILE_RESTORE checkFile() =");
                        textTop = getResources().getString(R.string.dlg_file_not_found);
                        textBottom = getResources().getString(R.string.dlg_sdcard_not_prepared);
                    }
                }else{
                    Log.d("TG", "DIALOG_FILE_RESTORE pathFolder =");
                    textTop = getResources().getString(R.string.dlg_folder_not_found);
                    textBottom = getResources().getString(R.string.dlg_sdcard_not_prepared);
                }
                break;
        }
    }

    private void initAllViewsInDialog(){
        adb = new AlertDialog.Builder(getActivity());
        layout = new LinearLayout(getActivity());
        textViewTop = new TextView(getActivity());
        textViewBottom = new TextView(getActivity());
        textTitle = new TextView(getActivity());
        textWarning = new TextView(getActivity());

        radioGroup = new RadioGroup(getActivity());
        rButtonSdcard = new RadioButton(getActivity());
        rButtonPhone = new RadioButton(getActivity());
        radioGroup.addView(rButtonSdcard);
        radioGroup.addView(rButtonPhone);
        radioGroup.setOnCheckedChangeListener(this);
        radioGroup.check(rButtonPhone.getId());

        int paddingH = (int) getResources().getDimension(R.dimen.dialog_padding_left_right);
        int paddingV = (int) getResources().getDimension(R.dimen.dialog_padding_up_down);
        int titleColor = ContextCompat.getColor(context, R.color.colorWhite);
        int bgTitleColor = ContextCompat.getColor(context, R.color.colorPrimary);
        int warningColor = ContextCompat.getColor(context, R.color.colorAccent);

        if (Build.VERSION.SDK_INT < 23) {
            textViewTop.setTextAppearance(context, android.R.style.TextAppearance_Small);
            textViewBottom.setTextAppearance(context, android.R.style.TextAppearance_Small);
            textTitle.setTextAppearance(context, android.R.style.TextAppearance_Large);
            textWarning.setTextAppearance(context, android.R.style.TextAppearance_Small);
        } else {
            textViewTop.setTextAppearance(android.R.style.TextAppearance_Small);
            textViewBottom.setTextAppearance(android.R.style.TextAppearance_Small);
            textTitle.setTextAppearance(android.R.style.TextAppearance_Large);
            textWarning.setTextAppearance(android.R.style.TextAppearance_Small);
        }

        layout.setOrientation(LinearLayout.VERTICAL);

        textViewTop.setTextColor(bgTitleColor);
        textViewBottom.setTextColor(bgTitleColor);
        textWarning.setTextColor(warningColor);

        textTitle.setBackgroundColor(bgTitleColor);
        textTitle.setTextColor(titleColor);
        textTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        textWarning.setGravity(Gravity.CENTER_HORIZONTAL);

        textViewTop.setPadding(paddingH, paddingV, paddingH, paddingV);
        textViewBottom.setPadding(paddingH, 0, paddingH, paddingV);
        textTitle.setPadding(paddingH, paddingV, paddingH, paddingV);
        rButtonSdcard.setPadding(paddingH, paddingV, paddingH, paddingV);
        rButtonPhone.setPadding(paddingH, paddingV, paddingH, paddingV);
        textWarning.setPadding(paddingH, paddingV, paddingH, paddingV);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
    }

    /** Check, if backup exist - get date of last modified */
    private String checkFile(){
        String dateLastModeFile = null;
        File backupDatabase = new File(pathFolder, BACKUP_FILENAME);
        if(backupDatabase.exists()) {
            Date lastModDate = new Date(backupDatabase.lastModified());
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy hh:mm:ss");
            dateLastModeFile = sdf.format(lastModDate);
        }
        return dateLastModeFile;
    }

    private String getPath(){
        String sdState = Environment.getExternalStorageState();
        String path = null;
        if(sdState.equals(Environment.MEDIA_MOUNTED))
        {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += "/" + FOLDER_NAME + "/";
        }
        return path;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_POSITIVE) {
            /** Dialog Category */
            if (idDialog == DIALOG_FILE_SAVE) {
                if(pathFolder != null) {
                    try {
                        new MainActivity().saveDatabaseFile(pathFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else if (idDialog == DIALOG_FILE_RESTORE) {
                if(pathFolder != null & backupFile != null) {
                    try {
                        new MainActivity().restoreDatabase(pathFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else onCancel(dialog);

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