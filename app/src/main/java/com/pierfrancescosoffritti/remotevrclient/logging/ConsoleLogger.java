package com.pierfrancescosoffritti.remotevrclient.logging;

import android.util.Log;

import com.squareup.otto.Subscribe;

/**
 * An {@link ILogger} that logs on the console.
 *
 * @author Pierfrancesco Soffritti
 */
public class ConsoleLogger implements ILogger {

    public void register() {
        LoggerBus.getInstance().register(this);
    }

    public void unregister() {
        LoggerBus.getInstance().unregister(this);
    }

    @Subscribe
    @Override
    public void onLog(LoggerBus.Log log){
        Log.d(log.getSender(), log.getMessage());
    }
}
