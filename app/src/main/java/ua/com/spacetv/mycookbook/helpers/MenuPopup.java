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

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuItem;
import com.shehabic.droppy.DroppyMenuPopup;

import ua.com.spacetv.mycookbook.FragTopCategory;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.tools.OnPopupMenuItemClickListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by salden on 04/01/2016.
 */
public class MenuPopup implements StaticFields, DroppyClickCallbackInterface {
    private OnPopupMenuItemClickListener onPopupMenuItemClickListener;
    private FragTopCategory fragTopCategory = new FragTopCategory();
    private int idPopupItem = ID_POPUP_ITEM_CANCEL;
    private int idList;

    public MenuPopup(Context context, View view, int idList) {
        try {
            onPopupMenuItemClickListener = fragTopCategory;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragTopCategory.toString()
                    + " must implement onPopupMenuItemClickListener");
        }
        Resources resources = context.getResources();
        DroppyMenuPopup droppyMenu;
        this.idList = idList;
        DroppyMenuPopup.Builder droppyBuilder = new DroppyMenuPopup.Builder(context, view);
        String item_rename = resources.getString(R.string.item_rename);
        String item_delete = resources.getString(R.string.item_delete);
        String item_move = resources.getString(R.string.item_move);
        String item_favorite = resources.getString(R.string.item_favorite);

        /** Build menu items depends from what is actual List now */
        droppyBuilder.addMenuItem(new DroppyMenuItem(item_rename,
                R.drawable.ic_mode_edit_black_18dp));
        droppyBuilder.addSeparator();
        if(idList == ID_TABLE_LIST_RECIPE){
            droppyBuilder.addMenuItem(new DroppyMenuItem(item_favorite,
                    R.drawable.ic_favorite_black_18dp));
            droppyBuilder.addSeparator();
            droppyBuilder.addMenuItem(new DroppyMenuItem(item_move,
                    R.drawable.ic_swap_horiz_black_18dp));
            droppyBuilder.addSeparator();
        }
        droppyBuilder.addMenuItem(new DroppyMenuItem(item_delete, R.drawable.ic_delete_black_18dp));

        droppyBuilder.triggerOnAnchorClick(false);
        droppyBuilder.setOnClick(this);
        droppyMenu = droppyBuilder.build();
        droppyMenu.show();
    }

    @Override
    public void call(View v, int id) {
        switch (idList){
            case ID_TABLE_TOP_CATEGORY:
                if(id == 0) idPopupItem = ID_POPUP_ITEM_REN;
                else idPopupItem = ID_POPUP_ITEM_DEL;
                break;
            case ID_TABLE_SUB_CATEGORY:
                if(id == 0) idPopupItem = ID_POPUP_ITEM_REN;
                else idPopupItem = ID_POPUP_ITEM_DEL;
                break;
            case ID_TABLE_LIST_RECIPE:
                if(id == 0) idPopupItem = ID_POPUP_ITEM_REN;
                else if(id == 1) idPopupItem = ID_POPUP_ITEM_FAV;
                else if(id == 2) idPopupItem = ID_POPUP_ITEM_MOV;
                else idPopupItem = ID_POPUP_ITEM_DEL;
                break;
            default: idPopupItem = ID_POPUP_ITEM_CANCEL;
        }
        onPopupMenuItemClickListener.onPopupMenuItemClick(idPopupItem);
    }
}
