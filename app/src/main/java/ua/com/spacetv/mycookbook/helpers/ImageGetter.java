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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.spacetv.mycookbook.R;

/**
 * Created by Roman Turas on 07/02/2016
 * It creates options to pick images from different sources, like camera, gallery, photos e.t.c.
 * */

public class ImageGetter {
    private static final String TAG = "TG";
    private static final String JPEG_FILE_PREFIX = "cook";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static String imagePath = null;
    private static File tempFile = null;

    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        tempFile = getTempFile();
        if(tempFile != null) {
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

    public static String getImageFromResult(int resultCode,
                                            Intent imageReturnedIntent) {
        Log.d(TAG, "getImageFromResult, resultCode: " + resultCode);
        if (resultCode == Activity.RESULT_OK) {
            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null  ||
                    imageReturnedIntent.getData().equals(Uri.fromFile(tempFile)));
            if (isCamera) {     /** CAMERA **/
                imagePath = tempFile.getPath();
                Log.d(TAG, "** CAMERA **, imagePath: " + imagePath);
                return imagePath;
            } else {            /** ALBUM **/
                imagePath = imageReturnedIntent.getData().getPath();
                Log.d(TAG, "** ALBUM ** -> imagePath: " + imagePath);}
        }
        return imagePath;
    }

    private static File getTempFile() {
        String sdState = Environment.getExternalStorageState();
        File fileBitmap = null;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                fileBitmap = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Log.d("TG", "ImageGetter -> SD card not found or was unmounted ");
        }
        return fileBitmap;
    }

    private static File getPath() {
        String pathDcim = android.os.Environment.DIRECTORY_DCIM;
        String sdState = Environment.getExternalStorageState();
        String path = null;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += "/" + pathDcim + "/";
        }
        return new File(path);
    }

    private static File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,
                JPEG_FILE_SUFFIX,
                getPath()
        );
        Log.d("TG", "ImageGetter -> path = " + image.getAbsolutePath());
        return image;
    }

    public static Bitmap decodeBitmapFromPath(String path, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap == null) return null; // image was deleted from SD
        float wBmp = bitmap.getWidth();
        float hBmp = bitmap.getHeight();
        float scale = wBmp/reqWidth;
        if(scale > 1){
            wBmp = wBmp/scale;
            hBmp = hBmp/scale;
        }
        options.inJustDecodeBounds = false;
        bitmap = Bitmap.createScaledBitmap(bitmap, (int)wBmp, (int)hBmp, false);
        return bitmap;
    }

}