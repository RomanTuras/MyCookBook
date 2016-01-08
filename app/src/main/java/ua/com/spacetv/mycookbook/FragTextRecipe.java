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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by Roman Turas on 07/01/2016.
 */
public class FragTextRecipe extends Fragment implements StaticFields {

    private static Context context;
    private static FragmentManager fragmentManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
    public static DataBaseHelper dataBaseHelper;
    public static SQLiteDatabase database;
    private ContentValues contentValues;
    private static View view;
    private EditText editTitleRecipe, editTextRecipe;
    private TextView textTitleRecipe, textTextRecipe;
    private String titleRecipeFromDatabase = null;
    private String textRecipeFromDatabase = null;
    private static int idItem = 0; // id of folder where was or will TEXT of RECIPE
    private static int typeFolder = 0; // Is two types of folders: PARENT (top) & CHILD (subFolder)
    private static int startupMode = MODE_REVIEW_RECIPE;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragTextRecipe.context = context;
        FragTextRecipe.dataBaseHelper = new DataBaseHelper(context);
        this.contentValues = new ContentValues();

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            idItem = bundle.getInt(TAG_PARENT_ITEM_ID);
            typeFolder = bundle.getInt(TAG_TYPE_FOLDER);
            startupMode = bundle.getInt(TAG_MODE);
            Log.d("TG", "idItem= "+idItem+" typeFolder= "+typeFolder+" startupMode= "+startupMode);
        }
        onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_text_recipe, null);
        editTitleRecipe = (EditText) view.findViewById(R.id.editTitleRecipe);
        editTextRecipe = (EditText) view.findViewById(R.id.editTextRecipe);
        textTitleRecipe = (TextView) view.findViewById(R.id.textTitleRecipe);
        textTextRecipe = (TextView) view.findViewById(R.id.textTextRecipe);

        database = dataBaseHelper.getWritableDatabase();
        fragmentManager = getFragmentManager();
        FragTextRecipe.view = view;

        if(startupMode == MODE_EDIT_RECIPE) modeEdit();
        else if(startupMode == MODE_REVIEW_RECIPE) modeReview();
        else if(startupMode == MODE_NEW_RECIPE) modeNewRecipe();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        Log.d("TG", "onCreateOptionsMenu");
        menu.clear();
        if(startupMode == MODE_REVIEW_RECIPE) inflater.inflate(R.menu.menu_review_recipe, menu);
        else if(startupMode == MODE_EDIT_RECIPE |
                startupMode == MODE_NEW_RECIPE) inflater.inflate(R.menu.menu_edit_recipe, menu);
    }

    private void modeNewRecipe() {
        startupMode = MODE_NEW_RECIPE;
        editTitleRecipe.setFocusableInTouchMode(true);
        editTitleRecipe.setFocusable(true);
        editTitleRecipe.requestFocus();
        editTextRecipe.setFocusableInTouchMode(true);
        editTextRecipe.setFocusable(true);
        textTitleRecipe.setVisibility(View.VISIBLE);
        textTitleRecipe.setText(R.string.title_recipe_edit);
        textTextRecipe.setText(R.string.text_recipe_edit);
        setHasOptionsMenu(true);
    }

    private void modeEdit(){
        startupMode = MODE_EDIT_RECIPE;
        editTitleRecipe.setFocusableInTouchMode(true);
        editTitleRecipe.setFocusable(true);
        editTextRecipe.setFocusableInTouchMode(true);
        editTextRecipe.setFocusable(true);
        textTitleRecipe.setVisibility(View.VISIBLE);
        textTitleRecipe.setText(R.string.title_recipe_edit);
        textTextRecipe.setText(R.string.text_recipe_edit);
        readRecipeFromDatabase();
        setHasOptionsMenu(false);
        setHasOptionsMenu(true);
    }

    private void modeReview() {
        startupMode = MODE_REVIEW_RECIPE;
        editTitleRecipe.setFocusableInTouchMode(false);
        editTitleRecipe.setFocusable(false);
        editTextRecipe.setFocusableInTouchMode(false);
        editTextRecipe.setFocusable(false);
        textTitleRecipe.setVisibility(View.GONE);
        textTextRecipe.setText(R.string.text_recipe_review);
        readRecipeFromDatabase();
        setHasOptionsMenu(false);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save: isChangesFromRecipe();
                break;
            case R.id.action_edit: modeEdit();
                break;
            case R.id.action_share:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void readRecipeFromDatabase() {
        Cursor cursor = database.query(TABLE_LIST_RECIPE, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(0) == idItem) {
                        titleRecipeFromDatabase = cursor.getString(1);
                        editTitleRecipe.setText(titleRecipeFromDatabase);
                        textRecipeFromDatabase = cursor.getString(2);
                        editTextRecipe.setText(textRecipeFromDatabase);
                    }
                }while (cursor.moveToNext());
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
        MainActivity.hideAllFloatButtons();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(startupMode == MODE_NEW_RECIPE | startupMode == MODE_EDIT_RECIPE) {
            if (isChangesFromRecipe()) saveRecipe();
        }
    }

    /* If at least one from two editText contains any text -> save recipe in database*/
    private boolean isChangesFromRecipe(){
        if(editTitleRecipe.getText().length()>0 & editTextRecipe.getText().length()>0){
            String tempTitle = editTitleRecipe.getText().toString();
            String tempText = editTextRecipe.getText().toString();

            if(tempTitle.equals(titleRecipeFromDatabase) &
                    tempText.equals(textRecipeFromDatabase)){
                makeSnackbar(context.getResources().getString(R.string.there_is_no_change));
                return false;
            }
        }else if(editTitleRecipe.getText().length() == 0 & editTextRecipe.getText().length() == 0){
            Log.d("TG", "editText's == 0");
            makeSnackbar(context.getResources().getString(R.string.nothing_was_entered));
            return false;
        }
        return true;
    }

    private void saveRecipe() {
        contentValues = new ContentValues();
        String titleRecipe = getString(R.string.text_recipe_no_title);
        String textRecipe = getString(R.string.text_recipe_no_text);
        if(editTitleRecipe.getText().length()>0) titleRecipe = editTitleRecipe.getText().toString();
        if(editTextRecipe.getText().length()>0) textRecipe = editTextRecipe.getText().toString();

        contentValues.put("recipe_title" , titleRecipe);
        contentValues.put("recipe" , textRecipe);
        if(typeFolder == PARENT){
            contentValues.put("category_id" , idItem);
            contentValues.put("sub_category_id" , DEFAULT_VALUE_COLUMN);
        }else if(typeFolder == CHILD){
            contentValues.put("category_id" , DEFAULT_VALUE_COLUMN);
            contentValues.put("sub_category_id" , idItem);
        }
        long rowId = 0;
        if(startupMode == MODE_NEW_RECIPE){
            rowId = database.insert(TABLE_LIST_RECIPE, null, contentValues);
        }else if(startupMode == MODE_EDIT_RECIPE) {
            rowId = database.update(TABLE_LIST_RECIPE, contentValues, "_ID=" + idItem, null);
        }
        modeReview();
        if(rowId >= 0)makeSnackbar(context.getResources().getString(R.string.success_saved));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        database.close();
        dataBaseHelper.close();
    }

    private void makeSnackbar(String text){
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
