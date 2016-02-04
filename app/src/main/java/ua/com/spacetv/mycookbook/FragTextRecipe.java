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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import java.io.File;
import java.io.IOException;

import ua.com.spacetv.mycookbook.google_services.Ads;
import ua.com.spacetv.mycookbook.google_services.Analytics;
import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;
import ua.com.spacetv.mycookbook.helpers.ImagePicker;
import ua.com.spacetv.mycookbook.helpers.PickImageDialog;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by Roman Turas on 07/01/2016.
 */
public class FragTextRecipe extends Fragment implements StaticFields {

    private static Context context;
    private static FragmentManager fragmentManager;
    private static OnFragmentEventsListener onFragmentEventsListener;
    public static DataBaseHelper dataBaseHelper;
    public static SQLiteDatabase database;
    private ContentValues contentValues;
    private static Ads ads;
    private static View view;
    private EditText editTitleRecipe, editTextRecipe;
    private TextView textTextRecipe;
    private String titleRecipeFromDatabase = null;
    private String textRecipeFromDatabase = null;
    private static int idReceivedFolderItem = 0; // id of folder where was or will TEXT of RECIPE
    private static int idRecipe = 0; // id Received (mode REVIEW) or Just Created recipe (mode NEW)
    private static int typeReceivedFolder = 0; // Is two types of folders: PARENT=0 (top) & CHILD=1 (subFolder)
    private static int topFolder_id = 0; //id top folder received from database
    private static int subFolder_id = 0; //id sub folder received from database
    private static int startupMode = MODE_REVIEW_RECIPE;
    private static ImageView imageView;
    private String selectedImagePath = null;
    private static Intent chooseImageIntent;
    private Uri picUri;
    private String path;
    private static String databaseImagePath = null;
    private File storageDir;
    private static File fileBitmap;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragTextRecipe.context = context;
        FragTextRecipe.dataBaseHelper = new DataBaseHelper(context);
        this.contentValues = new ContentValues();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            idReceivedFolderItem = bundle.getInt(TAG_PARENT_ITEM_ID, DEFAULT_VALUE_COLUMN);
            idRecipe = bundle.getInt(TAG_ID_RECIPE);
            typeReceivedFolder = bundle.getInt(TAG_TYPE_FOLDER);
            startupMode = bundle.getInt(TAG_MODE);
            Log.d("TG", "TextRecipe: idRecipe = " + idRecipe + " idReceivedFolderItem= " + idReceivedFolderItem + " typeReceivedFolder= " + typeReceivedFolder + " startupMode= " + startupMode);
        }
        onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
    }

    private void loadAds() {
        ads = new Ads(context);
        ads.initAds(); // init and preload Ads
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        view = inflater.inflate(R.layout.frag_text_recipe, null);
        editTitleRecipe = (EditText) view.findViewById(R.id.editTitleRecipe);
        editTextRecipe = (EditText) view.findViewById(R.id.editTextRecipe);
        textTextRecipe = (TextView) view.findViewById(R.id.textTextRecipe);
        imageView = (ImageView) view.findViewById(R.id.imageRecipe);

        database = dataBaseHelper.getWritableDatabase();
        fragmentManager = getFragmentManager();
//        FragTextRecipe.view = view;

        if (startupMode == MODE_EDIT_RECIPE) modeEdit();
        else if (startupMode == MODE_REVIEW_RECIPE) modeReview();
        else if (startupMode == MODE_NEW_RECIPE) modeNewRecipe();

//        setImage(R.id.imageRecipe, "img.jpg");
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if (startupMode == MODE_REVIEW_RECIPE) inflater.inflate(R.menu.menu_review_recipe, menu);
        else if (startupMode == MODE_EDIT_RECIPE |
                startupMode == MODE_NEW_RECIPE) inflater.inflate(R.menu.menu_edit_recipe, menu);
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
        new Analytics(context).sendAnalytics("myCookBook", "Text Category", "Review recipe", "nop");

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

    public void setImage(File pathToBitmap) {
//        Bitmap bitmap = getBitmapFromAsset("img.jpg");
        Bitmap bitmap = getBitmapFromFile(pathToBitmap);


        bitmap = getRoundedCornerBitmap(bitmap, 30);
        imageView.setImageBitmap(bitmap);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        display.getMetrics(metrics);

        int width = metrics.widthPixels;
        int picWidth = bitmap.getWidth();
        int picHeight = bitmap.getHeight();
        if (picWidth > width) {
            float scale = (float) picWidth / (float) width;
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

    private static Bitmap getBitmapFromFile(File pathToBitmap) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(pathToBitmap.getAbsolutePath(), bmOptions);
        Log.d("TG", "getBitmapFromFile bitmap = "+bitmap);

        bitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
        return bitmap;
    }

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
//                showPickImageDialog();
                getPickImageIntent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPickImageIntent() {
        chooseImageIntent = ImagePicker.getPickImageIntent(context);
        startActivityForResult(chooseImageIntent, 1);
    }



    public void showPickImageDialog() {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_PICK_IMG_DIALOG);
        if (fragment != null) {
            ft.remove(fragment);
        }
        ft.addToBackStack(null);

        DialogFragment dialogFragment = new PickImageDialog();
        dialogFragment.show(fragmentManager, TAG_PICK_IMG_DIALOG);
    }

    public void onPickImage() {
        int PICK_IMAGE_ID = 1;
        try {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, 1);


//            chooseImageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//            chooseImageIntent = ImagePicker.getPickImageIntent(context);
//
//            fileBitmap = null;
//            try {
//                fileBitmap = createImageFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (fileBitmap != null) {
//                chooseImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileBitmap));
//                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
//            }
//
        } catch (ActivityNotFoundException anfe) {
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            makeSnackbar(errorMessage);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            Log.d("TG", "onActivityResult ");

            switch (requestCode) {
                case 234:
//                    selectedImagePath = getAbsolutePath(data.getData());
//                    Bitmap bitmap = ImagePicker.getImageFromResult(context, resultCode, data);
                    Bitmap bitmap;
                    if (data != null) {
                        Log.d("TG", "data != null");
                        if (data.hasExtra("data")) {
                            bitmap = data.getParcelableExtra("data");
                            bitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false);
//                            setImage(bitmap);
                            // TODO Какие-то действия с миниатюрой
                        }
                    } else {
                        Log.d("TG", "data == null");
//                        bitmap = getBitmapFromFile(fileBitmap);
                        setImage(fileBitmap);
                    }
                    Log.d("TG", "data = " + data);
                    break;
                default:

                    break;
                case 1:
                    if(data!=null) {
                        selectedImagePath = getDataColumn(data.getData());
                        Log.d("TG", "getPath = " + selectedImagePath);
                        setImage(new File(selectedImagePath));
//                        setPathToImage(selectedImagePath);
                    }else{
                        bitmap = ImagePicker.getImageFromResult(context, resultCode, data);
//                        imageView.setImageBitmap(bitmap);
                        selectedImagePath = ImagePicker.getImagePath();
                        Log.d("TG", "getPath = " + selectedImagePath);
                        setImage(new File(selectedImagePath));
//                        setPathToImage(selectedImagePath);
                    }
                    break;
            }
        }
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
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
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
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

    private File createImageFile() throws IOException {
        storageDir = new File(getPath());
        // Create an image file name
        Log.d("TG", "createImageFile");
//        String timeStamp =
//                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File image = File.createTempFile(
                "asd",
                ".jpg",
                storageDir
        );

        String mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("TG", "mCurrentPhotoPath = " + mCurrentPhotoPath);
        return image;
    }

    private String getPath() {
        String pathDcim = android.os.Environment.DIRECTORY_DCIM;
        String sdState = Environment.getExternalStorageState();
        String path = null;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += "/" + pathDcim + "/";
        }
        Log.d("TG", "path = " + path);
        return path;
    }

    private void shareRecipe() {
        if (editTitleRecipe.getText().length() > 0 & editTextRecipe.getText().length() > 0) {
            String title = editTitleRecipe.getText().toString();
            String text = editTextRecipe.getText().toString();
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);

            emailIntent.setType("text/plain");
            startActivity(Intent.createChooser(emailIntent, context
                    .getString(R.string.text_share_recipe)));
        }
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
                        if(databaseImagePath != null && databaseImagePath != ""){
                            Log.d("TG", "databaseImagePath = "+ databaseImagePath);
                            setImage(new File(databaseImagePath));
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.overrideActionBar(context.getString(R.string.text_recipe), null);
        MainActivity.hideAllFloatButtons();
        loadAds();
    }



    @Override
    public void onStop() {
        super.onStop();
        Log.d("TG", "FragTextRecipe is onStop - startupMode= " + startupMode);
        if (startupMode == MODE_NEW_RECIPE | startupMode == MODE_EDIT_RECIPE) {
            if (isChangesFromRecipe()) saveRecipe();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("TG", "FragTextRecipe is onDetach - show ADS -  ");
        database.close();
        dataBaseHelper.close();
        ads.showAd();
    }

    /* If at least one from two editText contains any text -> save recipe in database*/
    private boolean isChangesFromRecipe() {
        if(selectedImagePath != null && !selectedImagePath.equals(databaseImagePath)){
            return true; // image was changed
        }
        if (editTitleRecipe.getText().length() > 0 & editTextRecipe.getText().length() > 0) {
            String tempTitle = editTitleRecipe.getText().toString();
            String tempText = editTextRecipe.getText().toString();

            if (tempTitle.equals(titleRecipeFromDatabase) &
                    tempText.equals(textRecipeFromDatabase)) {
                makeSnackbar(context.getString(R.string.there_is_no_change));
                return false;
            }
        } else if (editTitleRecipe.getText().length() == 0 & editTextRecipe.getText().length() == 0) {
            Log.d("TG", "editText's == 0");
            makeSnackbar(context.getString(R.string.nothing_was_entered));
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
                contentValues.put("image", selectedImagePath);
            } else if (typeReceivedFolder == CHILD) {
                contentValues.put("category_id", DEFAULT_VALUE_COLUMN);
                contentValues.put("sub_category_id", idReceivedFolderItem);
                contentValues.put("image", selectedImagePath);
            }
        } else {
            contentValues.put("category_id", topFolder_id);
            contentValues.put("sub_category_id", subFolder_id);
            contentValues.put("image", selectedImagePath);
        }
        long rowId = 0;
        if (startupMode == MODE_NEW_RECIPE) { // if Added a new recipe -> call 'insert' method
            rowId = database.insert(TABLE_LIST_RECIPE, null, contentValues);
        } else if (startupMode == MODE_EDIT_RECIPE) { // if edit existing recipe -> call 'update'
            rowId = database.update(TABLE_LIST_RECIPE, contentValues, "_ID=" + idRecipe, null);
        }
        modeReview();
        if (rowId >= 0) makeSnackbar(context.getString(R.string.success));
        new Analytics(context).sendAnalytics("myCookBook", "Text Category", "Save recipe", titleRecipe);
    }

    private void makeSnackbar(String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
