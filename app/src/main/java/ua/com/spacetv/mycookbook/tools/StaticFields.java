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

package ua.com.spacetv.mycookbook.tools;

/**
 * Created by salden on 02/01/2016.
 */
public interface StaticFields {

    /* Tags for fragments */
    String TAG_ABOUT = "ABOUT";
    String TAG_CATEGORY = "ID_CATEGORY";
    String TAG_SUBCATEGORY = "ID_SUBCATEGORY";
    String TAG_LIST_RECIPE = "ID_LIST_RECIPE";
    String TAG_TEXT_RECIPE = "ID_TEXT_RECIPE";
    String TAG_SEARCH = "SEARCH";
    String TAG_DIALOG = "TAG_DIALOG";


    /* id of Dialogs */
    String ID_DIALOG = "ID_DIALOG";
    String NAME_FOR_ACTION = "NAME_FOR_ACTION";
    byte DIALOG_ADD_CATEGORY = 0;
    byte DIALOG_REN_CATEGORY = 1;
    byte DIALOG_DEL_CATEGORY = 2;
    byte DIALOG_ADD_SUBCATEGORY = 3;
    byte DIALOG_REN_SUBCATEGORY = 4;
    byte DIALOG_DEL_SUBCATEGORY = 5;
    byte DIALOG_ADD_RECIPE_SUBCATEGORY = 6;
    byte DIALOG_REN_RECIPE_SUBCATEGORY = 7;
    byte DIALOG_DEL_RECIPE_SUBCATEGORY = 8;
    byte DIALOG_MOV_RECIPE_SUBCATEGORY = 9;
    byte DIALOG_REN_RECIPE_LISTRECIPE = 11;
    byte DIALOG_DEL_RECIPE_LISTRECIPE = 12;
    byte DIALOG_MOV_RECIPE_LISTRECIPE = 13;
    int NOP = -1;

    /* id of actions (from where do it) */
    int ID_ACTION_TOP_CATEGORY = 0;
    int ID_ACTION_SUB_CATEGORY_CATEGORY = 1;
    int ID_ACTION_SUB_CATEGORY_RECIPE = 2;
    int ID_ACTION_LIST_RECIPE = 5;

    /* id of pressed menu popup item */
    int ID_POPUP_ITEM_CANCEL = -1;
    int ID_POPUP_ITEM_REN = 0;
    int ID_POPUP_ITEM_DEL = 1;
    int ID_POPUP_ITEM_MOV = 2;
    int ID_POPUP_ITEM_FAV = 3;

    /* Types of Main menu */
    int MENU_MAIN = 0;
    int MENU_EDIT_RECIPE = 1;
    int MENU_REVIEW_RECIPE = 2;

    /* Names of a Tables*/
    String TABLE_TOP_CATEGORY = "tableMain";
    String TABLE_SUB_CATEGORY = "tableSubCat";
    String TABLE_LIST_RECIPE = "tableRecipe";

    /* id of parent item (was clicked in list and will passed to next fragment) */
    String TAG_PARENT_ITEM_ID = "TAG_PARENT_ITEM_ID";
    String TAG_TYPE_FOLDER = "TYPE_FOLDER";
    String TAG_MODE = "TAG_MODE";

    /* Switch start mode in FragTextRecipe */
    int MODE_EDIT_RECIPE = 0;
    int MODE_REVIEW_RECIPE = 1;
    int MODE_NEW_RECIPE = 2;

    /* key of Type Folder */
    int PARENT = 0;
    int CHILD = 1;

    /* Type of item in list */
    boolean IS_FOLDER = true;
    boolean IS_RECIPE = false;

    /*id icons for Folder or Recipe and for Like-icon*/
    byte ID_IMG_FOLDER = 0;
    byte ID_IMG_RECIPE = 1;
    byte ID_IMG_LIKE = 1;
    byte ID_IMG_LIKE_OFF = 0;

    /*Data Base Helper*/

    String DB_FILE = "db_cook.db";
    String DB_FOLDER = "myCookBook";

    /* Default value of columns 'category_id' and 'sub_category_id'*/
    int DEFAULT_VALUE_COLUMN = -1;
}
