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

package ua.com.spacetv.mycookbook.fragments;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ua.com.spacetv.mycookbook.MainActivity;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.google_services.Ads;
import ua.com.spacetv.mycookbook.google_services.Analytics;
import ua.com.spacetv.mycookbook.helpers.DbHelper;
import ua.com.spacetv.mycookbook.helpers.ImagePickHelper;
import ua.com.spacetv.mycookbook.interfaces.Constants;
import ua.com.spacetv.mycookbook.interfaces.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.Utilities;

/**
 * Fragment is managing of reviews, creating new and editing recipes
 * Depends of startup mode: MODE_EDIT_RECIPE, MODE_REVIEW_RECIPE, MODE_NEW_RECIPE
 */
public class FragTextRecipe extends Fragment implements Constants,
        OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_CODE = 2;
    private static Context mContext;
    private static FragmentManager fragmentManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
//    public static DataBaseHelper dataBaseHelper;
    private static DbHelper mDbHelper;
    public static SQLiteDatabase mDatabase;
    private ContentValues mContentValues;
    private static Ads mAds;
    private static View view;
    private EditText mEditTitleRecipe, mEditTextRecipe;
//    private TextView textTextRecipe;
    private static Intent mChooseImageIntent;
    private static ImageView imageView;
    private static int idReceivedFolderItem = 0; // id of folder where was or will TEXT of RECIPE
    private static int idRecipe = 0; // id Received (mode REVIEW) or Just Created recipe (mode NEW)
    private static int typeReceivedFolder = 0; // Is two types of folders: PARENT=0 (top) & CHILD=1 (subFolder)
    private static int topFolder_id = 0; //id top folder received from mDatabase
    private static int subFolder_id = 0; //id sub folder received from mDatabase
    private static int startupMode = MODE_REVIEW_RECIPE;
    private static int displayWidth;
    private final int CHOOSE_IMAGE = 1;
    private String selectedImagePath;
    private String titleRecipeFromDatabase = null;
    private String textRecipeFromDatabase = null;
    private static String databaseImagePath;
    private static Fragment mFragmentTextRecipe;
    public static Timer mScheduledTimeTimer;
    private static Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragTextRecipe.mContext = context;
//        FragTextRecipe.dataBaseHelper = new DataBaseHelper(context);
        mDbHelper = MainActivity.mDbHelper;
        mContentValues = new ContentValues();
        mFragmentTextRecipe = new FragTextRecipe();
        mActivity = getActivity();
        setRetainInstance(true);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            idReceivedFolderItem = bundle.getInt(TAG_PARENT_ITEM_ID, DEFAULT_VALUE_COLUMN);
            idRecipe = bundle.getInt(TAG_ID_RECIPE);
            typeReceivedFolder = bundle.getInt(TAG_TYPE_FOLDER);
            startupMode = bundle.getInt(TAG_MODE);
            Log.d("TG", "TextRecipe: idRecipe = " + idRecipe + " idReceivedFolderItem= " + idReceivedFolderItem + " typeReceivedFolder= " + typeReceivedFolder + " startupMode= " + startupMode);
        }
        selectedImagePath = null;
        databaseImagePath = null;
        onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
        Log.d("TG", "** FragTextRecipe onCreate **");
    }

    private void loadAds() {
        if (mAds.getInterstitialAd() == null) mAds.initAds(); // init and preload Ads
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        view = inflater.inflate(R.layout.frag_text_recipe, null);
        mEditTitleRecipe = (EditText) view.findViewById(R.id.editTitleRecipe);
        mEditTextRecipe = (EditText) view.findViewById(R.id.editTextRecipe);
//        textTextRecipe = (TextView) view.findViewById(R.id.textTextRecipe);
        imageView = (ImageView) view.findViewById(R.id.imageRecipe);

        getDisplayMetrics();

        mAds = new Ads(mContext);

        mDatabase = mDbHelper.getWritableDatabase();
//        mDatabase = dataBaseHelper.getWritableDatabase();
        fragmentManager = getFragmentManager();

        if (startupMode == MODE_EDIT_RECIPE) modeEdit();
        else if (startupMode == MODE_REVIEW_RECIPE) modeReview();
        else if (startupMode == MODE_NEW_RECIPE) modeNewRecipe();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if (startupMode == MODE_REVIEW_RECIPE) inflater.inflate(R.menu.menu_review_recipe, menu);
        else if (startupMode == MODE_EDIT_RECIPE |
                startupMode == MODE_NEW_RECIPE) inflater.inflate(R.menu.menu_edit_recipe, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Hiding software keyboard
     * @param v - View
     */
    private void hideKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void modeNewRecipe() {
        Log.d("TG", "modeNewRecipe *** ");
        if (mScheduledTimeTimer != null) mScheduledTimeTimer.cancel();
        mEditTitleRecipe.setFocusableInTouchMode(true);
        mEditTitleRecipe.setFocusable(true);
        mEditTitleRecipe.requestFocus();
        mEditTextRecipe.setFocusableInTouchMode(true);
        mEditTextRecipe.setFocusable(true);
        setHasOptionsMenu(true);
    }

    private void modeEdit() {
        Log.d("TG", "modeEdit *** ");
        if (mScheduledTimeTimer != null) mScheduledTimeTimer.cancel();
        mEditTitleRecipe.setFocusableInTouchMode(true);
        mEditTitleRecipe.setFocusable(true);
        mEditTextRecipe.setFocusableInTouchMode(true);
        mEditTextRecipe.setFocusable(true);
        readRecipeFromDatabase();
        startupMode = MODE_EDIT_RECIPE;
        setHasOptionsMenu(false);
        setHasOptionsMenu(true);
    }

    private void modeReview() {
        new Analytics(mContext).sendAnalytics("myCookBook", "Text Category", "Review recipe", "nop");
        //If the service is not purchased - show mAds
        if (!MainActivity.isPurchaseActive) startScheduledTimeTimer();

        Log.d("TG", "modeReview *** ");
        mEditTitleRecipe.setFocusableInTouchMode(false);
        mEditTitleRecipe.setFocusable(false);
        mEditTextRecipe.setFocusableInTouchMode(false);
        mEditTextRecipe.setFocusable(false);
        readRecipeFromDatabase();
        startupMode = MODE_REVIEW_RECIPE;
        setHasOptionsMenu(false);
        setHasOptionsMenu(true);
    }

    /**
     * Starting 10 seconds timer for showing mAds banner
     * Timer timer work once
     */
    private static void startScheduledTimeTimer() {
        mScheduledTimeTimer = new Timer();
        mScheduledTimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mAds.getInterstitialAd() != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d("TG", "-= startScheduledTimeTimer =-");
                            mAds.showAd();
                        }
                    });
                    if (mScheduledTimeTimer != null) mScheduledTimeTimer.cancel();
                }
            }
        }, 1000 * 10, 1000 * 10); //delay 100 ms, period 10 sec
    }

    private void getDisplayMetrics() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        display.getMetrics(metrics);
        displayWidth = metrics.widthPixels;
    }

    /**
     * Set image into image viev
     * Usage Glide library
     *
     * @param path
     */
    private void setImage(String path) {
        Glide.with(mContext).load(path).asBitmap().fitCenter()
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (resource != null) setBitmapToImageView(resource);
                    }
                });
    }

    /**
     * Set bitmap to image view
     *
     * @param bitmap
     */
    public void setBitmapToImageView(Bitmap bitmap) {
        bitmap = getRoundedCornerBitmap(bitmap, 30);
        imageView.setImageBitmap(bitmap);
        imageView.getLayoutParams().height = ActionBar.LayoutParams.WRAP_CONTENT;

        int picWidth = bitmap.getWidth();
        int picHeight = bitmap.getHeight();
        if (picWidth > displayWidth) {
            float scale = (float) picWidth / (float) displayWidth;
            picHeight = (int) ((float) picHeight / scale);
            imageView.getLayoutParams().height = picHeight;
        }
    }

    /**
     * Rounding corners on bitmap
     *
     * @param bitmap
     * @param pixels - radius
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * Getting action from menu
     *
     * action_save - if changes is it, hide keyboard and save recipe
     * action_edit - edit recipe
     * action_share - share current recipe
     * action_photo - pick image from camera or gallery
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (isChangesFromRecipe()){
                    hideKeyboard(mEditTitleRecipe);
                    saveRecipe();
                }
                break;
            case R.id.action_edit:
                modeEdit();
                break;
            case R.id.action_share:
                if (mScheduledTimeTimer != null) mScheduledTimeTimer.cancel();// stop ads
                shareRecipe();
                break;
            case R.id.action_photo:
                //get permissions to access camera or gallery
                hideKeyboard(mEditTitleRecipe);
                checkPermissions();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks the permissions for android 6
     * And shows the proper screen if there's no permissions
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                //Asks user to add the permission
                Log.d("TG", "Asks user to add the permission");
                requestMultiplePermissions();
            } else {
                Log.d("TG", "checkPermissions = PERMISSION_GRANTED");
                getPickImageIntent();
            }
        } else {
            Log.d("TG", "checkPermissions = VERSION < M");
            getPickImageIntent();
        }
    }

    /**
     * Request permissions
     */
    public void requestMultiplePermissions() {
        requestPermissions(
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                },
                PERMISSION_REQUEST_CODE);
    }

    public void getPickImageIntent() {
        mChooseImageIntent = ImagePickHelper.getPickImageIntent(mContext);
        startActivityForResult(mChooseImageIntent, CHOOSE_IMAGE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case CHOOSE_IMAGE:
                    Bitmap bitmap;
                    if (data != null) { // ** Gallery **
                        selectedImagePath = getDataColumn(data.getData());
                        setImage(selectedImagePath);
                        Log.d("TG", "data!=null; getPath = " + selectedImagePath);
                        new Analytics(mContext).sendAnalytics("myCookBook", "Text Category",
                                "Add Image", "Gallery");
                    } else { // ** Camera **
                        selectedImagePath = ImagePickHelper.getImageFromResult(resultCode, data);
                        ImagePickHelper.addImageToGallery(selectedImagePath);
                        setImage(selectedImagePath);
                        Log.d("TG", "data==null; getPath = " + selectedImagePath);
                        new Analytics(mContext).sendAnalytics("myCookBook", "Text Category",
                                "Add Image", "Camera");
                    }
                    break;
            }
        }
    }

    /**
     * Getting the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param uri The Uri to query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Uri uri) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            Log.d("TG", "mContext = " + mContext);
            cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private void shareRecipe() {
        if (mEditTitleRecipe.getText().length() > 0 & mEditTextRecipe.getText().length() > 0) {
            String title = mEditTitleRecipe.getText().toString();
            String text = mEditTextRecipe.getText().toString();
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
//            emailIntent.setType("image/jpeg");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
//            if (databaseImagePath != null && databaseImagePath != "") {
//                Uri imageUri = Uri.parse("file://" + databaseImagePath);
//                emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//            }
            startActivity(Intent.createChooser(emailIntent, mContext
                    .getString(R.string.text_share_recipe)));
        }
    }

    public void shareRecipe(int j) {
        Resources resources = getResources();
        String title = mEditTitleRecipe.getText().toString();
        String text = mEditTextRecipe.getText().toString();
//
        PackageManager pm = getActivity().getPackageManager();

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//            emailIntent.setType("text/plain");
        emailIntent.setType("image/jpeg");
//        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Title1");
//        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text of the messages");

        Uri imageUri = null;
        if (databaseImagePath != null && databaseImagePath != "") {
            imageUri = Uri.parse("file://" + databaseImagePath);
        }
//        emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

        Intent openInChooser = Intent.createChooser(emailIntent, resources.getString(R.string.text_share_recipe));
        List<ResolveInfo> resInfo = pm.queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Log.d("TG", "resInfo.size = " + resInfo.size());
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);

            String packageName = ri.activityInfo.packageName;

            if (packageName.toLowerCase().contains("com.viber.voip")) {
                Log.d("TG", "packageName.contains = " + packageName.toString());
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
                intent.setType("text/plain");
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            } else if (packageName.toLowerCase().contains("com.whatsapp") ||
                    packageName.contains("com.twitter.android") ||
                    packageName.contains("com.facebook.lite") ||
                    packageName.contains("com.facebook.katana") ||
                    packageName.contains("com.google.android.apps.plus") ||
                    packageName.contains("com.google.android.gm")) {
                Log.d("TG", "packageName.contains others = " + packageName.toString());
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
                intent.putExtra(android.content.Intent.EXTRA_STREAM, imageUri);
                intent.setType("image/jpeg");
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

// convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
        Log.d("TG", "intentList.size() = " + intentList.size());

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(openInChooser);
    }

    private void readRecipeFromDatabase() {
        Cursor cursor;
        if (startupMode == MODE_NEW_RECIPE) {
            String selectQuery = "SELECT * FROM " + TABLE_LIST_RECIPE +
                    " WHERE rowid=last_insert_rowid()";
            cursor = mDatabase.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            idRecipe = cursor.getInt(0);
        } else cursor = mDatabase.query(TABLE_LIST_RECIPE, null, null, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(0) == idRecipe) {
                        titleRecipeFromDatabase = cursor.getString(1);
                        mEditTitleRecipe.setText(titleRecipeFromDatabase);
                        textRecipeFromDatabase = cursor.getString(2);
                        mEditTextRecipe.setText(textRecipeFromDatabase);
                        topFolder_id = cursor.getInt(3);
                        subFolder_id = cursor.getInt(5);
                        databaseImagePath = cursor.getString(6);
                        Log.d("TG", "databaseImagePath = " + databaseImagePath);
                        if (databaseImagePath != null && databaseImagePath != "") {
                            setImage(databaseImagePath);
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("TG", "Table with recipeCategory - is Empty");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.overrideActionBar(mContext.getString(R.string.text_recipe), null);
        MainActivity.hideAllFloatButtons();
        loadAds();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mScheduledTimeTimer != null) mScheduledTimeTimer.cancel();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (startupMode == MODE_NEW_RECIPE | startupMode == MODE_EDIT_RECIPE) {
            if (isChangesFromRecipe()) saveRecipe();
        }
        mDatabase.close();
//        dataBaseHelper.close();
        if (mScheduledTimeTimer != null) mScheduledTimeTimer.cancel();
    }

    /* If text or image was changed -> save recipe in mDatabase*/
    private boolean isChangesFromRecipe() {
        if (selectedImagePath != null && !selectedImagePath.equals(databaseImagePath)) {
            databaseImagePath = selectedImagePath;
            return true; // image was changed
        }
        if (mEditTitleRecipe.getText().length() > 0 & mEditTextRecipe.getText().length() > 0) {
            String tempTitle = mEditTitleRecipe.getText().toString();
            String tempText = mEditTextRecipe.getText().toString();

            if (tempTitle.equals(titleRecipeFromDatabase) &
                    tempText.equals(textRecipeFromDatabase)) {
                makeSnackbar(mContext.getString(R.string.there_is_no_change));
                return false;
            }
        } else if (mEditTitleRecipe.getText().length() == 0 & mEditTextRecipe.getText().length() == 0) {
            Log.d("TG", "editText's == 0");
            makeSnackbar(mContext.getString(R.string.nothing_was_entered));
            return false;
        }
        return true;
    }

    private void saveRecipe() {
        mContentValues = new ContentValues();
        String titleRecipe = getString(R.string.text_recipe_no_title);
        String textRecipe = getString(R.string.text_recipe_no_text);
        if (mEditTitleRecipe.getText().length() > 0)
            titleRecipe = mEditTitleRecipe.getText().toString();
        if (mEditTextRecipe.getText().length() > 0) textRecipe = mEditTextRecipe.getText().toString();

        mContentValues.put("recipe_title", titleRecipe);
        mContentValues.put("recipe", textRecipe);
        if (startupMode == MODE_NEW_RECIPE) {
            if (typeReceivedFolder == PARENT) {
                mContentValues.put("category_id", idReceivedFolderItem);
                mContentValues.put("sub_category_id", DEFAULT_VALUE_COLUMN);
                mContentValues.put("image", databaseImagePath);
            } else if (typeReceivedFolder == CHILD) {
                mContentValues.put("category_id", DEFAULT_VALUE_COLUMN);
                mContentValues.put("sub_category_id", idReceivedFolderItem);
                mContentValues.put("image", databaseImagePath);
            }
        } else {
            mContentValues.put("category_id", topFolder_id);
            mContentValues.put("sub_category_id", subFolder_id);
            mContentValues.put("image", databaseImagePath);
        }
        long rowId = 0;
        if (startupMode == MODE_NEW_RECIPE) { // if Added a new recipe -> call 'insert' method
            rowId = mDatabase.insert(TABLE_LIST_RECIPE, null, mContentValues);
        } else if (startupMode == MODE_EDIT_RECIPE) { // if edit existing recipe -> call 'update'
            rowId = mDatabase.update(TABLE_LIST_RECIPE, mContentValues, "_ID=" + idRecipe, null);
        }
        modeReview();
        if (rowId >= 0) makeSnackbar(mContext.getString(R.string.success));
        new Analytics(mContext).sendAnalytics("myCookBook", "Text Category", "Save recipe", titleRecipe);
    }


    private void makeSnackbar(String text) {
        try {
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } catch (NullPointerException npe) {
        }
    }

    /**
     * Invokes when user selected the permissions
     * This is format for use from fragment
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 3) {
            if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 3) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                        grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                        grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    Utilities.showOkDialog(mContext,
                            getResources().getString(R.string.permissions_error),
                            new Utilities.IYesNoCallback() {
                                @Override
                                public void onYes() {
                                }
                            });
                } else {
                    getPickImageIntent();
                }
            }
        }
    }
}