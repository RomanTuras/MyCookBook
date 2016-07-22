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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.interfaces.Constants;

/**
 * Created by Roman Turas on 07/02/2016
 * It's creates options to pick images from different sources, like camera, gallery, photos e.t.c.
 */

public class ImagePickHelper implements Constants{
    private static final String TAG = "TG";
    private static final String JPEG_FILE_PREFIX = "cook";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static String imagePath = null;
    private static File tempFile = null;
    private static Context mContext;

    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;
        ImagePickHelper.mContext = context;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        tempFile = getTempFile();
        if (tempFile != null) {
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
            intentList = addIntentsToList(context, intentList, takePhotoIntent);
        }
        intentList = addIntentsToList(context, intentList, pickIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.dlg_pick_image_title));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    /** Get the list of all activities which can work with ACTION_PICK
     * @return List
     * */
    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
            Log.d(TAG, "Intent: " + intent.getAction() + " package: " + packageName);
        }
        return list;
    }

    public static String getImageFromResult(int resultCode, Intent imageReturnedIntent) {
        Log.d(TAG, "getImageFromResult, resultCode: " + resultCode);
        if (resultCode == Activity.RESULT_OK) {
            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null ||
                    imageReturnedIntent.getData().equals(Uri.fromFile(tempFile)));
            if (isCamera) {     /** CAMERA **/
                imagePath = getNewPathToImage(tempFile.getPath(), tempFile.getName());
                Log.d(TAG, "** CAMERA **, imagePath: " + imagePath);
                Log.d(TAG, "** CAMERA **, tempFile: " + tempFile.getName());
                return imagePath;
            } else {            /** ALBUM **/
                imagePath = imageReturnedIntent.getData().getPath();
                Log.d(TAG, "** ALBUM ** -> imagePath: " + imagePath);
            }
        }
        return imagePath;
    }

    /**
     * Moved file from default DCIM folder into myCookBook/images folder
     * @param path - full path to image
     * @param filename - image filename
     * @return new path + filename
     */
    private static String getNewPathToImage(String path, String filename) {
        boolean isFileCopied = false;
        try {
            isFileCopied = copyFileToImageFolder(path, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isFileCopied){
            deleteFile(path);
            return getCookBookPath() + filename;
        }else return null;
    }

    /**
     * Copied file from default DCIM camera folder into myCookBook/images folder
     *
     * @param sourcePath - full path to source file
     * @param filename - destination Filename
     * @throws IOException
     */
    public static boolean copyFileToImageFolder(String sourcePath, String filename) throws IOException {
        File src = new File(sourcePath);
        if (!src.exists()) return false; //source file not found

        File imageFolder = new File(getCookBookPath());
        if(!imageFolder.exists()) imageFolder.mkdir();
        File dst = new File(getCookBookPath() + filename);

        Log.d("TG", "dst file is - " + dst.toString());

        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        return dst.exists();
    }

    /**
     * Deleting file from default DCIM folder
     *
     * @param filename - filename
     */
    private static void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists()) file.delete();
    }


    /**
     * Registered image in to gallery
     */
    public static void addImageToGallery(String filePath) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /** Get the temporary file to obtain possible to save picture from camera
     * @return File
     * */
    private static File getTempFile() {
        String sdState = Environment.getExternalStorageState();
        File fileBitmap = null;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                fileBitmap = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("TG", "ImagePickHelper -> SD card not found or was unmounted ");
        }
        return fileBitmap;
    }

    /** Get the path to DCIM directory from external storage
     * @return File
     * */
    private static File getPath() {
        String pathDcim = android.os.Environment.DIRECTORY_DCIM;
        String sdState = Environment.getExternalStorageState();
        String path;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += "/" + pathDcim + "/";
            return new File(path);
        }
        return null;
    }

    /**
     * Getting path to myCookBook storage directory
     *
     * @return path, if SD card available, else - null
     */
    public static String getCookBookPath() {
        String sdState = Environment.getExternalStorageState();
        String path = null;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += "/" + IMAGE_FOLDER_NAME + "/";
        }
        return path;
    }

    /** Created a File with unique filename
     * @return File
     * @throws IOException
     * */
    private static File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,
                JPEG_FILE_SUFFIX,
                getPath()
        );
        Log.d("TG", "ImagePickHelper -> path = " + image.getAbsolutePath());
        return image;
    }

    /** Decoded and resize bitmap, according from display width
     * @param path String, path to bitmap
     * @param displayWidth int, width of display in pixels
     * @return Bitmap
     * */
    public static Bitmap decodeBitmapFromPath(String path, int displayWidth) {
        File tempFile = new File(path);
        if(!tempFile.exists()) return null; // if file not found (was deleted or SD unmount)

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int bmpWidth = bitmap.getWidth();
        options.inSampleSize = calculateRatio(bmpWidth, displayWidth);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /** Calculated a ratio for image reduction
     * @param bitmapWidth - width of loaded bitmap
     * @param displayWidth - int, width of display in pixels
     * @return int */
    public static int calculateRatio(int bitmapWidth, int displayWidth) {
        float ratio = 1;
        if(bitmapWidth > displayWidth) {
            ratio = Math.round((float) bitmapWidth / (float) displayWidth);
        }
        Log.d("TG", "ratio = " + ratio);
        return (int) ratio;
    }

    public static Bitmap decodeScaledBitmapFromPath(String path, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) return null; // image was deleted from SD
        float wBmp = bitmap.getWidth();
        float hBmp = bitmap.getHeight();
        float scale = wBmp / reqWidth;
        if (scale > 1) {
            wBmp = wBmp / scale;
            hBmp = hBmp / scale;
        }
        options.inJustDecodeBounds = false;
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) wBmp, (int) hBmp, false);
        return bitmap;
    }

}