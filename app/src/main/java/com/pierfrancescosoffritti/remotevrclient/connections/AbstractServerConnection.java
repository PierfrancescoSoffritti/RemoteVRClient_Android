package com.pierfrancescosoffritti.remotevrclient.connections;

import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Pierfrancesco on 18/03/2016.
 */
public abstract class AbstractServerConnection {

    protected final String LOG_TAG = getClass().getSimpleName();

    protected Socket mSocket;
    protected DataInputStream inSocket;
    protected DataOutputStream outSocket;

    public AbstractServerConnection(String ip, int port) {
        connect(ip, port);
        inSocket = getInputStream();
        outSocket = getOutputStream();
    }

    private void connect(String ip, int port) {
        try {
            mSocket = new Socket(InetAddress.getByName(ip), port);
            mSocket.setSoTimeout(0);

            LoggerBus.getInstance().post(new LoggerBus.Log("Connected to " + mSocket, LOG_TAG));
        } catch (Exception e) {
            LoggerBus.getInstance().post(new LoggerBus.Log("Error creating socket: " + e.getClass(), LOG_TAG, LoggerBus.Log.ERROR));
            e.printStackTrace();
        }
    }

    private DataInputStream getInputStream() {
        try {
            if(inSocket == null)
                inSocket = new DataInputStream(mSocket.getInputStream());
        } catch (Exception e) {
            LoggerBus.getInstance().post(new LoggerBus.Log("Error creating input stream: " + e.getClass(), LOG_TAG, LoggerBus.Log.ERROR));
            e.printStackTrace();
        }

        return inSocket;
    }

    private DataOutputStream getOutputStream() {
        try {
            if(outSocket == null)
                outSocket = new DataOutputStream(mSocket.getOutputStream());
        } catch (IOException e) {
            LoggerBus.getInstance().post(new LoggerBus.Log("Error creating output stream: " + e.getClass(), LOG_TAG, LoggerBus.Log.ERROR));
            e.printStackTrace();
        }

        return outSocket;
    }

    public void disconnect() {
        try {
            if(outSocket != null) { outSocket.flush(); outSocket.close(); }
            if(inSocket != null) { inSocket.close(); }

            mSocket.close();

            onDisconnected();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return mSocket.isConnected();
    }

    protected abstract void onDisconnected();
}
