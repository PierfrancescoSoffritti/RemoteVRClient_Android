package com.pierfrancescosoffritti.remotevrclient;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.pierfrancescosoffritti.remotevrclient.adapters.ViewPagerAdapter;
import com.pierfrancescosoffritti.remotevrclient.utils.Fragments;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_0 = "TAG_0";
    private static final String TAG_1 = "TAG_1";

    private ViewPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(ButterKnife.findById(this, R.id.toolbar));

        RemoteVRFragment remoteVRFragment;
        LogFragment logFragment;
        if(savedInstanceState == null) {
            remoteVRFragment = (RemoteVRFragment) Fragments.findFragment(getSupportFragmentManager(), RemoteVRFragment.newInstance());
            logFragment = (LogFragment) Fragments.findFragment(getSupportFragmentManager(), LogFragment.newInstance());
        } else {
            String tag0 = savedInstanceState.getString(TAG_0);
            String tag1 = savedInstanceState.getString(TAG_1);

            remoteVRFragment = (RemoteVRFragment) getSupportFragmentManager().findFragmentByTag(tag0);
            logFragment = (LogFragment) getSupportFragmentManager().findFragmentByTag(tag1);
        }

        setUpViewPager(
                new Pair(remoteVRFragment, getString(R.string.game)),
                new Pair(logFragment, getString(R.string.log))
        );
    }

    private void setUpViewPager(Pair<Fragment, String>... fragments) {
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        TabLayout tabs = ButterKnife.findById(this, R.id.tab_layout);
        tabs.setupWithViewPager(mViewPager);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(TAG_0, mPagerAdapter.getItem(0).getTag());
        outState.putString(TAG_1, mPagerAdapter.getItem(1).getTag());
    }
}
