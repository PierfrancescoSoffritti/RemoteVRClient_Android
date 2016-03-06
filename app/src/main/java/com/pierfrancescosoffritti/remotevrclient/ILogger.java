package com.pierfrancescosoffritti.remotevrclient;

import com.pierfrancescosoffritti.remotevrclient.utils.LoggerBus;

/**
 * Created by Pierfrancesco on 04/03/2016.
 */
public interface ILogger {

    void onLog(LoggerBus.Log log);
}
