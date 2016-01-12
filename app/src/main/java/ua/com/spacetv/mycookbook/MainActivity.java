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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ua.com.spacetv.mycookbook.helpers.SaveRestoreDialog;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StaticFields,
        OnFragmentEventsListener, View.OnClickListener {

    private static Context context;
    private static FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private static Fragment fragTopCategory, fragSubCategory, fragListRecipe, fragTextRecipe;
    private static FloatingActionButton fabAddTopCategory, fabAddRecipeListRecipe,
            fabAddRecipeSubCategory, fabAddFolderSubCategory;
    private static FloatingActionMenu fabSubCategory;
    private static android.support.v7.app.ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        context = getBaseContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        initFloatAction();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        setTopCategoryFragment();
        Log.d("TG", "main activity onCreate");

    }

    public static void overrideActionBar(String title, String subtitle) {
        if (actionBar != null) {
            if(title == null) actionBar.setTitle(R.string.app_name);
            else actionBar.setTitle(title);

            if(subtitle == null) actionBar.setSubtitle("");
            else actionBar.setSubtitle(subtitle);
        }
    }

    private void initFloatAction() {
        fabAddTopCategory = (FloatingActionButton) findViewById(R.id.fabAddTopCategory);
        fabAddTopCategory.setOnClickListener(this);
        fabSubCategory = (FloatingActionMenu) findViewById(R.id.fabMenuSubCategory);
        fabAddRecipeListRecipe = (FloatingActionButton) findViewById(R.id.fabAddRecipeListRecipe);
        fabAddRecipeListRecipe.setOnClickListener(this);
        fabAddRecipeSubCategory = (FloatingActionButton) findViewById(R.id.fabAddRecipeSubCategory);
        fabAddFolderSubCategory = (FloatingActionButton) findViewById(R.id.fabAddFolderSubCategory);
        fabAddRecipeSubCategory.setOnClickListener(this);
        fabAddFolderSubCategory.setOnClickListener(this);
        showFloatButtonTopCategory();
    }

    public static void showFloatButtonTopCategory() {
        fabAddTopCategory.show(true);
        fabSubCategory.hideMenuButton(false);
        fabAddRecipeListRecipe.hide(false);
    }

    public static void showFloatMenuSubCategory() {
        fabAddTopCategory.hide(false);
        fabSubCategory.showMenuButton(true);
        fabAddRecipeListRecipe.hide(false);
    }

    public static void showFloatButtonListRecipe() {
        fabAddTopCategory.hide(false);
        fabSubCategory.hideMenuButton(false);
        fabAddRecipeListRecipe.show(true);
    }

    public static void hideAllFloatButtons() {
        fabAddTopCategory.hide(false);
        fabSubCategory.hideMenuButton(false);
        fabAddRecipeListRecipe.hide(false);
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
        final SearchView searchView = (SearchView) searchItem.getActionView();
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("TG", "onQueryTextSubmit = " + query);
                if (query.length() > 1) {
                    fragListRecipe = new FragListRecipe();
                    if (!fragListRecipe.isAdded()) {
                        startListRecipeFragment(0, MODE_SEARCH_RESULT, query);
                    }else {
                        FragListRecipe.setParams(0, MODE_SEARCH_RESULT, query);
                        new FragListRecipe().showListRecipe();
                    }

                }
                else Snackbar.make(searchView, R.string.text_empty_request,
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
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
        int id = item.getItemId();

        if (id == R.id.drawer_home) {
            clearBackStackFragment();
            setTopCategoryFragment();
        } else if (id == R.id.drawer_favorite) {
            fragListRecipe = new FragListRecipe();
            if (!fragListRecipe.isAdded()) {
                startListRecipeFragment(0, MODE_FAVORITE_RECIPE, null);
            }else {
                FragListRecipe.setParams(0, MODE_FAVORITE_RECIPE, null);
                new FragListRecipe().showListRecipe();
            }

        } else if (id == R.id.drawer_export_db) {
            showSaveRestoreDialog(DIALOG_FILE_SAVE);

        } else if (id == R.id.drawer_import_db) {
            showSaveRestoreDialog(DIALOG_FILE_RESTORE);

        } else if (id == R.id.drawer_send_question) {
            sendMailToDevelopers();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendMailToDevelopers() {
        String title = context.getResources().getString(R.string.email_theme);
        String email = context.getResources().getString(R.string.email);
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        emailIntent.setType("text/plain");

        emailIntent.setData(Uri.parse(email));
        // this will make such that when user returns to your app, your app is displayed,
        // instead of the email app.
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(emailIntent);
    }

    public void restoreDatabase(String pathFolder) throws IOException {
        File workDatabase = context.getDatabasePath(FILENAME_WORKING_DB);
        File backupOfDatabase = new File(pathFolder, BACKUP_FILENAME);
        OutputStream receiverStream = new FileOutputStream(workDatabase);
        InputStream sourceStream = new FileInputStream(backupOfDatabase);
        byte[] buffer = new byte[1024];
        int lenght;
        while ((lenght = sourceStream.read(buffer)) > 0) {
            receiverStream.write(buffer, 0, lenght);
        }
        receiverStream.flush();
        receiverStream.close();
        sourceStream.close();

        makeSnackbar(context.getResources().getString(R.string.dlg_success_loaded));

        new FragTopCategory().showAllCategory();
    }

    public void saveDatabaseFile(String pathFolder) throws IOException {
        boolean success;
        File workDatabase = context.getDatabasePath(FILENAME_WORKING_DB);
        File newFolder = new File(pathFolder);
        if (!newFolder.exists()) success = newFolder.mkdirs();
        else success = true;
        if (success) {
            File backupDatabase = new File(pathFolder, BACKUP_FILENAME);
            if (!backupDatabase.exists()) {
                success = backupDatabase.createNewFile();
            }
            if (success) {
                OutputStream receiverStream = new FileOutputStream(backupDatabase);
                InputStream sourceStream = new FileInputStream(workDatabase);
                byte[] buffer = new byte[1024];
                int lenght;
                while ((lenght = sourceStream.read(buffer)) > 0) {
                    receiverStream.write(buffer, 0, lenght);
                }
                receiverStream.flush();
                receiverStream.close();
                sourceStream.close();
            } else makeSnackbar(context.getResources().getString(R.string.dlg_error_save_file));

        } else makeSnackbar(context.getResources().getString(R.string.dlg_error_save_file));
        makeSnackbar(context.getResources().getString(R.string.success_saved));
    }

    private void clearBackStackFragment() {
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }
    }

    @Override
    public void onListItemClick(int idActionFrom, int idItem) {
        switch (idActionFrom) {
            case ID_ACTION_TOP_CATEGORY:
                startSubCategoryFragment(idItem);
                break;
            case ID_ACTION_SUB_CATEGORY_CATEGORY:
                startListRecipeFragment(idItem, MODE_RECIPE_FROM_CATEGORY, null);
                break;
            case ID_ACTION_SUB_CATEGORY_RECIPE:
                Log.d("TG", "onListItemClick idItem = " + idItem);
                startTextRecipeFragment(idItem, PARENT, MODE_REVIEW_RECIPE);
                break;
            case ID_ACTION_LIST_RECIPE:
                startTextRecipeFragment(idItem, CHILD, MODE_REVIEW_RECIPE);
                break;
        }
    }

    private void setTopCategoryFragment() {
        fragTopCategory = new FragTopCategory();
        if (!fragTopCategory.isAdded()) {
            fragmentTransaction = fragmentManager.beginTransaction()
                    .replace(R.id.container, fragTopCategory, TAG_CATEGORY);
            fragmentTransaction.commit();
        }
    }

    private void startTextRecipeFragment(int idItem, int typeFolder, int startMode) {
        Bundle bundle = new Bundle();
        fragTextRecipe = new FragTextRecipe();
        if (!fragTextRecipe.isAdded()) {
            if (typeFolder == PARENT) {
                bundle.putInt(TAG_PARENT_ITEM_ID, FragSubCategory.idParentItem);//get id TOP category
            } else if (typeFolder == CHILD) {
                bundle.putInt(TAG_PARENT_ITEM_ID, FragListRecipe.idParentItem);//get id SUB category
            }
            bundle.putInt(TAG_ID_RECIPE, idItem);
            Log.d("TG", "startTextRecipeFragment idItem = " + idItem);
            bundle.putInt(TAG_MODE, startMode);
            bundle.putInt(TAG_TYPE_FOLDER, typeFolder);
            fragTextRecipe.setArguments(bundle);
            fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.replace(R.id.container, fragTextRecipe)
                    .addToBackStack(TAG_TEXT_RECIPE)
                    .commit();
        }
    }

    private void startListRecipeFragment(int idItem, int startMode, String searchRequest) {
        Log.d("TG", "startListRecipeFragment:"+
        "idItem= "+idItem+"  startMode= "+startMode+"  searchRequest= "+searchRequest);
        Bundle bundle = new Bundle();
        fragListRecipe = new FragListRecipe();
        if (!fragListRecipe.isAdded()) {
            bundle.putInt(TAG_PARENT_ITEM_ID, idItem);
            bundle.putInt(TAG_MODE, startMode);
            bundle.putString(TAG_SEARCH_STRING, searchRequest);
            fragListRecipe.setArguments(bundle);
            fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.replace(R.id.container, fragListRecipe)
                    .addToBackStack(TAG_LIST_RECIPE)
                    .commit();
        }
    }

    private void startSubCategoryFragment(int idItem) {
        Bundle bundle = new Bundle();
        fragSubCategory = new FragSubCategory();
        if (!fragSubCategory.isAdded()) {
            bundle.putInt(TAG_PARENT_ITEM_ID, idItem);
            fragSubCategory.setArguments(bundle);
            fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.replace(R.id.container, fragSubCategory)
                    .addToBackStack(TAG_SUBCATEGORY)
                    .commit();
        }
    }

    public void showSaveRestoreDialog(int idDialog) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(ID_DIALOG, idDialog);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_FILE_DIALOG);
        if (fragment != null) {
            ft.remove(fragment);
        }
        ft.addToBackStack(null);

        DialogFragment dialogFragment = new SaveRestoreDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fragmentManager, TAG_FILE_DIALOG);
    }

    /**
     * ADD folders or recipes
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fabAddTopCategory) {
            FragTopCategory.showDialog(DIALOG_ADD_CATEGORY, null);
            /** add recipe in TOP folder */
        } else if (view.getId() == R.id.fabAddRecipeSubCategory) {
            startTextRecipeFragment(DEFAULT_VALUE_COLUMN,
                    PARENT, MODE_NEW_RECIPE);
            fabSubCategory.close(true);
            /** add folder in TOP folder */
        } else if (view.getId() == R.id.fabAddFolderSubCategory) {
            FragSubCategory.showDialog(DIALOG_ADD_SUBCATEGORY, "");
            fabSubCategory.close(true);
            /** add recipe in SUB folder */
        } else if (view.getId() == R.id.fabAddRecipeListRecipe) {
            Log.d("TG", "fabAddRecipeListRecipe idItem = " + FragSubCategory.idItem);
            startTextRecipeFragment(DEFAULT_VALUE_COLUMN, CHILD, MODE_NEW_RECIPE);
            fabSubCategory.close(true);
        }

    }

    private void makeSnackbar(String text) {
        Snackbar.make(fabSubCategory, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
