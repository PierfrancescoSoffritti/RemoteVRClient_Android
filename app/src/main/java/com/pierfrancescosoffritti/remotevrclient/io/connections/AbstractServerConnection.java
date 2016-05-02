package com.pierfrancescosoffritti.remotevrclient.io.connections;

import com.pierfrancescosoffritti.remotevrclient.EventBus;
import com.pierfrancescosoffritti.remotevrclient.Events;
import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 * Created by Pierfrancesco on 18/03/2016.
 */
public abstract class AbstractServerConnection {

    protected final String LOG_TAG = getClass().getSimpleName();

    protected Socket mSocket;
    protected DataInputStream inSocket;
    protected DataOutputStream outSocket;

    /**
     * Opens the TCP connection with the server, with the corresponding Input and Output streams.
     * @param ip the server IP
     * @param port the server PORT
     * @throws IOException
     */
    protected AbstractServerConnection(String ip, int port) throws IOException {
        connect(ip, port);
        inSocket = getInputStream();
        outSocket = getOutputStream();
    }

    private void connect(String ip, int port) throws IOException {
        SocketAddress sockaddr = new InetSocketAddress(InetAddress.getByName(ip), port);
        mSocket = new Socket();
        EventBus.getInstance().post(new Events.ServerConnecting());

        try {
            mSocket.connect(sockaddr, 5000);
        } catch (SocketTimeoutException e) {
            EventBus.getInstance().post(new Events.ServerDisconnected());
            throw new IOException(e);
        }

        LoggerBus.getInstance().post(new LoggerBus.Log("Connected to " + mSocket, LOG_TAG));
    }

    private DataInputStream getInputStream() throws IOException {
        if(inSocket == null)
            inSocket = new DataInputStream(mSocket.getInputStream());

        return inSocket;
    }

    private DataOutputStream getOutputStream() throws IOException {
        if(outSocket == null)
            outSocket = new DataOutputStream(mSocket.getOutputStream());

        return outSocket;
    }

    /**
     * close the TCP connection with the server
     */
    public void disconnect() {
        try {
            if(outSocket != null) { outSocket.flush(); outSocket.close(); }
            if(inSocket != null) { inSocket.close(); }

            mSocket.close();

            onDisconnected();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean isConnected() {
        return mSocket.isConnected();
    }

    /**
     * this method is called automatically after the TCP connection with the server has been closed successfully
     */
    protected abstract void onDisconnected();
}
