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
import ua.com.spacetv.mycookbook.google_services.Analytics;
import ua.com.spacetv.mycookbook.helpers.DbHelper;
import ua.com.spacetv.mycookbook.interfaces.Constants;
import ua.com.spacetv.mycookbook.interfaces.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.ListAdapter;
import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.Preferences;

/**
 * Class is responsible for list of top category
 */
public class FragTopCategory extends Fragment implements Constants,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

//    private static DataBaseHelper mDataBaseHelper;
    private static DbHelper mDbHelper;
    private static SQLiteDatabase mDatabase;
    private static Context mContext;
    private static FragmentManager mFrManager;
    private static ListView mListView;
    private static ArrayList<ListData> mAdapter;
    private ArrayList<Integer> mArrayIdSubCategories;
    private static View mViewForSnackbar;
    private static TextView mTextView; //shoving when category is empty
    private static OnFragmentEventsListener onFragmentEventsListener;
    private ContentValues mContentValues;
    private static String mNameForAction;
    public static int mIdParentCategory;
    public static String mNameOfTopCategory = null;
    private static int mFirstVisibleItem = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContentValues = new ContentValues();
        FragTopCategory.mContext = context;
        onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
        setRetainInstance(false);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_top_category, null);
        mListView = (ListView) view.findViewById(R.id.listTopCategory);
        mTextView = (TextView) view.findViewById(R.id.text_empty_text_topcategory);

        mFrManager = getFragmentManager();
//        mDataBaseHelper = new DataBaseHelper(mContext);
//        mDatabase = mDataBaseHelper.getWritableDatabase();
        mDbHelper = MainActivity.mDbHelper;
        mDatabase = mDbHelper.getWritableDatabase();

        mViewForSnackbar = view;
        return view;
    }

    public void showAllCategory() {
        mAdapter = new ArrayList<>();

//        if (mDatabase == null | mDataBaseHelper == null) {
//            mDataBaseHelper = new DataBaseHelper(mContext);
//            mDatabase = mDataBaseHelper.getWritableDatabase();
//        }
        if(mDatabase == null) mDatabase = mDbHelper.getWritableDatabase();

        categoryInList();
        if (mAdapter.size() == 0) mTextView
                .setText(R.string.text_add_folder_top_category);
        else mTextView.setText(null);
        ListAdapter listAdapter = new ListAdapter(mContext, mAdapter);
        mListView.setAdapter(listAdapter);
        registerForContextMenu(mListView);
        mListView.setSelection(mFirstVisibleItem); //mechanism save and restore state of list view
        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);
    }

    private void categoryInList() {
        Cursor cursor = mDatabase.query(TABLE_TOP_CATEGORY, null, null, null, null, null,
                "category", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    String subCategories = getSubcategories(item_id);
                    int numberRecipe = countRecipe(item_id);
                    mAdapter.add(new ListData(cursor.getString(1), subCategories, ID_IMG_FOLDER,
                            ID_IMG_LIKE_OFF, numberRecipe, item_id, IS_FOLDER, 0, 0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private int countRecipe(int item_id) {
        int numberRecipe = 0;
        Cursor cursor = mDatabase.query(TABLE_LIST_RECIPE, null, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(3) == item_id) { //column 'category_id'
                        numberRecipe++; // count recipes in Top Category
                    } else {
                        for (Integer i : mArrayIdSubCategories) { // count recipes in Sub Categories
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
        mArrayIdSubCategories = new ArrayList<>(); // id all subcategories in parent category
        String subCategories = "";
        Cursor cursor = mDatabase.query(TABLE_SUB_CATEGORY, null, null, null, null, null,
                "name", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(3) == item_id) { //column 'parent_id'
                        subCategories += cursor.getString(1); //column 'name'
                        subCategories += ", ";
                        mArrayIdSubCategories.add(cursor.getInt(0)); //column '_id'
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
    public void onPause() {
        super.onPause();
        mFirstVisibleItem = mListView.getFirstVisiblePosition();
        Preferences.setSettingsToPreferences(mContext, FIRST_ITEM_TOP_CATEGORY, mFirstVisibleItem);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!mDatabase.isOpen()) mDatabase = mDbHelper.getWritableDatabase();
        mFirstVisibleItem = Preferences
                .getSettingsFromPreferences(mContext, FIRST_ITEM_TOP_CATEGORY);
        showAllCategory();
        MainActivity.showFloatButtonTopCategory();
        MainActivity.overrideActionBar(null, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mDataBaseHelper.close();
        mDatabase.close();
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
        mIdParentCategory = mAdapter.get(position).getItemId();
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
            showDialog(DIALOG_REN_CATEGORY, mNameForAction);
        } else if (item.getItemId() == ID_POPUP_ITEM_DEL) {
            if (isCategoryEmpty()) {
                showDialog(DIALOG_DEL_CATEGORY, mNameForAction);
            } else makeSnackbar(mContext.getString(R.string.folder_not_empty));
        } else {
            return false;
        }
        return false;
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
        new Analytics(mContext).sendAnalytics("myCookBook", "Top Category", "Add top category", param);
//        mDataBaseHelper = new DataBaseHelper(mContext);
//        mDatabase = mDataBaseHelper.getWritableDatabase();
        mDatabase = mDbHelper.getWritableDatabase();

        mContentValues = new ContentValues();
        mContentValues.put("category", param);
        long rowId = mDatabase.insert(TABLE_TOP_CATEGORY, null, mContentValues);
        showAllCategory();
        if (rowId >= 0) makeSnackbar(mContext.getString(R.string.success));
    }

    private void renameCategory(String param) {
//        mDataBaseHelper = new DataBaseHelper(mContext);
//        mDatabase = mDataBaseHelper.getWritableDatabase();
        mDatabase = mDbHelper.getWritableDatabase();

        mContentValues = new ContentValues();
        mContentValues.put("category", param);
        long rowId = mDatabase.update(TABLE_TOP_CATEGORY, mContentValues, "_ID=" + mIdParentCategory, null);
        showAllCategory();
        if (rowId >= 0) makeSnackbar(mContext.getString(R.string.success));
    }

    private void deleteCategory() {
//        mDataBaseHelper = new DataBaseHelper(mContext);
//        mDatabase = mDataBaseHelper.getWritableDatabase();
        mDatabase = mDbHelper.getWritableDatabase();

        long rowId = mDatabase.delete(TABLE_TOP_CATEGORY, "_ID=" + mIdParentCategory, null);
        showAllCategory();
        if (rowId >= 0) makeSnackbar(mContext.getString(R.string.success));
    }

    /**
     * Check in two tables (TABLE_SUB_CATEGORY and TABLE_LIST_RECIPE),
     * if at least one object is in parent category -> return false, else -> true
     */
    private boolean isCategoryEmpty() {
        Cursor cursor = mDatabase.query(TABLE_SUB_CATEGORY, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(3) == mIdParentCategory) { //column 'parent_id'
                        return false;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        cursor = mDatabase.query(TABLE_LIST_RECIPE, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(3) == mIdParentCategory) { //column 'category_id'
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
        ListData ld = mAdapter.get(position);
        mNameOfTopCategory = ld.getListTitle();
        onFragmentEventsListener.onListItemClick(ID_ACTION_TOP_CATEGORY, ld.getItemId());
    }

    private void makeSnackbar(String text) {
        Snackbar.make(mViewForSnackbar, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

}
