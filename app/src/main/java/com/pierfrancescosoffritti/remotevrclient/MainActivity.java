package com.pierfrancescosoffritti.remotevrclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.pierfrancescosoffritti.remotevrclient.utils.ConsoleLogger;

import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private RemoteVRView remoteVRView;

    private ServerConnection serverConnection;
    private Subscription subscription;

    private FPSCounter fpsCounter;
    private ConsoleLogger consoleLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        serverConnection = new ServerConnection();
        remoteVRView = ButterKnife.findById(this, R.id.remotevr_view);

        fpsCounter = new FPSCounter(ButterKnife.findById(this, R.id.fps_counter));
        consoleLogger = new ConsoleLogger();

        startClient();
    }

    @Override
    public void onResume() {
        super.onResume();
        fpsCounter.register();
        consoleLogger.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        fpsCounter.unregister();
        consoleLogger.unregister();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }

    private void startClient() {
        subscription = serverConnection
                .getServerOutput("192.168.1.23", ServerConnection.DEFAULT_PORT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(() -> serverConnection.close())
                .subscribe(bitmap -> remoteVRView.updateImage(bitmap));
    }
}
