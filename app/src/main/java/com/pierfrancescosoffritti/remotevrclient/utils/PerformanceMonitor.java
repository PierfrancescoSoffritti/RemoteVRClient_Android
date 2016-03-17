package com.pierfrancescosoffritti.remotevrclient.utils;

import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Pierfrancesco on 29/02/2016.
 */

public class PerformanceMonitor {

    public static final String LOG_TAG = PerformanceMonitor.class.getSimpleName();

    private Timer timer;
    private PerformanceMonitorTask task;

    public PerformanceMonitor() {
        timer = new Timer();
    }

    public void start() {
        task = new PerformanceMonitorTask();
        timer.schedule(task, 0, 1000);
    }

    public void stop() {
        timer.cancel();
        LoggerBus.getInstance().post(new LoggerBus.Log("AVG: " +task.avg +" frame/second" , LOG_TAG, LoggerBus.Log.STATS_AVG));
    }

    public double getAvg() {
        return task.avg;
    }

    public void incCounter() {
        task.incCounter();
    }

    class PerformanceMonitorTask extends TimerTask {

        List<Integer> history;
        int counter;
        int sum;
        double avg;

        PerformanceMonitorTask() {
            counter = 0;
            sum = 0;
            history = new ArrayList<>();
        }

        @Override
        public void run() {
            sum += counter;
            if(history.size() > 0)
                avg = sum/history.size();
            else
                avg = sum;

            LoggerBus.getInstance().post(new LoggerBus.Log(+counter +" frame/second", LOG_TAG, LoggerBus.Log.STATS_INST));
            LoggerBus.getInstance().post(new LoggerBus.Log("AVG: " +avg +" frame/second" , LOG_TAG, LoggerBus.Log.STATS_AVG));
            history.add(counter);
            counter = 0;
        }

        void incCounter() {
            counter++;
        }
    }
}
