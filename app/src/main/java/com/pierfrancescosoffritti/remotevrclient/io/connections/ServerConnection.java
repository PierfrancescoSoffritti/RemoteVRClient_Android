package com.pierfrancescosoffritti.remotevrclient.io.connections;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pierfrancescosoffritti.remotevrclient.EventBus;
import com.pierfrancescosoffritti.remotevrclient.Events;
import com.pierfrancescosoffritti.remotevrclient.io.data.GameInput;
import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;

import java.io.IOException;

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
    public ServerConnection(String ip, int port) throws IOException {
        super(ip, port);
    }

    /**
     * Sends the resolution of the screen (in pixel) to the server.
     * @param screenWidth
     * @param screenHeight
     */
    public void sendScreenResolution(int screenWidth, int screenHeight) throws IOException {
        outSocket.writeInt(screenWidth);
        outSocket.writeInt(screenHeight);
    }

    /**
     * The server output is a stream of images, representing the stream video of the game
     * @return an Observable representing the server output, which is a stream of images.
     */
    public Observable<Bitmap> getServerOutput() {
        Observable.OnSubscribe<Bitmap> onSubscribe = subscriber -> {
            try {
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
     * The server input is a stream of bytes, representing rotation (quaternions) or touch interactions
     * @return an Action1 containing the onNext() logic. Takes a {@link GameInput} and sends its type and payload to the server.
     */
    public Action1<GameInput> getServerInput() {
        return gameInput -> {
            try {
                outSocket.write(gameInput.getType());
                outSocket.write(gameInput.getPayload().array());
            } catch (Exception e) {
                throw new RuntimeException("Can't send game input", e);
            }
        };
    }

    @Override
    protected void onDisconnected() {
        LoggerBus.getInstance().post(new LoggerBus.Log("Ended connection with server.", LOG_TAG));
        EventBus.getInstance().post(new Events.ServerDisconnected());
    }
}
