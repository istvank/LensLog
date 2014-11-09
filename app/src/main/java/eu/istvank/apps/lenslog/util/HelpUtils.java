/*
 * Taken from https://github.com/google/iosched
 *
 * Copyright 2014 Google Inc. All rights reserved.
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

package eu.istvank.apps.lenslog.util;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.internal.widget.TintCheckBox;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.istvank.apps.lenslog.R;
import eu.istvank.apps.lenslog.activities.MainActivity;

/**
 * This is a set of helper methods for showing contextual help information in the app.
 */
public class HelpUtils {
    public static void showAbout(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_about");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new AboutDialog().show(ft, "dialog_about");
    }

    public static class AboutDialog extends DialogFragment {

        private static final String VERSION_UNAVAILABLE = "N/A";

        public AboutDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get app version
            PackageManager pm = getActivity().getPackageManager();
            String packageName = getActivity().getPackageName();
            String versionName;
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                versionName = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                versionName = VERSION_UNAVAILABLE;
            }

            // Build the about body view and append the link to see OSS licenses
            SpannableStringBuilder aboutBody = new SpannableStringBuilder();
            aboutBody.append(Html.fromHtml(getString(R.string.about_body, versionName)));

            // append privacy policy link
            SpannableString privacyLink = new SpannableString(getString(R.string.about_privacy_policy));
            privacyLink.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    HelpUtils.showPrivacyPolicy(getActivity(), false);
                }
            }, 0, privacyLink.length(), 0);
            aboutBody.append("\n\n");
            aboutBody.append(privacyLink);

            // append OSS license link
            SpannableString licensesLink = new SpannableString(getString(R.string.about_licenses));
            licensesLink.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    HelpUtils.showOpenSourceLicenses(getActivity());
                }
            }, 0, licensesLink.length(), 0);
            aboutBody.append("\n\n");
            aboutBody.append(licensesLink);

            // append EULA link
            SpannableString eulaLink = new SpannableString(getString(R.string.about_eula));
            eulaLink.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    HelpUtils.showEula(getActivity());
                }
            }, 0, eulaLink.length(), 0);
            aboutBody.append("\n\n");
            aboutBody.append(eulaLink);

            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            TextView aboutBodyView = (TextView) layoutInflater.inflate(R.layout.dialog_about, null);
            aboutBodyView.setText(aboutBody);
            aboutBodyView.setMovementMethod(new LinkMovementMethod());

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_about)
                    .setView(aboutBodyView)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }

    /**
     * Privacy Policy
     */

    public static void showPrivacyPolicy(Activity activity, boolean accept) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_licenses");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        if (accept) {
            new PrivacyPolicyAcceptDialog().show(ft, "dialog_licenses");
        } else {
            new PrivacyPolicyDialog().show(ft, "dialog_licenses");
        }
    }

    /**
     * Privacy Policy Dialog, shown from About
     */

    public static class PrivacyPolicyDialog extends DialogFragment {

        public PrivacyPolicyDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int padding = getResources().getDimensionPixelSize(R.dimen.content_padding_normal);

            TextView eulaTextView = new TextView(getActivity());
            eulaTextView.setText(Html.fromHtml(getString(R.string.privacy_policy_text)));
            eulaTextView.setMovementMethod(LinkMovementMethod.getInstance());
            eulaTextView.setPadding(padding, padding, padding, padding);

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.about_privacy_policy)
                    .setView(eulaTextView)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }

    /**
     * Privacy Policy Accept Dialog, shown on first start (until user accepts)
     */

    public static class PrivacyPolicyAcceptDialog extends DialogFragment {

        private boolean mAccepted = false;

        public PrivacyPolicyAcceptDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_accept_license, null);

            AlertDialog licenseDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.about_privacy_policy)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (mAccepted) {
                                        dialog.dismiss();
                                    } else {
                                        getActivity().finish();
                                    }
                                    SharedPreferences sp = PreferenceManager
                                            .getDefaultSharedPreferences(getActivity());
                                    sp.edit().putBoolean(MainActivity.PREF_USER_ACCEPTED_PRIVACY_POLICY, mAccepted).apply();
                                }
                            }
                    )
                    .setView(layout)
                    .create();

            CheckBox chkAccept = (CheckBox) layout.findViewById(R.id.dialogaccept_chk_accept);
            chkAccept.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mAccepted = b;
                }
            });

            return licenseDialog;
        }
    }

    /**
     * Open Source Licenses
     */

    public static void showOpenSourceLicenses(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_licenses");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new OpenSourceLicensesDialog().show(ft, "dialog_licenses");
    }

    public static class OpenSourceLicensesDialog extends DialogFragment {

        public OpenSourceLicensesDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            webView.loadUrl("file:///android_asset/licenses.html");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.about_licenses)
                    .setView(webView)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }

    /**
     * End-User License Agreement
     */

    public static void showEula(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_eula");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new EulaDialog().show(ft, "dialog_eula");
    }

    public static class EulaDialog extends DialogFragment {

        public EulaDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int padding = getResources().getDimensionPixelSize(R.dimen.content_padding_normal);

            TextView eulaTextView = new TextView(getActivity());
            eulaTextView.setText(Html.fromHtml(getString(R.string.eula_legal_text)));
            eulaTextView.setMovementMethod(LinkMovementMethod.getInstance());
            eulaTextView.setPadding(padding, padding, padding, padding);

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.about_eula)
                    .setView(eulaTextView)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }
}
