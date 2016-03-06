package com.pierfrancescosoffritti.remotevrclient;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.pierfrancescosoffritti.remotevrclient.adapters.ViewPagerAdapter;
import com.pierfrancescosoffritti.remotevrclient.fragments.LogFragment;
import com.pierfrancescosoffritti.remotevrclient.fragments.GameFragment;
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

        GameFragment gameFragment = (GameFragment) getFragment(savedInstanceState, GameFragment.newInstance(), TAG_0);
        LogFragment logFragment = (LogFragment) getFragment(savedInstanceState, LogFragment.newInstance(), TAG_1);

        setUpViewPager(
                new Pair(gameFragment, getString(R.string.game)),
                new Pair(logFragment, getString(R.string.log))
        );
    }

    private Fragment getFragment(Bundle savedInstanceState, Fragment fragment, String tag) {
        if(savedInstanceState == null)
            return Fragments.findFragment(getSupportFragmentManager(), fragment);
        else
            return getSupportFragmentManager().findFragmentByTag(savedInstanceState.getString(tag));
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
