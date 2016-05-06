package com.pierfrancescosoffritti.remotevrclient.logging;

import android.widget.TextView;

import com.squareup.otto.Subscribe;

/**
 * An {@link ILogger} that logs only FPS logs on a TextView.
 *
 * @author Pierfrancesco Soffritti
 */
public class FPSLogger implements ILogger {

    private TextView mView;

    public FPSLogger(TextView view) {
        mView = view;
    }

    @Subscribe
    @Override
    public void onLog(LoggerBus.Log log) {
        if (log.getType() == LoggerBus.Log.FPS)
            mView.setText(log.getMessage());
    }

    public void register() {
        LoggerBus.getInstance().register(this);
    }

    public void unregister() {
        LoggerBus.getInstance().unregister(this);
    }
}
