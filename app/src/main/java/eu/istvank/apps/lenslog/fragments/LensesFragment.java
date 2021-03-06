package eu.istvank.apps.lenslog.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import eu.istvank.apps.lenslog.R;
import eu.istvank.apps.lenslog.provider.LensLogContract;

/**
 * The LensesFragment shows a list of lens packages. It shows an "add" button to allow adding more
 * lens packages.
 *
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link eu.istvank.apps.lenslog.fragments.LensesFragment.OnPackageSelectedListener}
 * interface.
 */
public class LensesFragment extends Fragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The listener for package selection events.
     */
    private OnPackageSelectedListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * Identifies a particular Loader being used in this component
     */
    private static final int LENSES_LOADER = 0;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SimpleCursorAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static LensesFragment newInstance() {
        LensesFragment fragment = new LensesFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LensesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lenses, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        registerForContextMenu(mListView);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.attachToListView(mListView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onPackageSelected(null);
            }
        });

        mAdapter =
                new SimpleCursorAdapter(
                        getActivity(),                          // Current context
                        R.layout.lenses_list_item,    // Layout for a single row
                        null,                                   // No Cursor yet
                        new String[] {LensLogContract.Packages.NAME, LensLogContract.Packages.BRAND, LensLogContract.Packages.EYE},   // Cursor columns to use
                        new int[] {R.id.lens_name, R.id.brand, R.id.eye},         // Layout fields to use
                        0                                       // No flags
                );

        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        /*
         * Initializes the CursorLoader. The LENSES_LOADER value is eventually passed
         * to onCreateLoader().
         */
        getLoaderManager().initLoader(LENSES_LOADER, null, this);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.lenses, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == android.R.id.list) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.lenseslist, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.delete:
                // remove lens package
                Uri packageUri = ContentUris.withAppendedId(LensLogContract.Packages.CONTENT_URI, info.id);
                getActivity().getContentResolver().delete(packageUri, null, null);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPackageSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnPackageSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            Uri packageUri = ContentUris.withAppendedId(LensLogContract.Packages.CONTENT_URI, id);
            mListener.onPackageSelected(packageUri);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    */
    public interface OnPackageSelectedListener {
        public void onPackageSelected(Uri packageUri);
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
            case LENSES_LOADER:
                // Returns a new CursorLoader
                String[] projection = new String[] {
                        LensLogContract.Packages._ID,
                        LensLogContract.Packages.NAME,
                        LensLogContract.Packages.BRAND,
                        LensLogContract.Packages.EYE
                };
                return new CursorLoader(
                        getActivity(),  // Parent activity context
                        LensLogContract.Packages.CONTENT_URI,        // Table to query
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
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Sets the Adapter's backing data to null. This prevents memory leaks.
        mAdapter.changeCursor(null);
    }
}
