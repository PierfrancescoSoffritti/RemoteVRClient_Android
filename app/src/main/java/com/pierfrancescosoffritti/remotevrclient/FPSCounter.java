package com.pierfrancescosoffritti.remotevrclient;

import android.widget.TextView;

import com.pierfrancescosoffritti.remotevrclient.logging.ILogger;
import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;
import com.pierfrancescosoffritti.remotevrclient.utils.PerformanceMonitor;
import com.squareup.otto.Subscribe;

/**
 * Class responsible for showing FPS log events on a text view
 */
public class FPSCounter implements ILogger {

    private TextView mView;

    public FPSCounter(TextView view) {
        mView = view;
    }

    @Subscribe
    @Override
    public void onLog(LoggerBus.Log log) {
        if(log.getSender().equals(PerformanceMonitor.LOG_TAG)) {
            if (log.getType() == LoggerBus.Log.STATS_INST)
                mView.setText(log.getMessage());
        }
    }

    public void register() {
        LoggerBus.getInstance().register(this);
    }

    public void unregister() {
        LoggerBus.getInstance().unregister(this);
    }
}
