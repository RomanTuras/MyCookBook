package ua.com.spacetv.mycookbook.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by Roman Turas on 09/01/2016.
 * Only for the CHILD FOLDERS!!!
 */
public class PrepareListRecipes implements StaticFields {
    private static ArrayList<ListData> adapter;
    private SQLiteDatabase database;
    private DataBaseHelper dataBaseHelper;
    private static int idParentFolder;
    private final static int KEY_FAVORITE = 1;

    /**
     * To prepare a list of recipes that are in 'idParentFolder'
     */
    public PrepareListRecipes(Context context, int idParentFolder) {
        dataBaseHelper = new DataBaseHelper(context);
        database = dataBaseHelper.getWritableDatabase();
        adapter = new ArrayList<>();
        PrepareListRecipes.idParentFolder = idParentFolder;
        recipeToAdapter(idParentFolder);
    }

    /**
     * To prepare a list of favorite recipes
     */
    public PrepareListRecipes(Context context) {
        dataBaseHelper = new DataBaseHelper(context);
        database = dataBaseHelper.getWritableDatabase();
        adapter = new ArrayList<>();
        recipeToAdapter();
    }

    /** To prepare a list of recipes depend of search request */
    public PrepareListRecipes(Context context, String searchRequest) {
        dataBaseHelper = new DataBaseHelper(context);
        database = dataBaseHelper.getWritableDatabase();
        adapter = new ArrayList<>();
        String[] query = new String [1];

        searchRequest = setUpperCase(searchRequest);
        query[0] = "%" + searchRequest + "%";
        recipeToAdapter(query);

        searchRequest = setLowerCase(searchRequest);
        query[0] = "%" + searchRequest + "%";
        recipeToAdapter(query);
    }

    /**
     * Method. To prepare a list of recipes depend of search request
     */
    private void recipeToAdapter(String[] query) {
        Cursor cursor = database.query(TABLE_LIST_RECIPE, null, "recipe_title LIKE ?", query,
                null, null, null);
        readDatabaseInToAdapter(cursor);
        cursor.close();
    }

    /**
     * Method. To prepare a list of favorite recipes
     */
    private void recipeToAdapter() {
        String selectQuery = "SELECT * FROM " + TABLE_LIST_RECIPE +
                " WHERE make=" + KEY_FAVORITE + " ORDER BY recipe_title";
        readDatabaseInToAdapter(database.rawQuery(selectQuery, null));
    }

    /**
     * Method. To prepare a list of recipes that are in 'idParentFolder'
     */
    private void recipeToAdapter(int idParentFolder) {
        String selectQuery = "SELECT * FROM " + TABLE_LIST_RECIPE +
                " WHERE sub_category_id=" + idParentFolder + " ORDER BY recipe_title";
        readDatabaseInToAdapter(database.rawQuery(selectQuery, null));
    }

    private void readDatabaseInToAdapter(Cursor cursor){
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    int imgLike = cursor.getInt(4);

                    adapter.add(new ListData(cursor.getString(1),
                            "", ID_IMG_RECIPE, imgLike, 0, item_id, IS_RECIPE));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public ArrayList<ListData> getFilledAdapter() {
        return adapter;
    }

    public static int getIdParentFolder() {
        return idParentFolder;
    }

    public static String setUpperCase(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        CharacterIterator chit = new StringCharacterIterator(s);
        char ch = chit.current(), prev = ' ';
        while (ch != CharacterIterator.DONE) {
            if (Character.isWhitespace(prev) && Character.isLetter(ch)) {
                sb.append(Character.toUpperCase(ch));
            } else {
                sb.append(ch);
            }
            prev = ch;
            ch = chit.next();
        }
        return sb.toString();
    }

    public static String setLowerCase(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        CharacterIterator chit = new StringCharacterIterator(s);
        char ch = chit.current(), prev = ' ';
        while (ch != CharacterIterator.DONE) {
            if (Character.isWhitespace(prev) && Character.isLetter(ch)) {
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
            prev = ch;
            ch = chit.next();
        }
        return sb.toString();
    }
}
