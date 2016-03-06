package com.pierfrancescosoffritti.remotevrclient.logging;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Created by Pierfrancesco on 02/10/2015.
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

    public void post(final Object event) {
        handler.post(() -> bus.post(event));
    }

    public static class Log {

        public static final int NORMAL = 0;
        public static final int ERROR = 1;
        public static final int STATS_INST = 2;
        public static final int STATS_AVG = 3;

        private String mSender;
        private String mMessage;
        private int mType;

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

        public Log(String message, String sender, int type) {
            this(message, sender);
            mType = type;
        }

        public String getMessage() {
            return mMessage;
        }

        public String getSender() {
            return mSender;
        }

        public int getType() {
            return mType;
        }
    }
}
