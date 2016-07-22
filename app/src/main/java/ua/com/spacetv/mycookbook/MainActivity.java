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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ua.com.spacetv.mycookbook.fragments.FragListRecipe;
import ua.com.spacetv.mycookbook.fragments.FragSettings;
import ua.com.spacetv.mycookbook.fragments.FragSubCategory;
import ua.com.spacetv.mycookbook.fragments.FragTopCategory;
import ua.com.spacetv.mycookbook.google_services.Ads;
import ua.com.spacetv.mycookbook.google_services.Analytics;
import ua.com.spacetv.mycookbook.helpers.DbHelper;
import ua.com.spacetv.mycookbook.helpers.FragmentHelper;
import ua.com.spacetv.mycookbook.interfaces.Constants;
import ua.com.spacetv.mycookbook.interfaces.LicenseKey;
import ua.com.spacetv.mycookbook.interfaces.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.Preferences;
import ua.com.spacetv.mycookbook.util.IabHelper;
import ua.com.spacetv.mycookbook.util.IabResult;
import ua.com.spacetv.mycookbook.util.Inventory;
import ua.com.spacetv.mycookbook.util.Purchase;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Constants,
        OnFragmentEventsListener, View.OnClickListener, LicenseKey,
        IabHelper.OnIabPurchaseFinishedListener {

    private static final String ITEM_SKU = isDebugModeOn ? ITEM_SKU_DEBUG : ITEM_SKU_PRODUCTION;
    private static FragmentHelper mFragmentHelper;
    public static Context mContext;
    private static FragmentManager mFragmentManager;
    public static FloatingActionButton fabAddTopCategory;
    public static FloatingActionButton fabAddRecipeListRecipe;
    public static FloatingActionMenu fabSubCategory;
    private static android.support.v7.app.ActionBar actionBar;
    public static DbHelper mDbHelper;
    public static SQLiteDatabase mDatabase;
    //**** Purchase in app
    private static IabHelper mHelper;
    public static boolean isPurchaseOwned = false;//key to purchase
    private boolean isRemoveAdsPressed = false;//this key showing is button "Remove ads" is pressed
    public static Toolbar mToolbar;
    public static FrameLayout mFrameLayout;
    private boolean mIsBackgroundWhite;
    public static LinearLayout mHeaderNavigationDrawerLayout;
    public static Ads mAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getBaseContext();
        setColorTheme();
        setContentView(R.layout.activity_main);

        //init database and database helper
        mDbHelper = DbHelper.init(mContext);
        mDatabase = mDbHelper.getWritableDatabase();

        mFragmentManager = getSupportFragmentManager();
        mFragmentHelper = new FragmentHelper(mFragmentManager);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        actionBar = getSupportActionBar();

        //Container for all fragments
        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        if (mIsBackgroundWhite) mFrameLayout.setBackgroundColor(Color.WHITE);

        initFloatAction();
        initDrawerLayout(mToolbar);

        //adding TopCategoryFragment when activity recreated
        if (mFragmentManager.findFragmentByTag(FragTopCategory.class.getSimpleName()) == null) {
            mFragmentHelper.attachTopCategoryFragment();
        }

        //init ads
        loadAds();

        //Purchase in app section
        mHelper = new IabHelper(this, LICENSE_KEY);
        isPurchaseOwned = Preferences.getSettingsFromPreferences(mContext, IS_PURCHASE_OWNED, 0);
        if (!isPurchaseOwned) {
            setupBillingInApp();
        }
        Log.d("TG", "%%% Main Activity onCreate ");
    }

    /**
     * Init and preload ADS
     */
    private void loadAds() {
        mAds = new Ads(mContext);
        if (mAds.getInterstitialAd() == null) mAds.initAds();
    }

    /**
     * Init DrawerLayout and set listener
     * If Drawer is open - check and kill timer (witch showing ads banner)
     *
     * @param toolbar - Toolbar
     */
    private void initDrawerLayout(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                //Header od drawer layout, find him only when it is open!!!
                if (mHeaderNavigationDrawerLayout == null) {
                    mHeaderNavigationDrawerLayout =
                            (LinearLayout) findViewById(R.id.drawer_navigation_header);
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    /**
     * Getting and apply saved color theme
     */
    private void setColorTheme() {
        int numberOfTheme = Preferences.getSettingsFromPreferences(mContext, COLOR_THEME);
        mIsBackgroundWhite = Preferences.getSettingsFromPreferences(mContext,
                IS_BACKGROUND_WHITE, 0);
        switch (numberOfTheme) {
            case INDIGO:
                setTheme(R.style.IndigoTheme);
                break;
            case PINK:
                setTheme(R.style.PinkTheme);
                break;
            case PURPLE:
                setTheme(R.style.PurpleTheme);
                break;
            case DEEP_PURPLE:
                setTheme(R.style.DeepPurpleTheme);
                break;
            case RED:
                setTheme(R.style.RedTheme);
                break;
            case BLUE:
                setTheme(R.style.BlueTheme);
                break;
            case LIGHT_BLUE:
                setTheme(R.style.LightBlueTheme);
                break;
            case CYAN:
                setTheme(R.style.CyanTheme);
                break;
            case TEAL:
                setTheme(R.style.TealTheme);
                break;
            case GREEN:
                setTheme(R.style.GreenTheme);
                break;
            case LIGHT_GREEN:
                setTheme(R.style.LightGreenTheme);
                break;
            case LIME:
                setTheme(R.style.LimeTheme);
                break;
            case YELLOW:
                setTheme(R.style.YellowTheme);
                break;
            case AMBER:
                setTheme(R.style.AmberTheme);
                break;
            case ORANGE:
                setTheme(R.style.OrangeTheme);
                break;
            case DEEP_ORANGE:
                setTheme(R.style.DeepOrangeTheme);
                break;
            case BROWN:
                setTheme(R.style.BrownTheme);
                break;
            case GREY:
                setTheme(R.style.GreyTheme);
                break;
            case BLUE_GREY:
                setTheme(R.style.BlueGreyTheme);
                break;
            case BLACK_WHITE:
                setTheme(R.style.BlackWhiteTheme);
                break;
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
                    Log.d("TG", "In-app Billing is set up OK, consumeItem");
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
                Log.d("TG", "QueryInventoryFinishedListener - result.isFailure");
                // Handle failure
            } else {

                // Getting status of purchase and save it into preferences
                // called when app is started ('Remove ads' button is NOT pressed)
                if (!isRemoveAdsPressed) {
                    if (inventory.getPurchase(ITEM_SKU) != null) {
                        if (inventory.getPurchase(ITEM_SKU).getSku().equals(ITEM_SKU)) {
                            isPurchaseOwned = true; //purchase is already owned
                            Log.d("TG", "Purchase already is owned, save state to preference");
                        } else {
                            Log.d("TG", "Purchase still NOT owned, save state to preference");
                            isPurchaseOwned = false; //purchase is not owned
                        }
                        Preferences.setSettingsToPreferences(mContext, IS_PURCHASE_OWNED, isPurchaseOwned);
                    }
                } else { //'Remove ads' button pressed
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

                    //uncomment this try-block for reset purchase
//                    try {
//                        mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),//***
//                                mConsumeFinishedListener);//****
//                    } catch (IabHelper.IabAsyncInProgressException e) {
//                        e.printStackTrace();
//                    }
//                    }
                }
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    if (result.isSuccess()) {
                        isPurchaseOwned = false;
                        Preferences.setSettingsToPreferences(mContext, IS_PURCHASE_OWNED, false);
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
                new Analytics(mContext).sendAnalytics("myCookBook", "Main Activity",
                        "Purchasing error!", null);
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                isPurchaseOwned = true;
                Preferences.setSettingsToPreferences(mContext, IS_PURCHASE_OWNED, true);
                new Analytics(mContext).sendAnalytics("myCookBook", "Main Activity",
                        "Purchase is owned!", null);
                Log.d("TG", "Purchase is owned! ");
            }
        }
    };

    /**
     * 'Purchase in app'
     * Getting result
     * Check (mHelper != null) <-- fixed NullPointer from some device
     *
     * @param requestCode - requestCode
     * @param resultCode - resultCode
     * @param data - data
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
        Log.d("TG", "%%% Main Activity onDestroy ");
        //'Purchase in app'
        if (mContext != null && mHelper != null)
            try {
                mHelper.dispose();
            } catch (IllegalArgumentException | IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        mHelper = null;
        //close DbHelper
        if (mDbHelper != null) mDbHelper.close();
        if (mDatabase != null) mDatabase.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("TG", "%%% Main Activity onPause ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TG", "%%% Main Activity onResume ");
    }

    /**
     * Returned number of all fragments in the back stack
     *
     * @return - the number of fragments contained in the back stack
     */
    private int countBackStackFragment() {
        return mFragmentManager.getBackStackEntryCount();
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

    /**
     * Processed search item selected
     * @param item - search item
     * @return - if "action_search" preset return true, else return super.onOptionsItemSelected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_search || super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.drawer_home) {//action: 'home'
            clearBackStackOfFragments();
        } else if (id == R.id.drawer_remove_ads) {//action: 'remove ads'
            isRemoveAdsPressed = true;
            if (!isPurchaseOwned) consumeItem();

        } else if (id == R.id.drawer_favorite) {//action: 'favourite'
            String tag = FragListRecipe.class.getSimpleName();
            tag += "Favorite";
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment == null) {
                mFragmentHelper.attachListRecipeFragment(0, MODE_FAVORITE_RECIPE, null);
            } else {
                FragListRecipe.setParams(0, MODE_FAVORITE_RECIPE, null);
                new FragListRecipe().showListRecipe();
            }
        } else if (id == R.id.drawer_send_question) {
            sendMailToDevelopers();
        } else if (id == R.id.drawer_settings) {//action: 'settings'
            List<Fragment> fragments = mFragmentManager.getFragments();
            boolean isFragmentFound = false;
            for (Fragment fr : fragments) {
                if (fr != null)
                    if (fr.getTag().equals(FragSettings.class.getSimpleName()))
                        isFragmentFound = true;
            }
            if (!isFragmentFound) mFragmentHelper.attachSettingsFragment();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    /**
     * Restoring previously saved SQLite-file
     * @param pathFolder - path to folder with database file
     * @throws IOException
     */
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
        success = newFolder.exists() || newFolder.mkdirs();
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

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {

    }
}