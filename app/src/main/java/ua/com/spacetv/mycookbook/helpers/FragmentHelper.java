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

import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.fragments.FragListRecipe;
import ua.com.spacetv.mycookbook.fragments.FragSettings;
import ua.com.spacetv.mycookbook.fragments.FragSubCategory;
import ua.com.spacetv.mycookbook.fragments.FragTextRecipe;
import ua.com.spacetv.mycookbook.fragments.FragTopCategory;
import ua.com.spacetv.mycookbook.interfaces.Constants;

/**
 * Helper for fragments transaction, with arguments or without
 *
 * {@link ua.com.spacetv.mycookbook.MainActivity}
 */
public class FragmentHelper implements Constants {
    private FragmentManager mFrManager;

    public FragmentHelper(FragmentManager fragmentManager) {
        mFrManager = fragmentManager;
    }

    /**
     * Attaching FragTopCategory
     */
    public void attachTopCategoryFragment(){
        String tag = FragTopCategory.class.getSimpleName();
        Fragment fragment = mFrManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new FragTopCategory();
        }
        doTransaction(fragment, tag);
    }

    /**
     * Attaching FragSubCategory
     * @param idItem - id of parent category
     */
    public void attachSubCategoryFragment(int idItem) {
        Bundle bundle = new Bundle();
        String tag = FragSubCategory.class.getSimpleName();
        Fragment fragment = mFrManager.findFragmentByTag(tag);
        bundle.putInt(TAG_PARENT_ITEM_ID, idItem);
        if (fragment == null) {
            fragment = new FragSubCategory();
            fragment.setArguments(bundle);
        }
        doTransaction(fragment, tag);
    }

    /**
     * Attaching FragListRecipe
     * @param idItem - id of parent category
     * @param startMode - MODE_EDIT_RECIPE, MODE_REVIEW_RECIPE or MODE_NEW_RECIPE
     * @param searchRequest - search query
     */
    public void attachListRecipeFragment(int idItem, int startMode, String searchRequest) {
        Bundle bundle = new Bundle();
        String tag = FragListRecipe.class.getSimpleName();
        if (startMode == MODE_FAVORITE_RECIPE) tag += "Favorite";
        else if (startMode == MODE_SEARCH_RESULT) tag += "Search";
        Fragment fragment = mFrManager.findFragmentByTag(tag);
        bundle.putInt(TAG_PARENT_ITEM_ID, idItem);
        bundle.putInt(TAG_MODE, startMode);
        bundle.putString(TAG_SEARCH_STRING, searchRequest);
        if (fragment == null) {
            fragment = new FragListRecipe();
            fragment.setArguments(bundle);
        }
        doTransaction(fragment, tag);
    }

    /** Attach FragTextRecipe */
    public void attachTextRecipeFragment(int idItem, int startMode, int typeFolder) {
        Bundle bundle = new Bundle();
        String tag = FragTextRecipe.class.getSimpleName();
        Fragment fragment = mFrManager.findFragmentByTag(tag);
        if (typeFolder == PARENT) {
            bundle.putInt(TAG_PARENT_ITEM_ID, FragSubCategory.mIdParentItem);//get id TOP category
        } else if (typeFolder == CHILD) {
            bundle.putInt(TAG_PARENT_ITEM_ID, FragListRecipe.mIdParentItem);//get id SUB category
        }
        bundle.putInt(TAG_ID_RECIPE, idItem);
        bundle.putInt(TAG_MODE, startMode);
        bundle.putInt(TAG_TYPE_FOLDER, typeFolder);
        if (fragment == null) {
            fragment = new FragTextRecipe();
            fragment.setArguments(bundle);
        }
        doTransaction(fragment, tag);
    }

    /**
     * Attaching FragSettings
     */
    public void attachSettingsFragment(){
        String tag = FragSettings.class.getSimpleName();
        Fragment fragment = mFrManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new FragSettings();
        }
        doTransaction(fragment, tag);
    }

    /**
     * Confirm transaction with mFragment
     * @param fragment
     * @param tag
     */
    private void doTransaction(Fragment fragment, String tag) {
        mFrManager.beginTransaction().replace(R.id.container, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }
}
