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

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StaticFields,
        OnFragmentEventsListener, View.OnClickListener {

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment fragment;
    private static FloatingActionButton fabTopCategory, fabListRecipe;
    private static FloatingActionMenu fabSubCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initFloatAction();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Log.d("TG", "main activity onCreate");

    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("TG", "main activity onResume");
        fragmentManager = getSupportFragmentManager();
        fragment = new FragTopCategory();
        if(!fragment.isAdded()) addFragment(TAG_CATEGORY);
    }

    private void initFloatAction() {
        FloatingActionButton fabRecipeSubCategory, fabFolderSubCategory;

        fabTopCategory = (FloatingActionButton) findViewById(R.id.fabTopCategory);
        fabSubCategory = (FloatingActionMenu) findViewById(R.id.fabMenuSubCategory);
        fabListRecipe = (FloatingActionButton) findViewById(R.id.fabListRecipe);
        fabTopCategory.setOnClickListener(this);
        fabListRecipe.setOnClickListener(this);
        fabRecipeSubCategory = (FloatingActionButton) findViewById(R.id.fabRecipeSubCategory);
        fabFolderSubCategory = (FloatingActionButton) findViewById(R.id.fabFolderSubCategory);

        showFloatButtonTopCategory();

        fabRecipeSubCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TG", "add recipe in top folder");
                new FragSubCategory().showDialog(DIALOG_ADD_RECIPE_SUBCATEGORY, "");
                fabSubCategory.close(true);
            }
        });
        fabFolderSubCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TG", "add folder in top folder");
                new FragSubCategory().showDialog(DIALOG_ADD_SUBCATEGORY, "");
                fabSubCategory.close(true);
            }
        });
    }

    public static void showFloatButtonTopCategory(){
        fabTopCategory.show(true);
        fabSubCategory.hideMenuButton(false);
        fabListRecipe.hide(false);
    }

    public static void showFloatMenuSubCategory(){
        fabTopCategory.hide(false);
        fabSubCategory.showMenuButton(true);
        fabListRecipe.hide(false);
    }

    public static void showFloatButtonListRecipe(){
        fabTopCategory.hide(false);
        fabSubCategory.hideMenuButton(false);
        fabListRecipe.show(true);
    }

    private void addFragment(String tag){
        fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment, tag);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListItemClick(int idTable, int idItem) {
        switch (idTable){
            case ID_TABLE_TOP_CATEGORY:
                startSubCategoryFragment(idItem);
                break;
            case ID_TABLE_SUB_CATEGORY:
                startListRecipeFragment(idItem);
                break;
            case ID_TABLE_LIST_RECIPE:
                startSingleRecipeFragment(idItem);
                break;
        }
    }

    private void startSingleRecipeFragment(int idItem) {
        Bundle bundle = new Bundle();
        fragment = new FragSingleRecipe();
        bundle.putInt(TAG_PARENT_ITEM_ID, idItem);
        fragment.setArguments(bundle);
        fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment)
                .addToBackStack(TAG_TEXT_RECIPE)
                .commit();
    }

    private void startListRecipeFragment(int idItem) {
        Bundle bundle = new Bundle();
        fragment = new FragListRecipe();
        bundle.putInt(TAG_PARENT_ITEM_ID, idItem);
        fragment.setArguments(bundle);
        fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment)
                .addToBackStack(TAG_LIST_RECIPE)
                .commit();
    }

    private void startSubCategoryFragment(int idItem) {
        Bundle bundle = new Bundle();
                fragment = new FragSubCategory();
                bundle.putInt(TAG_PARENT_ITEM_ID, idItem);
        fragment.setArguments(bundle);
        fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment)
                .addToBackStack(TAG_SUBCATEGORY)
                .commit();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.fabTopCategory){
            Fragment fragment;
            fragmentManager = getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(TAG_CATEGORY);
            if(fragment != null){
                new FragTopCategory().showDialog(DIALOG_ADD_CATEGORY, null);
            }
        }

    }
}
