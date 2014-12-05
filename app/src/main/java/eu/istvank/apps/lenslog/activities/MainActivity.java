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

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.TimeZone;

import eu.istvank.apps.lenslog.R;
import eu.istvank.apps.lenslog.fragments.CalendarFragment;
import eu.istvank.apps.lenslog.fragments.EditLensFragment;
import eu.istvank.apps.lenslog.fragments.LensesFragment;
import eu.istvank.apps.lenslog.fragments.NavigationDrawerFragment;
import eu.istvank.apps.lenslog.provider.LensLogContract;
import eu.istvank.apps.lenslog.services.NotifySchedulingService;
import eu.istvank.apps.lenslog.util.HelpUtils;
import hirondelle.date4j.DateTime;


public class MainActivity extends ActionBarActivity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        LensesFragment.OnPackageSelectedListener,
        FragmentManager.OnBackStackChangedListener,
        CalendarFragment.OnUpdateWornListener {

    public static final String TAG = "MainActivity";

    // Fragment Tags
    public static final String FRAGMENT_NAVIGATION_DRAWER = "NavigationDrawer";
    public static final String FRAGMENT_SETTINGS = "Settings";
    public static final String FRAGMENT_LENSES = "Lenses";
    public static final String FRAGMENT_CALENDAR = "Calendar";

    // RequestCodes
    public static final int REQUEST_YES = 1;
    public static final int REQUEST_NO = 2;

    // views
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mUserLearnedDrawer;

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    public static final String PREF_USER_ACCEPTED_PRIVACY_POLICY = "pref_privacy_policy_accepted";

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // indicates whether the hamburger is shown (false) or the back button (true)
    private boolean mBackEnabled = false;
    private static final String STATE_BACK_ENABLED = "state_back_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Handle the intent sent from the notification
        if (getIntent().hasExtra(NotifySchedulingService.NOTIFICATION_WORN)) {
            //TODO: save date in intent so that we can retrieve whether it still applies for the current date
            DateTime utcDateTime = DateTime.today(TimeZone.getTimeZone("UTC"));
            long utcDateLong = utcDateTime.getMilliseconds(TimeZone.getTimeZone("UTC"));
            boolean isWorn = getIntent().getExtras().getBoolean(NotifySchedulingService.NOTIFICATION_WORN);

            onUpdateWorn(utcDateLong, isWorn);

            // cancel notification
            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NotifySchedulingService.NOTIFICATION_ID);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        // check if the user has already accepted the privacy policy
        boolean privacyPolicyAccepted = sp.getBoolean(PREF_USER_ACCEPTED_PRIVACY_POLICY, false);
        if (!privacyPolicyAccepted) {
            HelpUtils.showPrivacyPolicy(this, true);
        }

        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(getTitleColor());
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,  mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this);
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(this, R.id.navigation_drawer, mDrawerLayout);

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer) {
            mDrawerLayout.openDrawer(findViewById(R.id.navigation_drawer));
        }

        if (savedInstanceState != null) {
            // probably configuration change
            mBackEnabled = savedInstanceState.getBoolean(STATE_BACK_ENABLED);
            setBackEnabled(mBackEnabled);
        } else {
            // app freshly started, show calendar fragment
            onNavigationDrawerItemSelected(NavigationDrawerFragment.SECTION_CALENDAR);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setStatusBarBackgroundColor(R.color.primaryColor);

        mTitle = getTitle();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_BACK_ENABLED, mBackEnabled);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // Handle the intent sent from the notification. This is called if the app is already open.
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(NotifySchedulingService.NOTIFICATION_WORN)) {
            //TODO: save date in intent so that we can retrieve whether it still applies for the current date
            DateTime utcDateTime = DateTime.today(TimeZone.getTimeZone("UTC"));
            long utcDateLong = utcDateTime.getMilliseconds(TimeZone.getTimeZone("UTC"));
            boolean isWorn = intent.getExtras().getBoolean(NotifySchedulingService.NOTIFICATION_WORN);

            onUpdateWorn(utcDateLong, isWorn);

            // cancel notification
            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NotifySchedulingService.NOTIFICATION_ID);
        }

        super.onNewIntent(intent);
    }

    public void onUpdateWorn(long datetime, boolean worn) {
        // get the default lenses
        String[] projectionDefaults = new String[] {
                LensLogContract.Packages._ID,
                LensLogContract.Packages.EYE,
                LensLogContract.Packages.CONTENT,
                LensLogContract.Packages.REMAINING
        };
        String whereArgsDefaults = LensLogContract.Packages.DEFAULT_LENS + " = 1";
        Cursor cursorDefaults = getContentResolver().query(LensLogContract.Packages.CONTENT_URI, projectionDefaults, whereArgsDefaults, null, null);
        int defaultLensLeft = 0;
        int remainingLeft = 0;
        int defaultLensRight = 0;
        int remainingRight = 0;
        while (cursorDefaults.moveToNext()) {
            int defaultLens = cursorDefaults.getInt(cursorDefaults.getColumnIndexOrThrow(LensLogContract.Packages._ID));
            String eye = cursorDefaults.getString(cursorDefaults.getColumnIndexOrThrow(LensLogContract.Packages.EYE));

            int content = cursorDefaults.getInt(cursorDefaults.getColumnIndexOrThrow(LensLogContract.Packages.CONTENT));
            int remaining = cursorDefaults.getInt(cursorDefaults.getColumnIndexOrThrow(LensLogContract.Packages.REMAINING));
            // check if the number of remaining lenses equals the number of the content. If yes, then this
            // package has only been opened recently.
            if (content == remaining) {
                // decrease remaining by two if eye is both, otherwise by one
                if (eye.equals("left")) {
                    remaining --;
                    remainingLeft = remaining;
                } else if (eye.equals("right")) {
                    remaining --;
                    remainingRight = remaining;
                } else if (eye.equals("both")) {
                    remaining -= 2;
                    remainingLeft = remaining;
                    remainingRight = remaining;
                }
                ContentValues valuesRemaining = new ContentValues();
                valuesRemaining.put(LensLogContract.Packages.REMAINING, remaining);
                getContentResolver().update(LensLogContract.Packages.CONTENT_URI, valuesRemaining, LensLogContract.Packages._ID + " = " + defaultLens, null);
            }

            if (eye.equals("left")) {
                defaultLensLeft = defaultLens;
            } else if (eye.equals("right")) {
                defaultLensRight = defaultLens;
            } else if (eye.equals("both")) {
                defaultLensLeft = defaultLens;
                defaultLensRight = defaultLens;
            }
        }

        // check if today is already in database
        String[] projection = new String[] {
                LensLogContract.DaysWorn._ID
        };
        String whereArg = LensLogContract.DaysWorn.DATETIME + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(datetime) };
        Cursor c = getContentResolver().query(LensLogContract.DaysWorn.CONTENT_URI, projection, whereArg, selectionArgs, null);
        ContentValues values = new ContentValues();
        values.put(LensLogContract.DaysWorn.WASWORN, worn);
        values.put(LensLogContract.DaysWorn.LEFT_PACKAGE_ID, defaultLensLeft);
        values.put(LensLogContract.DaysWorn.LEFT_REMAINING, remainingLeft);
        values.put(LensLogContract.DaysWorn.RIGHT_PACKAGE_ID, defaultLensRight);
        values.put(LensLogContract.DaysWorn.RIGHT_REMAINING, remainingRight);
        if (c != null && c.moveToFirst()) {
            // date exists so update
            getContentResolver().update(LensLogContract.DaysWorn.CONTENT_URI, values, whereArg, selectionArgs);
        } else {
            // date does not exist so insert
            values.put(LensLogContract.DaysWorn.DATETIME, datetime);
            getContentResolver().insert(LensLogContract.DaysWorn.CONTENT_URI, values);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;
        String tag;

        if (position == NavigationDrawerFragment.SECTION_LENSES) {
            fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LENSES);
            if (fragment == null) {
                fragment = LensesFragment.newInstance();
            }
            tag = FRAGMENT_LENSES;
        } else {
            fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_CALENDAR);
            if (fragment == null) {
                fragment = CalendarFragment.newInstance();
            }
            tag = FRAGMENT_CALENDAR;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, tag)
                .commit();
    }

    /**
     * Called upon start of the app and when navigation drawer closes.
     */
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
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

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            HelpUtils.showAbout(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Interfaces
     */

    @Override
    public void onPackageSelected(Uri packageUri) {
        //TODO: show PackageDetailsFragment not the edit one
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, EditLensFragment.newInstance(packageUri))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void onBackStackChanged() {
        int entries = getSupportFragmentManager().getBackStackEntryCount();
        if (entries < 1) {
            // show the hamburger
            setBackEnabled(false);
        } else {
            // show the back button
            setBackEnabled(true);
        }
    }

    private void setBackEnabled(boolean backEnabled) {
        mBackEnabled = backEnabled;
        if (!backEnabled) {
            // show the hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            // enable swipe-to-right gesture
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            // don't show the hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            // disable swipe-to-right gesture
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            // show the back arrow in the toolbar
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            // enable back action for toolbar arrow
            mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getSupportFragmentManager().popBackStack();
                }
            });
        }
    }
}
