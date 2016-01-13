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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;
import ua.com.spacetv.mycookbook.helpers.FragDialog;
import ua.com.spacetv.mycookbook.helpers.PrepareListRecipes;
import ua.com.spacetv.mycookbook.tools.ListAdapter;
import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by Roman Turas on 07/01/2016.
 */
public class FragListRecipe extends Fragment implements StaticFields,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private static Context context;
    private static FragmentManager fragmentManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
    public static DataBaseHelper dataBaseHelper;
    public static SQLiteDatabase database;
    private static ListView listView;
    private static View view;
    public static ArrayList<ListData> adapter;
    private static TextView text_empty_text_list_recipe;
    private ContentValues contentValues;
    private static String nameForAction;
    private static int idItem;
    private static int fav; // key, added recipe in favorite list
    public static int idParentItem = 0; //id subcategory where is recipe
    private static int startupMode = MODE_RECIPE_FROM_CATEGORY;
    private static String searchString;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragListRecipe.context = context;
        dataBaseHelper = new DataBaseHelper(context);
        this.contentValues = new ContentValues();

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            idParentItem = bundle.getInt(TAG_PARENT_ITEM_ID);
            startupMode = bundle.getInt(TAG_MODE);
            searchString = bundle.getString(TAG_SEARCH_STRING);
        }
        Log.d("TG", "ListResipe:   = idParentItem"+ idParentItem+"  startupMode= "+startupMode);

        onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_list_recipe, null);
        listView = (ListView) view.findViewById(R.id.listRecipe);
        text_empty_text_list_recipe = (TextView) view.findViewById(R.id.text_empty_text_list_recipe);
        database = dataBaseHelper.getWritableDatabase();
        fragmentManager = getFragmentManager();
        FragListRecipe.view = view;
        return view;
    }

    public static void setParams(int idParentItem, int startupMode, String searchString){
        FragListRecipe.startupMode = startupMode;
        FragListRecipe.idParentItem = idParentItem;
        FragListRecipe.searchString = searchString;
    }

    public void showListRecipe() {
        if(startupMode == MODE_RECIPE_FROM_CATEGORY) {
            adapter = new PrepareListRecipes(context, idParentItem).getFilledAdapter();
            if(adapter.size() == 0){
                text_empty_text_list_recipe.setText(R.string.text_add_recipe);
            }else text_empty_text_list_recipe.setText(null);

            if(FragTopCategory.nameOfTopCategory != null){
                String path = FragTopCategory.nameOfTopCategory;
                if(FragSubCategory.nameOfSubCategory != null){
                    path += "\\ " + FragSubCategory.nameOfSubCategory;
                }
                MainActivity.overrideActionBar(null, path);
            }else MainActivity.overrideActionBar(null, null);

            MainActivity.showFloatButtonListRecipe();
        }else if(startupMode == MODE_FAVORITE_RECIPE){

            MainActivity.overrideActionBar(null,
                    context.getString(R.string.text_list_favorite_recipe));
            adapter = new PrepareListRecipes(context).getFilledAdapter();
            if(adapter.size() == 0){
                text_empty_text_list_recipe.setText(R.string.text_favorite_not_found);
            }else text_empty_text_list_recipe.setText(null);
            MainActivity.hideAllFloatButtons();
        }else if(startupMode == MODE_SEARCH_RESULT){
            MainActivity.overrideActionBar(null,
                    context.getString(R.string.text_list_search_result));

            adapter = new PrepareListRecipes(context, searchString).getFilledAdapter();
            if(adapter.size() == 0){
                text_empty_text_list_recipe.setText(R.string.text_search_not_found);
            }else text_empty_text_list_recipe.setText(null);
            MainActivity.hideAllFloatButtons();
        }
        ListAdapter listAdapter = new ListAdapter(context, adapter);
        listView.setAdapter(listAdapter);
        registerForContextMenu(listView);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        setHasOptionsMenu(true);
        setHasOptionsMenu(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        if(startupMode == MODE_RECIPE_FROM_CATEGORY) inflater.inflate(R.menu.main, menu);
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
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, ID_POPUP_ITEM_FAV, 0, R.string.item_favorite);
            menu.add(0, ID_POPUP_ITEM_REN, 0, R.string.item_rename);
            menu.add(0, ID_POPUP_ITEM_MOV, 0, R.string.item_move);
            menu.add(0, ID_POPUP_ITEM_DEL, 0, R.string.item_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case ID_POPUP_ITEM_REN:
                showDialog(DIALOG_REN_RECIPE_LISTRECIPE, nameForAction);
                break;
            case ID_POPUP_ITEM_DEL:
                showDialog(DIALOG_DEL_RECIPE_LISTRECIPE, nameForAction);
                break;
            case ID_POPUP_ITEM_MOV:
                showDialog(DIALOG_MOV_RECIPE_LISTRECIPE, nameForAction);
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
        Log.d("TG","ListResipe: onItemClick = "+ld.getItemId());
        onFragmentEventsListener.onListItemClick(ID_ACTION_LIST_RECIPE, ld.getItemId());
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
            case DIALOG_REN_RECIPE_LISTRECIPE:
                renameRecipe(param);
                break;
            case DIALOG_DEL_RECIPE_LISTRECIPE:
                deleteRecipe();
                break;
            case DIALOG_MOV_RECIPE_LISTRECIPE:
                if(idCategory != NOP) moveRecipe(typeFolder, idCategory);
                else makeSnackbar(context
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
        showListRecipe();
        Log.d("TG", "Frag List Recipe : moveRecipe ");

        if(rowId >= 0)makeSnackbar(context.getString(R.string.success));
    }

    private void deleteRecipe() {
        long rowId = database.delete(TABLE_LIST_RECIPE, "_ID="+idItem,null);
        showListRecipe();
        if(rowId >= 0)makeSnackbar(context.getString(R.string.success));
    }

    private void renameRecipe(String param) {
        contentValues = new ContentValues();
        contentValues.put("recipe_title" , param);
        long rowId = database.update(TABLE_LIST_RECIPE, contentValues, "_ID="+idItem, null);
        showListRecipe();
        if(rowId >= 0)makeSnackbar(context.getString(R.string.success));
    }

    private void makeSnackbar(String text){
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
