package com.pierfrancescosoffritti.remotevrclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;
import com.pierfrancescosoffritti.remotevrclient.utils.PerformanceMonitor;

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

/**
 * Created by  Pierfrancesco on 06/03/2016.
 */
public class ServerConnection {

    private final String LOG_NAME = getClass().getSimpleName();
    public static final int DEFAULT_PORT = 2099;

    private Socket socket;

    private Socket connect(String ip, int port) {
        try {
            Socket socket = new Socket(InetAddress.getByName(ip), port);
            socket.setSoTimeout(30000);
            return socket;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("UnknownHost " + e.getMessage(), LOG_NAME, LoggerBus.Log.ERROR));
        } catch (SocketException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("Unable to open socket " + e.getMessage(), LOG_NAME, LoggerBus.Log.ERROR));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("IOException " + e.getMessage(), LOG_NAME, LoggerBus.Log.ERROR));
        }

        return null;
    }

    private DataInputStream getInputStream(Socket socket) {
        try {
            return new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("InputStream not created " + e.getMessage(), LOG_NAME, LoggerBus.Log.ERROR));
        }

        return null;
    }

    private DataOutputStream getOutputStream(Socket socket) {
        try {
            return new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            LoggerBus.getInstance().post(new LoggerBus.Log("OutputStream not created " + e.getMessage(), LOG_NAME, LoggerBus.Log.ERROR));
        }

        return null;
    }

    public Observable<Bitmap> getServerOutput(String ip, int port) {
        PerformanceMonitor mPerformanceMonitor = new PerformanceMonitor();
        Observable.OnSubscribe<Bitmap> onSubscribe = subscriber -> {
            try {
                socket = connect(ip, port);
                DataInputStream inSock = getInputStream(socket);
                DataOutputStream outSock = getOutputStream(socket);

                outSock.writeInt(1);

                EventBus.getInstance().post(new Events.ServerConnected());
                LoggerBus.getInstance().post(new LoggerBus.Log("Started connection with server.", LOG_NAME));

                mPerformanceMonitor.start();
                int dim;

                while ((dim = inSock.readInt()) > 0) {
                    byte[] img = new byte[dim];
                    inSock.readFully(img, 0, dim);
                    subscriber.onNext(BitmapFactory.decodeByteArray(img, 0, img.length));

                    mPerformanceMonitor.incCounter();
                }

                subscriber.onCompleted();
                socket.close();

            } catch (EOFException e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("EOFException on socket: " + socket, LOG_NAME, LoggerBus.Log.ERROR));
            } catch (SocketTimeoutException e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("TimeOutException on socket: " + socket, LOG_NAME, LoggerBus.Log.ERROR));
            } catch (IOException e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("IOException on socket: " + socket, LOG_NAME, LoggerBus.Log.ERROR));
            } catch (NullPointerException e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("NullPointerException on socket: " + socket, LOG_NAME, LoggerBus.Log.ERROR));
            } finally {
                mPerformanceMonitor.stop();
                close();
            }
        };

        return Observable.create(onSubscribe);
    }

    public void close() {
        try {
            socket.close();
            LoggerBus.getInstance().post(new LoggerBus.Log("Ended connection with server.", LOG_NAME));
            EventBus.getInstance().post(new Events.ServerDisconnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
