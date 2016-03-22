package com.pierfrancescosoffritti.remotevrclient.connections;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pierfrancescosoffritti.remotevrclient.EventBus;
import com.pierfrancescosoffritti.remotevrclient.Events;
import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.representation.Quaternion;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by  Pierfrancesco on 06/03/2016.
 */
public class ServerConnection extends AbstractServerConnection {
    protected final String LOG_TAG = getClass().getSimpleName();
    public static final int DEFAULT_PORT = 2099;

    public ServerConnection(String ip, int port) {
        super(ip, port);
    }

    public Observable<Bitmap> getServerOutput() {
        Observable.OnSubscribe<Bitmap> onSubscribe = subscriber -> {
            try {
                outSocket.writeInt(1);

                int dim;
                while ((dim = inSocket.readInt()) > 0) {
                    byte[] img = new byte[dim];
                    inSocket.readFully(img, 0, dim);
                    subscriber.onNext(BitmapFactory.decodeByteArray(img, 0, img.length));
                }

            } catch (Exception e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("Exception: " + e.getClass(), LOG_TAG, LoggerBus.Log.ERROR));
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
                disconnect();
            }
        };

        return Observable.create(onSubscribe);
    }

    public Action1<Quaternion> getServerInput() {
        return quaternion -> {
            try {
                outSocket.writeFloat(quaternion.getX());
                outSocket.writeFloat(quaternion.getY());
                outSocket.writeFloat(quaternion.getZ());
                outSocket.writeFloat(quaternion.getW());
            } catch (Exception e) {
                throw new RuntimeException("Can't send quaternion", e);
            }
        };
    }

    @Override
    protected void onDisconnected() {
        LoggerBus.getInstance().post(new LoggerBus.Log("Ended connection with server.", LOG_TAG));
        EventBus.getInstance().post(new Events.ServerDisconnected());
    }
}
