package ua.com.spacetv.mycookbook.tools;

import android.content.Context;
import android.content.SharedPreferences;

import ua.com.spacetv.mycookbook.interfaces.Constants;

/**
 * Working with shared preferences
 *
 * {@link ua.com.spacetv.mycookbook.fragments.FragTopCategory}
 * {@link ua.com.spacetv.mycookbook.fragments.FragSubCategory}
 * {@link ua.com.spacetv.mycookbook.fragments.FragListRecipe}
 */

public class Preferences implements Constants {

    /**
     * Saving preferences type "int"
     * @param context
     * @param tag - TAG for save preferences
     * @param variable - int variable
     */
    public static void setSettingsToPreferences(Context context, String tag, int variable) {
        SharedPreferences userDetails =
                context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = userDetails.edit();
        edit.putInt(tag, variable);
        edit.apply();
    }

    /**
     * Getting stored preferences type "int"
     * @param context
     * @param tag - TAG for save preferences
     * @return  - int variable
     */
    public static int getSettingsFromPreferences(Context context, String tag) {
        SharedPreferences userDetails =
                context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return userDetails.getInt(tag, 0);
    }

    /**
     * Saving preferences type "boolean"
     * @param context
     * @param tag - TAG for save preferences
     * @param key - boolean
     */
    public static void setSettingsToPreferences(Context context, String tag, boolean key) {
        SharedPreferences userDetails =
                context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = userDetails.edit();
        edit.putBoolean(tag, key);
        edit.apply();
    }

    /**
     * Getting stored preferences type "boolean"
     * @param context
     * @param tag - TAG for save preferences
     *            @param i - any number, key for return boolean from preferences
     * @return  - boolean
     */
    public static boolean getSettingsFromPreferences(Context context, String tag, int i) {
        SharedPreferences userDetails =
                context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return userDetails.getBoolean(tag, false);
    }
}
