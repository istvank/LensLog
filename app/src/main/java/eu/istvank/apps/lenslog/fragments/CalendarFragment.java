package eu.istvank.apps.lenslog.fragments;


import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import eu.istvank.apps.lenslog.R;
import eu.istvank.apps.lenslog.provider.LensLogContract;
import hirondelle.date4j.DateTime;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CalendarFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    /**
     * The listener for package selection events.
     */
    private OnUpdateWornListener mListener;

    private CaldroidFragment mCaldroid;

    // a HashMap holding the background tints of the dates in the calendar
    HashMap<DateTime, Integer> mCalendarBackgrounds = new HashMap<DateTime, Integer>();

    /**
     * Identifies a particular Loader being used in this component
     */
    private static final int CALENDAR_LOADER = 0;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CalendarFragment.
     */
    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        return fragment;
    }
    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        mCaldroid = (CaldroidFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.calendar_container);

        if (mCaldroid == null) {
            mCaldroid = new CaldroidFragment();
            mCaldroid.setRetainInstance(true);

            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, Calendar.getInstance().getFirstDayOfWeek());
            mCaldroid.setArguments(args);

            mCaldroid.setCaldroidListener(new CaldroidListener() {
                @Override
                public void onSelectDate(Date date, View view) {
                    long localDateLong = date.getTime();
                    DateTime localDatetime = DateTime.forInstant(localDateLong, TimeZone.getDefault());
                    DateTime utcDateTime = DateTime.forDateOnly(localDatetime.getYear(), localDatetime.getMonth(), localDatetime.getDay());
                    long utcDateLong = utcDateTime.getMilliseconds(TimeZone.getTimeZone("UTC"));

                    // check if the date is already in the backgrounds HashMap and if not the background
                    // is empty.
                    if (mCalendarBackgrounds.containsKey(localDatetime) && (mCalendarBackgrounds.get(localDatetime) != com.caldroid.R.drawable.cell_bg)) {
                        // check color
                        int color = mCalendarBackgrounds.get(localDatetime);
                        String whereArg = LensLogContract.DaysWorn.DATETIME + " = ?";
                        String[] selectionArgs = new String[] { String.valueOf(utcDateLong) };
                        if (color == R.color.CalendarBackgroundGreen) {
                            // now worn should be set to false
                            mListener.onUpdateWorn(utcDateLong, false);
//                            ContentValues values = new ContentValues();
//                            values.put(LensLogContract.DaysWorn.WASWORN, false);
//                            getActivity().getContentResolver().update(LensLogContract.DaysWorn.CONTENT_URI, values, whereArg, selectionArgs);
                            view.setBackgroundResource(R.color.CalendarBackgroundRed);
                        } else {
                            // delete the entry
                            getActivity().getContentResolver().delete(LensLogContract.DaysWorn.CONTENT_URI, whereArg, selectionArgs);
                            // reset background
                            view.setBackgroundResource(com.caldroid.R.drawable.cell_bg);
                            // put the resource to the backgrounds array. We have to do this due to an
                            // ugly characteristic of the Caldroid library: the background HashMap is
                            // not reset and we have to stick with the backgrounds that were already
                            // there before.
                            mCalendarBackgrounds.put(localDatetime, com.caldroid.R.drawable.cell_bg);
                        }
                    } else {
                        // add date to database
                        mListener.onUpdateWorn(utcDateLong, true);
//                        ContentValues values = new ContentValues();
//                        values.put(LensLogContract.DaysWorn.DATETIME, utcDateLong);
//                        values.put(LensLogContract.DaysWorn.WASWORN, true);
//                        // add date to database
//                        getActivity().getContentResolver().insert(LensLogContract.DaysWorn.CONTENT_URI, values);
                        view.setBackgroundResource(R.color.CalendarBackgroundGreen);
                    }
                }
            });
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.calendar_container, mCaldroid)
                .commit();

        // Initialize the CursorLoader to retrieve the days when the lenses were worn.
        getLoaderManager().initLoader(CALENDAR_LOADER, null, this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnUpdateWornListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnUpdateWornListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void updateCalendarBackgrounds(HashMap<DateTime, Integer> backgrounds) {
        mCalendarBackgrounds.putAll(backgrounds);

        mCaldroid.setBackgroundResourceForDateTimes(mCalendarBackgrounds);
        mCaldroid.refreshView();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnUpdateWornListener {
        public void onUpdateWorn(long datetime, boolean worn);
    }

    /**
     * Interface implementations
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //TODO: more efficient implementation that only queries the currently shown month.
        // takes action based on the ID of the Loader
        switch (id) {
            case CALENDAR_LOADER:
                // Returns a new CursorLoader
                String[] projection = new String[] {
                        LensLogContract.DaysWorn._ID,
                        LensLogContract.DaysWorn.DATETIME,
                        LensLogContract.DaysWorn.WASWORN
                };
                return new CursorLoader(
                        getActivity(),  // Parent activity context
                        LensLogContract.DaysWorn.CONTENT_URI,        // Table to query
                        projection,     // Projection to return
                        null,           // No selection clause
                        null,           // No selection arguments
                        null            // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // we use a DateTime here as it is native to Caldroid
        HashMap<DateTime, Integer> backgrounds = new HashMap<DateTime, Integer>();

        while (data.moveToNext()) {
            int worn = data.getInt(data.getColumnIndexOrThrow(LensLogContract.DaysWorn.WASWORN));
            DateTime utcDateTime = DateTime.forInstant(data.getLong(data.getColumnIndexOrThrow(LensLogContract.DaysWorn.DATETIME)), TimeZone.getTimeZone("UTC"));
            if (worn == 1) {
                // the lens was worn
                backgrounds.put(utcDateTime, R.color.CalendarBackgroundGreen);
            } else {
                // the lens was explicitly not worn
                backgrounds.put(utcDateTime, R.color.CalendarBackgroundRed);
            }

        }

        // now update the calendar view
        updateCalendarBackgrounds(backgrounds);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
