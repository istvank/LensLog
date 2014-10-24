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

package eu.istvank.apps.lenslog.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.istvank.apps.lenslog.R;
import eu.istvank.apps.lenslog.receivers.NotifyAlarmReceiver;

/**
 * The PreferenceFragment showing the Settings of the LensLog app.
 *
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_NOTIFY_DAILY = "pref_notify_daily";

    NotifyAlarmReceiver alarm = new NotifyAlarmReceiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // has to be returned otherwise the onCreateOptionsMenu from the host Activity
        // is not called when coming from other Fragments that do not explicitly call
        // setHasOptionsMenu(true).
        setHasOptionsMenu(true);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_PREF_NOTIFY_DAILY)) {
            boolean notify = sharedPreferences.getBoolean(key, false);
            if (notify) {
                alarm.setAlarm(getActivity());
            } else {
                alarm.cancelAlarm(getActivity());
            }
        }
    }
}
