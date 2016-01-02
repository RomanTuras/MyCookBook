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

/**
 * Created by Roman Turas on 25/11/2015.
 * Container of information for fields in CardView on main screen
 */
public class ListData {
    private String listTitle;
    private String listSubTitle;
    private int imgIcon;
    private int imgLike;
    private int number;

    public ListData(String listTitle, String listSubTitle,
                    int imgIcon, int imgLike, int number){
        this.listTitle = listTitle;
        this.listSubTitle = listSubTitle;
        this.imgIcon = imgIcon;
        this.imgLike = imgLike;
        this.number = number;
    }

    public String getListTitle() {
        return listTitle;
    }

    public String getListSubTitle() {
        return listSubTitle;
    }

    public int getImgIcon() {
        return imgIcon;
    }

    public int getImgLike() {
        return imgLike;
    }

    public int getNumber() {
        return number;
    }
}
