package com.pierfrancescosoffritti.remotevrclient;

import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.view.View;

/**
 * Created by  Pierfrancesco on 22/03/2016.
 */
public class RemoteViewClickListener implements View.OnLongClickListener {

    private Activity mContext;
    private ActionBar mActionBar;
    private View mTabs;

    private boolean isFullScreen = false;

    public RemoteViewClickListener(Activity context, ActionBar supportActionBar, View tabs) {
        mContext = context;
        mActionBar = supportActionBar;
        mTabs = tabs;
    }

    @Override
    public boolean onLongClick(View view) {
        if(isFullScreen)
            exitFullScreen();
        else
            enterFullScreen();

        isFullScreen = !isFullScreen;

        return true;
    }

    // This snippet hides the system bars.
    private void hideSystemUI(View mDecorView) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI(View mDecorView) {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    public void enterFullScreen() {
        mActionBar.hide();
        mTabs.setVisibility(View.GONE);

        View decorView = mContext.getWindow().getDecorView();
        // Hide the status bar.
        //int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);

        hideSystemUI(decorView);

        mTabs.invalidate();
    }

    public void exitFullScreen() {
        mActionBar.show();
        mTabs.setVisibility(View.VISIBLE);

        View decorView = mContext.getWindow().getDecorView();
        // Hide the status bar.
        //int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);

        showSystemUI(decorView);

        mTabs.invalidate();
    }
}
