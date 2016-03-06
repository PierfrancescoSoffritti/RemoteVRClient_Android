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
import com.pierfrancescosoffritti.remotevrclient.activities.PreferencesActivity;
import com.pierfrancescosoffritti.remotevrclient.R;
import com.pierfrancescosoffritti.remotevrclient.RemoteVRView;
import com.pierfrancescosoffritti.remotevrclient.ServerConnection;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GameFragment extends BaseFragment {


    private ServerConnection serverConnection;
    private Subscription subscription;

    @Bind(R.id.remotevr_view) RemoteVRView remoteVRView;
    @Bind(R.id.connected_view) View connectedView;
    @Bind(R.id.not_connected_view) View notConnectedView;

    private FPSCounter fpsCounter;

    private View connectButton;
    private View disconnectButton;

    public GameFragment() {
        serverConnection = new ServerConnection();
    }

    public static GameFragment newInstance() {
        return new GameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remote_vr, container, false);
        ButterKnife.bind(this, view);

        fpsCounter = new FPSCounter(ButterKnife.findById(view, R.id.fps_counter));
        setupToolbar();

        return view;
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        if(toolbar.getChildCount() > 1)
            for(int i=1; i<toolbar.getChildCount(); i++)
                toolbar.removeView(toolbar.getChildAt(i));

        setupButtons(toolbar);
    }

    private void setupButtons(Toolbar toolbar) {
        View connectionControls = LayoutInflater.from(getContext()).inflate(R.layout.connection_controls, toolbar, false);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        toolbar.addView(connectionControls, params);

        connectButton = connectionControls.findViewById(R.id.connect);
        disconnectButton = connectionControls.findViewById(R.id.disconnect);

        connectButton.setOnClickListener((view) -> startClient());
        disconnectButton.setOnClickListener((view) -> { if(subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe(); });
        connectionControls.findViewById(R.id.settings).setOnClickListener((view) -> {
            Intent intent = new Intent(getActivity(), PreferencesActivity.class);
            getActivity().startActivity(intent);
        });
    }

    private void startClient() {
        // TODO cleanup
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String serverIP = sharedPreferences.getString(getString(R.string.server_ip), "192.168.1.23");
        String port = sharedPreferences.getString(getString(R.string.server_port), ServerConnection.DEFAULT_PORT + "");

        if(serverIP.isEmpty())
            serverIP="192.168.1.23";
        if(port.isEmpty())
            port = ServerConnection.DEFAULT_PORT+"";

        int serverPort = Integer.parseInt(port);

        subscription = serverConnection
                .getServerOutput(serverIP, serverPort)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(() -> serverConnection.close())
                .subscribe(bitmap -> remoteVRView.updateImage(bitmap));
    }

    @Override
    public void register() {
        fpsCounter.register();
        EventBus.getInstance().register(this);
    }

    @Override
    public void unregister() {
        fpsCounter.unregister();
        EventBus.getInstance().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
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
