package ua.com.spacetv.mycookbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuItem;
import com.shehabic.droppy.DroppyMenuPopup;

import java.util.ArrayList;

import ua.com.spacetv.mycookbook.tools.DataBaseHelper;
import ua.com.spacetv.mycookbook.tools.ListAdapter;
import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by salden on 02/01/2016.
 */
public class FragTopCategory extends Fragment implements StaticFields,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private OnFragmentEventsListener onFragmentEventsListener;
    private DataBaseHelper dataBaseHelper;
    private SQLiteDatabase database;
    private ContentValues contentValues;
    private ListView listView;
    private ArrayList<ListData> adapter;
    private ArrayList<Integer> arrayIdSubCategories;
    private PopupMenu popup;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.contentValues = new ContentValues();
        this.dataBaseHelper = new DataBaseHelper(context);

        try {
            onFragmentEventsListener = (OnFragmentEventsListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentEventsListener");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.frag_top_category, null);
        listView = (ListView) view.findViewById(R.id.listTopCategory);
        database = dataBaseHelper.getWritableDatabase();

        showAllCategory();
        return view;
    }

    public void showAllCategory() {
        categoryInList();
        ListAdapter listAdapter = new ListAdapter(getContext(), adapter);
        // if (adapter.size() == 0)
        // tv.setText(getString(R.string.title_category) + " "+
        // getString(R.string.empty));
        // else
        // tv.setText(R.string.title_category);
        listView.setAdapter(listAdapter);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
    }

    public void categoryInList() {
        adapter = new ArrayList<>();
        Cursor cursor = database.query(TABLE_TOP_CATEGORY, null, null, null, null, null,
                "category", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int item_id = cursor.getInt(0);
                    String subCategories = getSubcategories(item_id);
                    int numberRecipe = countRecipe(item_id);
                    adapter.add(new ListData(cursor.getString(1), subCategories, ID_IMG_FOLDER,
                            ID_IMG_LIKE_OFF, numberRecipe, item_id));
                } while (cursor.moveToNext());
            }
            cursor.close();
            dataBaseHelper.close();
        } else {
            dataBaseHelper.close();
        }
    }

    private int countRecipe(int item_id) {
        int numberRecipe = 0;
        Cursor cursor = database.query(TABLE_LIST_RECIPE, null, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(3) == item_id) { //column 'category_id'
                        numberRecipe++; // count recipes in Top Category
                    }else{
                        for(Integer i: arrayIdSubCategories){ // count recipes in Sub Categories
                            if(i == cursor.getInt(5)) numberRecipe++; //column 'sub_category_id'
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return numberRecipe;
    }

    private String getSubcategories(int item_id) {
        arrayIdSubCategories = new ArrayList<>(); // id all subcategories in parent category
        String subCategories = "";
        Cursor cursor = database.query(TABLE_SUB_CATEGORY, null, null, null, null, null,
                "name", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(3) == item_id) { //column 'parent_id'
                        subCategories += cursor.getString(1); //column 'name'
                        subCategories += ", ";
                        arrayIdSubCategories.add(cursor.getInt(0)); //column '_id'
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if(subCategories.length() != 0) //remove last symbols -> ", "
            subCategories = subCategories.substring(0, subCategories.length()-2);
        return subCategories;
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
        showPopupMenu(view);
        return true;
    }

    public void showPopupMenu(View view) {
        DroppyMenuPopup droppyMenu;
        DroppyMenuPopup.Builder droppyBuilder = new DroppyMenuPopup.Builder(getContext(), view);
        String item_rename = getResources().getString(R.string.item_rename);
        String item_delete = getResources().getString(R.string.item_delete);

        droppyMenu = droppyBuilder.addMenuItem(new DroppyMenuItem(item_rename,
                R.drawable.ic_mode_edit_black_18dp))
                .addSeparator()
                .addMenuItem(new DroppyMenuItem(item_delete, R.drawable.ic_delete_black_18dp))
                .triggerOnAnchorClick(false)
                .setOnClick(new DroppyClickCallbackInterface() {
                    @Override
                    public void call(View v, int id) {
                        Log.d("TG", String.valueOf(id));
                    }
                })
                .build();
        droppyMenu.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListData ld = adapter.get(position);
        onFragmentEventsListener.onListItemClick(ID_TABLE_TOP_CATEGORY, ld.getItemId());
    }
}
