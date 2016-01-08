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
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by Roman Turas on 07/01/2016.
 */
public class FragSingleRecipe extends Fragment implements StaticFields {

    private static Context context;
    private static FragmentManager fragmentManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
    public static DataBaseHelper dataBaseHelper;
    public static SQLiteDatabase database;
    private ContentValues contentValues;
    private static View view;
    private EditText editTitleRecipe, editTextRecipe;
    private static int idItem = 0;
    private static int typeFolder = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragSingleRecipe.context = context;
        FragSingleRecipe.dataBaseHelper = new DataBaseHelper(context);
        this.contentValues = new ContentValues();

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            FragSingleRecipe.idItem = bundle.getInt(TAG_PARENT_ITEM_ID);
            FragSingleRecipe.typeFolder = bundle.getInt(TAG_TYPE_FOLDER);
        }
        Log.d("TG", "Frag Single Recipe : TAG_ITEM_ID = "+ idItem);

        onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_single_recipe, null);
        editTitleRecipe = (EditText) view.findViewById(R.id.editTitleRecipe);
        editTextRecipe = (EditText) view.findViewById(R.id.editTextRecipe);
        database = dataBaseHelper.getWritableDatabase();
        fragmentManager = getFragmentManager();
        FragSingleRecipe.view = view;
        return view;
    }

    public void showTextRecipe() {
        recipeInList();
    }

    private void recipeInList() {
//        String selectQuery ="SELECT * FROM " + TABLE_LIST_RECIPE +
//                " WHERE _ID=" + idItem + " ORDER BY recipe_title";
//        Cursor cursor = database.rawQuery(selectQuery, null);

        Cursor cursor = database.query(TABLE_LIST_RECIPE, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(0) == idItem) {
                        editTitleRecipe.setText(cursor.getString(1));
                        editTextRecipe.setText(cursor.getString(2));
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
        showTextRecipe();
        MainActivity.showFloatButtonListRecipe();
    }

    @Override
    public void onPause(){
        super.onPause();
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
