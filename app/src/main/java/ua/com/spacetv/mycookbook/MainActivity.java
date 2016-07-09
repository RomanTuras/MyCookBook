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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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

import ua.com.spacetv.mycookbook.fragments.FragListRecipe;
import ua.com.spacetv.mycookbook.fragments.FragSubCategory;
import ua.com.spacetv.mycookbook.fragments.FragTopCategory;
import ua.com.spacetv.mycookbook.google_services.Analytics;
import ua.com.spacetv.mycookbook.helpers.FragmentHelper;
import ua.com.spacetv.mycookbook.interfaces.Constants;
import ua.com.spacetv.mycookbook.interfaces.LicenseKey;
import ua.com.spacetv.mycookbook.interfaces.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.Preferences;
import ua.com.spacetv.mycookbook.tools.RestoreDatabaseRecipes;
import ua.com.spacetv.mycookbook.tools.SaveDatabaseRecipes;
import ua.com.spacetv.mycookbook.tools.Utilities;
import ua.com.spacetv.mycookbook.util.IabHelper;
import ua.com.spacetv.mycookbook.util.IabResult;
import ua.com.spacetv.mycookbook.util.Inventory;
import ua.com.spacetv.mycookbook.util.Purchase;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Constants,
        OnFragmentEventsListener, View.OnClickListener, LicenseKey,
        IabHelper.OnIabPurchaseFinishedListener {

    private static final int PERMISSION_REQUEST_CODE = 2;
    private static FragmentHelper mFragmentHelper;
    private static Context mContext;
    private static FragmentManager mFragmentManager;
    private static FloatingActionButton fabAddTopCategory;
    private static FloatingActionButton fabAddRecipeListRecipe;
    private static FloatingActionMenu fabSubCategory;
    private static android.support.v7.app.ActionBar actionBar;
    private static int mAction;
    //**** Purchase in app
    private static IabHelper mHelper;
    public static boolean isPurchaseActive = false;//key to purchase
    private boolean isRemoveAdsPressed = false;//this key showing is button "Remove ads" is pressed
    boolean isClearPurchase = false; //test, remove it

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();
        mContext = getBaseContext();

        mFragmentHelper = new FragmentHelper(mFragmentManager);

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

        if (mFragmentManager.findFragmentByTag(FragTopCategory.class.getSimpleName()) == null) {
            mFragmentHelper.attachTopCategoryFragment();
        }

        mHelper = new IabHelper(this, LICENSE_KEY);
        isPurchaseActive = Preferences.getSettingsFromPreferences(mContext, IS_PURCHASE_OWNED, 0);
        if (!isPurchaseActive) {
            setupBillingInApp();
        } else {
            Log.d("TG", "Purchase already owned!");
//            setupBillingInApp();
        }


    }

    /**
     * 'Purchase in app'
     * Setup billing in app
     */
    private void setupBillingInApp() {
        mHelper.enableDebugLogging(isDebugModeOn);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d("TG", "In-app Billing setup failed: " + result);
                } else {
                    Log.d("TG", "In-app Billing is set up OK");
                    consumeItem();
                }
            }
        });
    }

    /**
     * 'Purchase in app'
     * Sending query inventory async
     */
    private void consumeItem() {
        try {
            mHelper.queryInventoryAsync(mReceivedInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    /**
     * 'Purchase in app'
     * Listener for 'queryInventoryAsync'
     * Getting status of purchase and save it into preferences
     */
    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // Handle failure
            } else {

                // Getting status of purchase and save it into preferences
                // called when 'Remove ads' button NOT pressed
                if (!isRemoveAdsPressed) {
                    if (inventory.getPurchase(ITEM_SKU) != null) {
                        if (inventory.getPurchase(ITEM_SKU).getSku().equals(ITEM_SKU)) {
                            isPurchaseActive = true; //purchase is owned
                        } else {
                            isPurchaseActive = false; //purchase is not owned
                        }
                        Preferences.setSettingsToPreferences(mContext, IS_PURCHASE_OWNED, isPurchaseActive);
                    }
                } else {
//                    if(!isClearPurchase) {
                    isRemoveAdsPressed = false;
                    if (inventory.getPurchase(ITEM_SKU) == null) {
                        try {
                            mHelper.launchPurchaseFlow(MainActivity.this, ITEM_SKU, RC_REQUEST,
                                    mPurchaseFinishedListener, "mypurchasetoken");
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }
                    }
//                    }else {
//                        isClearPurchase = false;
//                        try {
//                            mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),//***
//                                    mConsumeFinishedListener);//****
//                        } catch (IabHelper.IabAsyncInProgressException e) {
//                            e.printStackTrace();
//                        }
//                    }
                }
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    if (result.isSuccess()) {
                        isPurchaseActive = false;
                        Preferences.setSettingsToPreferences(mContext, IS_PURCHASE_OWNED, isPurchaseActive);
                        Log.d("TG", "Purchase is canceled! ");
                    } else {
                        Log.d("TG", "Purchase is NOT canceled! handle error ");
                    }
                }
            };

    /**
     * 'Purchase in app'
     * Listener for 'QueryInventoryFinishedListener'
     */
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                isPurchaseActive = true;
                Preferences.setSettingsToPreferences(mContext, IS_PURCHASE_OWNED, isPurchaseActive);
                Log.d("TG", "Purchase is owned! ");
            }
        }
    };

    /**
     * 'Purchase in app'
     * Getting result
     * Check (mHelper != null) <-- fixed NullPointer from some device
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper != null && !mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //'Purchase in app'
        if (mHelper != null) try {
            mHelper.dispose();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        mHelper = null;
    }

    /**
     * Returned number of all fragments in the back stack
     *
     * @return
     */
    private int countBackStackFragment() {
        int i = mFragmentManager.getBackStackEntryCount();
        return i;
    }

    /**
     * Clear all back stack of fragments, except FragTopCategory
     */
    private void clearBackStackOfFragments() {
        for (int i = 0; i < mFragmentManager.getBackStackEntryCount() - 1; i++) {
            mFragmentManager.popBackStack();
        }
    }

    public static void overrideActionBar(String title, String subtitle) {
        if (actionBar != null) {
            if (title == null) actionBar.setTitle(R.string.app_name);
            else actionBar.setTitle(title);

            if (subtitle == null) actionBar.setSubtitle("");
            else actionBar.setSubtitle(subtitle);
        }
    }

    private void initFloatAction() {
        fabAddTopCategory = (FloatingActionButton) findViewById(R.id.fabAddTopCategory);
        fabAddTopCategory.setOnClickListener(this);
        fabSubCategory = (FloatingActionMenu) findViewById(R.id.fabMenuSubCategory);
        fabAddRecipeListRecipe = (FloatingActionButton) findViewById(R.id.fabAddRecipeListRecipe);
        fabAddRecipeListRecipe.setOnClickListener(this);
        FloatingActionButton fabAddRecipeSubCategory =
                (FloatingActionButton) findViewById(R.id.fabAddRecipeSubCategory);
        FloatingActionButton fabAddFolderSubCategory =
                (FloatingActionButton) findViewById(R.id.fabAddFolderSubCategory);
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
        if (countBackStackFragment() == 0) this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 1) {
                    String tag = FragListRecipe.class.getSimpleName();
                    tag += "Search";
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                    if (fragment == null) {
                        mFragmentHelper.attachListRecipeFragment(0, MODE_SEARCH_RESULT, query);
                    } else {
                        FragListRecipe.setParams(0, MODE_SEARCH_RESULT, query);
                        new FragListRecipe().showListRecipe();
                    }

                } else Snackbar.make(searchView, R.string.text_empty_request,
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
            clearBackStackOfFragments();
        } else if (id == R.id.drawer_remove_ads) {
            isRemoveAdsPressed = true;
            if (!isPurchaseActive) consumeItem();
            if (!isDebugModeOn) {//if debug mode off - send analytics
                String str = "isPurchaseActive = " + isPurchaseActive;
                new Analytics(mContext).sendAnalytics("myCookBook", "Main Activity",
                        "Attempt to buy", str);
            }
        } else if (id == R.id.drawer_favorite) {
            String tag = FragListRecipe.class.getSimpleName();
            tag += "Favorite";
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment == null) {
                mFragmentHelper.attachListRecipeFragment(0, MODE_FAVORITE_RECIPE, null);
            } else {
                FragListRecipe.setParams(0, MODE_FAVORITE_RECIPE, null);
                new FragListRecipe().showListRecipe();
            }

        } else if (id == R.id.drawer_export_db) {
            // check permissions before call to dialog
            checkPermissions(DIALOG_FILE_SAVE);

        } else if (id == R.id.drawer_import_db) {
            // check permissions before call to dialog
            checkPermissions(DIALOG_FILE_RESTORE);

        } else if (id == R.id.drawer_send_question) {
            sendMailToDevelopers();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Checks the permissions for android 6
     * And shows the proper screen if there's no permissions
     *
     * @param action - what action will be called after request permissions
     */
    private void checkPermissions(int action) {
        //mAction - save selected type of dialog witch will
        // be called after permission is granted
        mAction = action;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                //Asks user to add the permission
                requestMultiplePermissions();
            } else {
                //permissions granted
                callSaveRestoreDialog(action);
            }
        } else {
            // VERSION < M
            callSaveRestoreDialog(action);
        }
    }

    /**
     * Request permissions
     */
    private void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                },
                PERMISSION_REQUEST_CODE);
    }

    /**
     * Calling dialog to save or restore mDatabase of recipes, depends of selected mode
     *
     * @param mode - DIALOG_FILE_SAVE / DIALOG_FILE_RESTORE
     */
    private void callSaveRestoreDialog(int mode) {
        switch (mode) {
            case DIALOG_FILE_SAVE:
                SaveDatabaseRecipes.dialogSaveDatabase(this);
                break;
            case DIALOG_FILE_RESTORE:
                RestoreDatabaseRecipes.dialogRestoreDatabase(this);
                break;
        }
    }

    private void sendMailToDevelopers() {
        String title = mContext.getResources().getString(R.string.email_theme);
        String email = mContext.getResources().getString(R.string.email);
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        emailIntent.setType("text/plain");
        emailIntent.setData(Uri.parse(email));
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(emailIntent);
    }

    public void restoreDatabase(String pathFolder) throws IOException {
        File workDatabase = mContext.getDatabasePath(FILENAME_WORKING_DB);
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

        makeSnackbar(mContext.getResources().getString(R.string.dlg_success_loaded));

        clearBackStackOfFragments();
        new FragTopCategory().showAllCategory();
    }

    public void saveDatabaseFile(String pathFolder) throws IOException {
        boolean success;
        File workDatabase = mContext.getDatabasePath(FILENAME_WORKING_DB);
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
            } else makeSnackbar(mContext.getResources().getString(R.string.dlg_error_save_file));

        } else makeSnackbar(mContext.getResources().getString(R.string.dlg_error_save_file));
        makeSnackbar(mContext.getResources().getString(R.string.success_saved));
    }

    @Override
    public void onListItemClick(int idActionFrom, int idItem) {
        switch (idActionFrom) {
            case ID_ACTION_TOP_CATEGORY:
                mFragmentHelper.attachSubCategoryFragment(idItem);
                break;
            case ID_ACTION_SUB_CATEGORY_CATEGORY:
                mFragmentHelper.attachListRecipeFragment(idItem, MODE_RECIPE_FROM_CATEGORY, null);
                break;
            case ID_ACTION_SUB_CATEGORY_RECIPE:
                mFragmentHelper.attachTextRecipeFragment(idItem, MODE_REVIEW_RECIPE, PARENT);
                break;
            case ID_ACTION_LIST_RECIPE:
                mFragmentHelper.attachTextRecipeFragment(idItem, MODE_REVIEW_RECIPE, CHILD);
                break;
        }
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
            mFragmentHelper.attachTextRecipeFragment(DEFAULT_VALUE_COLUMN, MODE_NEW_RECIPE, PARENT);
            fabSubCategory.close(true);
            /** add folder in TOP folder */
        } else if (view.getId() == R.id.fabAddFolderSubCategory) {
            FragSubCategory.showDialog(DIALOG_ADD_SUBCATEGORY, "");
            fabSubCategory.close(true);
            /** add recipe in SUB folder */
        } else if (view.getId() == R.id.fabAddRecipeListRecipe) {
            mFragmentHelper.attachTextRecipeFragment(DEFAULT_VALUE_COLUMN, MODE_NEW_RECIPE, CHILD);
            fabSubCategory.close(true);
        }
    }

    private void makeSnackbar(String text) {
        Snackbar.make(fabSubCategory, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    /**
     * Invokes when user selected the permissions
     * This is format for use from activity
     * <p/>
     * mAction - type of mode: save (DIALOG_FILE_SAVE) or
     * restore (DIALOG_FILE_RESTORE) mDatabase of recipes
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 3) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                Utilities.showOkDialog(this,
                        getResources().getString(R.string.permissions_error),
                        new Utilities.IYesNoCallback() {
                            @Override
                            public void onYes() {
                            }
                        });
            } else {
                callSaveRestoreDialog(mAction);
            }
        }
    }


    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {

    }
}