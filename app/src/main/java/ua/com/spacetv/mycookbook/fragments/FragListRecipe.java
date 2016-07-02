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

package ua.com.spacetv.mycookbook.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.spacetv.mycookbook.MainActivity;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.dialogs.FragDialog;
import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;
import ua.com.spacetv.mycookbook.helpers.PrepareListRecipes;
import ua.com.spacetv.mycookbook.interfaces.Constants;
import ua.com.spacetv.mycookbook.interfaces.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.ListAdapter;
import ua.com.spacetv.mycookbook.tools.ListData;

/**
 * Created by Roman Turas on 07/01/2016.
 *
 */
public class FragListRecipe extends Fragment implements Constants,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {

    private static Context mContext;
    private static FragmentManager mFrManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
    public static DataBaseHelper mDataBaseHelper;
    public static SQLiteDatabase mDatabase;
    private static ListView mListView;
    private static View view;
    public static ArrayList<ListData> mAdapter;
    private static TextView text_empty_text_list_recipe;
    private ContentValues mContentValues;
    private static String mNameForAction;
    private static int mIdItem;
    private static int mFav; // key, added recipe in favorite list
    public static int mIdParentItem = 0; //id subcategory where is recipe
    private static int mStartupMode = MODE_RECIPE_FROM_CATEGORY;
    private static String mSearchString;
    private static int mFirstVisibleItem = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragListRecipe.mContext = context;
        mDataBaseHelper = new DataBaseHelper(context);
        mContentValues = new ContentValues();
        setRetainInstance(true);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mIdParentItem = bundle.getInt(TAG_PARENT_ITEM_ID);
            mStartupMode = bundle.getInt(TAG_MODE);
            mSearchString = bundle.getString(TAG_SEARCH_STRING);
        }//else getSettingsFromPreferences();
        Log.d("TG", "ListResipe:   = mIdParentItem" + mIdParentItem + "  mStartupMode= " + mStartupMode);

        onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_list_recipe, null);
        mListView = (ListView) view.findViewById(R.id.listRecipe);
        text_empty_text_list_recipe = (TextView) view.findViewById(R.id.text_empty_text_list_recipe);
        mDatabase = mDataBaseHelper.getWritableDatabase();
        mFrManager = getFragmentManager();
        FragListRecipe.view = view;
        return view;
    }

    /**
     * Saving preferences
     * <p/>
     * mFirstVisibleItem - of the list view
     * mQuery - query if it is
     */
    private void setSettingsToPreferences() {
        SharedPreferences userDetails =
                mContext.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = userDetails.edit();
        edit.clear();
        edit.putInt(FIRST_VISIBLE_ITEM, mFirstVisibleItem);
        edit.putInt(SAVED_PARENT_ITEM_ID, mIdParentItem);
        edit.putInt(SAVED_MODE, mStartupMode);
        edit.putString(SAVED_QUERY, mSearchString);
        edit.apply();
    }

    /**
     * Getting stored preferences
     */
    private void getSettingsFromPreferences() {
        SharedPreferences userDetails =
                mContext.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mFirstVisibleItem = userDetails.getInt(FIRST_VISIBLE_ITEM, 0);//get preferences
        mIdParentItem = userDetails.getInt(SAVED_PARENT_ITEM_ID, 0);
        mStartupMode = userDetails.getInt(SAVED_MODE, 0);
        mSearchString = userDetails.getString(SAVED_QUERY, null);

    }


    public static void setParams(int idParentItem, int startupMode, String searchString) {
        FragListRecipe.mStartupMode = startupMode;
        FragListRecipe.mIdParentItem = idParentItem;
        FragListRecipe.mSearchString = searchString;
    }

    public void showListRecipe() {
        switch (mStartupMode) {
            case MODE_RECIPE_FROM_CATEGORY:
                mAdapter = new PrepareListRecipes(mContext, mIdParentItem).getFilledAdapter();
                Log.d("TG", "MODE_RECIPE_FROM_CATEGORY");
                if (mAdapter.size() == 0) {
                    text_empty_text_list_recipe.setText(R.string.text_add_recipe);
                } else text_empty_text_list_recipe.setText(null);

                if (FragTopCategory.nameOfTopCategory != null) {
                    String path = FragTopCategory.nameOfTopCategory;
                    if (FragSubCategory.nameOfSubCategory != null) {
                        path += "\\ " + FragSubCategory.nameOfSubCategory;
                    }
                    MainActivity.overrideActionBar(null, path);
                } else MainActivity.overrideActionBar(null, null);

                MainActivity.showFloatButtonListRecipe();
                setHasOptionsMenu(false); // do not override menu, keep it from main activity
                break;

            case MODE_FAVORITE_RECIPE:
                MainActivity.overrideActionBar(null,
                        mContext.getString(R.string.text_list_favorite_recipe));
                Log.d("TG", "MODE_FAVORITE_RECIPE");
                mAdapter = new PrepareListRecipes(mContext).getFilledAdapter();
                if (mAdapter.size() == 0) {
                    text_empty_text_list_recipe.setText(R.string.text_favorite_not_found);
                } else text_empty_text_list_recipe.setText(null);
                MainActivity.hideAllFloatButtons();
                setHasOptionsMenu(true); //override menu, call 'onCreateOptionsMenu' in this class
                break;

            case MODE_SEARCH_RESULT:
                MainActivity.overrideActionBar(null,
                        mContext.getString(R.string.text_list_search_result));
                Log.d("TG", "MODE_SEARCH_RESULT");
                mAdapter = new PrepareListRecipes(mContext, mSearchString).getFilledAdapter();
                if (mAdapter.size() == 0) {
                    text_empty_text_list_recipe.setText(R.string.text_search_not_found);
                } else text_empty_text_list_recipe.setText(null);
                MainActivity.hideAllFloatButtons();
                setHasOptionsMenu(true); //override menu, call 'onCreateOptionsMenu' in this class
                break;
        }
        ListAdapter listAdapter = new ListAdapter(mContext, mAdapter);
        mListView.setAdapter(listAdapter);
        registerForContextMenu(mListView);
        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setSelection(mFirstVisibleItem); //mechanism save and restore state of list view
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        setSettingsToPreferences();
//        Log.d("TG", "onPause FragListRecipe : ");
//        MainActivity.saveListState(TAG_LIST_RECIPE, mFirstVisibleItem); //save list view state
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
//        mFirstVisibleItem = MainActivity.restoreListState(TAG_LIST_RECIPE); //restore list view state
        MainActivity.isNoFragmentsAttached = false; //fragment attached
        MainActivity.listAllFragments();
//        getSettingsFromPreferences();
        showListRecipe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity.saveListState(TAG_LIST_RECIPE, 0); //reset list view state
        mDatabase.close();
        mDataBaseHelper.close();
    }

    /**
     * onLongClick() - This returns a boolean to indicate whether you have consumed the event and
     * it should not be carried further. That is, return true to indicate that you have handled
     * the event and it should stop here; return false if you have not handled it and/or the event
     * should continue to any other on-click listeners.
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mNameForAction = mAdapter.get(position).getListTitle();
        mIdItem = mAdapter.get(position).getItemId();
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
        switch (item.getItemId()) {
            case ID_POPUP_ITEM_REN:
                showDialog(DIALOG_REN_RECIPE_LISTRECIPE, mNameForAction);
                break;
            case ID_POPUP_ITEM_DEL:
                showDialog(DIALOG_DEL_RECIPE_LISTRECIPE, mNameForAction);
                break;
            case ID_POPUP_ITEM_MOV:
                showDialog(DIALOG_MOV_RECIPE_LISTRECIPE, mNameForAction);
                break;
            case ID_POPUP_ITEM_FAV:
                setUnsetFav();
                break;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListData ld = mAdapter.get(position);
        Log.d("TG", "ListResipe: onItemClick = " + ld.getItemId());
        onFragmentEventsListener.onListItemClick(ID_ACTION_LIST_RECIPE, ld.getItemId());
    }

    private void setUnsetFav() {
        Cursor cursor = mDatabase.query(TABLE_LIST_RECIPE, null, null, null, null,
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(0) == mIdItem) {
                        mFav = cursor.getInt(4);
                        mFav = mFav == 0 ? 1 : 0; // if recipe was 'like' unlike him
                    }
                } while (cursor.moveToNext());
            }
            mContentValues = new ContentValues();
            mContentValues.put("make", mFav);
            long rowId = mDatabase.update(TABLE_LIST_RECIPE, mContentValues, "_ID=" + mIdItem, null);
            showListRecipe();
        }
    }

    public void showDialog(int idDialog, String nameForAction) {
        Bundle bundle = new Bundle();
        bundle.putInt(ID_DIALOG, idDialog);
        bundle.putString(NAME_FOR_ACTION, nameForAction);
        FragmentTransaction ft = mFrManager.beginTransaction();
        Fragment fragment = mFrManager.findFragmentByTag(TAG_DIALOG);
        if (fragment != null) {
            ft.remove(fragment);
        }
        ft.addToBackStack(null);

        DialogFragment dialogFragment = new FragDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(mFrManager, TAG_DIALOG);
    }

    public void onDialogClick(int idDialog, String param, int typeFolder, int idCategory) {
        switch (idDialog) {
            case DIALOG_REN_RECIPE_LISTRECIPE:
                renameRecipe(param);
                break;
            case DIALOG_DEL_RECIPE_LISTRECIPE:
                deleteRecipe();
                break;
            case DIALOG_MOV_RECIPE_LISTRECIPE:
                if (idCategory != NOP) moveRecipe(typeFolder, idCategory);
                else makeSnackbar(mContext
                        .getString(R.string.folder_folder_not_select));
                break;
        }
    }

    /**
     * Just change value in columns "category_id" & "sub_category_id" in TABLE_LIST_RECIPE
     */
    private void moveRecipe(int typeFolder, int idCategory) {
        mContentValues = new ContentValues();
        if (typeFolder == PARENT) {
            mContentValues.put("category_id", idCategory);
            mContentValues.put("sub_category_id", DEFAULT_VALUE_COLUMN);
        } else if (typeFolder == CHILD) {
            mContentValues.put("category_id", DEFAULT_VALUE_COLUMN);
            mContentValues.put("sub_category_id", idCategory);
        }
        long rowId = mDatabase.update(TABLE_LIST_RECIPE, mContentValues, "_ID=" + mIdItem, null);
        showListRecipe();
        Log.d("TG", "Frag List Recipe : moveRecipe ");

        if (rowId >= 0) makeSnackbar(mContext.getString(R.string.success));
    }

    private void deleteRecipe() {
        long rowId = mDatabase.delete(TABLE_LIST_RECIPE, "_ID=" + mIdItem, null);
        showListRecipe();
        if (rowId >= 0) makeSnackbar(mContext.getString(R.string.success));
    }

    private void renameRecipe(String param) {
        mContentValues = new ContentValues();
        mContentValues.put("recipe_title", param);
        long rowId = mDatabase.update(TABLE_LIST_RECIPE, mContentValues, "_ID=" + mIdItem, null);
        showListRecipe();
        if (rowId >= 0) makeSnackbar(mContext.getString(R.string.success));
    }

    private void makeSnackbar(String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
    }
}
