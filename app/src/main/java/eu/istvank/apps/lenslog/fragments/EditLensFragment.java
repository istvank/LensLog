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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
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
    private Spinner mSpnContent;
    private Spinner mSpnRemaining;
    private Spinner mSpnReplacementValue;
    private Spinner mSpnReplacementPeriod;
    private EditText mEdtExpiration;
    private EditText mEdtPurchased;
    private EditText mEdtShop;
    private Spinner mSpnType;
    private EditText mEdtBaseCurve;
    private EditText mEdtDiameter;
    private TextView mLblSphere;
    private EditText mEdtSphere;
    private TextView mLblCylinder;
    private EditText mEdtCylinder;
    private TextView mLblAxis;
    private EditText mEdtAxis;
    private TextView mLblAdd;
    private EditText mEdtAdd;

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

    // the types of lenses
    public static final int LENS_TYPE_MYOPIA = 0;
    public static final int LENS_TYPE_ASTIGMATISM = 1;
    public static final int LENS_TYPE_MULTIFOCAL = 2;
    public static final int LENS_TYPE_DECORATIVE = 3;

    // the periods
    public static final int PERIOD_DAILY = 0;
    public static final int PERIOD_MONTHLY = 1;
    public static final int PERIOD_YEARLY = 2;

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

        //TODO: does this work at all?
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
        final ArrayAdapter<CharSequence> adapterEye = ArrayAdapter.createFromResource(getActivity(),
                R.array.eye_array, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapterEye.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpnEye.setAdapter(adapterEye);

        // Content spinner
        ArrayList<String> countContent = new ArrayList<String>();
        for (int i = 1; i <= 100; i++) {
            countContent.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapterContent = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, countContent);
        mSpnContent = (Spinner) view.findViewById(R.id.newlens_spn_content);
        mSpnContent.setAdapter(adapterContent);

        // Remaining spinner
        ArrayList<String> countRemaining = new ArrayList<String>();
        for (int i = 0; i <= 100; i++) {
            countRemaining.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapterRemaining = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, countRemaining);
        mSpnRemaining = (Spinner) view.findViewById(R.id.newlens_spn_remaining);
        mSpnRemaining.setAdapter(adapterRemaining);

        // Replacement value spinner
        // reuse countContent's ArrayList as it goes from 1 to 100
        ArrayAdapter<String> adapterReplacementValue = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, countContent);
        mSpnReplacementValue = (Spinner) view.findViewById(R.id.newlens_spn_replacement_value);
        mSpnReplacementValue.setAdapter(adapterReplacementValue);

        // Replacement period spinner
        mSpnReplacementPeriod = (Spinner) view.findViewById(R.id.newlens_spn_replacement_period);
        ArrayAdapter<CharSequence> adapterReplacementPeriod = ArrayAdapter.createFromResource(getActivity(),
                R.array.replacement_period_array, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapterReplacementPeriod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpnReplacementPeriod.setAdapter(adapterReplacementPeriod);

        // Lens type spinner
        mSpnType = (Spinner) view.findViewById(R.id.newlens_spn_type);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(getActivity(),
                R.array.lens_type_array, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpnType.setAdapter(adapterType);
        mSpnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                togglePrescriptionFields(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                togglePrescriptionFields(adapterView.getSelectedItemPosition());
            }
        });

        mEdtBaseCurve = (EditText) view.findViewById(R.id.newlens_edt_base_curve);
        mEdtDiameter = (EditText) view.findViewById(R.id.newlens_edt_diameter);

        mLblSphere = (TextView) view.findViewById(R.id.newlens_lbl_sphere);
        mEdtSphere = (EditText) view.findViewById(R.id.newlens_edt_sphere);
        mLblCylinder = (TextView) view.findViewById(R.id.newlens_lbl_cylinder);
        mEdtCylinder = (EditText) view.findViewById(R.id.newlens_edt_cylinder);
        mLblAxis = (TextView) view.findViewById(R.id.newlens_lbl_axis);
        mEdtAxis = (EditText) view.findViewById(R.id.newlens_edt_axis);
        mLblAdd = (TextView) view.findViewById(R.id.newlens_lbl_add);
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
        } else {
            // set remaining lenses to 1 (which coincidentally has index 1)
            mSpnRemaining.setSelection(1);
            // set replacement period to monthly
            mSpnReplacementPeriod.setSelection(PERIOD_MONTHLY);
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
            values.put(LensLogContract.Packages.NAME, mEdtName.getText().toString());
            values.put(LensLogContract.Packages.BRAND, mEdtBrand.getText().toString());

            // eye
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
            values.put(LensLogContract.Packages.EYE, eye);

            // lens type
            int lensTypePos = mSpnType.getSelectedItemPosition();
            String lensType;
            switch (lensTypePos) {
                case LENS_TYPE_ASTIGMATISM:
                    lensType = "astigmatism";
                    break;
                case LENS_TYPE_MULTIFOCAL:
                    lensType = "multifocal";
                    break;
                case LENS_TYPE_DECORATIVE:
                    lensType = "decorative";
                    break;
                default:
                    lensType = "myopia";
            }
            values.put(LensLogContract.Packages.LENS_TYPE, lensType);

            // at content spinner, we start with 1
            values.put(LensLogContract.Packages.CONTENT, mSpnContent.getSelectedItemPosition() + 1);
            values.put(LensLogContract.Packages.REMAINING, mSpnRemaining.getSelectedItemPosition());
            values.put(LensLogContract.Packages.REPLACEMENT_VALUE, mSpnReplacementValue.getSelectedItemPosition() + 1);

            // replacement period
            int replacementPeriodPos = mSpnReplacementPeriod.getSelectedItemPosition();
            String replacementPeriod;
            switch (replacementPeriodPos) {
                case PERIOD_DAILY:
                    replacementPeriod = "daily";
                    break;
                case PERIOD_YEARLY:
                    replacementPeriod = "yearly";
                    break;
                default:
                    replacementPeriod = "monthly";
            }
            values.put(LensLogContract.Packages.REPLACEMENT_PERIOD, replacementPeriod);

            values.put(LensLogContract.Packages.SPHERE, mEdtSphere.getText().toString());
            values.put(LensLogContract.Packages.BASE_CURVE, mEdtBaseCurve.getText().toString());
            values.put(LensLogContract.Packages.DIAMETER, mEdtDiameter.getText().toString());
            values.put(LensLogContract.Packages.CYLINDER, mEdtCylinder.getText().toString());
            values.put(LensLogContract.Packages.AXIS, mEdtAxis.getText().toString());
            values.put(LensLogContract.Packages.ADD_POWER, mEdtAdd.getText().toString());
            values.put(LensLogContract.Packages.EXPIRATION_DATE, mExpirationDate);
            values.put(LensLogContract.Packages.PURCHASED_DATE, mPurchasedDate);
            values.put(LensLogContract.Packages.SHOP, mEdtShop.getText().toString());

            if (mLensUri != null) {
                // change existing lens package
                getActivity().getContentResolver().update(mLensUri, values, null, null);
            } else {
                // add new lens to database
                getActivity().getContentResolver().insert(LensLogContract.Packages.CONTENT_URI, values);
            }

            // close this fragment and return to previous
            getFragmentManager().popBackStack();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // keyboard stays open after fragment is removed, so close it
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
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
     * Shows and hides input fields according to lens type
     *
     * @param i the index of the current lens type.
     */
    private void togglePrescriptionFields(int i) {
        switch (i) {
            case LENS_TYPE_MYOPIA:
                mLblSphere.setVisibility(View.VISIBLE);
                mEdtSphere.setVisibility(View.VISIBLE);
                mLblCylinder.setVisibility(View.GONE);
                mEdtCylinder.setVisibility(View.GONE);
                mLblAxis.setVisibility(View.GONE);
                mEdtAxis.setVisibility(View.GONE);
                mLblAdd.setVisibility(View.GONE);
                mEdtAdd.setVisibility(View.GONE);
                break;
            case LENS_TYPE_ASTIGMATISM:
                mLblSphere.setVisibility(View.VISIBLE);
                mEdtSphere.setVisibility(View.VISIBLE);
                mLblCylinder.setVisibility(View.VISIBLE);
                mEdtCylinder.setVisibility(View.VISIBLE);
                mLblAxis.setVisibility(View.VISIBLE);
                mEdtAxis.setVisibility(View.VISIBLE);
                mLblAdd.setVisibility(View.GONE);
                mEdtAdd.setVisibility(View.GONE);
                break;
            case LENS_TYPE_MULTIFOCAL:
                mLblSphere.setVisibility(View.VISIBLE);
                mEdtSphere.setVisibility(View.VISIBLE);
                mLblCylinder.setVisibility(View.GONE);
                mEdtCylinder.setVisibility(View.GONE);
                mLblAxis.setVisibility(View.GONE);
                mEdtAxis.setVisibility(View.GONE);
                mLblAdd.setVisibility(View.VISIBLE);
                mEdtAdd.setVisibility(View.VISIBLE);
                break;
            default:
                // also applies to LENS_TYPE_DECORATIVE
                mLblSphere.setVisibility(View.GONE);
                mEdtSphere.setVisibility(View.GONE);
                mLblCylinder.setVisibility(View.GONE);
                mEdtCylinder.setVisibility(View.GONE);
                mLblAxis.setVisibility(View.GONE);
                mEdtAxis.setVisibility(View.GONE);
                mLblAdd.setVisibility(View.GONE);
                mEdtAdd.setVisibility(View.GONE);
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
                        LensLogContract.Packages._ID,
                        LensLogContract.Packages.NAME,
                        LensLogContract.Packages.BRAND,
                        LensLogContract.Packages.EYE,
                        LensLogContract.Packages.CONTENT,
                        LensLogContract.Packages.REMAINING,
                        LensLogContract.Packages.REPLACEMENT_VALUE,
                        LensLogContract.Packages.REPLACEMENT_PERIOD,
                        LensLogContract.Packages.LENS_TYPE,
                        LensLogContract.Packages.SPHERE,
                        LensLogContract.Packages.BASE_CURVE,
                        LensLogContract.Packages.DIAMETER,
                        LensLogContract.Packages.CYLINDER,
                        LensLogContract.Packages.AXIS,
                        LensLogContract.Packages.ADD_POWER,
                        LensLogContract.Packages.EXPIRATION_DATE,
                        LensLogContract.Packages.PURCHASED_DATE,
                        LensLogContract.Packages.SHOP
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
        mEdtName.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.NAME)));
        mEdtBrand.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.BRAND)));

        // eye
        int eyeSelection = EYE_LEFT;
        String eye = data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.EYE));
        if (eye.equals("right")) {
            eyeSelection = EYE_RIGHT;
        } else if (eye.equals("both")) {
            eyeSelection = EYE_BOTH;
        }
        mSpnEye.setSelection(eyeSelection);

        // content and remaining
        int content = data.getInt(data.getColumnIndexOrThrow(LensLogContract.Packages.CONTENT));
        mSpnContent.setSelection(content - 1);
        int remaining = data.getInt(data.getColumnIndexOrThrow(LensLogContract.Packages.REMAINING));
        mSpnRemaining.setSelection(remaining);

        // replacement schedule
        int replacementValue = data.getInt(data.getColumnIndexOrThrow(LensLogContract.Packages.REPLACEMENT_VALUE));
        mSpnReplacementValue.setSelection(replacementValue - 1);
        int replacementPeriodSelection = PERIOD_MONTHLY;
        String replacementPeriod = data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.REPLACEMENT_PERIOD));
        if (replacementPeriod.equals("daily")) {
            replacementPeriodSelection = PERIOD_DAILY;
        } else if (replacementPeriod.equals("yearly")) {
            replacementPeriodSelection = PERIOD_YEARLY;
        }
        mSpnReplacementPeriod.setSelection(replacementPeriodSelection);

        // lens type
        int lensTypeSelection = LENS_TYPE_MYOPIA;
        String lensType = data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.LENS_TYPE));
        if (lensType.equals("astigmatism")) {
            lensTypeSelection = LENS_TYPE_ASTIGMATISM;
        } else if (lensType.equals("multifocal")) {
            lensTypeSelection = LENS_TYPE_MULTIFOCAL;
        } else if (lensType.equals("decorative")) {
            lensTypeSelection = LENS_TYPE_DECORATIVE;
        }
        mSpnType.setSelection(lensTypeSelection);
        togglePrescriptionFields(lensTypeSelection);

        mEdtSphere.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.SPHERE)));
        mEdtBaseCurve.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.BASE_CURVE)));
        mEdtDiameter.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.DIAMETER)));
        mEdtCylinder.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.CYLINDER)));
        mEdtAxis.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.AXIS)));
        mEdtAdd.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.ADD_POWER)));
        // get formatted expiration date
        mExpirationDate = data.getLong(data.getColumnIndexOrThrow(LensLogContract.Packages.EXPIRATION_DATE));
        if (mExpirationDate != 0) {
            mEdtExpiration.setText(DateUtils.formatDateTime(getActivity(), mExpirationDate, mDateBitmask));
        }
        // get formatted purchased date
        mPurchasedDate = data.getLong(data.getColumnIndexOrThrow(LensLogContract.Packages.PURCHASED_DATE));
        if (mPurchasedDate != 0) {
            mEdtPurchased.setText(DateUtils.formatDateTime(getActivity(), mPurchasedDate, mDateBitmask));
        }
        mEdtShop.setText(data.getString(data.getColumnIndexOrThrow(LensLogContract.Packages.SHOP)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
