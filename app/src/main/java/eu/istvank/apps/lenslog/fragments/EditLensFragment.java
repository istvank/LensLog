package eu.istvank.apps.lenslog.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import eu.istvank.apps.lenslog.R;
import eu.istvank.apps.lenslog.provider.LensLogContract;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditLensFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditLensFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EditLensFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // the fragment initialization parameters
    private static final String ARG_LENSURI = "lensUri";

    private Uri mLensUri;

    private OnFragmentInteractionListener mListener;

    // views
    private EditText mEdtLensName;

    /**
     * Identifies a particular Loader being used in this component
     */
    private static final int URL_LOADER = 0;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditLensFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditLensFragment newInstance(Uri lensUri) {
        EditLensFragment fragment = new EditLensFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_LENSURI, lensUri);
        fragment.setArguments(args);
        return fragment;
    }
    public EditLensFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLensUri = getArguments().getParcelable(ARG_LENSURI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.title_fragment_newlens);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_lens, container, false);

        // link views
        mEdtLensName = (EditText) view.findViewById(R.id.newlens_edt_name);

        // check if a new item is created or an old edited
        if (mLensUri != null) {
            /*
            * Initializes the CursorLoader. The URL_LOADER value is eventually passed
            * to onCreateLoader().
            */
            getLoaderManager().initLoader(URL_LOADER, null, this);
        }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.newlens, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            ContentValues values = new ContentValues();
            values.put(LensLogContract.PacksColumns.EYE, "left");
            values.put(LensLogContract.PacksColumns.LENS_TYPE, "great");
            values.put(LensLogContract.PacksColumns.NAME, mEdtLensName.getText().toString());

            if (mLensUri != null) {
                // change existing lens pack
                getActivity().getContentResolver().update(mLensUri, values, null, null);
            } else {
                // add new lens to database
                getActivity().getContentResolver().insert(LensLogContract.Packs.CONTENT_URI, values);
            }

            // close this fragment and return to previous
            getFragmentManager().popBackStack();

            // close the keyboard
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    /**
     * Interfaces
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /*
        * Takes action based on the ID of the Loader that's being created
        */
        switch (id) {
            case URL_LOADER:
                // Returns a new CursorLoader
                String[] projection = new String[] {
                        LensLogContract.Packs._ID,
                        LensLogContract.Packs.NAME
                };
                return new CursorLoader(
                        getActivity(),  // Parent activity context
                        mLensUri,        // Table to query
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
        data.moveToFirst();
        mEdtLensName.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.NAME)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
