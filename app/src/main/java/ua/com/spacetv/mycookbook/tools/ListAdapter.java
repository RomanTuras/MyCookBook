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

package ua.com.spacetv.mycookbook.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.File;
import java.util.ArrayList;

import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.interfaces.Constants;

/**
 * Created by Roman Turas on 25/11/2015.
 */
public class ListAdapter extends BaseAdapter implements Constants {
    Context mContext;
    ArrayList<ListData> arrayData;
    LayoutInflater lInflater;
    private boolean isShowingDefaultImageRecipe = false;

    public ListAdapter(Context context, ArrayList<ListData> arrayData) {
        mContext = context;
        this.arrayData = arrayData;
        lInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrayData.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = lInflater.inflate(R.layout.format_list_view, parent, false);
        ListData listData = ((ListData) getItem(position));
        TextView textTitle = (TextView) view.findViewById(R.id.textTitle);
        textTitle.setText(listData.getListTitle());

        /** if any subcategory nof found -> get out TextView 'textSubTitle' */
        TextView textSubTitle = (TextView) view.findViewById(R.id.textSubTitle);
        String subTitle = listData.getListSubTitle();
        if(subTitle.equals(null)|subTitle.equals("")){
            textSubTitle.setVisibility(view.GONE);
        }else textSubTitle.setText(subTitle);

        /** don't show numbers of recipe if they is 0 */
        TextView textNumber = (TextView) view.findViewById(R.id.textNumber);
        int numberRecipe = listData.getNumber();
        if(numberRecipe == 0){
            textNumber.setVisibility(view.GONE);
        }else textNumber.setText(String.format("%d", numberRecipe));

        /** switch icons between folders and recipes */
        ImageView imageIcon = (ImageView) view.findViewById(R.id.imageIcon);
        if(listData.getImgIcon() == ID_IMG_FOLDER){
            imageIcon.setImageResource(R.drawable.ic_folder_open_black_24dp);
        }else if(listData.getImgIcon() == ID_IMG_RECIPE){
            if(isShowingDefaultImageRecipe) {
                imageIcon.setImageResource(R.drawable.ic_document_24);
            }else{
                String path = listData.getPathImage();
                if(path == null || path.equals("") || !ifFileFound(path)) {
                    imageIcon.setImageResource(R.drawable.ic_document_24);
                }else {
                    setImage(path, imageIcon);
                }
            }
        }

        /** if 'Like' not set on recipe -> disable show layoutImgLike */
        if(listData.getImgLike() == ID_IMG_LIKE_OFF) {
            LinearLayout layoutImgLike = (LinearLayout) view.findViewById(R.id.layoutImgLike);
            layoutImgLike.setVisibility(View.GONE);
        }

        return view;
    }

    /**
     * Checking whatever the file is present
     *
     * @param filename - name of the image
     * @return true, if exist
     */
    public boolean ifFileFound(String filename) {
        File file = new File(filename);
        return file.exists();
    }

    /**
     * Set image into image viev
     * Usage Glide library
     *
     * @param path
     */
    private void setImage(String path, final ImageView img) {
        Glide.with(mContext).load(path).asBitmap().centerCrop().into(new BitmapImageViewTarget(img) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                img.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

}