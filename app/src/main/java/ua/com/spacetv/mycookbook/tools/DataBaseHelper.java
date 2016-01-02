package ua.com.spacetv.mycookbook.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * Created by salden on 14/12/2015.
 *
 */
public class DataBaseHelper extends SQLiteOpenHelper implements StaticFields{

    public SQLiteDatabase mDataBase;
    private Context mContext;
    private ContentValues mContentValues =new ContentValues();
//    private static String mSdState = Environment.getExternalStorageDirectory().getAbsolutePath();
//    private static String mPath = (mSdState + "/" + DB_FOLDER + "/" + DB_FILE);
    private static int mVers=1;

    public DataBaseHelper(Context context) {
        super(context, DB_FILE, null, mVers);
        mContext = context;
    }


    /** Checking availability of database file and return true if it is */
    private boolean checkDataBase() {
        File dbFile = mContext.getDatabasePath(DB_FILE);
        Log.d("TG", "checkDataBase ="+dbFile.exists());
        return dbFile.exists();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String mTableMain = "create table tableMain (_id integer primary key autoincrement, "
                + "category text);";

        String tableRecipe = "create table tableRecipe (_id integer primary key autoincrement, "
                + "recipe_title text, recipe text, category_id integer, make integer);";

        sqLiteDatabase.execSQL(mTableMain);
        sqLiteDatabase.execSQL(tableRecipe);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
