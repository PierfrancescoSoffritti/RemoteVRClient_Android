package com.pierfrancescosoffritti.remotevrclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.remotevrclient.utils.ConsoleLogger;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RemoteVRFragment extends Fragment {

    private RemoteVRView remoteVRView;

    private ServerConnection serverConnection;
    private Subscription subscription;

    private FPSCounter fpsCounter;
    private ConsoleLogger consoleLogger;

    @Bind(R.id.connected_view) View connectedView;
    @Bind(R.id.not_connected_view) View notConnectedView;

    private View connectButton;
    private View disconnectButton;

    public RemoteVRFragment() {
        serverConnection = new ServerConnection();
    }

    public static RemoteVRFragment newInstance() {
        RemoteVRFragment fragment = new RemoteVRFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remote_vr, container, false);
        ButterKnife.bind(this, view);

        remoteVRView = ButterKnife.findById(view, R.id.remotevr_view);

        fpsCounter = new FPSCounter(ButterKnife.findById(view, R.id.fps_counter));
        consoleLogger = new ConsoleLogger();

        setupToolbar();

        return view;
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

        if(toolbar.getChildCount() > 2)
            for(int i=2; i<toolbar.getChildCount(); i++)
                toolbar.removeView(toolbar.getChildAt(i));

        View connectionControls = LayoutInflater.from(getContext()).inflate(R.layout.connection_controls, toolbar, false);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        toolbar.addView(connectionControls, params);

        connectButton = connectionControls.findViewById(R.id.connect);
        disconnectButton = connectionControls.findViewById(R.id.disconnect);

        connectButton.setOnClickListener((view) -> startClient());
        disconnectButton.setOnClickListener((view) -> { if(subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe(); });
    }

    private void startClient() {
        subscription = serverConnection
                .getServerOutput("192.168.1.23", ServerConnection.DEFAULT_PORT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(() -> serverConnection.close())
                .subscribe(bitmap -> remoteVRView.updateImage(bitmap));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        fpsCounter.register();
        consoleLogger.register();
        EventBus.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        fpsCounter.unregister();
        consoleLogger.unregister();
        EventBus.getInstance().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }

    @Subscribe
    public void onServerConnected(Events.ServerConnected e) {
        System.out.println("server connected");
        connectedView.setVisibility(View.VISIBLE);
        notConnectedView.setVisibility(View.GONE);
        connectButton.setVisibility(View.GONE);
        disconnectButton.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onServerDisconnected(Events.ServerDisconnected e) {
        System.out.println("server disconnected");
        connectedView.setVisibility(View.GONE);
        notConnectedView.setVisibility(View.VISIBLE);
        connectButton.setVisibility(View.VISIBLE);
        disconnectButton.setVisibility(View.GONE);
    }
}
