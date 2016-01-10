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
 */
public class FragSubCategory extends Fragment implements StaticFields,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private static Context context;
    private static FragmentManager fragmentManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
    public static DataBaseHelper dataBaseHelper;
    public static SQLiteDatabase database;
    private static ListView listView;
    private static View view;
    public static ArrayList<ListData> adapter;
    private ContentValues contentValues;
    private static TextView text_empty_text_subcategory;
    private static String nameForAction;
    public static int idItem; //id sub category
    private static int fav;
    public static int idParentItem = 0; //id TOP category, income in params
    private static boolean isFolder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragSubCategory.context = context;
        FragSubCategory.dataBaseHelper = new DataBaseHelper(context);
        this.contentValues = new ContentValues();

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            FragSubCategory.idParentItem = bundle.getInt(TAG_PARENT_ITEM_ID);
        }
        Log.d("TG", "TAG_PARENT_ITEM_ID = "+ idParentItem);

        try {
            onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentEventsListener");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_sub_category, null);
        listView = (ListView) view.findViewById(R.id.listSubCategory);
        text_empty_text_subcategory = (TextView) view.findViewById(R.id.text_empty_text_subcategory);
        database = dataBaseHelper.getWritableDatabase();
        fragmentManager = getFragmentManager();
        FragSubCategory.view = view;
        return view;
    }

    public void showCategoryAndRecipe() {
        adapter = new ArrayList<>();
        subCategoryInList();
        recipeInList();
        if(adapter.size() == 0) text_empty_text_subcategory.setText(R.string.text_add_folder_or_recipe);
        ListAdapter listAdapter = new ListAdapter(context, adapter);
        listView.setAdapter(listAdapter);
        registerForContextMenu(listView);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        listView.requestFocus();
    }

    private void recipeInList() {
        String selectQuery ="SELECT * FROM " + TABLE_LIST_RECIPE +
                " WHERE category_id=" + idParentItem + " ORDER BY recipe_title";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    int imgLike = cursor.getInt(4);

                    adapter.add(new ListData(cursor.getString(1),
                            "", ID_IMG_RECIPE,
                            imgLike, 0, item_id, IS_RECIPE));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("TG", "Table with recipeCategory - is Empty");
        }
    }

    public void subCategoryInList() {
        String selectQuery ="SELECT * FROM " + TABLE_SUB_CATEGORY +
                " WHERE parent_id=" + idParentItem + " ORDER BY name";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    int numberRecipe = countRecipe(item_id);
                    adapter.add(new ListData(cursor.getString(1), "",
                            ID_IMG_FOLDER, ID_IMG_LIKE_OFF, numberRecipe, item_id, IS_FOLDER));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("TG", "Table with SubCategories - is Empty");
        }
    }

    private int countRecipe(int item_id) {
        int numberRecipe = 0;
        Cursor cursor = database.query(TABLE_LIST_RECIPE, null, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(5) == item_id) { //column 'sub_category_id'
                        numberRecipe++; // count recipes in Sub Category
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return numberRecipe;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        showCategoryAndRecipe();
        MainActivity.showFloatMenuSubCategory();
        MainActivity.overrideActionBar(R.string.app_name, 0);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        database.close();
        dataBaseHelper.close();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    /** onLongClick() - This returns a boolean to indicate whether you have consumed the event and
     * it should not be carried further. That is, return true to indicate that you have handled
     * the event and it should stop here; return false if you have not handled it and/or the event
     * should continue to any other on-click listeners.*/
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        nameForAction = adapter.get(position).getListTitle();
        idItem = adapter.get(position).getItemId();
        isFolder = adapter.get(position).getTypeItem();
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if(isFolder) {
            menu.add(0, ID_POPUP_ITEM_REN, 0, R.string.item_rename);
            menu.add(0, ID_POPUP_ITEM_DEL, 0, R.string.item_delete);
        }else{
            menu.add(0, ID_POPUP_ITEM_FAV, 0, R.string.item_favorite);
            menu.add(0, ID_POPUP_ITEM_REN, 0, R.string.item_rename);
            menu.add(0, ID_POPUP_ITEM_MOV, 0, R.string.item_move);
            menu.add(0, ID_POPUP_ITEM_DEL, 0, R.string.item_delete);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case ID_POPUP_ITEM_REN:
                if(isFolder) showDialog(DIALOG_REN_SUBCATEGORY, nameForAction);
                else showDialog(DIALOG_REN_RECIPE_SUBCATEGORY, nameForAction);
                break;
            case ID_POPUP_ITEM_DEL:
                if(isFolder){
                    if(isSubCategoryEmpty()) showDialog(DIALOG_DEL_SUBCATEGORY, nameForAction);
                    else makeSnackbar(context.getResources().getString(R.string.folder_not_empty));
                }
                else showDialog(DIALOG_DEL_RECIPE_SUBCATEGORY, nameForAction);
                break;
            case ID_POPUP_ITEM_MOV:
                showDialog(DIALOG_MOV_RECIPE_SUBCATEGORY, nameForAction);
                break;
            case ID_POPUP_ITEM_FAV:
                setUnsetFav();
                break;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListData ld = adapter.get(position);
        isFolder = adapter.get(position).getTypeItem();
        idItem = ld.getItemId(); // get id of pressed item: Folder or Recipe
        if(isFolder) {
            onFragmentEventsListener.onListItemClick(ID_ACTION_SUB_CATEGORY_CATEGORY, idItem);
        }
        else onFragmentEventsListener.onListItemClick(ID_ACTION_SUB_CATEGORY_RECIPE, idItem);
    }

    private void setUnsetFav() {
        Cursor cursor = database.query(TABLE_LIST_RECIPE, null, null, null, null,
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(0) == idItem) {
                        fav = cursor.getInt(4);
                        fav = fav == 0 ? 1 : 0; // if recipe was 'like' unlike him
                    }
                }while (cursor.moveToNext());
            }
            contentValues = new ContentValues();
            contentValues.put("make", fav);
            long rowId = database.update(TABLE_LIST_RECIPE, contentValues, "_ID=" + idItem, null);
            showCategoryAndRecipe();
        }
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

    public void onDialogClick(int idDialog, String param, int typeFolder, int idCategory){
        switch (idDialog){
            case DIALOG_REN_SUBCATEGORY:
                renameSubCategory(param);
                break;
            case DIALOG_ADD_SUBCATEGORY:
                addSubCategory(param);
                break;
            case DIALOG_DEL_SUBCATEGORY:
                deleteSubCategory();
                break;
            case DIALOG_REN_RECIPE_SUBCATEGORY:
                renameRecipe(param);
                break;
            case DIALOG_ADD_RECIPE_SUBCATEGORY:
                break;
            case DIALOG_DEL_RECIPE_SUBCATEGORY:
                deleteRecipe();
                break;
            case DIALOG_MOV_RECIPE_SUBCATEGORY:
                if(idCategory != NOP) moveRecipe(typeFolder, idCategory);
                else makeSnackbar(context.getResources()
                        .getString(R.string.folder_folder_not_select));
                break;
        }
    }

    /** Just change value in columns "category_id" & "sub_category_id" in TABLE_LIST_RECIPE */
    private void moveRecipe(int typeFolder, int idCategory) {
        contentValues = new ContentValues();
        if(typeFolder == PARENT){
            contentValues.put("category_id" , idCategory);
            contentValues.put("sub_category_id" , DEFAULT_VALUE_COLUMN);
        }else if(typeFolder == CHILD){
            contentValues.put("category_id" , DEFAULT_VALUE_COLUMN);
            contentValues.put("sub_category_id" , idCategory);
        }
        long rowId = database.update(TABLE_LIST_RECIPE, contentValues, "_ID="+idItem, null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(context.getResources().getString(R.string.success));
    }

    private void deleteRecipe() {
        long rowId = database.delete(TABLE_LIST_RECIPE, "_ID="+idItem,null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(context.getResources().getString(R.string.success));
    }

    private void renameRecipe(String param) {
        contentValues = new ContentValues();
        contentValues.put("recipe_title" , param);
        long rowId = database.update(TABLE_LIST_RECIPE, contentValues, "_ID="+idItem, null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(context.getResources().getString(R.string.success));
    }

    private void renameSubCategory(String param) {
        contentValues = new ContentValues();
        contentValues.put("name" , param);
        long rowId = database.update(TABLE_SUB_CATEGORY, contentValues, "_ID="+idItem, null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(context.getResources().getString(R.string.success));
    }

    /** Add subcategory in to parent category */
    private void addSubCategory(String param) {
        contentValues = new ContentValues();
        contentValues.put("name" , param);
        contentValues.put("parent_id", idParentItem);
        long rowId = database.insert(TABLE_SUB_CATEGORY, null, contentValues);
        showCategoryAndRecipe();
        if(rowId >= 0) makeSnackbar(context.getResources().getString(R.string.success));
    }

    /** Search in table 'TABLE_LIST_RECIPE' recipe with 'sub_category_id = pressed id' */
    private boolean isSubCategoryEmpty() {
        Cursor cursor = database.query(TABLE_LIST_RECIPE, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(5) == idItem) { //column 'sub_category_id'
                        return false;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return true;
    }

    private void deleteSubCategory() {
        long rowId = database.delete(TABLE_SUB_CATEGORY, "_ID="+idItem,null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(context.getResources().getString(R.string.success));
    }

    private void makeSnackbar(String text){
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
