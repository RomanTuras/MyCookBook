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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;
import ua.com.spacetv.mycookbook.helpers.FragDialog;
import ua.com.spacetv.mycookbook.helpers.MenuPopup;
import ua.com.spacetv.mycookbook.tools.ListAdapter;
import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.OnPopupMenuItemClickListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by Roman Turas on 07/01/2016.
 */
public class FragListRecipe extends Fragment implements StaticFields,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener,
        OnPopupMenuItemClickListener {

    private static Context context;
    private static FragmentManager fragmentManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
    public static DataBaseHelper dataBaseHelper;
    public static SQLiteDatabase database;
    private ContentValues contentValues;
    private static ListView listView;
    private static View view;
    public static ArrayList<ListData> adapter;
    private static String nameForAction;
    private static int idItem;
    private static int fav;
    private static int idParentItem = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragListRecipe.context = context;
        FragListRecipe.dataBaseHelper = new DataBaseHelper(context);
        this.contentValues = new ContentValues();

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            FragListRecipe.idParentItem = bundle.getInt(PARENT_ITEM_ID);
        }
        Log.d("TG", "Frag List Recipe : PARENT_ITEM_ID = "+ idParentItem);

        onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_list_recipe, null);
        listView = (ListView) view.findViewById(R.id.listRecipe);
        database = dataBaseHelper.getWritableDatabase();
        fragmentManager = getFragmentManager();
        FragListRecipe.view = view;
        return view;
    }

    public void showListRecipe() {
        adapter = new ArrayList<>();
        recipeInList();
        ListAdapter listAdapter = new ListAdapter(context, adapter);
        listView.setAdapter(listAdapter);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
    }

    private void recipeInList() {
        String selectQuery ="SELECT * FROM " + TABLE_LIST_RECIPE +
                " WHERE sub_category_id=" + idParentItem + " ORDER BY recipe_title";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    int imgLike = cursor.getInt(4);

                    adapter.add(new ListData(cursor.getString(1),
                            "", ID_IMG_RECIPE, imgLike, 0, item_id, IS_RECIPE));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("TG", "Table with recipeCategory - is Empty");
        }
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
        showListRecipe();
        MainActivity.showFloatButtonListRecipe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        database.close();
        dataBaseHelper.close();
    }

    /** onLongClick() - This returns a boolean to indicate whether you have consumed the event and
     * it should not be carried further. That is, return true to indicate that you have handled
     * the event and it should stop here; return false if you have not handled it and/or the event
     * should continue to any other on-click listeners.*/
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        nameForAction = adapter.get(position).getListTitle();
        idItem = adapter.get(position).getItemId();
        new MenuPopup(context, view, ID_TABLE_LIST_RECIPE);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
            showListRecipe();
        }
    }

    public void showDialog(int idDialog, String nameForAction) {
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
            case DIALOG_REN_RECIPE_SUBCATEGORY:
                renameRecipe(param);
                break;
            case DIALOG_ADD_RECIPE_SUBCATEGORY:
                break;
            case DIALOG_DEL_RECIPE_SUBCATEGORY:
                deleteRecipe();
                break;
            case DIALOG_MOV_RECIPE_SUBCATEGORY:
                moveRecipe(typeFolder, idCategory);
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
        showListRecipe();
        if(rowId >= 0)makeSnackbar(context.getResources().getString(R.string.success));
    }

    @Override
    public void onClickPopupMenuItem(int idPopupItem) {
        switch (idPopupItem){
            case ID_POPUP_ITEM_REN:
                showDialog(DIALOG_REN_RECIPE_LIST, nameForAction);
                break;
            case ID_POPUP_ITEM_DEL:
                showDialog(DIALOG_DEL_RECIPE_LIST, nameForAction);
                break;
            case ID_POPUP_ITEM_MOV:
                showDialog(DIALOG_MOV_RECIPE_LIST, nameForAction);
                break;
            case ID_POPUP_ITEM_FAV:
                setUnsetFav();
                break;
        }
    }

    private void deleteRecipe() {
        long rowId = database.delete(TABLE_LIST_RECIPE, "_ID="+idItem,null);
        showListRecipe();
        if(rowId >= 0)makeSnackbar(context.getResources().getString(R.string.success));
    }

    private void renameRecipe(String param) {
        contentValues = new ContentValues();
        contentValues.put("recipe_title" , param);
        long rowId = database.update(TABLE_LIST_RECIPE, contentValues, "_ID="+idItem, null);
        showListRecipe();
        if(rowId >= 0)makeSnackbar(context.getResources().getString(R.string.success));
    }

    private void makeSnackbar(String text){
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
