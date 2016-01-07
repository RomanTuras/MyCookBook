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
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.com.spacetv.mycookbook.FragSubCategory;
import ua.com.spacetv.mycookbook.FragTopCategory;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.tools.StaticFields;

public class FragDialog extends DialogFragment implements StaticFields,
        DialogInterface.OnClickListener, AdapterView.OnItemClickListener {
    private EditText input;
    private ListView eListView;
    private String nameForAction = "";
    private ArrayList<Map<String, Object>> data = new ArrayList<>();
    private Map<String, Object> map;
    private SQLiteDatabase database = FragSubCategory.database;

    private int idDialog;
    private int typeFolder;
    private int idCategory;
    private static final String TITLE_CATEGORY = "title_category";
    private static final String TITLE_SUBCATEGORY = "title_subcategory";
    private static final String ID_ITEM = "id_item";
    private static final String TYPE_FOLDER = "type_folder";
    private static final String IMG = "img";

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
        eListView = new ListView(getActivity());
        final TextView msg = new TextView(getActivity());

        int title = 0;
        int message = 0;
        switch (idDialog) {
            /** Dialog Category */
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
                title = R.string.dlg_confirm_delete;
                adb.setMessage(nameForAction + "?");
                break;

            /** Dialog Sub Category */
            case DIALOG_ADD_SUBCATEGORY:
                title = R.string.dlg_add_category;
                adb.setMessage(R.string.dlg_space);
                input.setHint(R.string.dlg_hint);
                adb.setView(input);
                break;
            case DIALOG_REN_SUBCATEGORY:
                title = R.string.dlg_rename_category;
                adb.setView(input);
                input.setText(nameForAction);
                break;
            case DIALOG_DEL_SUBCATEGORY:
                title = R.string.dlg_confirm_delete;
                adb.setMessage(nameForAction + "?");
                break;

            /** Dialog Recipe */
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
            case DIALOG_MOV_RECIPE:
                title = R.string.dlg_move;
                categoryInList();
                eListView.setSelected(true);
                eListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                eListView.setSelector(R.drawable.color_list_selector);
                eListView.setOnItemClickListener(this);
                adb.setView(eListView);
                break;
        }
        adb.setTitle(title);
        adb.setPositiveButton(android.R.string.ok, this);
        adb.setNegativeButton(android.R.string.cancel, null);
        return adb.create();
    }

    private void categoryInList() {
        Cursor cursor = database.query(TABLE_TOP_CATEGORY, null, null, null, null, null,
                "category", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    map = new HashMap<>();
                    map.put(TITLE_CATEGORY, cursor.getString(1));
                    map.put(ID_ITEM, cursor.getInt(0));
                    map.put(TYPE_FOLDER, PARENT);
                    map.put(IMG, R.drawable.ic_folder_black_48dp);
                    data.add(map);
                    subCategoryInList(cursor.getInt(0));
                } while (cursor.moveToNext());
            }
        }

        String[] from = { TITLE_CATEGORY, TITLE_SUBCATEGORY, IMG, ID_ITEM, TYPE_FOLDER };
        int[] to = { R.id.textListDialogTitle, R.id.textListDialogSubTitle, R.id.imgTopCategory };

        SimpleAdapter sAdapter = new SimpleAdapter(getActivity(), data,
                R.layout.format_list_dialog, from, to);
        eListView.setAdapter(sAdapter);
    }

    private void subCategoryInList(int idParent){
        String selectQuery ="SELECT * FROM " + TABLE_SUB_CATEGORY +
                " WHERE parent_id=" + idParent + " ORDER BY name";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    map = new HashMap<>();
                    map.put(TITLE_SUBCATEGORY, cursor.getString(1));
                    map.put(ID_ITEM, cursor.getInt(0));
                    map.put(TYPE_FOLDER, CHILD);
                    map.put(IMG, R.drawable.ic_folder_open_black_24dp);
                    data.add(map);
                } while (cursor.moveToNext());
            }
        }
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

        /** Dialog Sub Category */
        if (idDialog == DIALOG_ADD_SUBCATEGORY) {
            if (input.getText().toString().length() != 0) {
                new FragSubCategory().onDialogClick(idDialog, input.getText().toString(), 0, 0);
            }
        }else if (idDialog == DIALOG_REN_SUBCATEGORY) {
            if (input.getText().toString().length() != 0) {
                new FragSubCategory().onDialogClick(idDialog, input.getText().toString(), 0, 0);
            }
        }else if (idDialog == DIALOG_DEL_SUBCATEGORY) {
            new FragSubCategory().onDialogClick(idDialog, null, 0, 0);
        }

        /** Dialog Recipe */
        else if (idDialog == DIALOG_ADD_RECIPE) {

        }else if (idDialog == DIALOG_REN_RECIPE) {
            new FragSubCategory().onDialogClick(idDialog, input.getText().toString(), 0, 0);

        }else if (idDialog == DIALOG_DEL_RECIPE) {
            new FragSubCategory().onDialogClick(idDialog, null, 0, 0);
        }else if(idDialog == DIALOG_MOV_RECIPE){
            new FragSubCategory().onDialogClick(idDialog, null, typeFolder, idCategory);
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

    /** Save params of select item*/
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        typeFolder = (int) data.get(position).get(TYPE_FOLDER);
        idCategory = (int) data.get(position).get(ID_ITEM);
        Log.d("TG", "typeFolder= "+typeFolder+"  idCategory = "+idCategory);
    }
}