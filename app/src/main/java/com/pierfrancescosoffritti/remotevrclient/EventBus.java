package com.pierfrancescosoffritti.remotevrclient;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Created by Pierfrancesco on 02/10/2015.
 */
public class EventBus {

    private static EventBus ourInstance = new EventBus();

    public static EventBus getInstance() {
        return ourInstance;
    }

    private Bus bus;
    private Handler handler;

    private EventBus() {
        bus = new Bus("events");
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
}
