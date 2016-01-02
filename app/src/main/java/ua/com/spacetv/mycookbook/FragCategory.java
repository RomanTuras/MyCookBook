package ua.com.spacetv.mycookbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ua.com.spacetv.mycookbook.tools.DataBaseHelper;
import ua.com.spacetv.mycookbook.tools.ListAdapter;
import ua.com.spacetv.mycookbook.tools.ListData;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;
import ua.com.spacetv.mycookbook.tools.StaticFields;

/**
 * Created by salden on 02/01/2016.
 */
public class FragCategory extends Fragment implements StaticFields, View.OnTouchListener, AdapterView.OnItemLongClickListener {

    private OnFragmentEventsListener onFragmentEventsListener;
    private DataBaseHelper dataBaseHelper;
    private SQLiteDatabase database;
    private ContentValues contentValues;
    private ListView listView;
    private ArrayList<ListData> adapter;
    private boolean isLongAction = false;

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
        View view = inflater.inflate(R.layout.frag_category, null);
        listView = (ListView) view.findViewById(R.id.listCategory);
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
        listView.setOnTouchListener(this);
        listView.setOnItemLongClickListener(this);
    }

    public void categoryInList() {
        adapter = new ArrayList<>();
        Cursor c = database.query("tableMain", null, null, null, null, null,
                "category", null);

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    int numberRecipe = 45; //cntRecipe(c.getInt(0));
//                    String tmp = getResources().getString(R.string.cnt_recipe);
//                    tmp += " " + n;

                    adapter.add(new ListData(c.getString(1),
                            "Соленья, Варенья, Лосятина, Говядина, Игуанодонятина", ID_IMG_FOLDER,
                            ID_IMG_LIKE_OFF, numberRecipe));
                } while (c.moveToNext());
            }
            c.close();
            dataBaseHelper.close();
        } else {
            dataBaseHelper.close();
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

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if((event.getAction() == event.ACTION_UP) & isLongAction == false) {
            Snackbar.make(view, "Action: onClick - " + view.getId(),
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }else isLongAction = false;

        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Snackbar.make(view, "Action: onLongClick - " + position,
                Snackbar.LENGTH_LONG).setAction("Action", null).show();
        isLongAction = true;
        return false;
    }
}
