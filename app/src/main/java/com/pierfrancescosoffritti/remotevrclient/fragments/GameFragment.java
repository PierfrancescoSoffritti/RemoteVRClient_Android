package com.pierfrancescosoffritti.remotevrclient.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.remotevrclient.EventBus;
import com.pierfrancescosoffritti.remotevrclient.Events;
import com.pierfrancescosoffritti.remotevrclient.FPSCounter;
import com.pierfrancescosoffritti.remotevrclient.FullScreenManager;
import com.pierfrancescosoffritti.remotevrclient.R;
import com.pierfrancescosoffritti.remotevrclient.RemoteVRView;
import com.pierfrancescosoffritti.remotevrclient.activities.PreferencesActivity;
import com.pierfrancescosoffritti.remotevrclient.io.connections.ServerConnection;
import com.pierfrancescosoffritti.remotevrclient.io.data.GyroInput;
import com.pierfrancescosoffritti.remotevrclient.io.data.TouchInput;
import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.MyOrientationProvider;
import com.pierfrancescosoffritti.remotevrclient.utils.PerformanceMonitor;
import com.pierfrancescosoffritti.remotevrclient.utils.SnackbarFactory;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GameFragment extends BaseFragment {

    protected final String LOG_TAG = getClass().getSimpleName();

    private ServerConnection serverConnection;

    private MyOrientationProvider orientationProvider;

    @Bind(R.id.remotevr_view) RemoteVRView remoteVRView;
    @Bind(R.id.connected_view) View connectedView;
    @Bind(R.id.not_connected_view) View notConnectedView;
    @Bind(R.id.connection_in_progress_view) View connectionInProgressView;

    private FullScreenManager fullScreenManager;

    private FPSCounter fpsCounter;

    // toolbar buttons, inflated manually.
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

        notConnectedView.setOnClickListener((v) -> startClient());

        fpsCounter = new FPSCounter(ButterKnife.findById(view, R.id.fps_counter));

        orientationProvider = new MyOrientationProvider(getContext());
        orientationProvider.start();

        // when long clicked goes full screen.
        fullScreenManager = new FullScreenManager(getActivity(), ((AppCompatActivity)getActivity()).getSupportActionBar(), getActivity().findViewById(R.id.tab_layout));

        return view;
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        // clear the toolbar
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
        disconnectButton.setOnClickListener((view) -> { if(serverConnection != null) serverConnection.disconnect(); });

        connectionControls.findViewById(R.id.settings).setOnClickListener((view) -> {
            Intent intent = new Intent(getActivity(), PreferencesActivity.class);
            getActivity().startActivity(intent);
        });
    }

    /**
     * starts the connection with the server
     * the IP and PORT of the server are saved in the app's DefaultSharedPreferences because can be changed in SETTINGS
     */
    private void startClient() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String serverIP = sharedPreferences.getString(getString(R.string.server_ip), "192.168.1.23");
        int serverPort = Integer.parseInt(sharedPreferences.getString(getString(R.string.server_port), ServerConnection.DEFAULT_PORT + ""));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        PerformanceMonitor mPerformanceMonitor = new PerformanceMonitor();

        // it's not nice to have this thread, but for now it's ok
        new Thread() {
            public void run() {
                try {
                    serverConnection = new ServerConnection(serverIP, serverPort);
                } catch (IOException e) {
                    LoggerBus.getInstance().post(new LoggerBus.Log("Error creating socket: " + e.getClass() + " . " +e.getMessage(), LOG_TAG, LoggerBus.Log.ERROR));
                    SnackbarFactory.snackbarRequest(getView(), R.string.error_cant_connect, -1, Snackbar.LENGTH_LONG);
                    return;
                }

                // send screen resolution
                try {
                    serverConnection.sendScreenResolution(screenWidth, screenHeight);
                } catch (IOException e) {
                    e.printStackTrace();
                    serverConnection.disconnect();
                }

                // game video
                serverConnection
                        .getServerOutput()

                        // performance monitor
                        .doOnSubscribe(mPerformanceMonitor::start)
                        .doOnNext(bitmap -> mPerformanceMonitor.incCounter())
                        .doOnUnsubscribe(mPerformanceMonitor::stop)

                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(() -> EventBus.getInstance().post(new Events.ServerConnected()))
                        .doOnSubscribe(() -> LoggerBus.getInstance().post(new LoggerBus.Log("Started connection with server.", LOG_TAG)))

                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError((error) -> SnackbarFactory.snackbarRequest(getView(), R.string.error_receiving_images, -1, Snackbar.LENGTH_LONG))
                        .subscribe(bitmap -> remoteVRView.updateImage(bitmap), Throwable::printStackTrace);

                // game input
                // gyro
                Observable.interval(16, TimeUnit.MILLISECONDS, Schedulers.io())
                        .map(tick -> orientationProvider.getQuaternion())
                        .map(quaternion -> GyroInput.getInstance().putPayload(quaternion))
                        .subscribeOn(Schedulers.io())
                        //.doOnSubscribe(orientationProvider::start)
                        //.doOnUnsubscribe(orientationProvider::stop)
                        .doOnError((error) -> SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_gyro, -1, Snackbar.LENGTH_LONG))
                        .subscribe(serverConnection.getServerInput(), Throwable::printStackTrace);

                // touch
                remoteVRView.getPublishSubject()
                        .filter(event -> event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP)
                        .map(event -> TouchInput.getInstance().putPayload(event))
                        .doOnError((error) -> SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_touch, -1, Snackbar.LENGTH_LONG))
                        .subscribe(serverConnection.getServerInput(), Throwable::printStackTrace);
            }
        }.start();
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
        if(serverConnection != null)
            serverConnection.disconnect();
        orientationProvider.stop();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onServerConnecting(Events.ServerConnecting e) {
        connectionInProgressView.setVisibility(View.VISIBLE);
        notConnectedView.setVisibility(View.GONE);
        connectedView.setVisibility(View.GONE);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onServerConnected(Events.ServerConnected e) {
        fullScreenManager.enterFullScreen();

        connectionInProgressView.setVisibility(View.GONE);
        connectedView.setVisibility(View.VISIBLE);
        notConnectedView.setVisibility(View.GONE);
        connectButton.setVisibility(View.GONE);
        disconnectButton.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onServerDisconnected(Events.ServerDisconnected e) {
        fullScreenManager.exitFullScreen();

        connectionInProgressView.setVisibility(View.GONE);
        connectedView.setVisibility(View.GONE);
        notConnectedView.setVisibility(View.VISIBLE);
        connectButton.setVisibility(View.VISIBLE);
        disconnectButton.setVisibility(View.GONE);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void disconnectServer(Events.DisconnectServer e) {
        if(serverConnection != null) serverConnection.disconnect();
    }
}
