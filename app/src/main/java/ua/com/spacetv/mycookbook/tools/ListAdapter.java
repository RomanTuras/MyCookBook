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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.spacetv.mycookbook.R;

/**
 * Created by Roman Turas on 25/11/2015.
 * Class CardsAdapter for adding content to Main Activity
 * It working with CardView
 */
public class ListAdapter extends BaseAdapter implements StaticFields {
    Context cont;
    ArrayList<ListData> arrayData;
    LayoutInflater lInflater;

    public ListAdapter(Context context, ArrayList<ListData> arrayData) {
        cont = context;
        this.arrayData = arrayData;
        lInflater = (LayoutInflater) cont
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
            imageIcon.setImageResource(R.drawable.ic_content_paste_black_24dp);
        }

        /** if 'Like' not set on recipe -> disable show layoutImgLike */
        if(listData.getImgLike() == ID_IMG_LIKE_OFF) {
            LinearLayout layoutImgLike = (LinearLayout) view.findViewById(R.id.layoutImgLike);
            layoutImgLike.setVisibility(View.GONE);
        }

        return view;
    }

}