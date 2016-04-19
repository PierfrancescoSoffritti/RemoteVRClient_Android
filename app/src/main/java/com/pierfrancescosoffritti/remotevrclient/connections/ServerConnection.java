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
 * This object represents a TCP connection with the server.
 */
public class ServerConnection extends AbstractServerConnection {
    protected final String LOG_TAG = getClass().getSimpleName();
    public static final int DEFAULT_PORT = 2099;

    /**
     * Creates a ServerConnection object and opens the TCP connection with the server, with the corresponding Input and Output streams.
     * @param ip the server IP
     * @param port the server PORT
     */
    public ServerConnection(String ip, int port) {
        super(ip, port);
    }

    /**
     * The server output is a stream of images, representing the stream video of the game
     * @return an Observable representing the server output, which is a stream of images.
     */
    public Observable<Bitmap> getServerOutput() {
        Observable.OnSubscribe<Bitmap> onSubscribe = subscriber -> {
            try {

                // the simple protocol with the server requires every client to identify himself.
                // a normal client (such as this) has to send a 1, while the client on which the game is running has to send a 0.
                outSocket.writeInt(1);

                // receive images
                int dim;
                while ((dim = inSocket.readInt()) > 0) {
                    byte[] img = new byte[dim];
                    inSocket.readFully(img, 0, dim);
                    // notify the subscribers that a new image is ready
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

    /**
     * The server input is a stream of quaternions, representing the phone rotations
     * @return an Action1 containing the onNext() logic. Takes a Quaternion and sends it to the server.
     */
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
