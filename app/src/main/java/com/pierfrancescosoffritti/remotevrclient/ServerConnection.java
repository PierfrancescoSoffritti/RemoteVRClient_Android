package com.pierfrancescosoffritti.remotevrclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.representation.Quaternion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by  Pierfrancesco on 06/03/2016.
 */
public class ServerConnection {

    private final String LOG_TAG = getClass().getSimpleName();

    public static final int DEFAULT_PORT = 2099;

    private Socket socket;

    private Subscription inputSubscription;

    public Socket connect(String ip, int port) {

        if(socket != null && !socket.isClosed())
            return socket;

        try {
            socket = new Socket(InetAddress.getByName(ip), port);
            socket.setSoTimeout(0);

            LoggerBus.getInstance().post(new LoggerBus.Log("Connected to " + socket, LOG_TAG));

            return socket;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("UnknownHost " + e.getMessage(), LOG_TAG, LoggerBus.Log.ERROR));
        } catch (SocketException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("Unable to open socket " + e.getMessage(), LOG_TAG, LoggerBus.Log.ERROR));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("IOException " + e.getMessage(), LOG_TAG, LoggerBus.Log.ERROR));
        }

        return null;
    }

    public DataInputStream getInputStream() {
        try {
            return new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("InputStream not created " + e.getMessage(), LOG_TAG, LoggerBus.Log.ERROR));
        }

        return null;
    }

    public DataOutputStream getOutputStream() {
        try {
            return new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("OutputStream not created " + e.getMessage(), LOG_TAG, LoggerBus.Log.ERROR));
        }

        return null;
    }

    public Observable<Bitmap> getServerOutput() {
        Observable.OnSubscribe<Bitmap> onSubscribe = subscriber -> {
            try {
                DataInputStream inSock = getInputStream();
                DataOutputStream outSock = getOutputStream();

                outSock.writeInt(1);

                EventBus.getInstance().post(new Events.ServerConnected());
                LoggerBus.getInstance().post(new LoggerBus.Log("Started connection with server.", LOG_TAG));

                int dim;
                while ((dim = inSock.readInt()) > 0) {
                    byte[] img = new byte[dim];
                    inSock.readFully(img, 0, dim);
                    subscriber.onNext(BitmapFactory.decodeByteArray(img, 0, img.length));
                }

                subscriber.onCompleted();

            } catch (EOFException e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("EOFException on socket: " + socket, LOG_TAG, LoggerBus.Log.ERROR));
            } catch (SocketTimeoutException e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("TimeOutException on socket: " + socket, LOG_TAG, LoggerBus.Log.ERROR));
            } catch (IOException e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("IOException on socket: " + socket, LOG_TAG, LoggerBus.Log.ERROR));
            } catch (NullPointerException e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("NullPointerException on socket: " + socket, LOG_TAG, LoggerBus.Log.ERROR));
            } finally {
                close();
            }
        };

        return Observable.create(onSubscribe);
    }

    public Action1<Quaternion> getServerInput() {

        DataOutputStream outSock = getOutputStream();

        return quaternion -> {
            try {
                outSock.writeFloat(quaternion.getX());
                outSock.writeFloat(quaternion.getY());
                outSock.writeFloat(quaternion.getZ());
                outSock.writeFloat(quaternion.getW());
            } catch (IOException e) {
                e.printStackTrace();
                inputSubscription.unsubscribe();
                LoggerBus.getInstance().post(new LoggerBus.Log("Server input unsubscribed.", LOG_TAG));
            }
        };
    }

    public void close() {
        try {
            socket.close();
            LoggerBus.getInstance().post(new LoggerBus.Log("Ended connection with server.", LOG_TAG));
            EventBus.getInstance().post(new Events.ServerDisconnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setInputSubscription(Subscription inputSubscription) {
        this.inputSubscription = inputSubscription;
    }
}
