package eu.istvank.apps.lenslog.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
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
public class EditLensFragment extends Fragment {

    // the fragment initialization parameters
    private static final String ARG_LENSURI = "lensUri";

    private Uri mLensUri;

    private OnFragmentInteractionListener mListener;

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
        return inflater.inflate(R.layout.fragment_new_lens, container, false);
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
            if (mLensUri != null) {
                // change existing lens pack
            } else {
                // add new lens to database
                EditText edtLensName = (EditText) getActivity().findViewById(R.id.newlens_edt_name);
                ContentValues values = new ContentValues();
                values.put(LensLogContract.PacksColumns.EYE, "left");
                values.put(LensLogContract.PacksColumns.LENS_TYPE, "great");
                values.put(LensLogContract.PacksColumns.NAME, edtLensName.getText().toString());
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

}
