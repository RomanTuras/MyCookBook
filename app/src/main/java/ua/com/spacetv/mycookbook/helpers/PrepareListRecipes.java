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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.Constants;

/**
 * Created by Roman Turas on 09/01/2016.
 * Only for the CHILD FOLDERS!!!
 */
public class PrepareListRecipes implements Constants {
    private static ArrayList<ListData> adapter;
    private SQLiteDatabase database;
    private DataBaseHelper dataBaseHelper;
    private final static int KEY_FAVORITE = 1;
    private final static byte UPPER = 1;
    private final static byte LOWER = 0;
    private final static byte TITLE = 0;
    private final static byte TEXT = 1;

    /**
     * To prepare a list of recipes that are in 'idParentFolder'
     */
    public PrepareListRecipes(Context context, int idParentFolder) {
        dataBaseHelper = new DataBaseHelper(context);
        database = dataBaseHelper.getWritableDatabase();
        adapter = new ArrayList<>();
        recipeToAdapter(idParentFolder);
    }

    /**
     * To prepare a list of favorite recipes
     */
    public PrepareListRecipes(Context context) {
        dataBaseHelper = new DataBaseHelper(context);
        database = dataBaseHelper.getWritableDatabase();
        adapter = new ArrayList<>();
        recipeToAdapter();
    }

    /** To prepare a list of recipes depend of search request */
    public PrepareListRecipes(Context context, String searchRequest) {
        dataBaseHelper = new DataBaseHelper(context);
        database = dataBaseHelper.getWritableDatabase();
        adapter = new ArrayList<>();
        String[] query = new String[2];

        searchRequest = startConversion(searchRequest, UPPER);
        query[0] = "'%" + searchRequest + "%'";

        searchRequest = startConversion(searchRequest, LOWER);
        query[1] = "'%" + searchRequest + "%'";

        recipeToAdapter(query);
    }

    /**
     * Method. To prepare a list of recipes depend of search request
     */
    private void recipeToAdapter(String[] query) {
        String selectQuery = "SELECT * FROM " + TABLE_LIST_RECIPE +
                " WHERE " + "recipe_title LIKE " + query[0] + " OR " +
                "recipe_title LIKE " + query[1] + " OR " +
                "recipe LIKE " + query[0] + " OR " +
                "recipe LIKE " + query[1] + " ORDER BY recipe_title";
        Cursor cursor = database.rawQuery(selectQuery, null);

        readDatabaseInToAdapter(cursor);
        cursor.close();
    }

    /**
     * Method. To prepare a list of favorite recipes
     */
    private void recipeToAdapter() {
        String selectQuery = "SELECT * FROM " + TABLE_LIST_RECIPE +
                " WHERE make=" + KEY_FAVORITE + " ORDER BY recipe_title";
        readDatabaseInToAdapter(database.rawQuery(selectQuery, null));
    }

    /**
     * Method. To prepare a list of recipes that are in 'idParentFolder'
     */
    private void recipeToAdapter(int idParentFolder) {
        String selectQuery = "SELECT * FROM " + TABLE_LIST_RECIPE +
                " WHERE sub_category_id=" + idParentFolder + " ORDER BY recipe_title";
        readDatabaseInToAdapter(database.rawQuery(selectQuery, null));
    }

    private void readDatabaseInToAdapter(Cursor cursor){
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    int imgLike = cursor.getInt(4);
                    int topFolder_id = cursor.getInt(3);
                    int subFolder_id = cursor.getInt(5);

                    adapter.add(new ListData(cursor.getString(1), "", ID_IMG_RECIPE, imgLike, 0,
                            item_id, IS_RECIPE, topFolder_id, subFolder_id));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        dataBaseHelper.close();
        database.close();
    }

    public ArrayList<ListData> getFilledAdapter() {
        return adapter;
    }

    @NonNull
    private String startConversion(String s, byte style) {
        StringBuilder sb = new StringBuilder(s.length());
        CharacterIterator chit = new StringCharacterIterator(s);
        char ch = chit.current(), prev = ' ';
        while (ch != CharacterIterator.DONE) {
            if (Character.isWhitespace(prev) && Character.isLetter(ch)) {
                if(style == UPPER) sb.append(Character.toUpperCase(ch));
                else if(style == LOWER) sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
            prev = ch;
            ch = chit.next();
        }
        return sb.toString();
    }
}
