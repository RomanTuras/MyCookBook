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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings, null);

        mSpinnerThemes = (Spinner) view.findViewById(R.id.spinner_themes);
        //getting settings of current theme
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

    /**
     * Set title to actionbar, hide all float buttons
     */
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

    /**
     * Application of the chosen theme on the fly
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //set default INDIGO theme
        int colorPrimaryDark = R.color.colorPrimaryDark; //status bar
        int colorPrimary = R.color.colorPrimary; //tool bar
        int colorPrimaryBackground = R.color.colorPrimaryBackground; //main background
        int colorTheme = R.style.IndigoTheme; //name of color theme

        switch (position) {
            case PINK:
                colorPrimaryDark = R.color.pinkColorPrimaryDark;
                colorPrimary = R.color.pinkColorPrimary;
                colorPrimaryBackground = R.color.pinkColorPrimaryBackground;
                colorTheme = R.style.PinkTheme;
                break;
            case PURPLE:
                colorPrimaryDark = R.color.purpleColorPrimaryDark;
                colorPrimary = R.color.purpleColorPrimary;
                colorPrimaryBackground = R.color.purpleColorPrimaryBackground;
                colorTheme = R.style.PurpleTheme;
                break;
            case DEEP_PURPLE:
                colorPrimaryDark = R.color.deepPurpleColorPrimaryDark;
                colorPrimary = R.color.deepPurpleColorPrimary;
                colorPrimaryBackground = R.color.deepPurpleColorPrimaryBackground;
                colorTheme = R.style.DeepPurpleTheme;
                break;
            case RED:
                colorPrimaryDark = R.color.redColorPrimaryDark;
                colorPrimary = R.color.redColorPrimary;
                colorPrimaryBackground = R.color.redColorPrimaryBackground;
                colorTheme = R.style.RedTheme;
                break;
            case BLUE:
                colorPrimaryDark = R.color.bluePrimaryDark;
                colorPrimary = R.color.bluePrimary;
                colorPrimaryBackground = R.color.bluePrimaryBackground;
                colorTheme = R.style.BlueTheme;
                break;
            case LIGHT_BLUE:
                colorPrimaryDark = R.color.lightBluePrimaryDark;
                colorPrimary = R.color.lightBluePrimary;
                colorPrimaryBackground = R.color.lightBluePrimaryBackground;
                colorTheme = R.style.LightBlueTheme;
                break;
            case CYAN:
                colorPrimaryDark = R.color.cyanPrimaryDark;
                colorPrimary = R.color.cyanPrimary;
                colorPrimaryBackground = R.color.cyanPrimaryBackground;
                colorTheme = R.style.CyanTheme;
                break;
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
            case LIGHT_GREEN:
                colorPrimaryDark = R.color.lightGreenColorPrimaryDark;
                colorPrimary = R.color.lightGreenColorPrimary;
                colorPrimaryBackground = R.color.lightGreenColorPrimaryBackground;
                colorTheme = R.style.LightGreenTheme;
                break;
            case LIME:
                colorPrimaryDark = R.color.limeColorPrimaryDark;
                colorPrimary = R.color.limeColorPrimary;
                colorPrimaryBackground = R.color.limeColorPrimaryBackground;
                colorTheme = R.style.LimeTheme;
                break;
            case YELLOW:
                colorPrimaryDark = R.color.yellowColorPrimaryDark;
                colorPrimary = R.color.yellowColorPrimary;
                colorPrimaryBackground = R.color.yellowColorPrimaryBackground;
                colorTheme = R.style.YellowTheme;
                break;
            case AMBER:
                colorPrimaryDark = R.color.amberColorPrimaryDark;
                colorPrimary = R.color.amberColorPrimary;
                colorPrimaryBackground = R.color.amberColorPrimaryBackground;
                colorTheme = R.style.AmberTheme;
                break;
            case ORANGE:
                colorPrimaryDark = R.color.orangeColorPrimaryDark;
                colorPrimary = R.color.orangeColorPrimary;
                colorPrimaryBackground = R.color.orangeColorPrimaryBackground;
                colorTheme = R.style.OrangeTheme;
                break;
            case DEEP_ORANGE:
                colorPrimaryDark = R.color.deepOrangeColorPrimaryDark;
                colorPrimary = R.color.deepOrangeColorPrimary;
                colorPrimaryBackground = R.color.deepOrangeColorPrimaryBackground;
                colorTheme = R.style.DeepOrangeTheme;
                break;
            case BROWN:
                colorPrimaryDark = R.color.brownColorPrimaryDark;
                colorPrimary = R.color.brownColorPrimary;
                colorPrimaryBackground = R.color.brownColorPrimaryBackground;
                colorTheme = R.style.BrownTheme;
                break;
            case GREY:
                colorPrimaryDark = R.color.greyColorPrimaryDark;
                colorPrimary = R.color.greyColorPrimary;
                colorPrimaryBackground = R.color.greyColorPrimaryBackground;
                colorTheme = R.style.GreyTheme;
                break;
            case BLUE_GREY:
                colorPrimaryDark = R.color.blueGreyColorPrimaryDark;
                colorPrimary = R.color.blueGreyColorPrimary;
                colorPrimaryBackground = R.color.blueGreyColorPrimaryBackground;
                colorTheme = R.style.BlueGreyTheme;
                break;
            case BLACK_WHITE:
                colorPrimaryDark = R.color.blackWhiteColorPrimaryDark;
                colorPrimary = R.color.blackWhiteColorPrimary;
                colorPrimaryBackground = R.color.blackWhiteColorPrimaryBackground;
                colorTheme = R.style.BlackWhiteTheme;
                break;
        }
        int colorStatusBar = ContextCompat.getColor(mContext, colorPrimaryDark);
        int colorToolbar = ContextCompat.getColor(mContext, colorPrimary);
        mColorBackground = ContextCompat.getColor(mContext, colorPrimaryBackground);
        MainActivity.mHeaderNavigationDrawerLayout.setBackgroundColor(colorToolbar);
        getActivity().setTheme(colorTheme);

        //Getting status bar if Lolipop+
        if (Build.VERSION.SDK_INT >= 21) {
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

    /**
     * Handling checkbox, to control of background color
     * If checked - leave background a white, else apply selected theme
     * @param buttonView
     * @param isChecked
     */
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