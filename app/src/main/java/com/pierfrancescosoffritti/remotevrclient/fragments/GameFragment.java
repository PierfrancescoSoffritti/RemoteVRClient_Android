package com.pierfrancescosoffritti.remotevrclient.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.remotevrclient.EventBus;
import com.pierfrancescosoffritti.remotevrclient.Events;
import com.pierfrancescosoffritti.remotevrclient.logging.FPSLogger;
import com.pierfrancescosoffritti.remotevrclient.R;
import com.pierfrancescosoffritti.remotevrclient.RemoteVRView;
import com.pierfrancescosoffritti.remotevrclient.io.connections.ServerIO;
import com.pierfrancescosoffritti.remotevrclient.io.connections.ServerTCP;
import com.pierfrancescosoffritti.remotevrclient.io.connections.ServerUDP;
import com.pierfrancescosoffritti.remotevrclient.io.data.GyroInput;
import com.pierfrancescosoffritti.remotevrclient.io.data.TouchInput;
import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;
import com.pierfrancescosoffritti.remotevrclient.headtracking.providers.CalibratedGyroscopeProvider;
import com.pierfrancescosoffritti.remotevrclient.headtracking.providers.OrientationProvider;
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

/**
 * this fragment contains the game view and controls
 *
 * @author Pierfrancesco Soffritti
 */
public class GameFragment extends BaseFragment {

    protected final String LOG_TAG = getClass().getSimpleName();

    private ServerIO serverConnection;
    private OrientationProvider orientationProvider;

    private FPSLogger fpsLogger;

    @Bind(R.id.remote_vr_view) RemoteVRView remoteVRView;

    /**
     * view shown when the game is connected. Contains the {@link RemoteVRView}.
     */
    @Bind(R.id.connected_view) View connectedView;

    /**
     * view shown when the game is not connected.
     */
    @Bind(R.id.not_connected_view) View notConnectedView;

    /**
     * view shown when the connection is in progress.
     */
    @Bind(R.id.connection_in_progress_view) View connectionInProgressView;

    public GameFragment() {
    }

    public static GameFragment newInstance() {
        return new GameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remote_vr, container, false);
        ButterKnife.bind(this, view);

        notConnectedView.setOnClickListener((v) -> startClient());

        fpsLogger = new FPSLogger(ButterKnife.findById(view, R.id.fps_counter));

        orientationProvider = new CalibratedGyroscopeProvider(getContext());
        orientationProvider.start();

        return view;
    }

    /**
     * starts the connection with the server
     * the IP and PORT of the server are saved in the app's DefaultSharedPreferences because can be changed in SETTINGS
     */
    private void startClient() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String serverIP = sharedPreferences.getString(getString(R.string.server_ip_key), "192.168.1.23");
        int serverPort = Integer.parseInt(sharedPreferences.getString(getString(R.string.server_port_key), ServerTCP.DEFAULT_PORT + ""));
        boolean useTCP = sharedPreferences.getBoolean(getString(R.string.use_TCP_key), true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        PerformanceMonitor mPerformanceMonitor = new PerformanceMonitor();

        // it's not nice to have this thread, but for now it's ok
        new Thread() {
            public void run() {
                try {
                    if(useTCP)
                        serverConnection = new ServerTCP(serverIP, serverPort);
                    else
                        serverConnection = new ServerUDP("192.168.1.255", serverPort);
                } catch (IOException e) {
                    LoggerBus.getInstance().post(new LoggerBus.Log("Error creating socket: " + e.getClass() + " . " +e.getMessage(), LOG_TAG, LoggerBus.Log.ERROR));
                    SnackbarFactory.snackbarRequest(getView(), R.string.error_cant_connect, -1, Snackbar.LENGTH_LONG);
                    return;
                }

                // send screen resolution
                try {
                    serverConnection.sendScreenResolution(screenWidth, screenHeight);
                } catch (IOException e) {
                    SnackbarFactory.snackbarRequest(getView(), R.string.error_cant_send_screen_res, -1, Snackbar.LENGTH_LONG);
                    serverConnection.disconnect();
                    return;
                }

                // game video
                serverConnection
                        .getServerOutput()

                        // performance monitor
                        .doOnSubscribe(mPerformanceMonitor::start)
                        .doOnNext(bitmap -> mPerformanceMonitor.newFrameReceived())
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
                        .observeOn(Schedulers.io())
                        .filter(event -> event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP)
                        .map(event -> TouchInput.getInstance().putPayload(event))
                        .doOnError((error) -> SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_touch, -1, Snackbar.LENGTH_LONG))
                        .subscribe(serverConnection.getServerInput(), Throwable::printStackTrace);
            }
        }.start();
    }

    @Override
    public void register() {
        fpsLogger.register();
        EventBus.getInstance().register(this);
    }

    @Override
    public void unregister() {
        fpsLogger.unregister();
        EventBus.getInstance().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(serverConnection != null)
            serverConnection.disconnect();
        orientationProvider.stop();
    }

    // events

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
        EventBus.getInstance().post(new Events.GoFullScreen(true));

        connectionInProgressView.setVisibility(View.GONE);
        connectedView.setVisibility(View.VISIBLE);
        notConnectedView.setVisibility(View.GONE);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onServerDisconnected(Events.ServerDisconnected e) {
        EventBus.getInstance().post(new Events.GoFullScreen(false));

        connectionInProgressView.setVisibility(View.GONE);
        connectedView.setVisibility(View.GONE);
        notConnectedView.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void disconnectServer(Events.DisconnectServer e) {
        if(serverConnection != null) serverConnection.disconnect();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onGameViewSwipeDetected(Events.RemoteView_SwipeTopBottom e) {
        disconnectServer(null);
    }
}
