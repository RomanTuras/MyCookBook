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
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import ua.com.spacetv.mycookbook.interfaces.Constants;

/**
 * Created by salden on 14/12/2015.
 * Load and showing ADS block in any place
 */

public class Ads extends AdListener implements Constants {

    private InterstitialAd interstitialAd = null;
    private Context context;
    private boolean isAdLoaded;

    public Ads(Context context) {
        this.context = context;
        this.isAdLoaded = false;
    }

    public void initAds() {
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(AD_UNIT_ID);

        AdRequest adRequest;
        if (isDebugModeOn) {
            adRequest = new com.google.android.gms.ads.AdRequest.Builder()
                    .addTestDevice(TEST_DEVICE).build();
        } else {
            adRequest = new AdRequest.Builder().build();
        }

        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(this);
    }

    @Override
    public void onAdLoaded() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            this.isAdLoaded = true;
        }
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        Log.d("TG", "ADS not loaded errorCode=" + errorCode);
//        super.onAdFailedToLoad(errorCode);
    }

    public InterstitialAd getInterstitialAd(){
        return interstitialAd;
    }

    public void showAd(){
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    @Override
    public void onAdClosed() {    }
}
