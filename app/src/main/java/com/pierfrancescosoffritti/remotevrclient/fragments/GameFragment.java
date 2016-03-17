package com.pierfrancescosoffritti.remotevrclient.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.remotevrclient.EventBus;
import com.pierfrancescosoffritti.remotevrclient.Events;
import com.pierfrancescosoffritti.remotevrclient.FPSCounter;
import com.pierfrancescosoffritti.remotevrclient.R;
import com.pierfrancescosoffritti.remotevrclient.RemoteVRView;
import com.pierfrancescosoffritti.remotevrclient.ServerConnection;
import com.pierfrancescosoffritti.remotevrclient.activities.PreferencesActivity;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.RotationProvider;
import com.pierfrancescosoffritti.remotevrclient.utils.PerformanceMonitor;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GameFragment extends BaseFragment {

    private ServerConnection serverConnection;
    private Subscription serverOutputSubscription;

    private RotationProvider rotationProvider;
    private Subscription serverInputSubscription;

    @Bind(R.id.remotevr_view) RemoteVRView remoteVRView;
    @Bind(R.id.connected_view) View connectedView;
    @Bind(R.id.not_connected_view) View notConnectedView;

    private FPSCounter fpsCounter;

    private View connectButton;
    private View disconnectButton;

    public GameFragment() {
    }

    public static GameFragment newInstance() {
        return new GameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remote_vr, container, false);
        ButterKnife.bind(this, view);

        setupToolbar();

        fpsCounter = new FPSCounter(ButterKnife.findById(view, R.id.fps_counter));

        serverConnection = new ServerConnection();
        rotationProvider = new RotationProvider(getContext());

        return view;
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        if(toolbar.getChildCount() > 1)
            for(int i=1; i<toolbar.getChildCount(); i++)
                toolbar.removeView(toolbar.getChildAt(i));

        setupToolbarButtons(toolbar);
    }

    private void setupToolbarButtons(Toolbar toolbar) {
        View connectionControls = LayoutInflater.from(getContext()).inflate(R.layout.connection_controls, toolbar, false);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        toolbar.addView(connectionControls, params);

        connectButton = connectionControls.findViewById(R.id.connect);
        disconnectButton = connectionControls.findViewById(R.id.disconnect);

        // listeners
        connectButton.setOnClickListener((view) -> startClient());
        disconnectButton.setOnClickListener((view) -> {
            if(serverOutputSubscription != null && !serverOutputSubscription.isUnsubscribed()) serverOutputSubscription.unsubscribe();
            if(serverInputSubscription != null && !serverInputSubscription.isUnsubscribed()) serverInputSubscription.unsubscribe();
        });

        connectionControls.findViewById(R.id.settings).setOnClickListener((view) -> {
            Intent intent = new Intent(getActivity(), PreferencesActivity.class);
            getActivity().startActivity(intent);
        });
    }

    private void startClient() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String serverIP = sharedPreferences.getString(getString(R.string.server_ip), "192.168.1.23");
        String port = sharedPreferences.getString(getString(R.string.server_port), ServerConnection.DEFAULT_PORT + "");
        int serverPort = Integer.parseInt(port);

        PerformanceMonitor mPerformanceMonitor = new PerformanceMonitor();

        new Thread() {
            public void run() {
                serverConnection.connect(serverIP, serverPort);

                serverOutputSubscription = serverConnection
                        .getServerOutput()

                        // performance monitor
                        .doOnSubscribe(mPerformanceMonitor::start)
                        .doOnNext(bitmap -> mPerformanceMonitor.incCounter())
                        .doOnUnsubscribe(mPerformanceMonitor::stop)

                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnUnsubscribe(serverConnection::close)
                        .subscribe(bitmap -> remoteVRView.updateImage(bitmap));

                serverInputSubscription = Observable.interval(16, TimeUnit.MILLISECONDS, Schedulers.io())
                        .map(tick -> rotationProvider.getQuaternion())
                        .doOnUnsubscribe(rotationProvider::stop)
                        .subscribeOn(Schedulers.io())
                        .subscribe(serverConnection.getServerInput());

                serverConnection.setInputSubscription(serverInputSubscription);
            }
        }.start();
    }

    @Override
    public void register() {
        fpsCounter.register();
        EventBus.getInstance().register(this);
        rotationProvider.start();
    }

    @Override
    public void unregister() {
        fpsCounter.unregister();
        EventBus.getInstance().unregister(this);
        rotationProvider.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serverOutputSubscription.unsubscribe();
        serverInputSubscription.unsubscribe();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onServerConnected(Events.ServerConnected e) {
        connectedView.setVisibility(View.VISIBLE);
        notConnectedView.setVisibility(View.GONE);
        connectButton.setVisibility(View.GONE);
        disconnectButton.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onServerDisconnected(Events.ServerDisconnected e) {
        connectedView.setVisibility(View.GONE);
        notConnectedView.setVisibility(View.VISIBLE);
        connectButton.setVisibility(View.VISIBLE);
        disconnectButton.setVisibility(View.GONE);
    }
}
