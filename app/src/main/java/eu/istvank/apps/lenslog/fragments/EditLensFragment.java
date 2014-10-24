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
 * Use the {@link EditLensFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EditLensFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // the fragment initialization parameters
    private static final String ARG_LENSURI = "lensUri";

    private Uri mLensUri;

    // views
    private EditText mEdtName;
    private EditText mEdtBrand;
    private EditText mEdtEye;
    private EditText mEdtSphere;
    private EditText mEdtBaseCurve;
    private EditText mEdtDiameter;
    private EditText mEdtCylinder;
    private EditText mEdtAxis;
    private EditText mEdtAdd;
    private EditText mEdtExpiration;
    private EditText mEdtPurchased;
    private EditText mEdtShop;

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
        View view = inflater.inflate(R.layout.fragment_edit_lens, container, false);

        // link views
        mEdtName = (EditText) view.findViewById(R.id.newlens_edt_name);
        mEdtBrand = (EditText) view.findViewById(R.id.newlens_edt_brand);
        mEdtEye = (EditText) view.findViewById(R.id.newlens_edt_eye);
        mEdtSphere = (EditText) view.findViewById(R.id.newlens_edt_sphere);
        mEdtBaseCurve = (EditText) view.findViewById(R.id.newlens_edt_base_curve);
        mEdtDiameter = (EditText) view.findViewById(R.id.newlens_edt_diameter);
        mEdtCylinder = (EditText) view.findViewById(R.id.newlens_edt_cylinder);
        mEdtAxis = (EditText) view.findViewById(R.id.newlens_edt_axis);
        mEdtAdd = (EditText) view.findViewById(R.id.newlens_edt_add);
        mEdtExpiration = (EditText) view.findViewById(R.id.newlens_edt_expiration);
        mEdtPurchased = (EditText) view.findViewById(R.id.newlens_edt_purchased);
        mEdtShop = (EditText) view.findViewById(R.id.newlens_edt_shop);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.newlens, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            ContentValues values = new ContentValues();
            values.put(LensLogContract.PacksColumns.NAME, mEdtName.getText().toString());
            values.put(LensLogContract.PacksColumns.BRAND, mEdtBrand.getText().toString());
            values.put(LensLogContract.PacksColumns.EYE, mEdtEye.getText().toString());
            values.put(LensLogContract.PacksColumns.SPHERE, mEdtSphere.getText().toString());
            values.put(LensLogContract.PacksColumns.BASE_CURVE, mEdtBaseCurve.getText().toString());
            values.put(LensLogContract.PacksColumns.DIAMETER, mEdtDiameter.getText().toString());
            values.put(LensLogContract.PacksColumns.CYLINDER, mEdtCylinder.getText().toString());
            values.put(LensLogContract.PacksColumns.AXIS, mEdtAxis.getText().toString());
            values.put(LensLogContract.PacksColumns.ADD_POWER, mEdtAdd.getText().toString());
            values.put(LensLogContract.PacksColumns.EXPIRATION_DATE, mEdtExpiration.getText().toString());
            values.put(LensLogContract.PacksColumns.PURCHASED_DATE, mEdtPurchased.getText().toString());
            values.put(LensLogContract.PacksColumns.SHOP, mEdtShop.getText().toString());

            if (mLensUri != null) {
                // change existing lens pack
                getActivity().getContentResolver().update(mLensUri, values, null, null);
            } else {
                // add new lens to database
                getActivity().getContentResolver().insert(LensLogContract.Packs.CONTENT_URI, values);
            }

            // close this fragment and return to previous
            getFragmentManager().popBackStack();

            // keyboard stays open after fragment is removed, so close it
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
                        LensLogContract.Packs.NAME,
                        LensLogContract.Packs.BRAND,
                        LensLogContract.Packs.EYE,
                        LensLogContract.Packs.SPHERE,
                        LensLogContract.Packs.BASE_CURVE,
                        LensLogContract.Packs.DIAMETER,
                        LensLogContract.Packs.CYLINDER,
                        LensLogContract.Packs.AXIS,
                        LensLogContract.Packs.ADD_POWER,
                        LensLogContract.Packs.EXPIRATION_DATE,
                        LensLogContract.Packs.PURCHASED_DATE,
                        LensLogContract.Packs.SHOP
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
        mEdtName.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.NAME)));
        mEdtBrand.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.BRAND)));
        mEdtEye.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.EYE)));
        mEdtSphere.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.SPHERE)));
        mEdtBaseCurve.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.BASE_CURVE)));
        mEdtDiameter.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.DIAMETER)));
        mEdtCylinder.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.CYLINDER)));
        mEdtAxis.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.AXIS)));
        mEdtAdd.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.ADD_POWER)));
        mEdtExpiration.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.EXPIRATION_DATE)));
        mEdtPurchased.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.PURCHASED_DATE)));
        mEdtShop.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.SHOP)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
