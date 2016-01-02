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

import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Roman Turas on 09/12/2015.
 * Assigns values depending from display resolution.
 */
public class DisplayMetrics {

    WindowManager windowManager;
    private int widthImage;
    private int widthImageArrow;

    private int widthImageIndicatolLevel;
    private int widthImageIndicatorFrequency;
    private int padding;
    private float elevation;
    private float widthDisplay;
    private float heightDisplay;
    private float dpiDisplay;
    private float sizeTextH1;
    private float sizeTextH2;
    private float sizeTextH3;
    private float sizeTextH4;
    private float sizeTextH6;

    public DisplayMetrics(WindowManager windowManager){
        this.windowManager = windowManager;

        Display display = windowManager.getDefaultDisplay();
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        display.getMetrics(metrics);

        widthDisplay = metrics.widthPixels;
        heightDisplay = metrics.heightPixels;
        dpiDisplay = metrics.densityDpi;
        sizeTextH1 = (widthDisplay/15)*(160/dpiDisplay);
        sizeTextH2 = (widthDisplay/18)*(160/dpiDisplay);
        sizeTextH3 = (widthDisplay/22)*(160/dpiDisplay);
        sizeTextH4 = (widthDisplay/28)*(160/dpiDisplay);
        sizeTextH6 = (widthDisplay/34)*(160/dpiDisplay);
        widthImage = (int)((widthDisplay/5));
        widthImageIndicatolLevel = (int)((widthDisplay/10));
        widthImageIndicatorFrequency = (int)((widthDisplay/20));
        widthImageArrow = (int)((widthDisplay/15));

        padding = (int)((widthDisplay/48));
        elevation = (widthDisplay/24);
    }

    public int getPadding(){
        return this.padding;
    }

    public float getElevation(){
        return this.elevation;
    }

    public float getSizeTextH1(){
        return this.sizeTextH1;
    }

    public float getSizeTextH2() {
        return sizeTextH2;
    }

    public float getSizeTextH3() {
        return sizeTextH3;
    }

    public float getSizeTextH4(){
        return this.sizeTextH4;
    }

    public float getSizeTextH6() {
        return sizeTextH6;
    }

    public int getWidthImage(){
        return widthImage;
    }

    public int getWidthImageIndicatolLevel() {
        return widthImageIndicatolLevel;
    }

    public int getWidthImageIndicatorFrequency() {
        return widthImageIndicatorFrequency;
    }

    public int getWidthImageArrow(){
        return widthImageArrow;
    }

    public float getWidthDisplay(){
        return widthDisplay;
    }

    public float getHeightDisplay(){
        return heightDisplay;
    }

    public float getDpiDisplay(){
        return dpiDisplay;
    }
}
