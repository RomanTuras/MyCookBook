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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.fragments.FragListRecipe;
import ua.com.spacetv.mycookbook.fragments.FragSubCategory;
import ua.com.spacetv.mycookbook.fragments.FragTextRecipe;
import ua.com.spacetv.mycookbook.fragments.FragTopCategory;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by Roman Turas on 16/02/2016.
 *
 */
public class FragmentHelper implements StaticFields{
    private FragmentManager fragmentManager;
    private Bundle bundle;
    private Fragment fragment;
    private String tag;

    public FragmentHelper(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    /** Attach FragTopCategory */
    public void attachTopCategoryFragment(){
        bundle = new Bundle();
        tag = FragTopCategory.class.getSimpleName();
        fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new FragTopCategory();
            Log.d("TG", "fragment == null" + fragment.toString());
        } else Log.d("TG", "fragment != null" + fragment.toString());
        doTransaction();
    }

    /** Attach FragSubCategory */
    public void attachSubCategoryFragment(int idItem) {
        bundle = new Bundle();
        tag = FragSubCategory.class.getSimpleName();
        fragment = fragmentManager.findFragmentByTag(tag);
        bundle.putInt(TAG_PARENT_ITEM_ID, idItem);
        if (fragment == null) {
            fragment = new FragSubCategory();
            fragment.setArguments(bundle);
        }
        Log.d("TG", "attachFragment, fragment = " + fragment.toString());
        doTransaction();
    }

    /** Attach FragListRecipe */
    public void attachListRecipeFragment(int idItem, int startMode, String searchRequest) {
        bundle = new Bundle();
        tag = FragListRecipe.class.getSimpleName();
        if (startMode == MODE_FAVORITE_RECIPE) tag += "Favorite";
        else if (startMode == MODE_SEARCH_RESULT) tag += "Search";
        fragment = fragmentManager.findFragmentByTag(tag);
        bundle.putInt(TAG_PARENT_ITEM_ID, idItem);
        bundle.putInt(TAG_MODE, startMode);
        bundle.putString(TAG_SEARCH_STRING, searchRequest);
        if (fragment == null) {
            fragment = new FragListRecipe();
            fragment.setArguments(bundle);
        }
        Log.d("TG", "attachFragment, fragment = " + tag);
        doTransaction();
    }

    /** Attach FragTextRecipe */
    public void attachTextRecipeFragment(int idItem, int startMode, int typeFolder) {
        bundle = new Bundle();
        tag = FragTextRecipe.class.getSimpleName();
        fragment = fragmentManager.findFragmentByTag(tag);
        if (typeFolder == PARENT) {
            bundle.putInt(TAG_PARENT_ITEM_ID, FragSubCategory.idParentItem);//get id TOP category
        } else if (typeFolder == CHILD) {
            bundle.putInt(TAG_PARENT_ITEM_ID, FragListRecipe.idParentItem);//get id SUB category
        }
        bundle.putInt(TAG_ID_RECIPE, idItem);
        bundle.putInt(TAG_MODE, startMode);
        bundle.putInt(TAG_TYPE_FOLDER, typeFolder);
        if (fragment == null) {
            fragment = new FragTextRecipe();
            fragment.setArguments(bundle);
        }
        Log.d("TG", "attachFragment, fragment = " + fragment.toString());
        doTransaction();
    }

    /** Confirm transaction with fragment */
    private void doTransaction() {
        fragmentManager.beginTransaction().replace(R.id.container, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }
}
