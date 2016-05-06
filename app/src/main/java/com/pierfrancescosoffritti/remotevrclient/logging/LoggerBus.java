package com.pierfrancescosoffritti.remotevrclient.logging;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;

import com.squareup.otto.Bus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * EventBus for log events only.
 *
 * @author Pierfrancesco Soffritti
 */
public class LoggerBus {

    private static LoggerBus ourInstance = new LoggerBus();

    public static LoggerBus getInstance() {
        return ourInstance;
    }

    private Bus bus;
    private Handler handler;

    private LoggerBus() {
        bus = new Bus("logger");
        handler = new Handler(Looper.getMainLooper());
    }

    public void register(Object obj) {
        bus.register(obj);
    }

    public void unregister(Object obj) {
        bus.unregister(obj);
    }

    public void post(final Log log) {
        handler.post(() -> bus.post(log));
    }

    /**
     * Class defining a log
     *
     * @author Pierfrancesco Soffritti
     */
    public static class Log {

        public static final int NORMAL = 0;
        public static final int ERROR = 1;
        public static final int FPS = 2;
        public static final int FPS_AVG = 3;

        /**
         * Defines the different types of log.
         */
        @IntDef({NORMAL, ERROR, FPS, FPS_AVG})
        @Retention(RetentionPolicy.SOURCE)
        @interface LogType {}

        private String mSender;
        private String mMessage;
        @LogType private int mType;

        public Log(String message) {
            mMessage = message;
            mSender = "";
            mType = NORMAL;
        }

        public Log(String message, String sender) {
            this(message);
            mSender = sender;
            mType = NORMAL;
        }

        public Log(String message, String sender, @LogType int type) {
            this(message, sender);
            mType = type;
        }

        public String getMessage() {
            return mMessage;
        }

        public String getSender() {
            return mSender;
        }

        public @LogType int getType() {
            return mType;
        }
    }
}
