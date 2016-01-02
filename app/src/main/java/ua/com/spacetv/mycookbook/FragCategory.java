package ua.com.spacetv.mycookbook;

import android.content.ContentValues;
import android.content.Context;
import android.support.v4.app.ListFragment;

import ua.com.spacetv.mycookbook.tools.DataBaseHelper;
import ua.com.spacetv.mycookbook.tools.OnFragmentEventsListener;

/**
 * Created by salden on 02/01/2016.
 */
public class FragCategory extends ListFragment {

    private OnFragmentEventsListener onFragmentEventsListener;
    private DataBaseHelper dataBaseHelper;
    private ContentValues contentValues;

    @Override
    public void onAttach(Context context){
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
}
