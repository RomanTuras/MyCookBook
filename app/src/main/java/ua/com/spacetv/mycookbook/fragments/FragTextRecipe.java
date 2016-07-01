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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ua.com.spacetv.mycookbook.MainActivity;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.google_services.Ads;
import ua.com.spacetv.mycookbook.google_services.Analytics;
import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;
import ua.com.spacetv.mycookbook.helpers.ImagePickHelper;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.Constants;
import ua.com.spacetv.mycookbook.tools.Utilities;

/**
 * Created by Roman Turas on 07/01/2016
 */
public class FragTextRecipe extends Fragment implements Constants,
        OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final int REQUEST_INTERNET = 1;
    private static Context mContext;
    private static FragmentManager fragmentManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
    public static DataBaseHelper dataBaseHelper;
    public static SQLiteDatabase database;
    private ContentValues contentValues;
    private static Ads ads;
    private static View view;
    private EditText editTitleRecipe, editTextRecipe;
    private TextView textTextRecipe;
    private static Intent chooseImageIntent;
    private static ImageView imageView;
    private static int idReceivedFolderItem = 0; // id of folder where was or will TEXT of RECIPE
    private static int idRecipe = 0; // id Received (mode REVIEW) or Just Created recipe (mode NEW)
    private static int typeReceivedFolder = 0; // Is two types of folders: PARENT=0 (top) & CHILD=1 (subFolder)
    private static int topFolder_id = 0; //id top folder received from database
    private static int subFolder_id = 0; //id sub folder received from database
    private static int startupMode = MODE_REVIEW_RECIPE;
    private static int displayWidth;
    private final int CHOOSE_IMAGE = 1;
    private String selectedImagePath;
    private String titleRecipeFromDatabase = null;
    private String textRecipeFromDatabase = null;
    private static String databaseImagePath;
    private static Fragment mFragmentTextRecipe;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragTextRecipe.mContext = context;
        FragTextRecipe.dataBaseHelper = new DataBaseHelper(context);
        this.contentValues = new ContentValues();
        mFragmentTextRecipe = new FragTextRecipe();

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
        if (ads.getInterstitialAd() == null) ads.initAds(); // init and preload Ads
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        view = inflater.inflate(R.layout.frag_text_recipe, null);
        editTitleRecipe = (EditText) view.findViewById(R.id.editTitleRecipe);
        editTextRecipe = (EditText) view.findViewById(R.id.editTextRecipe);
        textTextRecipe = (TextView) view.findViewById(R.id.textTextRecipe);
        imageView = (ImageView) view.findViewById(R.id.imageRecipe);

        getDisplayMetrics();

        ads = new Ads(mContext);

        database = dataBaseHelper.getWritableDatabase();
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

    private void modeNewRecipe() {
        Log.d("TG", "modeNewRecipe *** ");
        editTitleRecipe.setFocusableInTouchMode(true);
        editTitleRecipe.setFocusable(true);
        editTitleRecipe.requestFocus();
        editTextRecipe.setFocusableInTouchMode(true);
        editTextRecipe.setFocusable(true);
        setHasOptionsMenu(true);
    }

    private void modeEdit() {
        Log.d("TG", "modeEdit *** ");
        editTitleRecipe.setFocusableInTouchMode(true);
        editTitleRecipe.setFocusable(true);
        editTextRecipe.setFocusableInTouchMode(true);
        editTextRecipe.setFocusable(true);
        readRecipeFromDatabase();
        startupMode = MODE_EDIT_RECIPE;
        setHasOptionsMenu(false);
        setHasOptionsMenu(true);
    }

    private void modeReview() {
        new Analytics(mContext).sendAnalytics("myCookBook", "Text Category", "Review recipe", "nop");

        Log.d("TG", "modeReview *** ");
        editTitleRecipe.setFocusableInTouchMode(false);
        editTitleRecipe.setFocusable(false);
        editTextRecipe.setFocusableInTouchMode(false);
        editTextRecipe.setFocusable(false);
        readRecipeFromDatabase();
        startupMode = MODE_REVIEW_RECIPE;
        setHasOptionsMenu(false);
        setHasOptionsMenu(true);
    }

    private void getDisplayMetrics() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        display.getMetrics(metrics);
        displayWidth = metrics.widthPixels;
    }

    public void setImage(Bitmap bitmap) {
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
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (isChangesFromRecipe()) saveRecipe();
                break;
            case R.id.action_edit:
                modeEdit();
                break;
            case R.id.action_share:
                shareRecipe();
                break;
            case R.id.action_photo:
                //get permissions to access camera or gallery
                checkPermissions();
//                getPickImageIntent();
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
        chooseImageIntent = ImagePickHelper.getPickImageIntent(mContext);
        startActivityForResult(chooseImageIntent, CHOOSE_IMAGE);
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
                        bitmap = ImagePickHelper.decodeBitmapFromPath(selectedImagePath, displayWidth);
                        if (bitmap != null) setImage(bitmap);
//                        ImageLoader.getInstance().displayImage("file://" + selectedImagePath, imageView);
//                        imageLoader.loadImage("file://" + selectedImagePath, targetSize, this);
                        Log.d("TG", "width = " + bitmap.getWidth() + "  height = " + bitmap.getHeight());
                        Log.d("TG", "data!=null; getPath = " + selectedImagePath);
                        new Analytics(mContext).sendAnalytics("myCookBook", "Text Category", "Add Image", "Gallery");
                    } else { // ** Camera **
                        selectedImagePath = ImagePickHelper.getImageFromResult(resultCode, data);
                        ImagePickHelper.addImageToGallery(selectedImagePath);
                        bitmap = ImagePickHelper.decodeBitmapFromPath(selectedImagePath, displayWidth);
                        if (bitmap != null) setImage(bitmap);
//                        ImageLoader.getInstance().displayImage("file://" + selectedImagePath, imageView);
//                        imageLoader.loadImage("file://" + selectedImagePath, targetSize, this);
                        Log.d("TG", "width = " + bitmap.getWidth() + "  height = " + bitmap.getHeight());
                        Log.d("TG", "data==null; getPath = " + selectedImagePath);
                        new Analytics(mContext).sendAnalytics("myCookBook", "Text Category", "Add Image", "Camera");
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
        if (editTitleRecipe.getText().length() > 0 & editTextRecipe.getText().length() > 0) {
            String title = editTitleRecipe.getText().toString();
            String text = editTextRecipe.getText().toString();
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
        String title = editTitleRecipe.getText().toString();
        String text = editTextRecipe.getText().toString();
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
            cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            idRecipe = cursor.getInt(0);
        } else cursor = database.query(TABLE_LIST_RECIPE, null, null, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(0) == idRecipe) {
                        titleRecipeFromDatabase = cursor.getString(1);
                        editTitleRecipe.setText(titleRecipeFromDatabase);
                        Log.d("TG", "readRecipeFromDatabase title= " + titleRecipeFromDatabase);
                        textRecipeFromDatabase = cursor.getString(2);
                        editTextRecipe.setText(textRecipeFromDatabase);
                        topFolder_id = cursor.getInt(3);
                        subFolder_id = cursor.getInt(5);
                        databaseImagePath = cursor.getString(6);
                        if (databaseImagePath != null && databaseImagePath != "") {
                            Log.d("TG", "readRecipeFromDatabase -> databaseImagePath = " + databaseImagePath);
                            Bitmap bitmap = ImagePickHelper.decodeBitmapFromPath(databaseImagePath,
                                    displayWidth);
                            if (bitmap != null) {
                                setImage(bitmap);
                                Log.d("TG", "width = " + bitmap.getWidth() + "  height = " + bitmap.getHeight());
                            } else Log.d("TG", "Picture not found!");
//                            ImageLoader.getInstance().displayImage("file://" + databaseImagePath, imageView);
//                            imageLoader.loadImage("file://" + databaseImagePath, targetSize, this);
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
    public void onDetach() {
        super.onDetach();
        Log.d("TG", "FragTextRecipe is onDetach - show ADS -  ");
        if (startupMode == MODE_NEW_RECIPE | startupMode == MODE_EDIT_RECIPE) {
            if (isChangesFromRecipe()) saveRecipe();
        }
        database.close();
        dataBaseHelper.close();
        ads.showAd();
    }

    /* If text or image was changed -> save recipe in database*/
    private boolean isChangesFromRecipe() {
        if (selectedImagePath != null && !selectedImagePath.equals(databaseImagePath)) {
            databaseImagePath = selectedImagePath;
            return true; // image was changed
        }
        if (editTitleRecipe.getText().length() > 0 & editTextRecipe.getText().length() > 0) {
            String tempTitle = editTitleRecipe.getText().toString();
            String tempText = editTextRecipe.getText().toString();

            if (tempTitle.equals(titleRecipeFromDatabase) &
                    tempText.equals(textRecipeFromDatabase)) {
                makeSnackbar(mContext.getString(R.string.there_is_no_change));
                return false;
            }
        } else if (editTitleRecipe.getText().length() == 0 & editTextRecipe.getText().length() == 0) {
            Log.d("TG", "editText's == 0");
            makeSnackbar(mContext.getString(R.string.nothing_was_entered));
            return false;
        }
        return true;
    }

    private void saveRecipe() {
        contentValues = new ContentValues();
        String titleRecipe = getString(R.string.text_recipe_no_title);
        String textRecipe = getString(R.string.text_recipe_no_text);
        if (editTitleRecipe.getText().length() > 0)
            titleRecipe = editTitleRecipe.getText().toString();
        if (editTextRecipe.getText().length() > 0) textRecipe = editTextRecipe.getText().toString();

        contentValues.put("recipe_title", titleRecipe);
        contentValues.put("recipe", textRecipe);
        if (startupMode == MODE_NEW_RECIPE) {
            if (typeReceivedFolder == PARENT) {
                contentValues.put("category_id", idReceivedFolderItem);
                contentValues.put("sub_category_id", DEFAULT_VALUE_COLUMN);
                contentValues.put("image", databaseImagePath);
            } else if (typeReceivedFolder == CHILD) {
                contentValues.put("category_id", DEFAULT_VALUE_COLUMN);
                contentValues.put("sub_category_id", idReceivedFolderItem);
                contentValues.put("image", databaseImagePath);
            }
        } else {
            contentValues.put("category_id", topFolder_id);
            contentValues.put("sub_category_id", subFolder_id);
            contentValues.put("image", databaseImagePath);
        }
        long rowId = 0;
        if (startupMode == MODE_NEW_RECIPE) { // if Added a new recipe -> call 'insert' method
            rowId = database.insert(TABLE_LIST_RECIPE, null, contentValues);
        } else if (startupMode == MODE_EDIT_RECIPE) { // if edit existing recipe -> call 'update'
            rowId = database.update(TABLE_LIST_RECIPE, contentValues, "_ID=" + idRecipe, null);
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