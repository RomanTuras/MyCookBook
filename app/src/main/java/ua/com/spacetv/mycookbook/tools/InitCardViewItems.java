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
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;

/**
 * Created by Roman Turas on 10/12/2015.
 * Fill arrayList with data required to Cards from Main Screen
 */
public class InitCardViewItems implements StaticFields{
    private Context context;
    private DataBaseHelper dataBaseHelper;
    private SQLiteDatabase database;
    private ArrayList<ListData> cardViewData;
    private SimpleDateFormat sdf;
    private Date objectDate;
    private String todayDate;
    private Calendar calendar;
    private Resources resources;

    public InitCardViewItems(Context context) {
        this.context = context;
        dataBaseHelper = new DataBaseHelper(context);
        sdf = new SimpleDateFormat("d.MM.yyyy");
        objectDate = new Date();
        todayDate = sdf.format(objectDate);
        calendar = Calendar.getInstance();
        resources = context.getResources();
        cardViewData = new ArrayList<>();
        initCardViewItems();
    }

    public void initCardViewItems() {
        initFirstCard();
    }

    /** Fill a first card (Card About) */
    private void initFirstCard() {
//        cardViewData.add(new ListData(resources.getString(R.string.titleAbout),
//                resources.getString(R.string.descriptionAbout),
//                resources.getString(R.string.textIndicatorAbout),
//                null,
//                R.drawable.ic_help_outline_black_24dp,
//                0,
//                0));
    }

    public ArrayList<ListData> getArrayList() {
        return cardViewData;
    }
}
