/*
 * Copyright 2014 istvank.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.istvank.apps.lenslog.activities;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import eu.istvank.apps.lenslog.R;
import eu.istvank.apps.lenslog.fragments.CalendarFragment;
import eu.istvank.apps.lenslog.fragments.EditLensFragment;
import eu.istvank.apps.lenslog.fragments.LensesFragment;
import eu.istvank.apps.lenslog.fragments.NavigationDrawerFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LensesFragment.OnPackSelectedListener {

    public static final String TAG = "MainActivity";

    // Fragment Tags
    public static final String FRAGMENT_SETTINGS = "Settings";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;

        if (position == NavigationDrawerFragment.SECTION_LENSES) {
            fragment = LensesFragment.newInstance();
        } else {
            fragment = CalendarFragment.newInstance();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    /**
     * Called upon start of the app and when navigation drawer closes.
     */
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);

            // do not show Settings item if showing Settings fragment
            Fragment frgSettings = getSupportFragmentManager().findFragmentByTag(FRAGMENT_SETTINGS);
            if (frgSettings != null && frgSettings.isVisible()) {
                menu.removeItem(R.id.action_settings);
            }

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //TODO: this will be a PreferenceActivity to support older devices.
            // Display the fragment in the main container.
            //getSupportFragmentManager().beginTransaction()
            //        .replace(R.id.container, new SettingsFragment(), FRAGMENT_SETTINGS)
            //        .addToBackStack(null)
            //        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            //        .commit();
            return true;
        } else if (id == R.id.action_newlens) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, EditLensFragment.newInstance(null))
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Interfaces
     */

    @Override
    public void onPackSelected(Uri packUri) {
        //TODO: show PackDetailsFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, EditLensFragment.newInstance(packUri))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

}
