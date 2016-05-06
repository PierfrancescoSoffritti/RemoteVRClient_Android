package com.pierfrancescosoffritti.remotevrclient.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import com.pierfrancescosoffritti.remotevrclient.EventBus;
import com.pierfrancescosoffritti.remotevrclient.Events;
import com.pierfrancescosoffritti.remotevrclient.FullScreenManager;
import com.pierfrancescosoffritti.remotevrclient.R;
import com.pierfrancescosoffritti.remotevrclient.adapters.ViewPagerAdapter;
import com.pierfrancescosoffritti.remotevrclient.fragments.GameFragment;
import com.pierfrancescosoffritti.remotevrclient.fragments.LogFragment;
import com.pierfrancescosoffritti.remotevrclient.utils.Fragments;
import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_GAME_FRAGMENT = "TAG_GAME_FRAGMENT";
    private static final String TAG_LOG_FRAGMENT = "TAG_LOG_FRAGMENT";

    private FullScreenManager fullScreenManager;

    private ViewPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // force the Activity to stay in landscape and to keep the screen on
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TabLayout tabs = (TabLayout) findViewById(R.id.tab_layout);

        setSupportActionBar(toolbar);

        GameFragment gameFragment = (GameFragment) Fragments.findFragment(getSupportFragmentManager(), GameFragment.newInstance(), TAG_GAME_FRAGMENT);
        LogFragment logFragment = (LogFragment) Fragments.findFragment(getSupportFragmentManager(), LogFragment.newInstance(), TAG_LOG_FRAGMENT);

        setupViewPager(
                tabs,
                new Pair(gameFragment, getString(R.string.game)),
                new Pair(logFragment, getString(R.string.log))
        );

        fullScreenManager = new FullScreenManager(this, toolbar, tabs);
    }

    private void setupViewPager(TabLayout tabs, Pair<Fragment, String>... fragments) {
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        tabs.setupWithViewPager(mViewPager);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        // This is necessary if I want to retrieve those fragments instances from the stack.
        // Otherwise the ViewPager will re instantiate the fragments when events like configuration changes occurs, and I won't have any control on them.
        // This could result in a double instantiation of the fragments which will lead to the usual fragment problems.
        outState.putString(TAG_GAME_FRAGMENT, mPagerAdapter.getItem(0).getTag());
        outState.putString(TAG_LOG_FRAGMENT, mPagerAdapter.getItem(1).getTag());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getInstance().unregister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void goFullScreen(Events.GoFullScreen e) {
        if(e.isGoFullScreen())
            fullScreenManager.enterFullScreen();
        else
            fullScreenManager.exitFullScreen();

    }
}
