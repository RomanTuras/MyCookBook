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

import ua.com.spacetv.mycookbook.MainActivity;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.dialogs.FragDialog;
import ua.com.spacetv.mycookbook.google_services.Ads;
import ua.com.spacetv.mycookbook.google_services.Analytics;
import ua.com.spacetv.mycookbook.helpers.DbHelper;
import ua.com.spacetv.mycookbook.interfaces.Constants;
import ua.com.spacetv.mycookbook.interfaces.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.ListAdapter;
import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.Preferences;

/**
 * Created by salden on 02/01/2016.
 *
 */
public class FragSubCategory extends Fragment implements Constants,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private static Context mContext;
    private static FragmentManager mFrManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
    public static DbHelper mDbHelper;
    public static SQLiteDatabase mDatabase;
    private static ListView mListView;
    private static View mViewForSnackbar;
    public static ArrayList<ListData> mAdapter;
    private ContentValues mContentValues;
    private static TextView mTextView; //shoving when subcategory is empty
    private static String mNameForAction;
    public static String mNameOfSubCategory;
    public static int mIdItem; //id sub category
    private static int mFav;
    public static int mIdParentItem = 0; //id TOP category, income in params
    private static boolean mIsFolder;
    private static int mFirstVisibleItem = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragSubCategory.mContext = context;
        mDbHelper = MainActivity.mDbHelper;
        mContentValues = new ContentValues();
        setRetainInstance(true);

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            FragSubCategory.mIdParentItem = bundle.getInt(TAG_PARENT_ITEM_ID);
        }
        Log.d("TG", "TAG_PARENT_ITEM_ID = " + mIdParentItem);

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
        mListView = (ListView) view.findViewById(R.id.listSubCategory);
        mTextView = (TextView) view.findViewById(R.id.text_empty_text_subcategory);
//        mDatabase = mDataBaseHelper.getWritableDatabase();
//        mDatabase = mDbHelper.getWritableDatabase();
        mDatabase = MainActivity.mDatabase;

        mFrManager = getFragmentManager();

        mViewForSnackbar = view;
        return view;
    }

    public void showCategoryAndRecipe() {
        mAdapter = new ArrayList<>();
        subCategoryInList();
        recipeInList();
        if(mAdapter.size() == 0) mTextView.setText(R.string.text_add_folder_or_recipe);
        else mTextView.setText(null);
        ListAdapter listAdapter = new ListAdapter(mContext, mAdapter);
        mListView.setAdapter(listAdapter);
        registerForContextMenu(mListView);
        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setSelection(mFirstVisibleItem); //mechanism save and restore state of list view
        mListView.requestFocus();
    }

    private void recipeInList() {
        String selectQuery ="SELECT * FROM " + TABLE_LIST_RECIPE +
                " WHERE category_id=" + mIdParentItem + " ORDER BY recipe_title";
        Cursor cursor = mDatabase.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    int imgLike = cursor.getInt(4);
                    int topFolder_id = cursor.getInt(3);
                    int subFolder_id = cursor.getInt(5);
                    String path = cursor.getString(6);

                    mAdapter.add(new ListData(cursor.getString(1),
                            "", ID_IMG_RECIPE,
                            imgLike, 0, item_id, IS_RECIPE, topFolder_id, subFolder_id, path));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("TG", "Table with recipeCategory - is Empty");
        }
    }

    public void subCategoryInList() {
        String selectQuery ="SELECT * FROM " + TABLE_SUB_CATEGORY +
                " WHERE parent_id=" + mIdParentItem + " ORDER BY name";
        Cursor cursor = mDatabase.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    int numberRecipe = countRecipe(item_id);

                    mAdapter.add(new ListData(cursor.getString(1), "", ID_IMG_FOLDER, ID_IMG_LIKE_OFF,
                            numberRecipe, item_id, IS_FOLDER, 0, 0, null));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("TG", "Table with SubCategories - is Empty");
        }
    }

    private int countRecipe(int item_id) {
        int numberRecipe = 0;
        Cursor cursor = mDatabase.query(TABLE_LIST_RECIPE, null, null, null, null, null, null, null);
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
    public void onPause() {
        super.onPause();
        mFirstVisibleItem = mListView.getFirstVisiblePosition();
        Preferences.setSettingsToPreferences(mContext, FIRST_ITEM_SUB_CATEGORY, mFirstVisibleItem);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
//        if(!mDatabase.isOpen()) mDatabase = mDbHelper.getWritableDatabase();
        mFirstVisibleItem = Preferences
                .getSettingsFromPreferences(mContext, FIRST_ITEM_SUB_CATEGORY);
        showCategoryAndRecipe();
        MainActivity.showFloatMenuSubCategory();
        if(FragTopCategory.mNameOfTopCategory != null){
            MainActivity.overrideActionBar(null, FragTopCategory.mNameOfTopCategory);
        }else MainActivity.overrideActionBar(null, null);

        MainActivity.mAds = new Ads(mContext);
        if (MainActivity.mAds.getInterstitialAd() == null) MainActivity.mAds.initAds();
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mDatabase.close();
//        mDataBaseHelper.close();
    }

    /** onLongClick() - This returns a boolean to indicate whether you have consumed the event and
     * it should not be carried further. That is, return true to indicate that you have handled
     * the event and it should stop here; return false if you have not handled it and/or the event
     * should continue to any other on-click listeners.*/
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mNameForAction = mAdapter.get(position).getListTitle();
        mIdItem = mAdapter.get(position).getItemId();
        mIsFolder = mAdapter.get(position).getTypeItem();
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if(mIsFolder) {
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
                if(mIsFolder) showDialog(DIALOG_REN_SUBCATEGORY, mNameForAction);
                else showDialog(DIALOG_REN_RECIPE_SUBCATEGORY, mNameForAction);
                break;
            case ID_POPUP_ITEM_DEL:
                if(mIsFolder){
                    if(isSubCategoryEmpty()) showDialog(DIALOG_DEL_SUBCATEGORY, mNameForAction);
                    else makeSnackbar(mContext.getString(R.string.folder_not_empty));
                }
                else showDialog(DIALOG_DEL_RECIPE_SUBCATEGORY, mNameForAction);
                break;
            case ID_POPUP_ITEM_MOV:
                showDialog(DIALOG_MOV_RECIPE_SUBCATEGORY, mNameForAction);
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
        mIsFolder = mAdapter.get(position).getTypeItem();
        mIdItem = ld.getItemId(); // get id of pressed item: Folder or Recipe
        mNameOfSubCategory = ld.getListTitle();
        if(mIsFolder) {
            onFragmentEventsListener.onListItemClick(ID_ACTION_SUB_CATEGORY_CATEGORY, mIdItem);
        }
        else onFragmentEventsListener.onListItemClick(ID_ACTION_SUB_CATEGORY_RECIPE, mIdItem);
    }

    private void setUnsetFav() {
        Cursor cursor = mDatabase.query(TABLE_LIST_RECIPE, null, null, null, null,
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(0) == mIdItem) {
                        mFav = cursor.getInt(4);
                        mFav = mFav == 0 ? 1 : 0; // if recipe was 'like' unlike him
                    }
                }while (cursor.moveToNext());
            }
            mContentValues = new ContentValues();
            mContentValues.put("make", mFav);
            long rowId = mDatabase.update(TABLE_LIST_RECIPE, mContentValues, "_ID=" + mIdItem, null);
            showCategoryAndRecipe();
        }
    }

    public static void showDialog(int idDialog, String nameForAction) {
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
                else makeSnackbar(mContext
                        .getString(R.string.folder_folder_not_select));
                break;
        }
    }

    /** Just change value in columns "category_id" & "sub_category_id" in TABLE_LIST_RECIPE */
    private void moveRecipe(int typeFolder, int idCategory) {
        mContentValues = new ContentValues();
        if(typeFolder == PARENT){
            mContentValues.put("category_id", idCategory);
            mContentValues.put("sub_category_id", DEFAULT_VALUE_COLUMN);
        }else if(typeFolder == CHILD){
            mContentValues.put("category_id", DEFAULT_VALUE_COLUMN);
            mContentValues.put("sub_category_id", idCategory);
        }
        long rowId = mDatabase.update(TABLE_LIST_RECIPE, mContentValues, "_ID="+ mIdItem, null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(mContext.getString(R.string.success));
    }

    private void deleteRecipe() {
        long rowId = mDatabase.delete(TABLE_LIST_RECIPE, "_ID=" + mIdItem, null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(mContext.getString(R.string.success));
    }

    private void renameRecipe(String param) {
        mContentValues = new ContentValues();
        mContentValues.put("recipe_title", param);
        long rowId = mDatabase.update(TABLE_LIST_RECIPE, mContentValues, "_ID=" + mIdItem, null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(mContext.getString(R.string.success));
    }

    private void renameSubCategory(String param) {
        mContentValues = new ContentValues();
        mContentValues.put("name", param);
        long rowId = mDatabase.update(TABLE_SUB_CATEGORY, mContentValues, "_ID=" + mIdItem, null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(mContext.getString(R.string.success));
    }

    /** Add subcategory in to parent category */
    private void addSubCategory(String param) {
        new Analytics(mContext).sendAnalytics("myCookBook","Sub Category","Add sub category", param);

        mContentValues = new ContentValues();
        mContentValues.put("name", param);
        mContentValues.put("parent_id", mIdParentItem);
        long rowId = mDatabase.insert(TABLE_SUB_CATEGORY, null, mContentValues);
        showCategoryAndRecipe();
        if(rowId >= 0) makeSnackbar(mContext.getString(R.string.success));
    }

    /** Search in table 'TABLE_LIST_RECIPE' recipe with 'sub_category_id = pressed id' */
    private boolean isSubCategoryEmpty() {
        Cursor cursor = mDatabase.query(TABLE_LIST_RECIPE, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(5) == mIdItem) { //column 'sub_category_id'
                        return false;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return true;
    }

    private void deleteSubCategory() {
        long rowId = mDatabase.delete(TABLE_SUB_CATEGORY, "_ID=" + mIdItem, null);
        showCategoryAndRecipe();
        if(rowId >= 0)makeSnackbar(mContext.getString(R.string.success));
    }

    private void makeSnackbar(String text){
        Snackbar.make(mViewForSnackbar, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

}
