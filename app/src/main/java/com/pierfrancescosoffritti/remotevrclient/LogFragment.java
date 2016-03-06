package com.pierfrancescosoffritti.remotevrclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pierfrancescosoffritti.remotevrclient.utils.LoggerBus;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;


public class LogFragment extends Fragment implements ILogger {

    @Bind(R.id.log_view) TextView logView;

    public LogFragment() {
    }

    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        LoggerBus.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        LoggerBus.getInstance().unregister(this);
    }

    @Subscribe
    @Override
    public void onLog(LoggerBus.Log log) {
        switch (log.getType()) {
            case LoggerBus.Log.STATS_AVG:
                break;
            case LoggerBus.Log.STATS_INST:
                break;
            case LoggerBus.Log.ERROR:
                logView.append("\n" +log.getSender() +" : " +log.getMessage());
                break;
            case LoggerBus.Log.NORMAL:
                logView.append("\n" +log.getSender() +" : " +log.getMessage());
                break;
        }
    }
}
