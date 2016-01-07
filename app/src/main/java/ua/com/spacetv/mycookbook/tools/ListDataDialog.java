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
 * Created by Roman Turas on 06/01/2016.
 * Container of information for list in dialog
 */
public class ListDataDialog {
    private String listTitle;
    private String listSubTitle;
    private int item_id;
    private boolean isRoot;

    public ListDataDialog(String listTitle, String listSubTitle, int item_id, boolean isRoot){
        this.listTitle = listTitle;
        this.listSubTitle = listSubTitle;
        this.item_id = item_id;
        this.isRoot = isRoot;
    }

    public String getListTitle() {
        return listTitle;
    }

    public String getListSubTitle() {
        return listSubTitle;
    }

    public int getItemId() { return item_id; }

    public boolean getHierarchy(){
        return isRoot;
    }
}
