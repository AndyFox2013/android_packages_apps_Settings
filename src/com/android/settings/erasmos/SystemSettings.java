/*
 * Copyright (C) 2012 The CyanogenMod project
 * Copyright (C) 2013 The CyanogenMod project
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

package com.android.settings.erasmos;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = "SystemSettings";

   
    private static final String KEY_TABLET_UI = "tablet_ui";

    private CheckBoxPreference mTabletUI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.system_settings);
        PreferenceScreen prefScreen = getPreferenceScreen();

        mTabletUI = (CheckBoxPreference) findPreference(KEY_TABLET_UI);
        mTabletUI.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.ENABLE_TABLET_MODE, 0) == 1));
        mTabletUI.setOnPreferenceChangeListener(this);

        // remove the tablet UI preference smallest width is >= 720
        int smallestWidth = getActivity().getResources().getConfiguration().smallestScreenWidthDp;
        if (smallestWidth >= 720) {
            prefScreen.removePreference(mTabletUI);
        }

        // Don't display the lock clock preference if its not installed
        removePreferenceIfPackageNotInstalled(findPreference(KEY_LOCK_CLOCK));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTabletUI) {
            boolean tabletMode = ((Boolean) newValue).equals(Boolean.TRUE);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.ENABLE_TABLET_MODE, tabletMode ? 1 : 0);
            return true;
        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();

        // All users
        updateLightPulseDescription();

        // Primary user only
        if (mIsPrimary) {
            updateBatteryPulseDescription();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private boolean removePreferenceIfPackageNotInstalled(Preference preference) {
        String intentUri = ((PreferenceScreen) preference).getIntent().toUri(1);
        Pattern pattern = Pattern.compile("component=([^/]+)/");
        Matcher matcher = pattern.matcher(intentUri);

        String packageName = matcher.find() ? matcher.group(1) : null;
        if (packageName != null) {
            try {
                getPackageManager().getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "package " + packageName + " not installed, hiding preference.");
                getPreferenceScreen().removePreference(preference);
                return true;
            }
        }
        return false;
    }
}
