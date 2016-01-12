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

package ua.com.spacetv.mycookbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;
import ua.com.spacetv.mycookbook.helpers.FragDialog;
import ua.com.spacetv.mycookbook.tools.ListAdapter;
import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by salden on 02/01/2016.
 * Class is responsible for list top category
 */
public class FragTopCategory extends Fragment implements StaticFields,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private static DataBaseHelper dataBaseHelper;
    private static Context context;
    private static SQLiteDatabase database;
    private static FragmentManager fragmentManager;
    private static ListView listView;
    private static ArrayList<ListData> adapter;
    private static View view;
    private static TextView text_empty_text_topcategory;
    private static String nameForAction;
    public static int  idParentCategory;
    private static OnFragmentEventsListener onFragmentEventsListener;
    private ArrayList<Integer> arrayIdSubCategories;
    private ContentValues contentValues;
    public static String nameOfTopCategory = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.contentValues = new ContentValues();
        FragTopCategory.context = context;
        dataBaseHelper = new DataBaseHelper(context);

        try {
            onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentEventsListener");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_top_category, null);
        listView = (ListView) view.findViewById(R.id.listTopCategory);
        text_empty_text_topcategory = (TextView) view.findViewById(R.id.text_empty_text_topcategory);
        fragmentManager = getFragmentManager();
        database = dataBaseHelper.getWritableDatabase();
        FragTopCategory.view = view;
        return view;
    }

    public void showAllCategory() {
        adapter = new ArrayList<>();
        categoryInList();
        if(adapter.size() == 0) text_empty_text_topcategory
                .setText(R.string.text_add_folder_top_category);
        else text_empty_text_topcategory.setText(null);
        ListAdapter listAdapter = new ListAdapter(context, adapter);
        listView.setAdapter(listAdapter);
        registerForContextMenu(listView);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
    }

    private void categoryInList() {
        Cursor cursor = database.query(TABLE_TOP_CATEGORY, null, null, null, null, null,
                "category", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    String subCategories = getSubcategories(item_id);
                    int numberRecipe = countRecipe(item_id);
                    adapter.add(new ListData(cursor.getString(1), subCategories, ID_IMG_FOLDER,
                            ID_IMG_LIKE_OFF, numberRecipe, item_id, IS_FOLDER));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private int countRecipe(int item_id) {
        int numberRecipe = 0;
        Cursor cursor = database.query(TABLE_LIST_RECIPE, null, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(3) == item_id) { //column 'category_id'
                        numberRecipe++; // count recipes in Top Category
                    } else {
                        for (Integer i : arrayIdSubCategories) { // count recipes in Sub Categories
                            if (i == cursor.getInt(5)) numberRecipe++; //column 'sub_category_id'
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return numberRecipe;
    }

    private String getSubcategories(int item_id) {
        arrayIdSubCategories = new ArrayList<>(); // id all subcategories in parent category
        String subCategories = "";
        Cursor cursor = database.query(TABLE_SUB_CATEGORY, null, null, null, null, null,
                "name", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(3) == item_id) { //column 'parent_id'
                        subCategories += cursor.getString(1); //column 'name'
                        subCategories += ", ";
                        arrayIdSubCategories.add(cursor.getInt(0)); //column '_id'
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if (subCategories.length() != 0) //remove last symbols -> ", "
            subCategories = subCategories.substring(0, subCategories.length() - 2);
        return subCategories;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("TG", "TopCategoryFragment onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        showAllCategory();
        MainActivity.showFloatButtonTopCategory();
        MainActivity.overrideActionBar(null, null);
        Log.d("TG", "TopCategoryFragment onResume");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TG", "TopCategoryFragment onCreate");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        database.close();
        dataBaseHelper.close();
    }

    /**
     * onLongClick() - This returns a boolean to indicate whether you have consumed the event and
     * it should not be carried further. That is, return true to indicate that you have handled
     * the event and it should stop here; return false if you have not handled it and/or the event
     * should continue to any other on-click listeners.
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        nameForAction = adapter.get(position).getListTitle();
        idParentCategory = adapter.get(position).getItemId();
        Log.d("TG", "onItemLongClick "+idParentCategory);
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, ID_POPUP_ITEM_REN, 0, R.string.item_rename);
        menu.add(0, ID_POPUP_ITEM_DEL, 0, R.string.item_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getItemId() == ID_POPUP_ITEM_REN) {
            showDialog(DIALOG_REN_CATEGORY, nameForAction);
        } else if (item.getItemId() == ID_POPUP_ITEM_DEL) {
            if (isCategoryEmpty()){
                showDialog(DIALOG_DEL_CATEGORY, nameForAction);
            }
            else makeSnackbar(context.getResources().getString(R.string.folder_not_empty));
        } else {
            return false;
        }
        return false;
    }

    public static void showDialog(int idDialog, String nameForAction) {
        Bundle bundle = new Bundle();
        bundle.putInt(ID_DIALOG, idDialog);
        bundle.putString(NAME_FOR_ACTION, nameForAction);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_DIALOG);
        if (fragment != null) {
            ft.remove(fragment);
        }
        ft.addToBackStack(null);

        DialogFragment dialogFragment = new FragDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fragmentManager, TAG_DIALOG);
    }

    public void onDialogClick(int idDialog, String param) {
        switch (idDialog) {
            case DIALOG_REN_CATEGORY:
                renameCategory(param);
                break;
            case DIALOG_ADD_CATEGORY:
                addCategory(param);
                break;
            case DIALOG_DEL_CATEGORY:
                deleteCategory();
                break;
        }
    }

    private void addCategory(String param) {
        contentValues = new ContentValues();
        contentValues.put("category", param);
        long rowId = database.insert(TABLE_TOP_CATEGORY, null, contentValues);
        showAllCategory();
        if (rowId >= 0) makeSnackbar(context.getResources().getString(R.string.success));
    }

    private void renameCategory(String param) {
        contentValues = new ContentValues();
        contentValues.put("category", param);
        long rowId = database.update(TABLE_TOP_CATEGORY, contentValues, "_ID=" + idParentCategory, null);
        showAllCategory();
        if (rowId >= 0) makeSnackbar(context.getResources().getString(R.string.success));
    }

    private void deleteCategory() {
        long rowId = database.delete(TABLE_TOP_CATEGORY, "_ID=" + idParentCategory, null);
        showAllCategory();
        if (rowId >= 0) makeSnackbar(context.getResources().getString(R.string.success));
    }

    /**
     * Check in two tables (TABLE_SUB_CATEGORY and TABLE_LIST_RECIPE),
     * if at least one object is in parent category -> return false, else -> true
     */
    private boolean isCategoryEmpty() {
        Cursor cursor = database.query(TABLE_SUB_CATEGORY, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(3) == idParentCategory) { //column 'parent_id'
                        return false;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        cursor = database.query(TABLE_LIST_RECIPE, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(3) == idParentCategory) { //column 'category_id'
                        return false;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListData ld = adapter.get(position);
        nameOfTopCategory = ld.getListTitle();
        onFragmentEventsListener.onListItemClick(ID_ACTION_TOP_CATEGORY, ld.getItemId());
    }

    private void makeSnackbar(String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }


}
