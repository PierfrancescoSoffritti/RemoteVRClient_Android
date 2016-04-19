package com.pierfrancescosoffritti.remotevrclient.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.pierfrancescosoffritti.remotevrclient.R;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO fix
        addPreferencesFromResource(R.xml.settings);

    }
}