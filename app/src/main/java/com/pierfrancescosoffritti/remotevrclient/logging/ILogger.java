package com.pierfrancescosoffritti.remotevrclient.logging;

/**
 * A class that implements this interface is capable of receiving and handling log events.
 *
 * @author Pierfrancesco Soffritti
 */
public interface ILogger {
    void onLog(LoggerBus.Log log);
}
