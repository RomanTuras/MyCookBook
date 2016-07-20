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

package ua.com.spacetv.mycookbook.interfaces;

/**
 * Created by Roman Turas on 02/01/2016
 * used constants
 */
public interface Constants {

    /* Tags for fragments */
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

    //Preferences
    String SHARED_PREFERENCES = "SHARED_PREFERENCES";
    String IS_PURCHASE_OWNED = "IS_PURCHASE_OWNED";
    String COLOR_THEME = "COLOR_THEME";
    String IS_BACKGROUND_WHITE = "IS_BACKGROUND_WHITE";
    String FIRST_ITEM_TOP_CATEGORY = "FIRST_ITEM_TOP_CATEGORY";
    String FIRST_ITEM_SUB_CATEGORY = "FIRST_ITEM_SUB_CATEGORY";
    String FIRST_ITEM_LIST_RESIPE = "FIRST_ITEM_LIST_RESIPE";

    //Color themes
    int INDIGO = 0;
    int PINK = 1;
    int PURPLE = 2;
    int DEEP_PURPLE = 3;
    int RED = 4;
    int BLUE = 5;
    int LIGHT_BLUE = 6;
    int CYAN = 7;
    int TEAL = 8;
    int GREEN = 9;
    int LIGHT_GREEN = 10;
    int LIME = 11;
    int YELLOW = 12;
    int AMBER = 13;
    int ORANGE = 14;
    int DEEP_ORANGE = 15;
    int BROWN = 16;
    int GREY = 17;
    int BLUE_GREY = 18;
    int BLACK_WHITE = 19;


    /* id of actions (from where do it) */
    int ID_ACTION_TOP_CATEGORY = 0;
    int ID_ACTION_SUB_CATEGORY_CATEGORY = 1;
    int ID_ACTION_SUB_CATEGORY_RECIPE = 2;
    int ID_ACTION_LIST_RECIPE = 5;

    /* id of pressed menu popup item */
    int ID_POPUP_ITEM_REN = 0;
    int ID_POPUP_ITEM_DEL = 1;
    int ID_POPUP_ITEM_MOV = 2;
    int ID_POPUP_ITEM_FAV = 3;

    /* Save Restore Dialog */
    int DIALOG_FILE_SAVE = 0;
    int DIALOG_FILE_RESTORE = 1;

    /* Path and Filenames*/
    String FILENAME_WORKING_DB = "db_cook.db";
    String BACKUP_FILENAME = "cook.db";
    String FOLDER_NAME = "myCookBook";

    /* Names of a Tables*/
    String TABLE_TOP_CATEGORY = "tableMain";
    String TABLE_SUB_CATEGORY = "tableSubCat";
    String TABLE_LIST_RECIPE = "tableRecipe";

    /* id of parent item (was clicked in list and will passed to next fragment) */
    String TAG_PARENT_ITEM_ID = "TAG_PARENT_ITEM_ID";
    String TAG_TYPE_FOLDER = "TYPE_FOLDER";
    String TAG_MODE = "TAG_MODE";
    String TAG_SEARCH_STRING = "";
    String TAG_ID_RECIPE = "TAG_ID_RECIPE";

    /* Switch start mode to FragTextRecipe */
    int MODE_EDIT_RECIPE = 0;
    int MODE_REVIEW_RECIPE = 1;
    int MODE_NEW_RECIPE = 2;

    /* Switch start mode to FragListRecipe */
    int MODE_RECIPE_FROM_CATEGORY = 0;
    int MODE_SEARCH_RESULT = 1;
    int MODE_FAVORITE_RECIPE = 2;

    /* key of Type Folder */
    int PARENT = 0;
    int CHILD = 1;

    /* Type of item in list */
    boolean IS_FOLDER = true;
    boolean IS_RECIPE = false;

    /*id icons for Folder or Recipe and for Like-icon*/
    byte ID_IMG_FOLDER = 0;
    byte ID_IMG_RECIPE = 1;
    byte ID_IMG_LIKE_OFF = 0;

    /* Default value of columns 'category_id' and 'sub_category_id'*/
    int DEFAULT_VALUE_COLUMN = -1;

    /* Google services */
    String TRACK_ID ="UA-64362552-1";
    String AD_UNIT_ID = "ca-app-pub-7481052517653982/5697171795";

    boolean isDebugModeOn = false; // change to 'false' before production !!

    String TEST_DEVICE = "77A1F05FE188A3F51A0FF103708EDEF9";
}
