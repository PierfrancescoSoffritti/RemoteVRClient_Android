package com.pierfrancescosoffritti.remotevrclient.utils;

import android.util.Log;

import com.pierfrancescosoffritti.remotevrclient.ILogger;
import com.squareup.otto.Subscribe;

/**
 * Created by Pierfrancesco on 03/03/2016.
 */
public class ConsoleLogger implements ILogger {

    public void register() {
        LoggerBus.getInstance().register(this);
    }

    public void unregister() {
        LoggerBus.getInstance().unregister(this);
    }

    @Subscribe
    public void onLog(LoggerBus.Log log){
        Log.d(log.getSender(), log.getMessage());
    }
}
