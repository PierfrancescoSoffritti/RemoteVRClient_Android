package com.pierfrancescosoffritti.remotevrclient.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pierfrancescosoffritti.remotevrclient.R;
import com.pierfrancescosoffritti.remotevrclient.logging.ILogger;
import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;


public class LogFragment extends BaseFragment implements ILogger {

    @Bind(R.id.log_view) TextView logView;

    public LogFragment() {
    }

    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void register() {
        LoggerBus.getInstance().register(this);
    }

    @Override
    public void unregister() {
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
                logView.append(Html.fromHtml("\n<br/><font color=\"red\">" +log.getSender() +" : " +log.getMessage() +"</font><br/>\n"));
                break;
            case LoggerBus.Log.NORMAL:
                logView.append("\n" +log.getSender() +" : " +log.getMessage() +"\n");
                break;
        }
    }
}
