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

package ua.com.spacetv.mycookbook.google_services;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ua.com.spacetv.mycookbook.tools.Constants;

/**
 * Created by salden on 29/12/2015.
 * Google analytics service
 */
public class Analytics implements Constants {
    private Context context;

    public Analytics(Context context){
        this.context = context;
    }

    public void sendAnalytics(String screenName, String category, String action, String label)
    {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        analytics.setLocalDispatchPeriod(1800);
        Tracker tracker = analytics.newTracker(TRACK_ID);
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category).setAction(action)
                .setLabel(label).build());
    }
}
