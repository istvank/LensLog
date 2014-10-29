package eu.istvank.apps.lenslog.activities;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import eu.istvank.apps.lenslog.R;
import eu.istvank.apps.lenslog.fragments.SettingsFragment;

public class SettingsActivity extends ActionBarActivity implements View.OnClickListener {

    // views
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setTitle(R.string.title_settings);
        mToolbar.setNavigationOnClickListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_fragment, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}
