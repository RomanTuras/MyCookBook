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

    /* id of Fragments */
    byte ID_CATEGORY = 0;
    byte ID_SUBCATEGORY = 1;
    byte ID_LIST_RECIPE = 2;
    byte ID_TEXT_RECIPE = 3;

    /*id icons for Folder or Recipe and for Like-icon*/
    byte ID_IMG_FOLDER = 0;
    byte ID_IMG_RECIPE = 1;
    byte ID_IMG_LIKE = 2;
    byte ID_IMG_LIKE_OFF = 3;

    /*Data Base Helper*/

    String DB_FILE = "db_cook.db";
    String DB_FOLDER = "myCookBook";
}
