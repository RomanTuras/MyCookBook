package ua.com.spacetv.mycookbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ua.com.spacetv.mycookbook.helpers.DataBaseHelper;
import ua.com.spacetv.mycookbook.tools.ListAdapter;
import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by salden on 02/01/2016.
 */
public class FragSubCategory extends Fragment implements StaticFields,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private OnFragmentEventsListener onFragmentEventsListener;
    private DataBaseHelper dataBaseHelper;
    private SQLiteDatabase database;
    private ContentValues contentValues;
    private ListView listView;
    private ArrayList<ListData> adapter;
    int idItem = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.contentValues = new ContentValues();
        this.dataBaseHelper = new DataBaseHelper(context);

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            this.idItem = bundle.getInt(ITEM_ID);
        }
        Log.d("TG", "ITEM_ID = "+idItem);

        try {
            onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentEventsListener");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_sub_category, null);
        listView = (ListView) view.findViewById(R.id.listSubCategory);
        database = dataBaseHelper.getWritableDatabase();

        showCategoryAndRecipe();
        return view;
    }

    public void showCategoryAndRecipe() {
        adapter = new ArrayList<>();
        subCategoryInList();
        recipeInList();
        ListAdapter listAdapter = new ListAdapter(getContext(), adapter);
        // if (adapter.size() == 0)
        // tv.setText(getString(R.string.title_category) + " "+
        // getString(R.string.empty));
        // else
        // tv.setText(R.string.title_category);
        dataBaseHelper.close();
        listView.setAdapter(listAdapter);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
    }

    private void recipeInList() {
        String selectQuery ="SELECT * FROM " + TABLE_LIST_RECIPE +
                " WHERE category_id=" + idItem + " ORDER BY recipe_title";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int numberRecipe = 45; //cntRecipe(cursor.getInt(0));
                    int item_id = cursor.getInt(0);
                    int imgLike = cursor.getInt(4);

                    adapter.add(new ListData(cursor.getString(1),
                            "", ID_IMG_RECIPE,
                            imgLike, numberRecipe, item_id));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("TG", "Table with recipeCategory - is Empty");
        }
    }

    public void subCategoryInList() {
        String selectQuery ="SELECT * FROM " + TABLE_SUB_CATEGORY +
                " WHERE parent_id=" + idItem + " ORDER BY name";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int numberRecipe = 45; //cntRecipe(cursor.getInt(0));
                    int item_id = cursor.getInt(0);

                    adapter.add(new ListData(cursor.getString(1),
                            "Соленья, Варенья, Лосятина, Говядина, Игуанодонятина", ID_IMG_FOLDER,
                            ID_IMG_LIKE_OFF, numberRecipe, item_id));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("TG", "Table with SubCategories - is Empty");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /** onLongClick() - This returns a boolean to indicate whether you have consumed the event and
     * it should not be carried further. That is, return true to indicate that you have handled
     * the event and it should stop here; return false if you have not handled it and/or the event
     * should continue to any other on-click listeners.*/
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Snackbar.make(view, "Action: onLongClick - " + position,
                Snackbar.LENGTH_LONG).setAction("Action", null).show();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Snackbar.make(view, "Action: onClick - "+position,
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
