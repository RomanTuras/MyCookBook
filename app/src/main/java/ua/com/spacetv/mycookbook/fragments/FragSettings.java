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

package ua.com.spacetv.mycookbook.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import ua.com.spacetv.mycookbook.MainActivity;
import ua.com.spacetv.mycookbook.R;
import ua.com.spacetv.mycookbook.interfaces.Constants;
import ua.com.spacetv.mycookbook.tools.Preferences;

/**
 * Settings
 * - selecting color theme
 */
public class FragSettings extends Fragment implements Constants, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
    private Spinner mSpinnerThemes;
    private Context mContext;
    private boolean isBackgroundWhite; //key is background white
    private CheckBox mCheckBox;
    private int mColorBackground;

    // TODO set checkbox from preferences !!!!

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings, null);

        mSpinnerThemes = (Spinner) view.findViewById(R.id.spinner_themes);
        int numberOfTheme = Preferences.getSettingsFromPreferences(mContext, COLOR_THEME);
        mSpinnerThemes.setSelection(numberOfTheme);
        mSpinnerThemes.setOnItemSelectedListener(this);
        mCheckBox = (CheckBox) view.findViewById(R.id.check_white_background);
        mCheckBox.setOnCheckedChangeListener(this);
        isBackgroundWhite = Preferences.getSettingsFromPreferences(mContext, IS_BACKGROUND_WHITE, 0);
        mCheckBox.setChecked(isBackgroundWhite);


        //getting settings of background
        isBackgroundWhite = Preferences.getSettingsFromPreferences(mContext, IS_BACKGROUND_WHITE, 0);

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.overrideActionBar(mContext.getString(R.string.action_settings), null);
        MainActivity.hideAllFloatButtons();
    }

    /**
     * Saving settings to preferences when fragment onPause
     */
    @Override
    public void onPause() {
        super.onPause();
        int numberOfTheme = mSpinnerThemes.getSelectedItemPosition();
        Preferences.setSettingsToPreferences(mContext, COLOR_THEME, numberOfTheme);
        Preferences.setSettingsToPreferences(mContext, IS_BACKGROUND_WHITE, isBackgroundWhite);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int colorPrimaryDark = R.color.colorPrimaryDark;
        int colorPrimary = R.color.colorPrimary;
        int colorPrimaryBackground = R.color.colorPrimaryBackground;
        int colorTheme = R.style.IndigoTheme;

        switch (position) {
            case TEAL:
                colorPrimaryDark = R.color.tealColorPrimaryDark;
                colorPrimary = R.color.tealColorPrimary;
                colorPrimaryBackground = R.color.tealColorPrimaryBackground;
                colorTheme = R.style.TealTheme;
                break;
            case GREEN:
                colorPrimaryDark = R.color.greenColorPrimaryDark;
                colorPrimary = R.color.greenColorPrimary;
                colorPrimaryBackground = R.color.greenColorPrimaryBackground;
                colorTheme = R.style.GreenTheme;
                break;
            case LIME:
                colorPrimaryDark = R.color.limeColorPrimaryDark;
                colorPrimary = R.color.limeColorPrimary;
                colorPrimaryBackground = R.color.limeColorPrimaryBackground;
                colorTheme = R.style.LimeTheme;
                break;
            case ORANGE:
                colorPrimaryDark = R.color.orangeColorPrimaryDark;
                colorPrimary = R.color.orangeColorPrimary;
                colorPrimaryBackground = R.color.orangeColorPrimaryBackground;
                colorTheme = R.style.OrangeTheme;
                break;
        }
        int colorStatusBar = ContextCompat.getColor(mContext, colorPrimaryDark);
        int colorToolbar = ContextCompat.getColor(mContext, colorPrimary);
        mColorBackground = ContextCompat.getColor(mContext, colorPrimaryBackground);
        MainActivity.mHeaderNavigationDrawerLayout.setBackgroundColor(colorToolbar);
        getActivity().setTheme(colorTheme);

        if (Build.VERSION.SDK_INT >= 21) {
            //Getting status bar
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(colorStatusBar);
        }
        MainActivity.mToolbar.setBackgroundColor(colorToolbar);
        if (MainActivity.mFrameLayout != null)
            if (isBackgroundWhite) {
                MainActivity.mFrameLayout.setBackgroundColor(Color.WHITE);
            } else MainActivity.mFrameLayout.setBackgroundColor(mColorBackground);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            isBackgroundWhite = true;
            MainActivity.mFrameLayout.setBackgroundColor(Color.WHITE);
        } else {
            isBackgroundWhite = false;
            MainActivity.mFrameLayout.setBackgroundColor(mColorBackground);
        }
    }
}


//Getting color from current theme
//            TypedValue typedValue = new TypedValue();
//            mContext.getTheme().resolveAttribute(android.R.attr.colorBackground, typedValue, true);
//            int color = typedValue.data;