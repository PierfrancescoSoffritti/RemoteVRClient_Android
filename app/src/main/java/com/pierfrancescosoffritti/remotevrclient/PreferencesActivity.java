package com.pierfrancescosoffritti.remotevrclient;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO fix
        addPreferencesFromResource(R.xml.settings);

    }
}