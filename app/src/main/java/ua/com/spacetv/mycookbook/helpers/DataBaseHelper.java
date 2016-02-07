package ua.com.spacetv.mycookbook.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by salden on 14/12/2015.
 */
public class DataBaseHelper extends SQLiteOpenHelper implements StaticFields {
    private static int versDb = 2;

    public DataBaseHelper(Context context) {
        super(context, FILENAME_WORKING_DB, null, versDb);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String tableMain = "create table tableMain (_id integer primary key autoincrement, "
                + "category text);";

        String tableSubCategory = "create table tableSubCat (_id integer primary key autoincrement, "
                + "name text, hierarchy integer, parent_id integer);";

        String tableRecipe = "create table tableRecipe (_id integer primary key autoincrement, "
                + "recipe_title text, recipe text, category_id integer default -1, make integer, " +
                "sub_category_id integer default -1, image text);";

        sqLiteDatabase.execSQL(tableMain);
        sqLiteDatabase.execSQL(tableSubCategory);
        sqLiteDatabase.execSQL(tableRecipe);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersDb, int versDb) {
        Log.d("TG", "on Upgrade start");
        String upgradeQuery =
                "ALTER TABLE tableRecipe ADD COLUMN sub_category_id integer default -1;";
        String upgradeQuery2 = "ALTER TABLE tableRecipe ADD COLUMN image text;";
        String tableSubCategory = "create table tableSubCat (_id integer primary key autoincrement, "
                + "name text, hierarchy integer, parent_id integer);";

        if (oldVersDb == 1 && versDb == 2) {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.execSQL(tableSubCategory);
                sqLiteDatabase.execSQL(upgradeQuery);
                sqLiteDatabase.execSQL(upgradeQuery2);
                Log.d("TG", "on Upgrade make a new db");
                sqLiteDatabase.setTransactionSuccessful();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }
}
