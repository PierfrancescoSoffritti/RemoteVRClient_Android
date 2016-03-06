package com.pierfrancescosoffritti.remotevrclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private RemoteVRView remoteVRView;

    private ServerConnection serverConnection;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        serverConnection = new ServerConnection();

        remoteVRView = ButterKnife.findById(this, R.id.customView);

        startClient();
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
