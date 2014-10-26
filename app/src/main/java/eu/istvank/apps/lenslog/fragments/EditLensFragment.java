package eu.istvank.apps.lenslog.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Calendar;

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
    private Spinner mSpnEye;
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
     * The calendar for the calendar picker
     */
    Calendar mCalendar = Calendar.getInstance();

    private enum DateField {
        EXPIRATION,
        PURCHASED
    }

    private DateField mCurrentDateField;

    private long mExpirationDate;
    private long mPurchasedDate;

    private static final int mDateBitmask = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;


    // according to internet research, having the same prescription on both eyes is less likely than
    // having different ones. That's why the "both" value is the last item.
    public static final int EYE_LEFT = 0;
    public static final int EYE_RIGHT = 1;
    public static final int EYE_BOTH = 2;

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

        //TODO: is this handled at all?
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.title_fragment_newlens);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_lens, container, false);

        // link views
        mEdtName = (EditText) view.findViewById(R.id.newlens_edt_name);
        mEdtBrand = (EditText) view.findViewById(R.id.newlens_edt_brand);
        mSpnEye = (Spinner) view.findViewById(R.id.newlens_spn_eye);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.eye_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpnEye.setAdapter(adapter);
        mEdtSphere = (EditText) view.findViewById(R.id.newlens_edt_sphere);
        mEdtBaseCurve = (EditText) view.findViewById(R.id.newlens_edt_base_curve);
        mEdtDiameter = (EditText) view.findViewById(R.id.newlens_edt_diameter);
        mEdtCylinder = (EditText) view.findViewById(R.id.newlens_edt_cylinder);
        mEdtAxis = (EditText) view.findViewById(R.id.newlens_edt_axis);
        mEdtAdd = (EditText) view.findViewById(R.id.newlens_edt_add);
        mEdtExpiration = (EditText) view.findViewById(R.id.newlens_edt_expiration);
        mEdtExpiration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentDateField = DateField.EXPIRATION;
                if (mExpirationDate != 0) {
                    mCalendar.setTimeInMillis(mExpirationDate);
                } else {
                    mCalendar = Calendar.getInstance();
                }
                new DatePickerDialog(getActivity(), mDatePicker, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mEdtPurchased = (EditText) view.findViewById(R.id.newlens_edt_purchased);
        mEdtPurchased.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentDateField = DateField.PURCHASED;
                if (mPurchasedDate != 0) {
                    mCalendar.setTimeInMillis(mPurchasedDate);
                } else {
                    mCalendar = Calendar.getInstance();
                }
                new DatePickerDialog(getActivity(), mDatePicker, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
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
            int eyePos = mSpnEye.getSelectedItemPosition();
            String eye;
            switch (eyePos) {
                case EYE_LEFT:
                    eye = "left";
                    break;
                case EYE_RIGHT:
                    eye = "right";
                    break;
                default:
                    eye = "both";
            }
            values.put(LensLogContract.PacksColumns.EYE, eye);
            values.put(LensLogContract.PacksColumns.LENS_TYPE, "");
            values.put(LensLogContract.PacksColumns.SPHERE, mEdtSphere.getText().toString());
            values.put(LensLogContract.PacksColumns.BASE_CURVE, mEdtBaseCurve.getText().toString());
            values.put(LensLogContract.PacksColumns.DIAMETER, mEdtDiameter.getText().toString());
            values.put(LensLogContract.PacksColumns.CYLINDER, mEdtCylinder.getText().toString());
            values.put(LensLogContract.PacksColumns.AXIS, mEdtAxis.getText().toString());
            values.put(LensLogContract.PacksColumns.ADD_POWER, mEdtAdd.getText().toString());
            values.put(LensLogContract.PacksColumns.EXPIRATION_DATE, mExpirationDate);
            values.put(LensLogContract.PacksColumns.PURCHASED_DATE, mPurchasedDate);
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
     * DatePickerDialog
     */

    DatePickerDialog.OnDateSetListener mDatePicker = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateField();
        }

    };

    private void updateDateField() {
        if (mCurrentDateField.equals(DateField.EXPIRATION)) {
            mExpirationDate = mCalendar.getTimeInMillis();
            mEdtExpiration.setText(DateUtils.formatDateTime(getActivity(), mExpirationDate, mDateBitmask));
        } else {
            mPurchasedDate = mCalendar.getTimeInMillis();
            mEdtPurchased.setText(DateUtils.formatDateTime(getActivity(), mPurchasedDate, mDateBitmask));
        }
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
                        mLensUri,       // Table to query
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
        int eyeSelection = EYE_LEFT;
        String eye = data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.EYE));
        if (eye.equals("right")) {
            eyeSelection = EYE_RIGHT;
        } else if (eye.equals("both")) {
            eyeSelection = EYE_BOTH;
        }
        mSpnEye.setSelection(eyeSelection);
        mEdtSphere.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.SPHERE)));
        mEdtBaseCurve.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.BASE_CURVE)));
        mEdtDiameter.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.DIAMETER)));
        mEdtCylinder.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.CYLINDER)));
        mEdtAxis.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.AXIS)));
        mEdtAdd.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.ADD_POWER)));
        // get formatted expiration date
        mExpirationDate = data.getLong(data.getColumnIndexOrThrow(LensLogContract.Packs.EXPIRATION_DATE));
        if (mExpirationDate != 0) {
            mEdtExpiration.setText(DateUtils.formatDateTime(getActivity(), mExpirationDate, mDateBitmask));
        }
        // get formatted purchased date
        mPurchasedDate = data.getLong(data.getColumnIndexOrThrow(LensLogContract.Packs.PURCHASED_DATE));
        if (mPurchasedDate != 0) {
            mEdtPurchased.setText(DateUtils.formatDateTime(getActivity(), mPurchasedDate, mDateBitmask));
        }
        mEdtShop.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packs.SHOP)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
